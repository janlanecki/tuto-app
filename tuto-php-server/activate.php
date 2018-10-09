<?php

echo "<!DOCTYPE><html><head><meta charset='UTF-8'></head><body>";

if (isset($_GET['email']) && isset($_GET['key'])) {
    $link = pg_connect("host=... dbname=... user=... password=...");

    $email = pg_escape_string($_GET['email']);
    $key = pg_escape_string($_GET['key']);
    $hashed_key = hash('sha256', $key);

    $result = pg_query($link, "SELECT name, surname, email, password, start_date, hash FROM inactive_users WHERE email = '$email'");
    $result = pg_fetch_array($result);
    $hash = $result['hash'];
    
    if ($hashed_key === $hash) {
        $query_result = pg_query($link, 
        "BEGIN;
         INSERT INTO users (name, surname, email, password, start_date)
            VALUES ('". $result['name']. "',
                    '". $result['surname'] ."',
                    '". $result['email'] ."',
                    '". $result['password'] ."',
                    '". $result['start_date'] ."');
         DELETE FROM inactive_users WHERE email = '$email';
         COMMIT;");

        if ($query_result !== false) {
            echo '<h1>Konto aktywowane!</h1>';
        } else {
            echo '<h1>Wystąpił błąd, odśwież stronę lub wyślij link aktywacyjny ponownie.</h1>';
        }
    } else {
        echo '<h1>Niepoprawny link.</h1>';
    }

    pg_close($link);
}
else {
    echo '<h1>Błąd 404</h1>';
}

echo "</body></html>";