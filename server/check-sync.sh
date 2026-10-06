#!/bin/bash
# Prüft eine deployte sync.php (Standard: rapidsplit.app) nach dem Upload.
# Legt eine Test-Tour an und löscht sie am Ende wieder.
# Aufruf:  bash server/check-sync.sh [https://host/sync.php]
set -u
URL="${1:-https://rapidsplit.app/sync.php}"
ID="chk$(LC_ALL=C tr -dc 'a-z0-9' </dev/urandom | head -c 12)"
BLOB="$(LC_ALL=C tr -dc 'A-Za-z0-9_-' </dev/urandom | head -c 64)"
fehler=0
ok()   { echo "  ✓ $1"; }
fail() { echo "  ✗ $1"; fehler=1; }

echo "sync.php-Check gegen $URL"
r=$(curl -s -X POST --data "$BLOB" "$URL?id=$ID");                     [ "$r" = "ok" ] && ok "Upload" || fail "Upload: $r"
r=$(curl -s "$URL?id=$ID");                                             [ "$r" = "$BLOB" ] && ok "Download" || fail "Download"
HASH=$(printf %s "$BLOB" | shasum -a 256 | cut -d' ' -f1)
BLOB2="$(LC_ALL=C tr -dc 'A-Za-z0-9_-' </dev/urandom | head -c 64)"
s=$(curl -s -o /dev/null -w '%{http_code}' -X POST --data "$BLOB2" "$URL?id=$ID&base=0000"); [ "$s" = "409" ] && ok "Veralteter Stand wird nicht überschrieben (409)" || fail "Upload mit falschem base: HTTP $s statt 409 – neue sync.php deployt?"
s=$(curl -s -o /dev/null -w '%{http_code}' -X POST --data "$BLOB2" "$URL?id=$ID&base=neu");  [ "$s" = "409" ] && ok "Neu-Anlage überschreibt nichts (409)" || fail "base=neu auf bestehende Tour: HTTP $s statt 409"
r=$(curl -s -X POST --data "$BLOB2" "$URL?id=$ID&base=$HASH");         [ "$r" = "ok" ] && ok "Upload auf aktuellem Stand" || fail "Upload mit passendem base: $r"
r=$(curl -s "$URL?id=$ID");                                             [ "$r" = "$BLOB2" ] && ok "Neuer Stand abrufbar" || fail "Download nach Vergleichs-Upload"
TID="t$(LC_ALL=C tr -dc 'A-Za-z0-9_-' </dev/urandom | head -c 16)"
s=$(curl -s -o /dev/null -w '%{http_code}' -X POST --data "$BLOB" "$URL?id=$TID");          [ "$s" = "428" ] && ok "Neue Touren nur mit Vergleichswert (428)" || fail "Upload ohne base auf t-ID: HTTP $s statt 428"
r=$(curl -s -X POST --data "$BLOB" "$URL?id=$TID&base=neu");           [ "$r" = "ok" ] && ok "Erst-Teilen mit base=neu" || fail "base=neu: $r"
curl -s -o /dev/null -X POST --data x "$URL?del=1&id=$TID"
IID="chkinv$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 12)"
r=$(curl -s -X POST --data "$BLOB" "$URL?inv=$IID");                   [ "$r" = "ok" ] && ok "Einladung anlegen" || fail "Einladung anlegen: $r – neue sync.php deployt?"
r=$(curl -s "$URL?inv=$IID");                                           [ "$r" = "$BLOB" ] && ok "Einladung abrufen" || fail "Einladung abrufen"
s=$(curl -s -o /dev/null -w '%{http_code}' -X POST --data "$BLOB2" "$URL?inv=$IID");        [ "$s" = "409" ] && ok "Einladung nicht überschreibbar (409)" || fail "Einladung überschreiben: HTTP $s statt 409"
s=$(curl -s -o /dev/null -w '%{http_code}' "$URL?inv=chkinvGibtEsNicht");                  [ "$s" = "404" ] && ok "Unbekannte Einladung (404)" || fail "unbekannte Einladung: HTTP $s"
s=$(curl -s -o /dev/null -w '%{http_code}' -X POST --data "$ID.abcdefghijklmnopqrstuvwxyz" "$URL?mkcode=1")
[ "$s" = "410" ] && ok "Kurzcodes abgeschaltet (kein Schlüssel auf dem Server)" || fail "mkcode antwortet HTTP $s statt 410 – neue sync.php deployt?"
s=$(curl -s -o /dev/null -w '%{http_code}' "$URL?code=ABCDEF");        [ "$s" = "410" ] && ok "Kurzcode-Abfrage abgeschaltet" || fail "code-Abfrage HTTP $s statt 410"
r=$(curl -s -X POST --data x "$URL?del=1&id=$ID");                      [ "$r" = "deleted" ] && ok "Teilen beenden" || fail "del: $r"
s=$(curl -s -o /dev/null -w '%{http_code}' "$URL?id=$ID");             [ "$s" = "404" ] && ok "Tour gelöscht" || fail "Tour nach del noch da (HTTP $s)"
s=$(curl -s -o /dev/null -w '%{http_code}' -X POST --data "$BLOB" "$URL?id=$ID"); [ "$s" = "410" ] && ok "Beendete Teilung bleibt beendet" || fail "Upload nach del: HTTP $s statt 410"
s=$(curl -s -o /dev/null -w '%{http_code}' "$URL?id=a%5B%5D");         [ "$s" = "400" ] && ok "Ungültige ID abgewiesen" || fail "ungültige ID: HTTP $s"
[ $fehler -eq 0 ] && echo "Alles in Ordnung." || echo "FEHLER – siehe oben."
exit $fehler
