<?php

include 'common.php';

$result = pg_query($link, "SELECT password FROM users WHERE email = " . "'$params->email'");
$row = pg_fetch_array($result);
if (!$row) {
    $events = array(
        "result" => "email");
} else if ($params->password !== $row["password"]) {
    $events = array(
        "result" => "password");
} else {
    $events = array(
        "result" => "OK");
}

sleep(1);

echo json_encode($events, JSON_FORCE_OBJECT);
