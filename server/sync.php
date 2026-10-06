<?php
/* RapidSplit Cloud-Sync – Zero-Knowledge-Speicher für Ende-zu-Ende-verschlüsselte Touren.
 * Ablage: auf dem Webspace neben die Web-App legen (z. B. https://rapidsplit.app/sync.php).
 * Der Server sieht NUR Ciphertext und zufällige Kennungen. Tour-Schlüssel sind zufällig; der Tour-Code
 * (bzw. QR-Code) öffnet nur eine verschlüsselte Einladung (3 Tage) und erreicht diesen Server nie. */

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");
header("Cache-Control: no-store");
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

$dir = __DIR__ . '/sync-data';
if (!is_dir($dir)) { @mkdir($dir, 0700, true); @file_put_contents("$dir/.htaccess", "Require all denied\n"); }

/* Bis 4.2.2 hinterlegten 6-stellige Kurz-Codes den vollen Schlüssel hier. Das ist abgeschaltet:
   noch vorhandene Code-Dateien (samt Schlüssel) werden gelöscht, neue nicht mehr angenommen. */
$cdir = "$dir/codes";
if (is_dir($cdir)) {
    foreach (array_merge(glob("$cdir/*") ?: [], glob("$cdir/.[!.]*") ?: []) as $f) { if (is_file($f)) @unlink($f); }
    @rmdir($cdir);
}
$rdir = "$dir/rl";
if (is_dir($rdir)) {
    foreach (glob("$rdir/*") ?: [] as $f) { if (is_file($f)) @unlink($f); }
    @rmdir($rdir);
}
if (isset($_GET['mkcode']) || isset($_GET['code'])) { http_response_code(410); echo 'short codes disabled'; exit; }

/* Einladungen: Der Tour-Code öffnet nur sie – verschlüsselt, mit Tour-ID und Schlüssel darin. Sie werden einmalig
   angelegt und nie überschrieben. Nach 3 Tagen wird der Inhalt gelöscht; eine leere Marke hält den Platz für immer
   belegt (sonst könnte jemand mit dem alten Code dort eine Einladung zu einer fremden Tour hinterlegen) und lässt
   die App „abgelaufen“ melden (410). Der Server kann Einladungen nicht lesen. */
$EINL_TTL = 3 * 86400;
$idir = "$dir/inv";
$adir = "$idir/abg";
if (!is_dir($adir)) { @mkdir($adir, 0700, true); }
function einlAufraeumen($idir, $adir, $ttl) {   // höchstens alle 15 Minuten, nie zwei gleichzeitig, begrenzt
    $sh = @fopen("$idir/.aufgeraeumt", 'c');
    if ($sh === false) { return; }
    if (flock($sh, LOCK_EX | LOCK_NB)) {
        clearstatcache();
        $m = @filemtime("$idir/.aufgeraeumt");
        if ($m === false || $m < time() - 900) {
            @touch("$idir/.aufgeraeumt");
            $grenze = time() - $ttl; $n = 0;
            if (($dh = @opendir($idir)) !== false) {
                while ($n < 5000 && ($f = readdir($dh)) !== false) {
                    if (substr($f, -4) !== '.inv') { continue; }
                    $n++;
                    $t = @filemtime("$idir/$f");
                    if ($t !== false && $t < $grenze) { @touch("$adir/" . substr($f, 0, -4) . '.abg'); @unlink("$idir/$f"); }
                }
                closedir($dh);
            }
        }
        flock($sh, LOCK_UN);
    }
    fclose($sh);
}
einlAufraeumen($idir, $adir, $EINL_TTL);
if (isset($_GET['inv'])) {
    $iid = is_string($_GET['inv']) ? $_GET['inv'] : '';
    if (!preg_match('/^[A-Za-z0-9_-]{8,64}$/', $iid)) { http_response_code(400); echo 'bad id'; exit; }
    $ifile = "$idir/$iid.inv";
    $imarke = "$adir/$iid.abg";
    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $body = file_get_contents('php://input');
        if ($body === false || strlen($body) < 24) { http_response_code(400); echo 'empty'; exit; }
        if (strlen($body) > 4096) { http_response_code(413); echo 'too large'; exit; }
        if (!preg_match('/^[A-Za-z0-9_-]+$/', $body)) { http_response_code(400); echo 'bad payload'; exit; }
        $h = @fopen("$idir/.lock", 'c'); if ($h !== false) { flock($h, LOCK_EX); }
        if (is_file($ifile) || is_file($imarke)) { if ($h !== false) { flock($h, LOCK_UN); fclose($h); } http_response_code(409); echo 'exists'; exit; }
        $tmp = $ifile . '.tmp';
        $ok = (@file_put_contents($tmp, $body) !== false) && @rename($tmp, $ifile);
        if ($h !== false) { flock($h, LOCK_UN); fclose($h); }
        if (!$ok) { http_response_code(500); echo 'write failed'; exit; }
        echo 'ok'; exit;
    }
    if ($_SERVER['REQUEST_METHOD'] === 'GET') {
        $t = is_file($ifile) ? @filemtime($ifile) : false;
        if ($t !== false && $t < time() - $EINL_TTL) { @touch($imarke); @unlink($ifile); }   // erst Marke, dann löschen
        if (is_file($imarke)) { http_response_code(410); echo 'expired'; exit; }
        if (!is_file($ifile)) { http_response_code(404); echo 'not found'; exit; }
        header('Content-Type: text/plain; charset=utf-8');
        readfile($ifile); exit;
    }
    http_response_code(405); echo 'method not allowed'; exit;
}

