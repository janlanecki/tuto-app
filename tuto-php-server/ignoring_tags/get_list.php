<?php
include 'header.php';

$tags = array();
if (isset($userId)) {
    $queryResult = pg_query_params($link,
        "SELECT tags.*
         FROM ignored_tags
         JOIN tags ON tags.id = ignored_tags.tag_id
         WHERE ignored_tags.user_id = $1
         ORDER BY tags.id ASC",
        array($userId));

     $count = pg_numrows($queryResult);
     for ($i = 0; $i < $count; $i++) {
         $row = pg_fetch_array($queryResult, $i);
         $tags[] = array(
             "id" => intval($row["id"]),
             "name" => $row["name"]);
     }
}


pg_close($link);

echo json_encode($tags, true);
