<?php
include 'header.php';

$comment = '';
$code = 0;

// Próbowałem użyć pg_insert, ale nie mogłem sprawić aby zadziałało, ale wciąż
// zwracało false, a pg_last_error zwracało pusty string ~ (Eryk).
// TODO: użyć pg_insert, bo wygląda na całkiem wygodne:
// pg_insert($link, 'sessions', $values);
// Bez tego jest dość brzydko, bo trzeba $values rozbić na 3 wartości:

$value_names = "";
$values_indices = "";
$values_array = array();
$idx = 1;

$value_names = implode(', ', $paramList);
foreach ($paramList as $param) {
  $values_indices .= (empty($values_indices) ? '' : ', ') . "\${$idx}";
  $values_array[] = $values[$param];
  ++$idx;
}

/**
 * $values_indices == "$1, $2, $3, ..., $8";
 * $value_names == "title, desciption, ..., author";
 * $values_array = [tytuł, opis, ..., user_id]
 */
pg_query($link, 'BEGIN;');
$insert_result = pg_query_params($link,
    "INSERT INTO sessions ({$value_names})
     VALUES ({$values_indices})
     RETURNING id",
    $values_array);
if ($insert_result) {
    $row = pg_fetch_array($insert_result);
    $sessionId = $row['id']; // This is the inserted id.

    $comment = 'OK';
    $code = 0;

    foreach ($tags as $tagId) {
        $insert_tag = pg_query_params($link,
          "INSERT INTO categories (session_id, tag_id) VALUES ($1, $2)",
          array($sessionId, $tagId));
        if (!$insert_tag) {
            $code = 1;
            break;
        }
    }
}

if ($code != 0) {
    $comment = pg_last_error($link);
    pg_query($link, 'ROLLBACK;');
} else {
  $res = pg_query($link, 'COMMIT;');
  // Commit can also fail.
  if (!$res) {
    $code = 1;
    $comment = pg_last_error();
  }
}

pg_close($link);

$event = array('result_code' => $code, 'comment' => $comment);
echo json_encode($event, true);
