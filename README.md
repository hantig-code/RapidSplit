# 🛶 RapidSplit — Tour-Kosten fair teilen

RapidSplit rechnet die Kosten einer Kajak-, Vereins- oder Gruppentour **fair pro
Teilnehmer:in** ab — etappengenau statt pauschal: Fahrtkosten je Etappe, gemeinsame
Ausgaben, Salden und „wer zahlt wem". Läuft komplett im Browser, **offline**
nutzbar, alle Daten bleiben lokal auf dem Gerät (kein Konto, kein Tracking).

## ▶️ App öffnen (aktuelle Version 4)

**https://hantig-code.github.io/RapidSplit/app/**

> Die Adresse `…/app/` ist die dauerhafte, versionslose App-URL — Version 4 (und alle künftigen
> Updates) werden dort ausgeliefert; installierte Apps aktualisieren sich automatisch.

**Als App installieren:**

- **Android:** in Chrome öffnen → Menü ⋮ → **„App installieren"** — oder die
  native **Android-App (APK)** direkt von der RapidSplit-Website laden.
- **iPhone/iPad:** in **Safari** öffnen → Teilen (↑) → **„Zum Home-Bildschirm"**.
- **Windows/macOS/Linux:** in Chrome/Edge das Installations-Symbol in der
  Adressleiste, oder in Safari **Ablage → „Zum Dock hinzufügen"**.

Danach startet RapidSplit im eigenen Fenster und funktioniert vollständig offline.

## Funktionen (Version 3)

- **Etappengenaue Fahrtkosten:** km je Fahrer:in und Etappe; Abrechnung wahlweise
  über **€/km-Pauschale** oder **Spritkosten nach km-Anteil** — nur wer mitfuhr, zahlt mit
- **Fahrer-Auslagen** (Maut, Parkplatz, Permit …) etappengenau zuordenbar
- Gemeinsame **Ausgaben** (Einkäufe, Campingplatz …) mit beliebigen Zahler:innen
- **Verein/Familie**: Salden auf Wunsch zu einem Gruppensaldo zusammenfassen
- Ergebnis: Kosten pro Person, Saldo, **minimale Anzahl Ausgleichszahlungen**
- **Export als Datei** (Sicherung/Weitergabe) und **PDF-Export**
- Schritt-für-Schritt-Tutorial und Beispiel-Tour zum Ausprobieren
- Hell-/Dunkelmodus, Bottom-Navigation auf dem Handy, barrierearm

## Ordner in diesem Repository

| Ordner | Inhalt |
|--------|--------|
| [`app/`](app/) | **Die aktuelle App (Version 4)** — live unter [/RapidSplit/app/](https://hantig-code.github.io/RapidSplit/app/) |
| [`v2/`](v2/) | Weiterleitung: die frühere App-Adresse leitet zur neuen `app/`-URL um (installierte Apps wechseln automatisch, Daten bleiben erhalten) |
| [`android/`](android/) | **Quellcode der nativen Android-App** (WebView-Wrapper um die Offline-PWA, Gradle-Projekt, Paket `app.rapidsplit`) |
| [`fastlane/`](fastlane/) | App-Store-Metadaten (Beschreibungen, Icon) für F-Droid & Co. |
| [`v1/`](v1/) | 📦 Archiv: die ursprüngliche Version 1 („Kajak-Tour Kostenrechner"), weiterhin lauffähig unter [/RapidSplit/v1/](https://hantig-code.github.io/RapidSplit/v1/) |

Die frühere Root-Adresse leitet automatisch zur aktuellen App weiter.

## Technik

Statische Single-File-App (HTML/CSS/JS, kein Build, keine Abhängigkeiten),
`localStorage` zur Speicherung, Service Worker für den Offline-Betrieb.
Die Rechenlogik ist gegen einen unabhängigen Vergleichsrechner mit über
10.000 Zufallsfällen verifiziert (Abweichung < 0,5 Cent).

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
(benötigt JDK 17 und das Android SDK, compileSdk 34). Details zur
F-Droid-Aufnahme: [`android/FDROID.md`](android/FDROID.md).

## Lizenz

RapidSplit ist Freie Software unter der
[GNU General Public License v3.0](LICENSE) (GPL-3.0). Du darfst die App
nutzen, untersuchen, ändern und weitergeben — abgeleitete, verbreitete
Versionen müssen ebenfalls unter der GPL stehen. Name und Logo
„RapidSplit" sind davon ausgenommen.
