<?php

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

$link = pg_connect("host=... dbname=... user=... password=...");

// Tablica $_POST jest pusta, bo parametry są wysyłane jako JSON -
// - musimy je sami odczytać.
$params = get_params();

$events = array();
