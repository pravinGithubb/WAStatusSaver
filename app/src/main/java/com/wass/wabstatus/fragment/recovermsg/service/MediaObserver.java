package com.wass.wabstatus.fragment.recovermsg.service;

import android.os.Environment;
import android.os.FileObserver;
import android.util.Log;

import androidx.annotation.Nullable;

import com.wass.wabstatus.MyApp;
import com.wass.wabstatus.util.FileUtils;
import com.wass.wabstatus.util.Utils;

import java.io.File;

public class MediaObserver extends FileObserver {

    private static String TAG = "MediaObserver";
    public MediaObserver(String path, int mask) {
        super(path, mask);
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        Log.d("copy_file", "evnt : " + event + " path : " + path);
        if (event == 256) {
            try {
                String whatsappPath = "";
                if (new File(Environment.getExternalStorageDirectory() + File.separator + "Android/media/com.whatsapp/WhatsApp" + File.separator + "Media" + File.separator + "WhatsApp Images").isDirectory()) {
                    whatsappPath = "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images";
                } else {
                    whatsappPath = "WhatsApp/Media/WhatsApp Images";
                }

                String appStoreFolderPathForWhatsDeletedImages = "";
                if (new File(Environment.getExternalStorageDirectory() + File.separator + "Android/media/com.kessi.wawbstatussaver/Status Saver/WhatsDeleted Files").isDirectory()) {
                    appStoreFolderPathForWhatsDeletedImages = "Android/media/com.kessi.wawbstatussaver/Status Saver/WhatsDeleted Files";
                } else {
                    appStoreFolderPathForWhatsDeletedImages = "Status Saver/WhatsDeleted Files";
                }

                File root = Utils.getAppRoot(MyApp.getAppContext());
                File dir = new File(root.getAbsolutePath() + File.separator + "Status Saver" + File.separator + "WhatsDeleted Files");
                if (!dir.exists()) {
                    dir.mkdirs();
                    Log.d("copy_file", "make directory : " + dir.getAbsolutePath());
                } else {
                    Log.d("copy_file", "already exist directory : " + dir.getAbsolutePath());
                }

                File srcFile = new File(Environment.getExternalStorageDirectory(),
                        whatsappPath + File.separator + path);
                File destFile = new File(Environment.getExternalStorageDirectory(),
                        appStoreFolderPathForWhatsDeletedImages + File.separator + path);
                FileUtils.getInstance().copyFile(srcFile, destFile);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}