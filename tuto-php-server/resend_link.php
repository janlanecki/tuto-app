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

if (isset($params->resend_link_email)) {
    $email = pg_escape_string($params->resend_link_email);
    
    $result = pg_query($link, "SELECT 1 FROM inactive_users WHERE email = '$email'");
    $result = pg_fetch_array($result);
    $result = $result[0];

    if ($result == 1) {
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
            "UPDATE inactive_users SET hash = '$hash' WHERE email = '$email'");
            
            if ($insert_result === false) {
                $no_error = false;
                $events['result'] = 'error';
            } elseif ($insert_result == 0) {
                $no_error = false;
                $events['result'] = 'error bad email';
            }

            if ($no_error) {
                $message = new Mail\Message();
                
                $message->setEncoding('UTF-8');
                $message->addFrom("noreply@tuto.pl", "Tuto");
                $message->addTo("$email");
                $message->setSubject("Aktywacja konta Tuto");
                $message->setBody(
"Twój link aktywacyjny:
". $website . "?email=$email&key=$key
Link jest ważny przez godzinę. Po kliknięciu będzie można zalogować się w aplikacji.

Pozdrawiamy,
Zespół Tuto


Jeśli to nie Ty się rejestrowałaś/eś, zignoruj tę wiadomość.
Ta wiadomość nie posiada adresu zwrotnego, prosimy na nią nie odpowiadać."
                );
    
                $transport = new Mail\Transport\Sendmail();
                $sent = $transport->send($message);
    
                if ($sent !== false) {
                    $events["result"] = 'OK';
                } else {
                    $events["result"] = 'error';
                }
            }
    
        } else {
            $events['result'] = 'error';
        }
    } else {
        $events['result'] = 'error bad email';
    }
}
else {
    $events['result'] = 'error';
}

pg_close($link);

sleep(1);

echo json_encode($events);