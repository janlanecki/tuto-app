<?php

include 'common.php';

$session_id = intval($_GET["id"]);

$result = pg_query($link, "SELECT users.name, users.surname FROM users JOIN participants ON participants.user_id = users.id
							WHERE participants.session_id = $session_id
							UNION
							SELECT users.name, users.surname FROM users JOIN sessions ON users.id = sessions.author
							WHERE sessions.id = $session_id");

if ($result) {
    $row = pg_fetch_array($result);
    while($row) {
	    $participants[] = array("participant" => $row["name"] . " " . $row["surname"]);
	    $row = pg_fetch_array($result);
	}
}
pg_close($link);

echo json_encode($participants, true);
?>