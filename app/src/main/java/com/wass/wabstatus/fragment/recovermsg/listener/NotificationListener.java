package com.wass.wabstatus.fragment.recovermsg.listener;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.wass.wabstatus.MyApp;
import com.wass.wabstatus.fragment.recovermsg.db.DeletedMsgDatabaseClient;
import com.wass.wabstatus.fragment.recovermsg.db.DeletedMsgTable;

import java.text.DateFormat;
import java.util.Date;

public class NotificationListener extends NotificationListenerService {

    //                        PackageManager manager = context.getPackageManager();
//                        Resources resources = manager.getResourcesForApplication(event.getPackageName().toString());
//                        get notification.getSmallId, get resources by small_id

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        if (sbn.getPackageName().equalsIgnoreCase("com.whatsapp")
                || sbn.getPackageName().equalsIgnoreCase("com.whatsapp.w4b") && !sbn.isOngoing()) {
            Bundle bundle = sbn.getNotification().extras;
            String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());

            StringBuilder sender_temp = new StringBuilder(bundle.getString("android.title", ""));
            StringBuilder msg_temp = new StringBuilder(bundle.getString("android.text", ""));

            String sender = bundle.getString("android.title", "");
            String msg = bundle.getString("android.text", "");
            long lastRowId = bundle.getLong("last_row_id", -1);
            try {

                // additional functionality commented
                if (sender.equalsIgnoreCase("WhatsApp") ||
                        sender.endsWith("missed voice calls") ||
                        sender.endsWith("missed video calls") ||
                        msg.endsWith("new messages") ||
                        msg.equals("")
                ) {
                    return;
                }

                if (sender.contains(": ")) {
                    if (sender.contains("messages): ")) {
                        msg = sender.substring(sender.lastIndexOf("messages): ") + 11) + ": " + msg;
                        if (sender.contains("(")) {
                            sender = sender.substring(0, sender.lastIndexOf("(") - 1);
                        } else {
                            sender = sender.substring(0, sender.lastIndexOf(": "));
                        }
                    } else {
                        msg = sender.substring(sender.lastIndexOf(": ") + 2) + ": " + msg;
                        sender = sender.substring(0, sender.lastIndexOf(": "));
                    }
                }
            } catch (Exception error) {
                FirebaseCrashlytics.getInstance().recordException(
                        new RuntimeException("Listener exception: " + error + "\nsender: " +
                                bundle.getString("android.title", "") + " \nmsg: " +
                                bundle.getString("android.text", "")));
            }
            String finalSender = sender;
            String finalMsg = msg;
            new Thread(() -> {

                if (!TextUtils.isEmpty(finalSender) && !TextUtils.isEmpty(finalMsg)
                        && finalMsg.endsWith("This message was deleted")
                        && lastRowId != -1) {
                    DeletedMsgTable deletedMsgTable = DeletedMsgDatabaseClient.getInstance(MyApp.getAppContext()).getAppDatabase().daoDeletedMsgAccess().getLastNotDeletedRecordByUsername(finalSender, false);
                    if (deletedMsgTable != null) {
                        deletedMsgTable.setIs_deleted(true);
                        deletedMsgTable.setDeleted_at(date);
                        DeletedMsgDatabaseClient.getInstance(MyApp.getAppContext()).getAppDatabase().daoDeletedMsgAccess().updateRecord(deletedMsgTable);
                    }
                } else if (!TextUtils.isEmpty(finalSender) && !TextUtils.isEmpty(finalMsg) && lastRowId != -1) {
                    DeletedMsgTable deletedMsgTable = new DeletedMsgTable(finalSender, finalMsg, date, null, false);
                    DeletedMsgDatabaseClient.getInstance(MyApp.getAppContext()).getAppDatabase().daoDeletedMsgAccess().insertRecord(deletedMsgTable);
                }
            }).start();

        }

    }

//
//    /**
//     * Pojo to Json
//     *
//     * @param pojoObject
//     * @return
//     */

//    private String pojoToJson(Object pojoObject) {
//        return new Gson().toJson(pojoObject);
//    }

//    private String bundleToString(Bundle bundle) {
//        String data = "";
//        for (String key : bundle.keySet()) {
//            try {
//                Object obj = bundle.get(key);
//                if (TextUtils.isEmpty(data)) {
//                    data = "key : " + key + " value :" + obj;
//                } else {
//                    data = "\nkey : " + key + " value :" + obj;
//                }
//            } catch (Exception | Error e) {
//                e.printStackTrace();
//            }
//        }
//        return data;
//    }
}