$id = (isset($_GET['id']) && is_string($_GET['id'])) ? $_GET['id'] : '';
if (!preg_match('/^[A-Za-z0-9_-]{8,64}$/', $id)) { http_response_code(400); echo 'bad id'; exit; }
$file = "$dir/" . $id . '.blob';

$gone = "$dir/" . $id . '.gone';   // Grabstein: beendete Teilungen bleiben beendet

/* Eine Sperre je Tour: Prüfen und Schreiben passieren atomar (Grabstein, Vergleich). */
function sperren($file) { $h = @fopen($file . '.lock', 'c'); if ($h !== false) { flock($h, LOCK_EX); } return $h; }
function freigeben($h) { if ($h !== false) { flock($h, LOCK_UN); fclose($h); } }

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_GET['del'])) {
    $h = sperren($file);
    @touch($gone);
    $gab = is_file($file);
    if ($gab) { @unlink($file); }
    freigeben($h);
    if ($gab) { echo 'deleted'; } else { http_response_code(404); echo 'not found'; }
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $body = file_get_contents('php://input');
    if ($body === false || strlen($body) < 24) { http_response_code(400); echo 'empty'; exit; }
    if (strlen($body) > 400000) { http_response_code(413); echo 'too large'; exit; }              // ~400 KB reichen locker
    if (!preg_match('/^[A-Za-z0-9_-]+$/', $body)) { http_response_code(400); echo 'bad payload'; exit; }  // nur Base64url-Ciphertext
    /* Touren ab 4.2.3 (IDs „t…“, 17 Zeichen) schreiben nur mit Vergleichswert – nie blind überschreiben.
       Ältere Versionen nutzen 16-stellige IDs ohne „t“ und sind davon nicht betroffen. */
    if (preg_match('/^t[A-Za-z0-9_-]{16}$/', $id) && !isset($_GET['base'])) { http_response_code(428); echo 'base required'; exit; }
    $h = sperren($file);
    if (is_file($gone)) { freigeben($h); http_response_code(410); echo 'gone'; exit; }   // Mitglieder legen beendete Teilungen nicht neu an
    /* Vergleich ohne Klartext: Die App schickt den Hash des Chiffretexts, den sie zuletzt kannte.
       Hat inzwischen jemand anderes hochgeladen, wird nicht still überschrieben (409). */
    if (isset($_GET['base']) && is_string($_GET['base'])) {
        $aktuell = is_file($file) ? hash_file('sha256', $file) : 'neu';
        if (!hash_equals((string)$aktuell, $_GET['base'])) { freigeben($h); http_response_code(409); echo 'conflict'; exit; }
    }
    $tmp = $file . '.tmp';   // erst vollständig schreiben, dann atomar ersetzen – Leser sehen nie halbe Blobs
    $ok = (@file_put_contents($tmp, $body) !== false) && @rename($tmp, $file);
    freigeben($h);
    if (!$ok) { http_response_code(500); echo 'write failed'; exit; }
    echo 'ok'; exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    if (is_file($gone) || !is_file($file)) { http_response_code(404); echo 'not found'; exit; }
    header('Content-Type: text/plain; charset=utf-8');
    readfile($file); exit;
}

http_response_code(405); echo 'method not allowed';
