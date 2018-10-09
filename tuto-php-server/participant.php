<?php

$session_id = intval($_GET["id"]);
$email = $_GET["email"];

$link = pg_connect("host=... dbname=... user=... password=...");

$result = pg_query($link, "SELECT id FROM users WHERE email = '" . "$email". "'");
$user_id = pg_fetch_result($result, null, "id");

if ($user_id) {
    $result = pg_query($link, "INSERT INTO participants VALUES ($user_id, $session_id)");
    if ($result) {
        $result = true;
    } else {
        $result = false;
    }
} else {
    $result = false;
}

$events = array("result" => ($result ? "success" : "failure"));

pg_close($link);

echo json_encode($events, true);
