<?php
include 'header.php';

$result = '';

if (isset($userId) && isset($tagId)) {
    $result = pg_query_params($link,
        "DELETE FROM ignored_tags
         WHERE user_id = $1 AND tag_id = $2",
        array($userId, $tagId));
    if ($result) {
        $result = 'OK';
    }
}

if (empty($result)) {
  $result = 'ERROR';
}

pg_close($link);

$event = array('result' => $result);
echo json_encode($event, true);
