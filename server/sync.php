<?php
/* RapidSplit Cloud-Sync – Zero-Knowledge-Speicher für Ende-zu-Ende-verschlüsselte Touren.
 * Ablage: auf dem Infomaniak-Webspace neben die Web-App legen (z. B. https://rapidsplit.app/sync.php).
 * Der Server sieht NUR Ciphertext; der Schlüssel steckt ausschließlich im geteilten Code. */

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");
header("Cache-Control: no-store");
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

$dir = __DIR__ . '/sync-data';
if (!is_dir($dir)) { @mkdir($dir, 0700, true); @file_put_contents("$dir/.htaccess", "Require all denied\n"); }

/* Kurz-Codes: 6 Zeichen -> voller Code (24 h gültig, Komfortpfad; QR/Langcode bleibt zero-knowledge) */
$cdir = "$dir/codes";
if (!is_dir($cdir)) { @mkdir($cdir, 0700, true); }
if (isset($_GET['mkcode']) && $_SERVER['REQUEST_METHOD'] === 'POST') {
    $full = trim(file_get_contents('php://input'));
    if (!preg_match('/^[A-Za-z0-9_-]{8,64}\.[A-Za-z0-9_-]{16,64}$/', $full)) { http_response_code(400); echo 'bad code'; exit; }
    $chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
    for ($try = 0; $try < 20; $try++) {
        $c = '';
        for ($i = 0; $i < 6; $i++) { $c .= $chars[random_int(0, strlen($chars) - 1)]; }
        if (!is_file("$cdir/$c")) break;
    }
    @file_put_contents("$cdir/$c", $full, LOCK_EX);
    usleep(150000);
    echo $c; exit;
}
if (isset($_GET['code'])) {
    usleep(300000);   // einfache Brute-Force-Bremse
    $c = strtoupper($_GET['code']);
    if (!preg_match('/^[A-Z2-9]{6}$/', $c)) { http_response_code(400); echo 'bad code'; exit; }
    $f = "$cdir/$c";
    if (!is_file($f) || (time() - filemtime($f)) > 86400) { if (is_file($f)) @unlink($f); http_response_code(404); echo 'not found'; exit; }
    header('Content-Type: text/plain; charset=utf-8');
    readfile($f); exit;
}

$id = isset($_GET['id']) ? $_GET['id'] : '';
if (!preg_match('/^[A-Za-z0-9_-]{8,64}$/', $id)) { http_response_code(400); echo 'bad id'; exit; }
$file = "$dir/" . $id . '.blob';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_GET['del'])) {
    if (is_file($file)) { @unlink($file); echo 'deleted'; }
    else { http_response_code(404); echo 'not found'; }
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $body = file_get_contents('php://input');
    if ($body === false || strlen($body) < 24) { http_response_code(400); echo 'empty'; exit; }
    if (strlen($body) > 400000) { http_response_code(413); echo 'too large'; exit; }              // ~400 KB reichen locker
    if (!preg_match('/^[A-Za-z0-9_-]+$/', $body)) { http_response_code(400); echo 'bad payload'; exit; }  // nur Base64url-Ciphertext
    if (@file_put_contents($file, $body, LOCK_EX) === false) { http_response_code(500); echo 'write failed'; exit; }
    echo 'ok'; exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if (!is_file($file)) { http_response_code(404); echo 'not found'; exit; }
    header('Content-Type: text/plain; charset=utf-8');
    readfile($file); exit;
}

http_response_code(405); echo 'method not allowed';
