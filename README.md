# 🛶 RapidSplit — Tour-Kosten fair teilen

RapidSplit rechnet die Kosten einer Kajak-, Vereins- oder Gruppentour **fair pro
Teilnehmer:in** ab — etappengenau statt pauschal: Fahrtkosten je Etappe, gemeinsame
Ausgaben, Salden und „wer zahlt an wen". Läuft im Browser oder als Android-App,
**offline** nutzbar, die Daten liegen lokal auf dem Gerät (kein Konto, kein Tracking).
Nur wer eine Tour teilt, schickt sie über den Server — Ende-zu-Ende-verschlüsselt, der
Server kann sie nicht lesen. Karte, Ortssuche, Strecke und automatische Wechselkurse
(Schalter „Kurse automatisch laden“) fragen externe Dienste an, und zwar nur, wenn du sie
nutzt; dabei gehen z. B. der Suchtext oder die Koordinaten von Start und Ziel an den
jeweiligen Dienst.

## ▶️ App öffnen (aktuelle Version 4)

**https://hantig-code.github.io/RapidSplit/app/**

> Die Adresse `…/app/` ist die dauerhafte, versionslose App-URL — Version 4 (und alle künftigen
> Updates) werden dort ausgeliefert; installierte Apps aktualisieren sich automatisch.

**Als App installieren:**

- **Android:** in Chrome öffnen → Menü ⋮ → **„App installieren"** — oder die
  native **Android-App** über [Google Play](https://play.google.com/store/apps/details?id=com.hantig.rapidsplit)
  bzw. [F-Droid](https://f-droid.org/packages/app.rapidsplit/) installieren.
- **iPhone/iPad:** in **Safari** öffnen → Teilen (↑) → **„Zum Home-Bildschirm"**.
- **Windows/macOS/Linux:** in Chrome/Edge das Installations-Symbol in der
  Adressleiste, oder in Safari **Ablage → „Zum Dock hinzufügen"**.

Danach startet RapidSplit im eigenen Fenster und rechnet auch ohne Internet. Nur Kartenbild,
Ortssuche, Strecke, Wechselkurse und das Teilen brauchen eine Verbindung.

## Funktionen (Version 4)

- **Etappengenaue Fahrtkosten:** km je Fahrer:in und Etappe; Abrechnung wahlweise
  über **€/km-Pauschale** oder **Spritkosten nach km-Anteil** — nur wer mitfuhr, zahlt mit
- **Etappen-Karte:** Start und Ziel suchen oder auf der Karte wählen, Strecke und km
  kommen automatisch
- **Fahrt planen:** Sitz- und Bootsplätze je Auto, Vorschlag mit möglichst wenigen Autos,
  Shuttle mit einem Auto am Ausstieg
- **Ausgaben** (Maut, Parkplatz, Permit, Einkäufe, Campingplatz …) je Etappe oder für
  ausgewählte Personen, mit beliebigen Zahler:innen
- Ergebnis: Kosten pro Person, Saldo und „wer zahlt an wen" mit **möglichst wenigen
  Überweisungen**; bezahlen per Bank-QR-Code (GiroCode, bei Euro) oder PayPal, Zahlungen abhaken
- **Touren teilen** per QR-Code oder Tour-Code, Ende-zu-Ende-verschlüsselt
- Mehrere Touren, **Export als Datei** (Sicherung/Weitergabe) und **PDF-Export**
- Schritt-für-Schritt-Tutorial und Beispiel-Tour zum Ausprobieren
- Deutsch und Englisch, Hell-/Dunkelmodus, Bottom-Navigation auf dem Handy, barrierearm

## Ordner in diesem Repository

| Ordner | Inhalt |
|--------|--------|
| [`app/`](app/) | **Die aktuelle App (Version 4)** — live unter [/RapidSplit/app/](https://hantig-code.github.io/RapidSplit/app/) |
| [`v2/`](v2/) | Weiterleitung: die frühere App-Adresse leitet zur neuen `app/`-URL um (installierte Apps wechseln automatisch, Daten bleiben erhalten) |
| [`android/`](android/) | **Quellcode der nativen Android-App** (WebView-Wrapper um die Offline-PWA, Gradle-Projekt, Paket `app.rapidsplit`) |
| [`fastlane/`](fastlane/) | App-Store-Metadaten (Beschreibungen, Icon) für F-Droid & Co. |
| [`server/`](server/) | Quellcode des Sync-Servers fürs Teilen (`sync.php`, speichert nur Chiffretext) und Prüfskript `check-sync.sh` |
| [`v1/`](v1/) | 📦 Archiv: die ursprüngliche Version 1 („Kajak-Tour Kostenrechner"), weiterhin lauffähig unter [/RapidSplit/v1/](https://hantig-code.github.io/RapidSplit/v1/) |

Die frühere Root-Adresse leitet automatisch zur aktuellen App weiter.

## Technik

Statische Single-File-App (HTML/CSS/JS, kein Build-Schritt), `localStorage` zur
Speicherung, Service Worker für den Offline-Betrieb. Fremde Bibliotheken: der
QR-Code-Generator von Kazuhiko Arase (MIT) und Leaflet (BSD-2-Clause) sind eingebettet,
MapLibre GL JS (BSD-3-Clause) und seine Leaflet-Anbindung (ISC) fürs Kartenbild liegen in `app/vendor/`.
Die Rechenlogik ist durch automatische Tests abgesichert: Salden und Überweisungen
gehen centgenau auf, kein Cent geht verloren, alle Salden ergeben zusammen genau null –
geprüft mit zufällig erzeugten Touren; die Beispiel-Tour wird zusätzlich unabhängig
nachgerechnet.

Die Beispiel-Tour nutzt zur Veranschaulichung Namen bekannter
Wildwasser-Kajak-Fahrer:innen — rein illustrativ.

## Android-App selbst bauen

Die native Android-App ist ein schlanker WebView-Wrapper (keine externen
Abhängigkeiten, keine Tracker) um die Offline-PWA. Bauen mit Gradle:

```
cd android
./gradlew assembleRelease
```

Ergebnis: `android/app/build/outputs/apk/release/app-release-unsigned.apk`
(benötigt JDK 17 und das Android SDK, compileSdk 35; die veröffentlichten APKs
werden wie bei F-Droid mit JDK 21 gebaut). Wie F-Droid die App reproduzierbar
nachbaut, woher die Signatur kommt und wie ein Release getaggt wird:
[`android/FDROID.md`](android/FDROID.md).

## Lizenz

RapidSplit ist Freie Software unter der
[GNU General Public License v3.0](LICENSE) (GPL-3.0). Du darfst die App
nutzen, untersuchen, ändern und weitergeben — abgeleitete, verbreitete
Versionen müssen ebenfalls unter der GPL stehen. Name und Logo
„RapidSplit" sind davon ausgenommen.
