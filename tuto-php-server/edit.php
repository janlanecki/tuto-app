<?php
include 'auth_header.php';

unset($params->user);
$id = $params->id;
unset($params->id);
unset($params->tags);

$updated = "";
foreach ($params as $key => $value) {
    $updated .= $key . " = '" . $value . "', ";
}
$updated = rtrim($updated, ", ");

if (!empty($updated)) {
    pg_query($link, "UPDATE sessions SET {$updated} WHERE id = {$id}");

    $error = pg_last_error();
    $event = array('result_code' => ($error ? 1 : 0), 'comment' => $error);
} else {
    $event = array('result_code' => 0, 'comment' => "No changes");
}

pg_close($link);

echo json_encode($event, true);
