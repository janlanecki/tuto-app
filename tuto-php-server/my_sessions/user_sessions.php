<?php

include '../auth_header.php';

define("RETURNED_SESSIONS_MAX_COUNT", 20);
define("LOAD_MORE_SESSIONS_COUNT", 15);

// Make sure these values are not set(e.g. in included files)
unset($lastId);
unset($tags);


if (isset($params)) {
  if (isset($params->last_id)) {
    $lastId = intval($params->last_id);
    if (!is_int($lastId)) {
      unset($lastId);
    }
  }
  if (isset($params->tags)) {
    $tags = json_decode($params->tags, true);
    if (!is_array($tags) || empty($tags)) {
      unset($tags);
    }
  }
}

if (!isset($lastId) && isset($_GET["last_id"])) {
    $lastId = intval($_GET["last_id"]);
}
if (!isset($tags) && isset($_GET["tags"])) {
  $tags = json_decode($_GET["tags"], true);
  if (!is_array($tags) || empty($tags)) {
      unset($tags);
  }
}

if (isset($lastId)) {
  $lastId = pg_escape_string($lastId);
}

$withClause = "";
$whereClause = "";
$joinClause = "";
$limitClause = "";

function addWithStatement(&$clause, $statement) {
  if (empty($clause)) {
    $clause .= "WITH {$statement}";
  } else {
    $clause .= ", {$statement}";
  }
}

function addWhereStatement(&$clause, $statement) {
  if (empty($clause)) {
    $clause .= "WHERE {$statement}";
  } else {
    $clause .= " AND {$statement}";
  }
}

function addStatement(&$clause, $statement) {
  $clause .= " {$statement}";
}

addWithStatement(&$withClause, "RECURSIVE ignoredTagsCte AS (
    SELECT parent, child
      FROM tags_dag
    WHERE parent IN (SELECT tag_id FROM ignored_tags WHERE user_id={$userId})
    UNION  ALL
    SELECT t.parent, t.child
    FROM ignoredTagsCte c
    JOIN tags_dag t ON c.child = t.parent)");
addWithStatement(&$withClause, "ignoredTagsSet AS (
  SELECT DISTINCT sub.id
    FROM (SELECT parent AS id FROM ignoredTagsCte
          UNION ALL
          SELECT child AS id FROM ignoredTagsCte
          UNION ALL
          (SELECT tag_id AS id FROM ignored_tags WHERE user_id={$userId})) sub)");
addWhereStatement(&$whereClause,
    "(NOT EXISTS
        (SELECT tag_id
         FROM categories
         WHERE session_id=S.id
           AND tag_id IN (SELECT * FROM ignoredTagsSet)))");


if (isset($tags) && !empty($tags)) {
    $tagSet = "";
    $tagsUnion = "";
    foreach ($tags as $tagId) {
      $tagId = pg_escape_string($tagId);
      $tagSet .= (empty($tagSet) ? "" : ",") . "{$tagId}";
      $tagUnion .= "UNION SELECT {$tagId} AS id ";
    }
    addWithStatement(&$withClause, "tagsCte AS (
        SELECT parent, child
          FROM tags_dag
        WHERE parent IN ({$tagSet})
        UNION  ALL
        SELECT t.parent, t.child
        FROM tagsCte c
        JOIN tags_dag t ON c.child = t.parent)");
    addWithStatement(&$withClause, "tagsSet AS (
      SELECT DISTINCT sub.id
        FROM (SELECT parent AS id FROM tagsCte
              UNION ALL
              SELECT child AS id FROM tagsCte
              {$tagUnion}) sub)");
    addStatement(&$joinClause, "JOIN categories ct ON ct.session_id = S.id");
    addWhereStatement(&$whereClause, "ct.tag_id IN (SELECT * FROM tagsSet)");
}

if (isset($lastId)) {
    addWhereStatement(&$whereClause, "S.id > {$lastId}");
    $limitClause = "LIMIT " . constant("LOAD_MORE_SESSIONS_COUNT");
}

if (empty($limitClause)) {
    $limitClause = "LIMIT " . constant("RETURNED_SESSIONS_MAX_COUNT");
}

$queryResult = pg_query($link,
    "{$withClause}
     SELECT sessions.*, users.name, users.surname
     FROM sessions JOIN users ON sessions.author = users.id
     WHERE sessions.id IN (
       SELECT DISTINCT S.id
       FROM sessions S
       {$joinClause}
       {$whereClause}
       ORDER BY S.id ASC
       {$limitClause})
     AND users.id = $userId
     ORDER BY sessions.id ASC");
echo pg_last_error($link);
$events = array();
if (isset($queryResult)) {
  $ids = "";
    $count = pg_numrows($queryResult);
    for ($i = 0; $i < $count; $i++) {
        $row = pg_fetch_array($queryResult, $i);
        $id = intval($row["id"]);
        $ids .= ($i == 0 ? "" : ", ") . $id;
        $events[] = array(
            "id" => $id,
            "label" => $row["title"],
            "date" => $row["due_date"],
            "time" => intval($row["due_time"]),
            "time2" => $row["due_time"],
            "place" => $row["place"],
            "duration" => intval($row["duration"]),
            "tags" => array(),
            "super_tags" => array(),
            "author" => $row["name"] . " " . $row["surname"],
            "own" => "true");
    }
    if (!empty($ids)) {
    $tagsSQL = "SELECT categories.*, tags.*
     FROM categories
     JOIN tags ON categories.tag_id = tags.id
     WHERE categories.session_id IN ({$ids})
     ORDER BY categories.session_id ASC";
    $tagsQuery = pg_query($link, $tagsSQL);
    $count = pg_numrows($tagsQuery);
    for ($i = 0; $i < $count; $i++) {
      $row = pg_fetch_array($tagsQuery, $i);
      $sessionId = intval($row["session_id"]);
      foreach ($events as &$e) {
        if ($e["id"] == $sessionId) {
          $e["tags"][] = array(
            "id" => $row["id"],
            "name" => $row["name"]);
        }
      }
    }
    $superTagsQuery = pg_query($link,
        "SELECT c.*, t.*
         FROM categories c
         JOIN tags_dag d ON c.tag_id = d.child
         JOIN tags t ON d.parent = t.id
         WHERE c.session_id IN ({$ids})
         ORDER BY c.session_id ASC");
         $count = pg_numrows($superTagsQuery);
      for ($i = 0; $i < $count; $i++) {
        $row = pg_fetch_array($superTagsQuery, $i);
        $sessionId = intval($row["session_id"]);
        foreach ($events as &$e) {
          if ($e["id"] == $sessionId) {
            $e["super_tags"][] = array(
              "id" => $row["id"],
              "name" => $row["name"]);
          }
        }
      }
  }
}

pg_close($link);

echo json_encode($events, true);
