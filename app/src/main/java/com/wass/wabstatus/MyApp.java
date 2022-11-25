package com.wass.wabstatus;

import android.app.Application;
import android.content.Context;

import com.wass.wabstatus.util.AdController;
import com.wass.wabstatus.util.AppOpenManager;

public class MyApp extends Application {

    AppOpenManager appOpenManager;
    private static final String ONESIGNAL_APP_ID = "0508f571-7ca1-46fa-975c-6c3efaa7dd9a";
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        // Enable verbose OneSignal logging to debug issues if needed.
// pravin's comment
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
// pravin's comment
        /*
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
*/

        if (!AdController.isLoadIronSourceAd){
            AdController.initAd(this);
            appOpenManager = new AppOpenManager(this);
        }
    }
    public static Context getAppContext() {
        return context;
    }
}
