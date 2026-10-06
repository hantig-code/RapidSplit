# RapidSplit bei F-Droid

RapidSplit ist seit dem **29.08.2026** im offiziellen F-Droid-Repository:
<https://f-droid.org/packages/app.rapidsplit/> (Paket `app.rapidsplit`, erste
Version dort: 4.2.2). Das Build-Rezept liegt bei F-Droid in
[`metadata/app.rapidsplit.yml`](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/app.rapidsplit.yml)
im Repository `fdroid/fdroiddata`.

Dieses Dokument beschreibt, wie F-Droid die App aus diesem Repo baut, woher die
Signatur kommt und wie ein neues Release zu F-Droid gelangt.

## Reproduzierbarer Build mit Entwickler-Signatur

F-Droid baut RapidSplit selbst aus dem Quellcode nach, liefert aber die vom
Entwickler signierte APK aus – vorausgesetzt, beide stimmen überein
([Reproducible Builds](https://f-droid.org/docs/Reproducible_Builds/)):

1. Zu jeder Version hängt am GitHub-Release des Tags `v<versionName>` die
   signierte APK `RapidSplit-<versionName>.apk`. Darauf zeigt im Rezept
   `Binaries:`
   (`https://github.com/hantig-code/RapidSplit/releases/download/v%v/RapidSplit-%v.apk`).
2. Der F-Droid-Buildserver baut den im Rezept genannten Commit mit Gradle
   (`subdir: android/app`).
3. Er vergleicht seinen Nachbau mit der APK vom GitHub-Release. Bis auf die
   Signatur müssen beide byte-genau übereinstimmen.
4. Stimmt das und ist die APK mit dem in `AllowedAPKSigningKeys`
   hinterlegten Zertifikat signiert, veröffentlicht F-Droid **diese Datei
   mit der Entwickler-Signatur** – nicht mit einem eigenen F-Droid-Schlüssel.

Dadurch tragen die APK bei F-Droid und die APK am GitHub-Release dieselbe
Signatur. Wer RapidSplit aus einem GitHub-Release installiert hat, kann über
F-Droid weiter aktualisieren und umgekehrt.

### Signaturzertifikat

| | |
|---|---|
| Keystore | `rapidsplit-release.keystore` (seit 29.07.2026) |
| Zertifikat | `CN=RapidSplit, O=hantig, C=DE` |
| SHA-256-Fingerabdruck | `b284a1a8bcfbb0ed2eb5878f72a393dd3be985fc009a350b5a868e11ba9a0933` |

Genau dieser Fingerabdruck steht in `AllowedAPKSigningKeys`. Prüfen lässt er
sich mit dem `apksigner` aus den Android-SDK-Build-Tools:

```
apksigner verify --print-certs RapidSplit-<versionName>.apk
```

Die Zeile `Signer #1 certificate SHA-256 digest` muss den Wert oben zeigen.
Bis Ende Juli 2026 wurde mit einem anderen Schlüssel signiert (`release-v2.keystore`,
Fingerabdruck `8e2ed969…`). Er gilt für F-Droid nicht; ältere Release-APKs,
die noch dieses Zertifikat tragen, lassen sich nicht direkt auf die F-Droid-Version
aktualisieren.

## Build-Umgebung

Damit der Vergleich gelingt, muss der Release-Build unter denselben
Bedingungen entstehen wie auf dem F-Droid-Buildserver:

- **JDK 21** – die veröffentlichten APKs werden wie bei F-Droid mit JDK 21
  gebaut. Ein Build mit anderem JDK kann abweichen und fällt dann beim
  Vergleich durch.
- Gradle-Wrapper 8.2, Android Gradle Plugin 8.2.2
  ([`build.gradle`](build.gradle)), `compileSdk 35`, `targetSdk 35`,
  `minSdk 21` ([`app/build.gradle`](app/build.gradle)).
- Keine Abhängigkeiten: Die App ist ein reiner Android-Framework-Wrapper
  (WebView) um die Offline-PWA in `app/src/main/assets/`.

Einstellungen in [`app/build.gradle`](app/build.gradle), die den Build
reproduzierbar halten – nicht ändern:

- `crunchPngs false` – aapt2 komprimiert PNGs unter macOS und Linux
  unterschiedlich.
- `dependenciesInfo { includeInApk = false; includeInBundle = false }` –
  kein verschlüsselter Google-Play-Abhängigkeitsblock in der APK.
- `minifyEnabled false`, `shrinkResources false` – kein R8 im F-Droid-Build.

### Selbst nachbauen

```
git clone https://github.com/hantig-code/RapidSplit.git
cd RapidSplit
git checkout v<versionName>
cd android
JAVA_HOME=/pfad/zu/jdk-21 ./gradlew clean assembleRelease
```

Ergebnis: `android/app/build/outputs/apk/release/app-release-unsigned.apk`.
Das Android SDK wird über `ANDROID_HOME` oder `android/local.properties`
(`sdk.dir=…`) gefunden.

## Release-Ablauf

1. **Version setzen** in [`app/build.gradle`](app/build.gradle):
   `versionName` = `MAJOR.MINOR.PATCH` (immer dreistellig),
   `versionCode` = `MAJOR·10000 + MINOR·100 + PATCH` (4.2.2 → `40202`).
   Changelogs dazu in `fastlane/metadata/android/{en-US,de-DE}/changelogs/<versionCode>.txt`.
2. **Commit und Tag:** Tag `v<versionName>` (z. B. `v4.2.2`) auf den
   Release-Commit setzen, Commit und Tag pushen. Der Tag muss zum Muster
   `^v[0-9.]+$` passen.
3. **APK bauen:** aus einem sauberen Checkout genau dieses Tags mit JDK 21
   `./gradlew clean assembleRelease`, danach `zipalign` und `apksigner` mit
   `rapidsplit-release.keystore`.
4. **GitHub-Release** zum Tag anlegen und die APK exakt als
   `RapidSplit-<versionName>.apk` anhängen – sonst findet F-Droid sie über
   `Binaries:` nicht.
5. **F-Droid übernimmt den Rest:** Das Rezept prüft neue Tags
   (`UpdateCheckMode: Tags ^v[0-9.]+$`) und nimmt neue Versionen automatisch
   auf (`AutoUpdateMode: Version`). F-Droid baut die neue Version nach,
   vergleicht mit der Release-APK und veröffentlicht sie. Ein Merge Request bei `fdroiddata`
   ist für normale Updates nicht nötig.

Ein Merge Request ist nur nötig, wenn sich am Rezept selbst etwas ändert –
etwa bei einem neuen Signaturschlüssel (`AllowedAPKSigningKeys`). Ein
Schlüsselwechsel würde außerdem Updates bestehender Installationen
verhindern.

## Store-Texte und Bilder

Titel, Kurz- und Langbeschreibung, Changelogs, Icon, Funktionsgrafik und
Screenshots zieht F-Droid aus [`fastlane/metadata/android/`](../fastlane/metadata/android/)
dieses Repos (`de-DE` und `en-US`; `en-US` ist die Rückfallsprache für alle
anderen Sprachen). `full_description.txt` nutzt die von F-Droid erlaubte
HTML-Teilmenge. F-Droid zeigt alle Dateien eines Screenshot-Ordners an –
beim Erneuern alte Bilder entfernen.

## F-Droid und Google Play

| | F-Droid | Google Play |
|---|---|---|
| Paket | `app.rapidsplit` | `com.hantig.rapidsplit` |
| Signatur | Entwickler-Signatur (wie GitHub-Release) | Googles Signatur (Play App Signing) |

Die Versionsnummern sind in beiden Kanälen gleich. Wegen der
unterschiedlichen Paket-IDs sind es aber zwei getrennte Apps mit eigenem
Speicher, die sich nicht gegenseitig aktualisieren. Touren lassen sich per
Export und Import als Datei übertragen.
