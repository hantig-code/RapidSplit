package app.rapidsplit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Base64;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.OutputStream;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 1;
    private static final int SAVE_FILE_REQUEST = 2;
    private byte[] pendingBytes;
    private android.webkit.PermissionRequest pendingPerm;   // Kamera-Anfrage bis zur OS-Freigabe       // Inhalt, der nach SAF-Auswahl geschrieben wird
    private int navTopDp = 0;          // Höhe der System-Statusleiste (dp)
    private int navBottomDp = 0;       // Höhe der System-Navigations-/Gestenleiste (dp), an die Web-App weitergereicht
    private int imeBottomDp = 0;       // Höhe der eingeblendeten Tastatur (dp)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ausrichtungPruefen(getResources().getConfiguration());

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);     // localStorage bleibt zwischen Starts erhalten
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setSupportZoom(false);
        s.setMediaPlaybackRequiresUserGesture(false);   // Kamera-Vorschau ohne Play-Overlay
        s.setSupportMultipleWindows(true);   // target="_blank" -> onCreateWindow

        // Externe Links (http/https/mailto/tel) im System (Browser/Mail) öffnen, nicht in der WebView
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return handleUrl(req.getUrl().toString());
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                pushInsets();   // Leisten-Höhen an die Web-App geben (Header/Tab-Leiste nicht abschneiden)
                pushSystemDark();
            }
        });

        // Aktiviert JS-Dialoge (alert/confirm/prompt) UND die Datei-Auswahl für Import.
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                // target="_blank": Ziel-URL über eine Wegwerf-WebView abgreifen und extern öffnen
                WebView tmp = new WebView(view.getContext());
                tmp.setWebViewClient(new WebViewClient() {
                    @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) { openExternal(r.getUrl().toString()); destroyLater(v); return true; }
                    @Override public boolean shouldOverrideUrlLoading(WebView v, String u) { openExternal(u); destroyLater(v); return true; }
                });
                ((WebView.WebViewTransport) resultMsg.obj).setWebView(tmp);
                resultMsg.sendToTarget();
                return true;
            }
            @Override
            public void onPermissionRequest(final android.webkit.PermissionRequest request) {
                runOnUiThread(new Runnable() { @Override public void run() {
                    boolean cam = false;
                    for (String r : request.getResources()) if (android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) cam = true;
                    if (!cam) { request.deny(); return; }
                    if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.CAMERA)
                            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        pendingPerm = request;   // offen halten – wird nach dem OS-Dialog beantwortet
                        requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 77);
                    } else {
                        request.grant(request.getResources());
                    }
                }});
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;
                try {
                    Intent intent = params.createIntent();
                    // Manche Geräte liefern Export-Dateien mit abweichendem MIME-Typ; breiter zulassen.
                    // Der Import prüft die Datei-Signatur ohnehin, bevor etwas übernommen wird.
                    intent.setType("*/*");
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        // Brücke für Export (Datei) und PDF-Export aus dem Web-Code.
        web.addJavascriptInterface(new Bridge(), "KajakNative");

        setContentView(web, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Auf Android < 11 (API < 30) werden IME-Insets nicht gemeldet; adjustResize sorgt dafür,
        // dass die Tastatur fokussierte Eingabefelder nicht verdeckt.
        if (Build.VERSION.SDK_INT < 30) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }

        // Höhe der System-/Gestenleisten (oben & unten) an die Web-App geben → Header/Tab-Leiste klemmen nicht
        web.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int top, bottom, ime = 0;
                if (Build.VERSION.SDK_INT >= 30) {
                    Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                    top = bars.top; bottom = bars.bottom;
                    ime = insets.getInsets(WindowInsets.Type.ime()).bottom;   // Höhe der On-Screen-Tastatur
                } else {
                    top = insets.getSystemWindowInsetTop();
                    bottom = insets.getSystemWindowInsetBottom();
                }
                float dens = getResources().getDisplayMetrics().density;
                navTopDp = Math.round(top / dens);
                navBottomDp = Math.round(bottom / dens);
                imeBottomDp = Math.round(ime / dens);   // in dp an die Web-App (Wizard bleibt über der Tastatur)
                pushInsets();
                return v.onApplyWindowInsets(insets);
            }
        });

        // System-Leisten zunächst an das (vermutete) Theme anpassen; die Web-App verfeinert das
        int night = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        applyBars(night == Configuration.UI_MODE_NIGHT_YES ? "#0e1513" : "#f5faf8");

        if (savedInstanceState == null) {
            web.loadUrl("file:///android_asset/index.html");
        }
    }

    /** true = extern behandelt (nicht in der WebView laden). Interne file://-Navigation bleibt in der WebView. */
    private boolean handleUrl(String url) {
        if (url == null) return false;
        if (url.startsWith("http://") || url.startsWith("https://")
                || url.startsWith("mailto:") || url.startsWith("tel:")) {
            openExternal(url);
            return true;
        }
        return false;
    }

    private void openExternal(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Keine App gefunden für: " + url, Toast.LENGTH_LONG).show();
        }
    }

    /** Wegwerf-WebView (aus onCreateWindow) nach dem externen Öffnen sicher freigeben (kein Leak). */
    private void destroyLater(final WebView v) {
        if (v == null) return;
        v.post(new Runnable() { @Override public void run() { v.destroy(); } });
    }

    /** System-Hell/Dunkel an die Web-App melden (WebView liefert prefers-color-scheme nicht zuverlässig). */
    private void pushSystemDark() {
        if (web == null) return;
        int night = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        final boolean dark = night == Configuration.UI_MODE_NIGHT_YES;
        runOnUiThread(new Runnable() { @Override public void run() {
            try { web.evaluateJavascript("window.setSystemDark&&setSystemDark(" + dark + ")", null); } catch (Exception e) { }
        }});
    }

    /** Status-/Gestenleisten-Höhen als CSS-Variablen --sys-top / --sys-bottom an die Web-App geben. */
    private void pushInsets() {
        if (web == null) return;
        runOnUiThread(new Runnable() {
            @Override public void run() {
                try {
                    int kb = imeBottomDp;   // reine Tastaturhöhe (ohne die ohnehin berücksichtigte Gestenleiste)
                    web.evaluateJavascript(
                        /* In einer Funktion: kein globales „r“, und ganz früh beim Laden (Dokument ohne <html>) kein
                           TypeError – onPageFinished meldet die Werte danach ohnehin erneut. */
                        "(function(){var r=document.documentElement;if(!r)return;"
                        + "r.style.setProperty('--sys-top','" + navTopDp + "px');"
                        + "r.style.setProperty('--sys-bottom','" + navBottomDp + "px');"
                        + "r.style.setProperty('--kb-native','" + kb + "px');"
                        + "window.updPadExtra&&updPadExtra();})()", null);
                } catch (Exception e) { /* ignore */ }
            }
        });
    }

    /** Maximal immersiv: Status- UND Navigations-/Gestenleiste transparent, Inhalt zeichnet
     *  edge-to-edge dahinter (oben & unten); die App-Leisten (frosted Header / Tab-Leiste) scheinen durch.
     *  `hex` bestimmt nur die Icon-Helligkeit (hell/dunkel). */
    private void applyBars(final String hex) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                try {
                    int color = Color.parseColor(hex);
                    getWindow().setStatusBarColor(Color.TRANSPARENT);
                    getWindow().setNavigationBarColor(Color.TRANSPARENT);
                    if (Build.VERSION.SDK_INT >= 29) {
                        getWindow().setNavigationBarContrastEnforced(false);  // kein grauer Scrim über transparenter Leiste
                        getWindow().setStatusBarContrastEnforced(false);
                    }
                    double lum = 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color);
                    boolean lightBar = lum > 140;   // helle App-Fläche -> dunkle System-Icons
                    View dv = getWindow().getDecorView();
                    // Inhalt hinter beide Leisten zeichnen (edge-to-edge oben & unten)
                    int vis = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                    if (Build.VERSION.SDK_INT >= 23 && lightBar) vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    if (Build.VERSION.SDK_INT >= 26 && lightBar) vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    dv.setSystemUiVisibility(vis);
                } catch (Exception e) { /* ignore */ }
            }
        });
    }

    private class Bridge {
        @JavascriptInterface
        public void setBarColor(String hex) { applyBars(hex); }

        /** Anzeigedauer einer Snackbar (ms) nach der System-Einstellung „Zeit zum Reagieren“ (Bedienungshilfen, ab
         *  Android 10, wie die MDC-Snackbar). MIT Aktion („Rückgängig“) und TalkBack: -1 = stehen lassen – großzügiger
         *  als MDC ab Android 10, wie Compose M3/Flutter bei Snackbars mit Aktion. Die Web-App nimmt nie weniger als
         *  ihren eigenen Wert (8 s ohne, 10 s mit Aktion). Nur lesend. */
        @JavascriptInterface
        public int snackTimeout(int ms, boolean mitAktion) {
            try {
                AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
                if (am == null) return ms;
                if (mitAktion && am.isEnabled() && am.isTouchExplorationEnabled()) return -1;
                if (Build.VERSION.SDK_INT >= 29) {
                    int flags = AccessibilityManager.FLAG_CONTENT_TEXT | AccessibilityManager.FLAG_CONTENT_ICONS
                            | (mitAktion ? AccessibilityManager.FLAG_CONTENT_CONTROLS : 0);
                    return am.getRecommendedTimeoutMillis(ms, flags);
                }
            } catch (Exception e) { /* ignore */ }
            return ms;
        }

        /** Material-You-Tonpaletten (Android 12+), die das System aus dem Hintergrundbild ableitet:
         *  {"a1":{"100":"#rrggbb",…,"0":…},"a2":…,"a3":…,"n1":…,"n2":…} (Schlüssel = M3-Ton). Sonst "".
         *  Nur lesend und nur lokal – die Web-App setzt daraus ihre Farbrollen. */
        @JavascriptInterface
        public String getSystemPalette() {
            if (Build.VERSION.SDK_INT < 31) return "";
            try {
                final int[][] ids = {
                    { android.R.color.system_accent1_0, android.R.color.system_accent1_10, android.R.color.system_accent1_50, android.R.color.system_accent1_100, android.R.color.system_accent1_200, android.R.color.system_accent1_300, android.R.color.system_accent1_400, android.R.color.system_accent1_500, android.R.color.system_accent1_600, android.R.color.system_accent1_700, android.R.color.system_accent1_800, android.R.color.system_accent1_900, android.R.color.system_accent1_1000 },
                    { android.R.color.system_accent2_0, android.R.color.system_accent2_10, android.R.color.system_accent2_50, android.R.color.system_accent2_100, android.R.color.system_accent2_200, android.R.color.system_accent2_300, android.R.color.system_accent2_400, android.R.color.system_accent2_500, android.R.color.system_accent2_600, android.R.color.system_accent2_700, android.R.color.system_accent2_800, android.R.color.system_accent2_900, android.R.color.system_accent2_1000 },
                    { android.R.color.system_accent3_0, android.R.color.system_accent3_10, android.R.color.system_accent3_50, android.R.color.system_accent3_100, android.R.color.system_accent3_200, android.R.color.system_accent3_300, android.R.color.system_accent3_400, android.R.color.system_accent3_500, android.R.color.system_accent3_600, android.R.color.system_accent3_700, android.R.color.system_accent3_800, android.R.color.system_accent3_900, android.R.color.system_accent3_1000 },
                    { android.R.color.system_neutral1_0, android.R.color.system_neutral1_10, android.R.color.system_neutral1_50, android.R.color.system_neutral1_100, android.R.color.system_neutral1_200, android.R.color.system_neutral1_300, android.R.color.system_neutral1_400, android.R.color.system_neutral1_500, android.R.color.system_neutral1_600, android.R.color.system_neutral1_700, android.R.color.system_neutral1_800, android.R.color.system_neutral1_900, android.R.color.system_neutral1_1000 },
                    { android.R.color.system_neutral2_0, android.R.color.system_neutral2_10, android.R.color.system_neutral2_50, android.R.color.system_neutral2_100, android.R.color.system_neutral2_200, android.R.color.system_neutral2_300, android.R.color.system_neutral2_400, android.R.color.system_neutral2_500, android.R.color.system_neutral2_600, android.R.color.system_neutral2_700, android.R.color.system_neutral2_800, android.R.color.system_neutral2_900, android.R.color.system_neutral2_1000 } };
                final String[] keys = { "a1", "a2", "a3", "n1", "n2" };
                final int[] tone = { 100, 99, 95, 90, 80, 70, 60, 50, 40, 30, 20, 10, 0 };
                StringBuilder sb = new StringBuilder("{");
                for (int p = 0; p < ids.length; p++) {
                    if (p > 0) sb.append(',');
                    sb.append('"').append(keys[p]).append("\":{");
                    for (int i = 0; i < tone.length; i++) {
                        if (i > 0) sb.append(',');
                        int c = getResources().getColor(ids[p][i], null);
                        sb.append('"').append(tone[i]).append("\":\"#")
                          .append(Integer.toHexString(0x1000000 | (c & 0xFFFFFF)).substring(1)).append('"');
                    }
                    sb.append('}');
                }
                return sb.append('}').toString();
            } catch (Exception e) { return ""; }
        }

        @JavascriptInterface
        public void saveFile(final String name, final String mime, final String b64) {
            try {
                pendingBytes = Base64.decode(b64, Base64.DEFAULT);
            } catch (Exception e) {
                pendingBytes = null;
                return;
            }
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType(mime == null ? "application/octet-stream" : mime);
                    i.putExtra(Intent.EXTRA_TITLE, name);
                    try {
                        startActivityForResult(i, SAVE_FILE_REQUEST);
                    } catch (Exception e) {
                        pendingBytes = null;
                    }
                }
            });
        }

        @JavascriptInterface
        public void shareFile(final String name, final String mime, final String b64) {
            final byte[] bytes;
            try { bytes = Base64.decode(b64, Base64.DEFAULT); }
            catch (Exception e) { return; }
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    try {
                        java.io.File dir = new java.io.File(getCacheDir(), "share");
                        if (!dir.exists()) dir.mkdirs();
                        String safe = (name == null ? "" : name).replaceAll("[\\\\/:*?\"<>|\\x00-\\x1f]", "").trim().replaceAll("[. ]+$", "");
                        if (safe.isEmpty()) safe = "export.bin";
                        java.io.File f = new java.io.File(dir, safe);
                        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
                            fos.write(bytes); fos.flush();
                        }
                        Uri uri = Uri.parse("content://" + getPackageName() + ".share/" + Uri.encode(safe));
                        Intent send = new Intent(Intent.ACTION_SEND);
                        send.setType(mime == null ? "application/octet-stream" : mime);
                        send.putExtra(Intent.EXTRA_STREAM, uri);
                        send.setClipData(android.content.ClipData.newRawUri(null, uri));
                        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(send, "Auswertung teilen"));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Teilen fehlgeschlagen", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        /** Tour-Einladung teilen: QR-Code als PNG plus Text (Code und Anleitung) in EINEM Teilen-Vorgang – Messenger
         *  übernehmen den Text als Bildunterschrift, reine Text-Apps nur den Text. */
        @JavascriptInterface
        public void shareImageText(final String name, final String b64, final String text, final String title) {
            final byte[] bytes;
            try { bytes = Base64.decode(b64, Base64.DEFAULT); }
            catch (Exception e) { return; }
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    try {
                        java.io.File dir = new java.io.File(getCacheDir(), "share");
                        if (!dir.exists()) dir.mkdirs();
                        String safe = (name == null ? "" : name).replaceAll("[\\\\/:*?\"<>|\\x00-\\x1f]", "").trim().replaceAll("[. ]+$", "");
                        if (!safe.endsWith(".png")) safe = "rapidsplit-tour-code.png";
                        java.io.File f = new java.io.File(dir, safe);
                        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
                            fos.write(bytes); fos.flush();
                        }
                        Uri uri = Uri.parse("content://" + getPackageName() + ".share/" + Uri.encode(safe));
                        Intent send = new Intent(Intent.ACTION_SEND);
                        send.setType("image/png");
                        send.putExtra(Intent.EXTRA_STREAM, uri);
                        if (text != null && !text.isEmpty()) send.putExtra(Intent.EXTRA_TEXT, text);
                        send.setClipData(android.content.ClipData.newRawUri(null, uri));
                        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(send, title == null || title.isEmpty() ? "Teilen" : title));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Teilen fehlgeschlagen", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @JavascriptInterface
        public void exportPDF() {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    try {
                        PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                        String jobName = "Kajak-Tour Kostenrechner";
                        PrintDocumentAdapter adapter = web.createPrintDocumentAdapter(jobName);
                        pm.print(jobName, adapter, new PrintAttributes.Builder().build());
                    } catch (Exception e) { /* ignore */ }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(
                        WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                filePathCallback = null;
            }
        } else if (requestCode == SAVE_FILE_REQUEST) {
            boolean saved = false;
            if (resultCode == RESULT_OK && data != null && data.getData() != null
                    && pendingBytes != null) {
                OutputStream os = null;
                try {
                    os = getContentResolver().openOutputStream(data.getData());
                    if (os != null) { os.write(pendingBytes); os.flush(); saved = true; }
                } catch (Exception e) {
                    saved = false;
                } finally {
                    try { if (os != null) os.close(); } catch (Exception ignored) {}
                }
            }
            pendingBytes = null;
            // Erfolgsmeldung erst NACH tatsächlichem Schreiben (nicht schon beim Auslösen).
            final String msg = saved ? "Exportiert" : "Export nicht gespeichert";
            if (web != null) web.evaluateJavascript("window.toast&&toast('" + msg + "')", null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 77 && pendingPerm != null) {
            final android.webkit.PermissionRequest req = pendingPerm; pendingPerm = null;
            final boolean ok = grantResults.length > 0
                    && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED;
            runOnUiThread(new Runnable() { @Override public void run() {
                try { if (ok) req.grant(req.getResources()); else req.deny(); } catch (Exception e) { }
            }});
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        web.restoreState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Zurück in der App: Hintergrundbild/Systemfarben könnten sich geändert haben -> Web-App liest neu
        if (web != null) pushSystemDark();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ausrichtungPruefen(newConfig);
        // uiMode steht in configChanges -> kein Activity-Neustart beim Hell/Dunkel-Wechsel (WebView-Zustand bleibt).
        int night = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        applyBars(night == Configuration.UI_MODE_NIGHT_YES ? "#0e1513" : "#f5faf8");
        pushSystemDark();
    }

    /** Smartphones (kürzeste Seite < 600 dp) nur im Hochformat; Tablets, aufgeklappte Foldables und Chromebooks frei.
     *  Auch bei jedem Konfigurationswechsel neu prüfen: Auf-/Zuklappen eines Foldables startet die Activity wegen
     *  configChanges (smallestScreenSize) nicht neu. Im Mehrfenstermodus ignoriert Android die Vorgabe ohnehin. */
    private void ausrichtungPruefen(Configuration c) {
        int soll = c.smallestScreenWidthDp < 600
                ? android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                : android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if (getRequestedOrientation() != soll) setRequestedOrientation(soll);
    }

    @Override
    public void onBackPressed() {
        if (web == null) { super.onBackPressed(); return; }
        // Erst die Seite fragen: ein offener Dialog / der Wizard soll „Zurück" abfangen, statt die App zu beenden.
        web.evaluateJavascript("(window.handleAndroidBack&&window.handleAndroidBack())?1:0", new ValueCallback<String>() {
            @Override public void onReceiveValue(final String v) {
                runOnUiThread(new Runnable() { @Override public void run() {
                    if (!"1".equals(v)) {
                        if (web.canGoBack()) web.goBack(); else finish();
                    }
                }});
            }
        });
    }
}
