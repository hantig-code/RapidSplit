package app.rapidsplit;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;

/** Minimaler Provider, der Dateien aus cacheDir/share/ für das System-Teilen-Menü bereitstellt
 *  (Ersatz für androidx.core FileProvider – die Build-Pipeline hat keine Support-Libs). */
public class ShareProvider extends ContentProvider {
    @Override public boolean onCreate() { return true; }

    private File fileFor(Uri uri) {
        String name = uri.getLastPathSegment();
        if (name == null) return null;
        File dir = new File(getContext().getCacheDir(), "share");
        File f = new File(dir, name);
        try {   // Pfad-Traversal verhindern: Datei muss im share-Ordner liegen
            if (!f.getCanonicalPath().startsWith(dir.getCanonicalPath() + File.separator)) return null;
        } catch (Exception e) { return null; }
        return f;
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws java.io.FileNotFoundException {
        File f = fileFor(uri);
        if (f == null || !f.exists()) throw new java.io.FileNotFoundException();
        return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri uri, String[] projection, String sel, String[] args, String sort) {
        File f = fileFor(uri);
        if (f == null || !f.exists()) return null;
        if (projection == null) projection = new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        MatrixCursor c = new MatrixCursor(projection, 1);
        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; i++) {
            if (OpenableColumns.DISPLAY_NAME.equals(projection[i])) row[i] = f.getName();
            else if (OpenableColumns.SIZE.equals(projection[i])) row[i] = f.length();
        }
        c.addRow(row);
        return c;
    }

    @Override public String getType(Uri uri) {
        String n = uri.getLastPathSegment();
        if (n != null && n.endsWith(".pdf")) return "application/pdf";
        if (n != null && n.endsWith(".csv")) return "text/csv";
        if (n != null && n.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }

    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String sel, String[] args) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String sel, String[] args) { return 0; }
}
