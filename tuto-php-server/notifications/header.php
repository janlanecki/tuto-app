<?php
include '../auth_header.php';

$notification_modules = array('upcoming_sessions');

$notifications = array();

/* Load last notification time. */
if (isset($params) && isset($params->last_notification_time)) {
  $last_notification_time = $params->last_notification_time;
}

if (!isset($last_notification_time) && isset($_GET["last_notification_time"])) {
  $last_notification_time = $_GET["last_notification_time"];
}

/*********************************/

function addNotification($title, $contents, $time) {
  global $notifications;
  $notifications[] = array(
    'title' => $title,
    'contents' => $contents,
    'server_time' => $time);
}

function buildResponse($server_time) {
  global $notifications;
  return array('server_time' => time(),
               'notifications' => &$notifications);
}

function getLastNotificationTime() {
  global $last_notification_time;
  return $last_notification_time;
}

?>
