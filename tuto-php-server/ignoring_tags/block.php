<?php
include 'header.php';

$result = '';

if (isset($userId) && isset($tagId)) {
    $insert_result = pg_query_params($link,
        "INSERT INTO ignored_tags (user_id, tag_id)
         VALUES ($1, $2)",
        array($userId, $tagId));

    if ($insert_result !== false) {
        $result = 'OK';
    }
}

if (empty($result)) {
  $result= 'ERROR';
}

pg_close($link);

$event = array('result' => $result);
echo json_encode($event, true);
