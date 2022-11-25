package com.wass.wabstatus.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import com.wass.wabstatus.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String mPath;
    public static int perRequest = 21;

    public static String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


    public static boolean hasPermissions(Context context, String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasNoPermissions(Context context, String... permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean isNotificationServiceRunning(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName);
    }

    public static void mediaScanner(Context context, String newFilePath, String oldFilePath, String fileType) {
        try {
            MediaScannerConnection.scanFile(context, new String[]{newFilePath + new File(oldFilePath).getName()}, new String[]{fileType},
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        public void onMediaScannerConnected() {
                        }

                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getBack(String paramString1, String paramString2) {
        Matcher localMatcher = Pattern.compile(paramString2).matcher(paramString1);
        if (localMatcher.find()) {
            return localMatcher.group(1);
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean download(Context context, String sourceFile) {
        return copyFileInSavedDir(context, sourceFile);
    }

    static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

//    static boolean isVideoFile(String path) {
//        String mimeType = URLConnection.guessContentTypeFromName(path);
//        return mimeType != null && mimeType.startsWith("video");
//    }

    public static boolean isVideoFile(Context context, String path) {
        if (path.startsWith("content")) {
            DocumentFile fromTreeUri = DocumentFile.fromSingleUri(context, Uri.parse(path));
            String mimeType = null;
            if (fromTreeUri != null) {
                mimeType = fromTreeUri.getType();
            }
            return mimeType != null && mimeType.startsWith("video");
        } else {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            return mimeType != null && mimeType.startsWith("video");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean copyFileInSavedDir(Context context, String sourceFile) {

        String finalPath;

        finalPath = getDir(context, "Saved").getAbsolutePath();
        // pravin's comment
        //        if (isVideoFile(context, sourceFile)) {
//            finalPath = getDir(context, "Videos").getAbsolutePath();
//        } else {
//            finalPath = getDir(context, "Images").getAbsolutePath();
//        }

        String pathWithName = finalPath + File.separator + new File(sourceFile).getName();
        Uri destUri = Uri.fromFile(new File(pathWithName));

        InputStream is = null;
        OutputStream os = null;
        try {
            Uri uri;
            if (SharedPrefs.getandroidCheck(context)){
                uri = Uri.fromFile(new File(sourceFile));
            } else {
                uri = Uri.parse(sourceFile);
            }
            is = context.getContentResolver().openInputStream(uri);
            os = context.getContentResolver().openOutputStream(destUri, "w");

            byte[] buffer = new byte[1024];

            int length;
            while ((length = is.read(buffer)) > 0)
                os.write(buffer, 0, length);

            is.close();
            os.flush();
            os.close();

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(destUri);
            context.sendBroadcast(intent);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    static File getDir(Context context, String folder) {
        File rootFile = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + context.getResources().getString(R.string.app_name) + File.separator + folder);
        rootFile.mkdirs();
        return rootFile;
    }

    public static void setLanguage(Context context, String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void shareFile(Context context, boolean isVideo, String path) {
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        if (isVideo)
            share.setType("Video/*");
        else
            share.setType("image/*");

        Uri uri;
        if (path.startsWith("content")) {
            uri = Uri.parse(path);
        } else {
            uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", new File(path));
        }

        share.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(share);
    }

    public static void repostWhatsApp(Context context, boolean isVideo, String path) {
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        if (isVideo)
            share.setType("Video/*");
        else
            share.setType("image/*");

        Uri uri;
        if (path.startsWith("content")) {
            uri = Uri.parse(path);
        } else {
            uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", new File(path));
        }
        share.putExtra(Intent.EXTRA_STREAM, uri);
        try{
            share.setPackage("com.whatsapp");
            context.startActivity(share);
        }catch (Exception e){
            Toast.makeText(context, "Some Error Occur. Try Again in few seconds", Toast.LENGTH_SHORT).show();
        }
    }

    public static File getAppRoot(Context context) {
        File root = Environment.getExternalStorageDirectory();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (context.getExternalMediaDirs().length > 0) {
                    root = context.getExternalMediaDirs()[0];
                } else {
                    root = context.getExternalFilesDir(null);
                }
            } else {
                root = Environment.getExternalStorageDirectory();
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
        return root;
    }
}
