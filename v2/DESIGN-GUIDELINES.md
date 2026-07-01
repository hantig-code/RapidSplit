# RapidSplit — Design- & UX-Richtlinien

Designsystem **„Ledger Stromlinie"** — Fintech-Klarheit in Refactoring-UI-Disziplin.
Eine einzige, in sich stimmige Sprache für Web-App (PWA), Android und macOS. Light **und** Dark, AA-geprüft.

---

## 1. Marke & Identität

- **Name:** RapidSplit *(Beta)* — ein Tool, das die Kosten einer (Kajak-)Tour fair pro Teilnehmer:in aufteilt.
- **Logo:** ein **€-Zeichen, dessen zwei Querstriche Wildwasser-Paddel sind**, gefüllt mit einem Verlauf **Teal → Grün → Gold** (unten-links → oben-rechts).
  - **Bright** (Hauptvariante): €-Verlauf auf Weiß. Verwendung: Favicon, PWA-Icon, heller Header, App-Icon.
  - **Dark**: €-Verlauf auf dunklem Teal→Schwarz. Verwendung: **ausschließlich** der Header im Dark-Mode.
  - Quell-Dateien sind maßgeblich (`RapidSplit Logo_bright.png` / `_dark.png`) — **nicht neu zeichnen oder umfärben**. Das Verhältnis Motiv:Hintergrund muss überall identisch zur Quelle bleiben (Web-App, Favicon, macOS-Dock-Icon = Datei 1:1; nur der Android-Adaptive-Vordergrund wird in die Safe-Zone skaliert, damit der Launcher-Zoom die Paddel nicht abschneidet).
- **Tonalität:** ruhig, kompetent, freundlich. Kein Marketing-Lärm.

---

## 2. Farbsystem (Design-Tokens)

Farben werden **nie** hart codiert, sondern über CSS-Custom-Properties (`--token`) bezogen, damit Light/Dark automatisch funktionieren.

### Light
| Rolle | Token | Wert |
|---|---|---|
| Hintergrund | `--bg` | `#eef3f5` |
| Fläche dezent | `--bg-subtle` | `#f7fafb` |
| Karte | `--card` | `#ffffff` |
| Text | `--ink` | `#0f2730` |
| Text 2 | `--ink-2` | `#33525e` |
| Text gedämpft | `--muted` | `#52707b` |
| Linie / Linie stark | `--line` / `--line-strong` | `#dbe6ea` / `#c3d2d7` |
| **Marke** | `--brand` | `#0f766e` (Teal) |
| Marke dunkel (Hover) | `--brand-d` | `#0a5b54` |
| Marke-Tint (Flächen) | `--brand-tint` | `#e1f0ee` |
| **Akzent** (Gold) | `--accent` | `#b89422` |
| Text auf Marke | `--on-brand` | `#ffffff` |

### Dark
`--bg #0a1418` · `--card #15303a` · `--ink #eaf2f4` · **`--brand #33cdb4`** · `--brand-d #6fe3ce` · **`--accent #d8bb52`** · `--on-brand #062420`.

### Semantik (beide Themes, AA-korrigiert)
- **Grün** „bekommt zurück / positiv": `--green` (`#166534` / dark `#4ade80`)
- **Rot** „zahlt ein / löschen": `--red` (`#b91c1c` / dark `#f87171`)
- **Warn**: `--warn` · **Demo/„Beispiel laden"** (türkis): `--demo`
- Etappen bekommen kategorische Swatch-Farben aus einer festen 10er-Palette (`SEG_COLORS`); Avatare aus `AV_PALETTE` (deterministisch je Name).

### Regeln
- **Kontrast:** Text auf `--brand` ≥ 4.5:1 (AA). `--brand` wurde entsprechend dunkel gewählt (Light 5.3:1 auf Weiß). Bei jeder Marken-Änderung neu prüfen.
- **Textauswahl & Touch-Highlight** in Markenfarbe (`::selection`, `-webkit-tap-highlight-color` = `color-mix(--brand 22–26%, transparent)`) — kein Browser-Blau.
- Verläufe (Nav-Linie, Wizard-Fortschritt) laufen **`--brand → --accent → --brand`** und spiegeln so das Logo (Teal→Gold).

---

## 3. Typografie

