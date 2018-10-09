<?php
include '../auth_header.php';

// Make sure these values are not set(e.g. in included files)
unset($tagId);

if (isset($params)) {
  if (isset($params->tag)) {
    $tagId = $params->tag;
  }
}

if (!isset($tagId) && isset($_GET["tag_id"])) {
    $tagId = intval($_GET["tag_id"]);
}

if (isset($tagId)) {
    $tagId = pg_escape_string($tagId);
}
?>
