<?php

$session_id = intval($_GET["id"]);
$email = $_GET["email"];

$link = pg_connect("host=... dbname=... user=... password=...");

$result = pg_query($link, "SELECT id FROM users WHERE email = " . "'$email'");
$user_id = pg_fetch_result($result, null, "id");

if ($user_id) {
    $result = pg_fetch_row(pg_query($link, "SELECT * FROM participants WHERE session_id = $session_id AND user_id = $user_id"));
} else {
    $result = false;
}

$events = array("result" => ($result ? "true" : "false"));

pg_close($link);

echo json_encode($events, true);