- **Schrift:** System-Stack (`-apple-system, Segoe UI, Roboto, …`) — schnell, nativ, vertraut.
- Basis **15px**, Zeilenhöhe **1.45**. `-webkit-text-size-adjust:100%`.
- Hierarchie: Tour-Titel `h1` 20–22px/800 · Karten-Titel `h2` 15.5px/700 (Farbe `--brand-d`) · Hinweise `.hint` 12.5px/`--muted` · Fußnoten `.footnote`/`.mini` 12px.
- Zahlen in Tabellen/Feldern **`font-variant-numeric:tabular-nums`**, rechtsbündig.
- **Unterlängen nie abschneiden** (z. B. „g" in Nav-Labels): `line-height ≥ 1.28`, nicht `1`.

---

## 4. Layout & Spacing

- **Inhaltsbreite** max **920px**, zentriert. Auf Mobil volle Breite mit 12px Seitenrand.
- **Karten** (`.card`): `--card`-Fläche, 1px `--line`, Radius `--r-md` (12px), Padding **16px**, **Abstand zwischen Blöcken = 14px** (`margin-bottom`). Dieser 14px-Rhythmus ist der Standardabstand — auch der letzte Block hält ihn zur Navigationsleiste.
- **Radien:** `--r-sm` 8px (Felder/Buttons), `--r-md` 12px (Karten), `--r-pill` 999px (Pillen, aktiver Tab, Schalter).
- **Scroll-Modell:** die Leisten (Titel oben, Navigation) sind **fixe Overlays**; nur `<main>` scrollt. Dadurch bleibt beim Über-Scrollen (Bounce/Stretch) nur der Inhalt in Bewegung, die Leisten stehen still. `main` bekommt per JS `padding-top = Leistenhöhe + 16px` (stimmiger Abstand zum ersten Inhalt).
- **Immersiv:** Inhalt scrollt durchscheinend hinter die **frosted** Leisten (Titel und Navigation gleiche Transparenz, `color-mix(--card 70%, transparent)` + `backdrop-filter: blur`). Der frosted-Effekt liegt auf `header.app`/`nav.tabs`, **nie auf `#topbar`** (sonst würde `#topbar` zum Containing-Block der fixed Mobile-Tab-Leiste und diese nach oben rutschen).
- **Vollbreite Leisten:** die Leiste zieht sich farblich über die **gesamte Fensterbreite**; Inhalt (Logo, Tabs) sitzt am 920er-Raster zentriert (Padding `max(x, calc((100% − 920px)/2))`).

---

## 5. Navigation

- 6 Bereiche in fester Reihenfolge: **Tour · Gruppe · Wer & Wo · Fahrtkosten · Ausgaben · Auswertung**.
- **Desktop:** horizontale Tab-Leiste oben, durchgehende Verlaufslinie am unteren Rand; aktiver Tab in **Pillen-Form** mit **~6px Abstand** zur Linie (Buttons berühren die Linie nie).
- **Mobil:** dieselbe Leiste wird zur **fixen Bottom-Tab-Bar** (Icons + Kurz-Label), edge-to-edge über den Android-Gestenbereich, aktiver Tab als Pille.
- **Kein horizontales Wischen** zum Bereichswechsel (bewusst entfernt — kollidiert mit vertikalem Scrollen/Tabellen).

---

## 6. Komponenten

- **Buttons** (`.btn`): Marke-Fläche + `--on-brand`, Radius 8px, min-Höhe 44px, Icon+Text mit 7px Gap. Varianten: `.ghost` (Tint + Rahmen), `.demo` (türkis „Beispiel laden"), `.danger` (rot, für Löschen), `.sm` (kompakt). Aktiv: `scale(.98)`.
- **Eingabefelder:** Radius 8px, 44px min-Höhe, `font-size:16px` (verhindert iOS-Zoom). Fokus: 3px Ring in `--focus`. Geldfelder mit `€`-Adornment (`.cur`). Zahlenfelder markieren beim Fokus ihren Wert (direkt lostippen).
- **Segmentierter Regler** (`.seg-toggle`): eine Pille mit 2+ Optionen, aktive Option Marke-gefüllt. Für globale Entweder-Oder-Auswahl (z. B. €/km-Pauschale ⇄ Spritkosten).
- **Kipp-Schalter** (`.opt-toggle`, `.theme-switch`): Pillen-Track mit Knopf, für Ein/Aus.
- **Banner** (`.banner`): Info-Kachel je Bereich (Icon + Kurzanleitung).
- **Modale/Popups** (`.modal`): zentrierte Karte, `backdrop`, `modalpop`-Animation. Bestätigungen (`confirmDialog`) **und** die **Personen-Auswahl** (`pickPerson` — Avatare + Häkchen) nutzen dieselbe Optik. Fokus-Falle + `Esc` + Backdrop-Klick schließen; Hintergrund `inert`.
- **Tabellen:** horizontale Scroll-Container (`.tbl-wrap`), sticky erste Spalte, Summenzeilen hervorgehoben.
- **Wizard** (`.wiz-bar`): fixe Bodenleiste mit Fortschritt, führt Schritt für Schritt durch die Bereiche. **Hebt sich als einziges Element über die eingeblendete Tastatur** (via `--kb-*`).
- **Toast:** kurze, nicht-blockierende Rückmeldung unten.
- **Drag & Drop:** Etappen per Griff (`.drag-handle`, Pointer-basiert, touch-tauglich) sortierbar.

---

## 7. Interaktions- & UX-Prinzipien

1. **Leer starten.** Die App startet ohne Beispieldaten (bzw. mit den zuletzt eingetragenen). Ein „Start"-Block bietet **Tutorial** und **Beispiel laden**; er lässt sich dauerhaft ausblenden und wieder einblenden.
2. **Alles speichert automatisch** (localStorage), keine „Speichern"-Buttons. Export (PDF/Datei) & Import im Reiter „Auswertung"/„Tour → Daten".
3. **Neue Einträge unten anhängen** (natürliche Lesereihenfolge); Reihenfolge per Drag&Drop änderbar.
4. **Sichtbare Ergebnisse statt Formulare:** Salden, „wer zahlt wem", Kosten pro Person direkt sichtbar; optionales Zusammenfassen von **Verein/Familie** (Vorgabe: **aus**).
5. **Fehlertoleranz:** Fahrer:in-Häkchen entfernen löscht keine Eingaben (Backlog/Wiederherstellung).
6. **Leere Zustände** erklären den nächsten Schritt („Hier ist nichts zu sehen? Trage Etappen im Bereich „Tour" ein!").
7. **Native Integration:** Systemleisten-Farbe, edge-to-edge, externe Links öffnen extern, PDF-Export **immer im hellen Day-Mode** (unabhängig vom Theme).
8. **Bestätigung für Destruktives** („Alles leeren", Löschen) — immer über ein Modal, `--red`.

---

## 8. Barrierefreiheit

- **AA-Kontrast** durchgängig; semantische Farben zusätzlich mit Icon/Text codiert (nicht nur Farbe).
- Sichtbarer **Fokusring** (`:focus-visible`), Fokus-Fallen in Modalen, `aria-*`/`role` an Tabs, Schaltern, Dialogen.
- **Touch-Ziele ≥ 44px**; Labels immer sichtbar (nicht nur Placeholder).
- Icons dekorativ = `aria-hidden`; sinntragende Icons mit `.sr-only`-Text.
- `prefers-color-scheme` respektiert (Theme-Vorgabe = OS), manuell umschaltbar.

---

## 9. Ton & Sprache

- **Deutsch**, geschlechtsinklusiv (`:innen`, „Fahrer:in", „Teilnehmer:in").
- Knapp, konkret, ohne Fachjargon. Hinweise erklären das *Warum* in einem Satz.
- Anführungszeichen deutsch („…"). Beträge im `de-DE`-Format (`1.234,56 €`).

---

## 10. Plattform-Notizen

- **PWA:** `manifest.webmanifest` (`name`/`short_name` = **RapidSplit**), `display:standalone`, Icons `purpose:"any"` (kein maskable-Crop → Verhältnis bleibt wie die Quelldatei). Bei Icon-/Namensänderung `?v=`-Cache-Bust setzen und SW-`CACHE_VERSION` erhöhen.
- **Android (WebView):** edge-to-edge, Systemleisten transparent; Leistenhöhen **und** Tastaturhöhe kommen per `WindowInsets` als CSS-Variablen (`--sys-top/-bottom`, `--kb-native`). `windowSoftInputMode=adjustNothing`, damit die Bottom-Nav **nicht** über der Tastatur schwebt — nur der Wizard hebt sich.
- **Adaptive Icon:** Vordergrund = Logo in der Safe-Zone (0.72), Hintergrund passend hell; Monochrome-Layer für den „Minimal"-Themed-Icon.

---

*Leitsatz: Klarheit vor Cleverness. Jede Fläche, Farbe und Bewegung hat einen Grund — und denselben Grund auf allen Plattformen.*
