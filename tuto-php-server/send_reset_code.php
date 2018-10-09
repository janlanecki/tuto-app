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

function random_5_digits() {
    $rand = openssl_random_pseudo_bytes(3);
    $code = (hexdec(bin2hex($rand))) % 90000 + 10000;
    return $code;
}

$salt = '...'; // salt must be the same as in app
$website = 'https://.../activate.php/';
$link = pg_connect("host=... dbname=... user=... password=...");

$params = get_params();
$events = array('result' => 'error');
$no_error = false;

if (isset($params->change_password_email)) {
    $email = pg_escape_string($params->change_password_email);
    $code = random_5_digits();
    $hash = hash('sha256', $salt . $code);

    $select_result = pg_query($link, "SELECT 1 FROM users WHERE email = '$email'");
    $select_result = pg_fetch_array($select_result);
    $select_result = $select_result[0];
    
    if ($select_result == 1) {
        $insert_result = pg_query($link, "INSERT INTO reset_codes (email, code, sent_date) 
                                VALUES ('$email', '$hash', '". date('Y-m-d H:i:s') ."')");
        
        if ($insert_result !== false) {
            $message = new Mail\Message();
            
            $message->setEncoding('UTF-8');
            $message->addFrom("noreply@tuto.pl", "Tuto");
            $message->addTo("$email");
            $message->setSubject("Zmiana hasła Tuto");
            $message->setBody(
"$code
Powyższy kod wpisz w aplikacji razem z nowym hasłem.
Kod jest ważny przez godzinę.

Pozdrawiamy,
Zespół Tuto


Jeśli to nie Ty poprosiłeś/aś o wysłanie tego maila, zignoruj tę wiadomość.
Ta wiadomość nie posiada adresu zwrotnego, prosimy na nią nie odpowiadać."
            );
            
            $transport = new Mail\Transport\Sendmail();
            $sent = $transport->send($message);

            if ($sent !== false) {
                $no_error = true;
                $events['result'] = 'OK';
            }
        }
    } else {
        $events['result'] = 'error bad email';
    }
}

pg_close($link);

sleep(1);

echo json_encode($events);


