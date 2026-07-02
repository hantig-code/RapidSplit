# 🛶 RapidSplit — Tour-Kosten fair teilen

RapidSplit rechnet die Kosten einer Kajak-, Vereins- oder Gruppentour **fair pro
Teilnehmer:in** ab — etappengenau statt pauschal: Fahrtkosten je Etappe, gemeinsame
Ausgaben, Salden und „wer zahlt wem". Läuft komplett im Browser, **offline**
nutzbar, alle Daten bleiben lokal auf dem Gerät (kein Konto, kein Tracking).

## ▶️ App öffnen (aktuelle Version 3)

**https://hantig-code.github.io/RapidSplit/v2/**

> Die Adresse `…/v2/` ist die dauerhafte App-URL — Version 3 (und alle künftigen
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
| [`v2/`](v2/) | **Die aktuelle App (Version 3)** — live unter [/RapidSplit/v2/](https://hantig-code.github.io/RapidSplit/v2/) |
| [`v1/`](v1/) | 📦 Archiv: die ursprüngliche Version 1 („Kajak-Tour Kostenrechner"), weiterhin lauffähig unter [/RapidSplit/v1/](https://hantig-code.github.io/RapidSplit/v1/) |

Die frühere Root-Adresse leitet automatisch zur aktuellen App weiter.

## Technik

Statische Single-File-App (HTML/CSS/JS, kein Build, keine Abhängigkeiten),
`localStorage` zur Speicherung, Service Worker für den Offline-Betrieb.
Die Rechenlogik ist gegen einen unabhängigen Vergleichsrechner mit über
10.000 Zufallsfällen verifiziert (Abweichung < 0,5 Cent).

Die Beispiel-Tour nutzt zur Veranschaulichung Namen bekannter
Wildwasser-Kajak-Fahrer:innen — rein illustrativ.
