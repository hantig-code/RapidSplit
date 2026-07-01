# 🛶 RapidSplit - Der Kajak-Tour Kostenrechner

Eine kleine, eigenständige Web-App (PWA), um die Kosten einer Kajak-Tour
**fair pro Teilnehmer:in zu berechnen und auszugleichen** — Fahrtkosten je Etappe,
gemeinsame Ausgaben, Salden und „wer zahlt wem". Läuft komplett im Browser,
**offline** nutzbar, alle Daten bleiben lokal auf dem Gerät.

## Nutzen / Installieren

**▶️ Live (v1): https://hantig-code.github.io/RapidSplit/**
**🎨 Live (v2 – neues Design): https://hantig-code.github.io/RapidSplit/v2/**

> **v1** und **v2** laufen unabhängig parallel: gleiche Funktionen, aber v2 hat ein
> überarbeitetes, barriereärmeres Design (Light/Dark, Bottom-Navigation auf dem
> Handy, Settle-up mit Avataren & Anteilsbalken). Beide speichern ihre Daten
> getrennt und sind separat installierbar.

Als App installieren:

- **iPhone/iPad:** in **Safari** öffnen → Teilen (↑) → **„Zum Home-Bildschirm"**.
- **Android:** in Chrome öffnen → Menü ⋮ → **„App installieren"**.
- **Windows/macOS:** in Chrome/Edge das Installations-Symbol in der Adressleiste,
  oder in Safari **Ablage → „Zum Dock hinzufügen"**.

Danach startet sie im eigenen Fenster und funktioniert offline.

## Funktionen

- Etappen, Fahrer:innen (km × €/km) und zusätzliche Auslagen
- Teilnahme pro Etappe, gemeinsame Ausgaben mit beliebigen Zahlern
- Ergebnis: Kosten pro Person, Saldo, minimale Ausgleichszahlungen
- **Export als Datei** und **PDF-Export** (im Reiter „Ergebnis")
- Schritt-für-Schritt-Wizard, Beispiel-Tour zum Ausprobieren

## Technik

Statische Single-File-App (HTML/CSS/JS, kein Build, keine Abhängigkeiten),
`localStorage` zur Speicherung, Service Worker für Offline-Betrieb.
Details zum Hosting/Installieren in [ANLEITUNG.md](ANLEITUNG.md).

Die Beispiel-Tour nutzt zur Veranschaulichung Namen bekannter
Wildwasser-Kajak-Fahrer:innen — rein illustrativ.
