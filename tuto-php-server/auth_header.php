<?php
include 'common.php';

// Make sure these values are not set(e.g. in included files)
unset($userEmail);
unset($userId);

if (isset($params)) {
  if (isset($params->user)) {
    $userEmail = $params->user;
  }
}

if (!isset($userEmail) && isset($_GET["user_email"])) {
  $userEmail = $_GET["user_email"];
}

/**
 * Tu możemy sprawdzić, czy request jest poprawny - np. możemy do requestów,
 * które zmieniają bazę danych(jak dołączanie do sesji, dodawanie sesji,
 * ignorowanie tagów) dodać hash hasła i sprawdzić tu, czy się zgadza.
 */
if (isset($userEmail)) {
    $result = pg_query_params(
      $link,
      "SELECT id FROM users WHERE email = $1",
      array($userEmail));
    $userId = pg_fetch_result($result, null, "id");
}

if (!isset($userId) || !$userId) {
  die("Invalid email");
}
?>
