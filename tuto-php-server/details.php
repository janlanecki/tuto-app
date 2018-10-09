<?php

$id = intval($_GET["id"]);

$link = pg_connect("host=... dbname=... user=... password=...");

$events = array();

if ($id !== null) {
    $result = pg_query($link, "SELECT sessions.*, users.name, users.surname FROM sessions JOIN users ON sessions.author = users.id WHERE sessions.id = $id");
    $row = pg_fetch_array($result);
    if ($row) {
        $tags = pg_query($link, "SELECT tags.name FROM categories JOIN tags ON categories.tag_id = tags.id WHERE categories.session_id = $id");
        $tag1 = pg_fetch_array($tags, 0);
        $tag2 = pg_fetch_array($tags, 1);
        $events = array(
            "id" => $row["id"],
            "label" => $row["title"],
            "date" => date("d-m-Y", strtotime($row["due_date"])),
            "time" => date("H:i", strtotime($row["due_time"])),
            "duration" => $row["duration"],
            "limit" => $row["people_limit"],
            "place" => $row["place"],
            "description" => $row["description"],
            "author" => $row["name"] . " " . $row["surname"],
            "tag1" => $tag1["name"],
            "tag2" => $tag2["name"]);
    }
}

pg_close($link);

echo json_encode($events, JSON_FORCE_OBJECT);
