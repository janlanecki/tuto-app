<?php
include '../auth_header.php';

// Make sure these values are not set(e.g. in included files)
unset($values);

$paramList = array('title', 'description', 'due_time',
                 'due_date', 'duration', 'people_limit',
                 'place', 'author');
$values = array();

if (isset($params)) {
  foreach ($paramList as $param) {
    if (isset($params->{$param})) {
      $values[$param] = $params->{$param};
    }
  }
  if (isset($params->tags)) {
    $tmp = json_decode($params->tags, true);
    if (is_array($tmp)) {
      $tags = $tmp;
    }
    unset($tmp);
  }
}

foreach ($paramList as $param) {
  if (!isset($values[$param]) && isset($_GET[$param])) {
    $values[$param] = $_GET[$param];
  }
}

if (!isset($values['tags']) && isset($_GET['tags'])) {
  $tmp = json_decode($_GET['tags'], true);
  if (is_array($tmp)) {
    $tags = $tmp;
  }
  unset($tmp);
}

if (!isset($tags) || !is_array($tags)) {
  $tags = array();
}

$values['author'] = $userId;

// Jeżeli brakuje którejś wartości - zwróć błąd.
// $userId jest sprawdzane i ładowane w nagłówku
foreach ($paramList as $param) {
  if (!isset($values[$param])) {
    $event = array('result_code' => 1, 'comment' => "Brak parametru {$param}");
    echo json_encode($event, true);
    exit(1);
  }
}
?>
