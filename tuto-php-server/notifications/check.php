<?php
include 'header.php';

$server_time = time();

if (isset($last_notification_time)) {
  foreach ($notification_modules as $module) {
    require_once("{$module}.php");
  }
}

pg_close($link);

echo json_encode(buildResponse(), true);
