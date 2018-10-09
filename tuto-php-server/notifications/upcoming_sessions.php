<?php
$last_notification = getLastNotificationTime();

// Przypomnij dwie godziny przed sesją i godzinę przed sesją.
$reminders = array(
  1800 => "pół godziny",
  3600 => "godzinę",
  7200 => "dwie godziny");

$title = "Nadchodząca sesja";
$contents = function($name, $time) {
    return "Sesja \"{$name}\" rozpocznie się za {$time}";
};

$query = pg_query($link,
    "SELECT *
     FROM sessions
     WHERE id IN (SELECT DISTINCT s.id
       FROM sessions s JOIN participants p ON p.session_id = s.id
       WHERE p.user_id = {$userId}
       AND s.author != {$userId})
     OR author = {$userId}
     ORDER BY due_date DESC, due_time DESC, id ASC");

$count = pg_numrows($query);
for ($i = 0; $i < $count; $i++) {
  $row = pg_fetch_array($query, $i);
  $epoch = strtotime("{$row['due_date']} {$row['due_time']}");
  if ($epoch === false) {
    // Could not parse epoch time for this entry.
    continue;
  }
  $currentDiff = $epoch - time();
  $lastDiff = $epoch - $last_notification;
  foreach ($reminders as $diff => $label) {
    if ($lastDiff > $diff && $currentDiff <= $diff) {
      addNotification($title, $contents($row['title'], $label), 0);
      break;
    }
  }
}

?>
