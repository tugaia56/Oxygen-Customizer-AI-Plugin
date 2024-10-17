package it.dhd.oxygencustomizer.aiplugin.utils;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

import static it.dhd.oxygencustomizer.aiplugin.OxygenCustomizerAIPlugin.getAppContext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import it.dhd.oxygencustomizer.aiplugin.BuildConfig;

public class AppUtils {

    public static boolean hasStoragePermission() {
        return Environment.isExternalStorageManager() || Environment.isExternalStorageLegacy();
    }

    public static void requestStoragePermission(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
        ((Activity) context).startActivityForResult(intent, 0);

        ActivityCompat.requestPermissions((Activity) context, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }, 0);
    }

    public static void launchFilePicker(ActivityResultLauncher<Intent> launcher, String type) {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType(type);
        launcher.launch(chooseFile);
    }

    public static String getRealPath(Object obj) {
        if (obj instanceof Intent) {
            return getRealPathFromURI(((Intent) obj).getData());
        } else if (obj instanceof Uri) {
            return getRealPathFromURI((Uri) obj);
        } else {
            throw new IllegalArgumentException("Object must be an Intent or Uri");
        }
    }

    private static String getRealPathFromURI(Uri uri) {
        File file;
        try {
            @SuppressLint("Recycle") Cursor returnCursor = getAppContext().getContentResolver().query(uri, null, null, null, null);

            if (returnCursor == null) return null;

            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            file = new File(getAppContext().getFilesDir(), name);
            @SuppressLint("Recycle") InputStream inputStream = getAppContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read;
            int maxBufferSize = 1024 * 1024;

            if (inputStream == null) return null;

            int bytesAvailable = inputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file.getPath();
    }

}
