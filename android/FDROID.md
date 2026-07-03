# RapidSplit bei F-Droid einreichen

Dieses Dokument beschreibt die Aufnahme von RapidSplit (`app.rapidsplit`) in das
offizielle F-Droid-Repository. Der Quellcode in diesem Repo erfüllt die
[Aufnahmekriterien](https://f-droid.org/docs/Inclusion_Policy/): GPL-3.0,
keine proprietären Abhängigkeiten, keine Tracker, baubar mit Gradle.

## Fertige Metadatei für `fdroiddata`

Diese Datei kommt als `metadata/app.rapidsplit.yml` in einen Fork von
[fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata):

```yaml
Categories:
  - Money
License: GPL-3.0-only
AuthorName: Stefan Nester (hantig)
AuthorEmail: contact@rapidsplit.app
WebSite: https://rapidsplit.app
SourceCode: https://github.com/hantig-code/RapidSplit
IssueTracker: https://github.com/hantig-code/RapidSplit/issues

AutoName: RapidSplit

RepoType: git
Repo: https://github.com/hantig-code/RapidSplit.git
Binaries:
  https://github.com/hantig-code/RapidSplit/releases/download/v%v/RapidSplit-%v.apk

Builds:
  - versionName: 3.0.2
    versionCode: 5
    commit: v3.0.2
    subdir: android/app
    gradle:
      - yes

AllowedAPKSigningKeys: 8e2ed969456f5bea3aa6fa729b999861d14c8e6aa5b05d5455a4620c6550fef6

AutoUpdateMode: Version
UpdateCheckMode: Tags ^v[0-9.]+$
CurrentVersion: 3.0.2
CurrentVersionCode: 5
```

## Reproducible Builds (aktiv seit v3.0.2)

RapidSplit nutzt F-Droids Reproducible-Builds-Verfahren: Zu jedem Release wird
die selbst signierte APK als GitHub-Release-Asset veröffentlicht
(`RapidSplit-<versionName>.apk` am Tag `v<versionName>`). F-Droid baut aus dem
Quellcode nach, vergleicht byte-genau mit dem Asset (`Binaries:` +
`AllowedAPKSigningKeys:` oben) und veröffentlicht bei Übereinstimmung die APK
mit der **Entwickler-Signatur** (Cert-SHA256 `8e2ed969…`, Keystore
`release-v2.keystore`, Alias `kajakv2`).

Dafür nötig (bereits umgesetzt): `dependenciesInfo { includeInApk = false }`
in `app/build.gradle` — der verschlüsselte Play-Store-Abhängigkeitsblock würde
den byte-genauen Nachbau sonst stören.

**Release-Ablauf pro Version:** versionCode/versionName erhöhen → committen →
Tag `vX.Y(.Z)` pushen → `./gradlew clean assembleRelease` → APK mit apksigner
signieren → als `RapidSplit-X.Y(.Z).apk` an das GitHub-Release des Tags hängen.

Beschreibung, Kurzbeschreibung und Icon zieht F-Droid automatisch aus
[`fastlane/metadata/android/`](../fastlane/metadata/android/) dieses Repos.

## Einreichung (einmalig, ~15 Minuten)

1. **GitLab-Konto** auf [gitlab.com](https://gitlab.com) anlegen (falls noch keins).
2. [fdroid/fdroiddata](https://gitlab.com/fdroid/fdroiddata) **forken**
   (Button „Fork" oben rechts).
3. Im Fork über die Web-Oberfläche **neue Datei anlegen**:
   `metadata/app.rapidsplit.yml` mit dem YAML-Inhalt von oben.
   Commit-Message: `New app: RapidSplit`.
4. **Merge Request** gegen `fdroid/fdroiddata` (Branch `master`) eröffnen,
   Titel: `New app: RapidSplit`. Die Beschreibung kann kurz sein; die CI
   baut die App automatisch aus diesem Repo (Tag `v3.0`).
5. Auf **Reviewer-Feedback** antworten (kommt als Kommentar im MR;
   Bearbeitungszeit erfahrungsgemäß Tage bis einige Wochen).

## Nach der Aufnahme

- **Updates sind automatisiert:** Neue Version = `versionCode`/`versionName`
  in `app/build.gradle` erhöhen, committen, Git-Tag `vX.Y` pushen.
  F-Droid erkennt den Tag (`AutoUpdateMode: Version`), baut und
  veröffentlicht selbstständig.
- F-Droid **signiert mit eigenem Schlüssel**. Die F-Droid-Version ist damit
  eine von der Website-APK unabhängige Installation (bewusst auch eine
  eigene Paket-ID `app.rapidsplit`). Daten lassen sich per Export/Import
  zwischen beiden übertragen.
- Website/README dürfen erst nach der Aufnahme das offizielle
  F-Droid-Badge verlinken: `https://f-droid.org/packages/app.rapidsplit/`
