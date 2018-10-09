<?php

// works with zend-mail >= 2.8? (installed by composer) and php >= 5.2 
include('../../zend/vendor/autoload.php');
// include 'common.php';

use Zend\Mail;

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
if ($params !== false && 
    isset($params->signup_name) && 
    isset($params->signup_surname) && 
    isset($params->signup_email) &&
    isset($params->signup_password)) {

    $email = pg_escape_string($params->signup_email);
    $start_date = date('Y-m-d H:i:s');
    
    // do not change the order of these selects
    $inactive_user_result = pg_query($link, "SELECT COUNT(*) FROM inactive_users WHERE email = '$email' AND start_date >= NOW() - INTERVAL '1 hour'");
    $inactive_user = pg_fetch_array($inactive_user_result);
    $inactive_user = $inactive_user[0];
    
    $user_result = pg_query($link, "SELECT COUNT(*) FROM users WHERE email = '$email'");
    $user = pg_fetch_array($user_result);
    $user = $user[0];
    // ----------------------------------------
    
    if ($inactive_user == 0 && $user == 0) {
        $no_error = true;

        $crypto_strong = false;
        $key = openssl_random_pseudo_bytes(32, $crypto_strong);
        if ($crypto_strong == false) { // should not occur
            $no_error = false;
            $events['result'] = 'error';
        }        

        if ($no_error) {
            $key = hash('sha256', $key);
            $hash = hash('sha256', $key);
            
            $insert_result = pg_query($link, 
            "INSERT INTO inactive_users (name, surname, email, password, start_date, hash)
                VALUES ('". pg_escape_string($params->signup_name) ."',
                        '". pg_escape_string($params->signup_surname) ."',
                        '". $email ."',
                        '". pg_escape_string($params->signup_password) ."',
                        '". $start_date ."',
                        '". $hash ."')");
            
            if ($insert_result === false) {
                $no_error = false;
                $events['result'] = 'error';
            }
        }
        
        if ($no_error) {
            $message = new Mail\Message();
            
            $message->setEncoding('UTF-8');
            $message->addFrom("noreply@tuto.pl", "Tuto");
            $message->addTo("$email");
            $message->setSubject("Aktywacja konta Tuto");
            $message->setBody(
"Twój link aktywacyjny:
". $website ."?email=$email&key=$key
Link jest ważny przez godzinę. Po kliknięciu będzie można zalogować się w aplikacji.
Jeśli kiedykolwiek zechcesz się wyrejestrować lub usunąć przechowywane przez nas "
."dane osobowe, napisz na adres e-mail tuto@students.mimuw.edu.pl.

Pozdrawiamy,
Zespół Tuto


Jeśli to nie Ty się rejestrowałeś/aś, zignoruj tę wiadomość.
Ta wiadomość nie posiada adresu zwrotnego, prosimy na nią nie odpowiadać."
            );

            $transport = new Mail\Transport\Sendmail();
            $sent = $transport->send($message);

            if ($sent !== false) {
                $events["result"] = 'OK';
            } else {
                $events["result"] = 'error email not sent'; // email could not be sent
            }
        }

    } else if ($user == 1) {
        $events["result"] = 'error email taken';
    } else { // $inactive_user == 1
        $events["result"] = 'error already inactive';
    }

} else {
    $events["result"] = "error";
}

pg_close($link);

sleep(1);

echo json_encode($events);