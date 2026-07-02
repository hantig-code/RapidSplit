# Kajak-Tour Kostenrechner — Stand-Alone-App (PWA)

Diese App ist eine **Progressive Web App (PWA)**: ein Web-Programm, das sich auf
Android, Windows und macOS wie eine echte App installieren lässt — mit eigenem
Icon, eigenem Fenster (ohne Browser-Leiste) und **vollständig offline** nutzbar.
Alle Daten bleiben lokal auf dem Gerät (kein Server, kein Konto).

## Was liegt in diesem Ordner?

| Datei | Zweck |
|-------|-------|
| `index.html` | Die App selbst |
| `manifest.webmanifest` | App-Name, Icon, Standalone-Modus |
| `sw.js` | Service Worker — macht die App offline-fähig |
| `icons/` | App-Icons (192 / 512 px + Apple-Touch-Icon) |
| `serve.py` | Kleiner lokaler Test-Server (nur zum Ausprobieren) |

## Wichtig: PWAs brauchen einmal eine HTTPS-Adresse

Damit sich die App **installieren** lässt, muss sie über **`https://…`**
(oder testweise `http://localhost`) ausgeliefert werden. Ein direktes
Doppelklick-Öffnen der Datei (`file://…`) funktioniert **nicht** für die
Installation. Du brauchst also einmalig einen kostenlosen Hosting-Platz.

### Empfohlen: GitHub Pages (kostenlos, dauerhaft)

1. Kostenloses Konto auf <https://github.com> anlegen.
2. Neues Repository erstellen (z. B. `kajak-kostenrechner`), „Public".
3. Den **gesamten Inhalt dieses Ordners** hochladen
   (`index.html`, `manifest.webmanifest`, `sw.js`, Ordner `icons/`).
4. Im Repo: **Settings → Pages → Branch: `main` / Root → Save**.
5. Nach ~1 Minute ist die App erreichbar unter
   `https://DEIN-NAME.github.io/kajak-kostenrechner/`.

Diese eine URL rufst du dann auf jedem Gerät auf und installierst die App dort.

> Alternativen mit demselben Ergebnis: **Netlify Drop** (<https://app.netlify.com/drop> —
> Ordner einfach per Drag-&-Drop hochladen) oder **Cloudflare Pages**.

## Installieren auf den Geräten

**Android (Chrome):** URL öffnen → Menü `⋮` → **„App installieren"** /
„Zum Startbildschirm hinzufügen". Danach liegt das Kajak-Icon im App-Drawer.

**Windows (Chrome oder Edge):** URL öffnen → in der Adressleiste das
**Installations-Symbol** (Monitor mit Pfeil) anklicken → **Installieren**.
Die App erscheint im Startmenü und kann an die Taskleiste geheftet werden.

**macOS:**
- *Chrome/Edge:* URL öffnen → Installations-Symbol in der Adressleiste → Installieren.
- *Safari 17+:* URL öffnen → Menü **Ablage → „Zum Dock hinzufügen"**.

Nach der Installation startet die App im eigenen Fenster und funktioniert
**offline** weiter. Aktualisierte Daten (neue Touren) speichert sie automatisch
lokal; zum Sichern/Teilen gibt es in der App **Export/Import** und **Drucken/PDF**.

## App aktualisieren

Wenn du `index.html` (oder andere Dateien) änderst:
1. In `sw.js` die Zeile `CACHE_VERSION = "kajak-kostenrechner-v1"` hochzählen
   (z. B. `…-v2`) — sonst zeigt die installierte App die alte Version aus dem Cache.
2. Dateien neu hochladen. Beim nächsten Start lädt die App die neue Version.

## Lokal testen (optional)

Im Terminal aus diesem Ordner:

```bash
python3 serve.py 8766
```

Dann im Browser <http://localhost:8766> öffnen. Über `localhost` ist die App
ebenfalls installierbar — ideal zum Ausprobieren vor dem Hochladen.

## Hinweis zu „echten" Installer-Dateien (.apk / .exe / .dmg)

Die PWA deckt alle drei Plattformen mit einer Codebasis ab und braucht keine
App-Stores. Falls du später doch klassische Installer möchtest, lässt sich die
PWA mit Werkzeugen wie **Tauri** oder **PWABuilder** (<https://pwabuilder.com>)
in native Pakete verpacken — frag bei Bedarf einfach nach.
