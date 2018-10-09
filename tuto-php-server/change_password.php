<?php

// gets params posted as json
function get_params() {
    $str = file_get_contents("php://input");
    if (strlen($str) == 0) {
        return false;
    }
    $obj = json_decode($str);
    if (json_last_error() == JSON_ERROR_NONE) {
        return $obj;
    }
    return false;
}

$website = 'https://.../activate.php/';
$link = pg_connect("host=... dbname=... user=... password=...");

$params = get_params();
$events = array();
$no_error = false;

if (isset($params->change_password_email) && isset($params->change_password_password) && isset($params->change_password_code)) {
    $email = pg_escape_string($params->change_password_email);
    $password = pg_escape_string($params->change_password_password);
    $code = pg_escape_string($params->change_password_code); // in sha256
    
    $users_result = pg_query($link, "SELECT 1 FROM users WHERE email = '$email'");
    $code_result = pg_query($link, "SELECT 1 FROM reset_codes WHERE email = '$email' AND code = '$code'");
    $users_result = pg_fetch_result($users_result, 0);
    $code_result = pg_fetch_result($code_result, 0);
    
    if ($users_result == 1 && $code_result == 1) {
        $transaction_result = pg_query($link, "BEGIN;
                                               UPDATE users SET password = '$password' WHERE email = '$email';
                                               DELETE FROM reset_codes WHERE email = '$email';
                                               COMMIT;");

        if ($transaction_result !== false) {
            $no_error = true;
            $events['result'] = 'OK';
        }
    } elseif ($users_result != 1) {
        $events['result'] = 'error bad email';
    } elseif ($code_result != 1) {
        $events['result'] = 'error bad code';
    }
} else {
    $events['result'] = 'error';
}

pg_close($link);

sleep(1);

echo json_encode($events);


