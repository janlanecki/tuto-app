<?php
include 'auth_header.php';

// Make sure these values are not set(e.g. in included files)
unset($input);
unset($picked);

if (isset($params)) {
  if (isset($params->input)) {
    $input = $params->input;
  }
  if (isset($params->picked)) {
    $picked = json_decode($params->picked, true);
    if (!is_array($picked)) {
      unset($picked);
    }
  }
}

if (!isset($input) && isset($_GET["input"])) {
  $input = $_GET["input"];
}
if (!isset($picked) && isset($_GET["picked"])) {
  $picked = json_decode($_GET["picked"], true);
  if (!is_array($picked)) {
      unset($picked);
  }
}
if (!isset($input) || !isset($picked)) {
  die('Missing parameters');
}

$picked[] = -1; // It can not be empty or the query crashes.
$picked = implode(', ', $picked);

$withBase = "
  WITH RECURSIVE superTags AS (
    SELECT id, 0 AS level, -1 AS parent
    FROM tags
    WHERE NOT EXISTS (SELECT * FROM tags_dag WHERE child=id)
  ), pickedTags AS (
    SELECT id
    FROM tags
    WHERE id IN ($picked)
    UNION ALL
    SELECT parent AS id
    FROM tags_dag d
    JOIN pickedTags p
    ON p.id = d.child
  ) , tagsCte AS (
      SELECT * FROM superTags
      UNION  ALL
      SELECT t.child AS id, c.level + 1, t.parent
      FROM tagsCte c
      JOIN tags_dag t ON c.id = t.parent
      WHERE t.parent IN (SELECT * FROM pickedTags)
  )";

$sqlQuery = "
  {$withBase}
  SELECT DISTINCT t.id, t.name, level, parent
  FROM tags t
  JOIN tagsCte cte ON t.id = cte.id
  ORDER BY level ASC, name ASC
  ";

function inputSQL($input) {
  global $withBase;
  $input = pg_escape_string($input);
  return "
    {$withBase} , underTagsCte AS (
        SELECT * FROM tagsCte
        WHERE id IN (SELECT * FROM pickedTags UNION SELECT id FROM superTags)
        UNION  ALL
        SELECT t.child AS id, c.level + 1, t.parent
        FROM underTagsCte c
        JOIN tags_dag t ON c.id = t.parent
    ), unionCte AS (
      SELECT * FROM underTagsCte
      UNION ALL
      SELECT * FROM tagsCte
    )
    SELECT DISTINCT t.id, t.name, parent, level
    FROM tags t
    JOIN unionCte cte ON t.id = cte.id
    WHERE LOWER( t.name ) LIKE '{$input}%'
    ORDER BY level ASC, name ASC
    LIMIT 11
    ";
}
$tags = array();
$events = array();
if (!empty($input)) {
  $query = pg_query($link, inputSQL($input));
} else {
  $query = pg_query($link, $sqlQuery);
}

if (isset($query)) {
  $count = pg_numrows($query);
  for ($i = 0; $i < $count; $i++) {
      $row = pg_fetch_array($query, $i);
      $events[] = array(
        'id' => $row['id'],
        'name' => $row['name'],
        'parent' => $row['parent']);
  }
}

pg_close($link);

// echo str_replace(array('},'), array('},<br>'), json_encode($events, true));
echo json_encode($events, true);
