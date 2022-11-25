package com.wass.wabstatus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.ironsource.mediationsdk.IronSource;
import com.wass.wabstatus.font.FontActivity;
import com.wass.wabstatus.fragment.DirectChatFragment;
import com.wass.wabstatus.fragment.MyPhotos;
import com.wass.wabstatus.fragment.RecentWapp;
import com.wass.wabstatus.fragment.RecentWappBus;
import com.wass.wabstatus.fragment.recovermsg.DeletedMsgFrag;
import com.wass.wabstatus.fragment.recovermsg.service.MediaObserverService;
import com.wass.wabstatus.util.AdController;
import com.wass.wabstatus.util.SharedPrefs;
import com.wass.wabstatus.util.Utils;
import com.wass.wabstatus.waweb.WAWebActivity;

import java.util.ArrayList;
import java.util.List;

import slidingrootnav.SlidingRootNav;
import slidingrootnav.SlidingRootNavBuilder;

public class MainActivity extends AppCompatActivity {

    private static final int RC_APP_UPDATE = 45;
    private SlidingRootNav slidingRootNav;
    ImageView whatsIV, navIV, nTop, shareIV;

    LinearLayout nWapp, nWbapp, nSaved,
            nLang, nShare, nRate, nPrivacy, nMore, nHow, nWeb, nChat, nFont;
    RelativeLayout nDark;

    ImageView niWapp, niWbapp, niSaved,
            niDark, niLang, niShare, niRate, niPrivacy, niMore, niHow, niWeb, niChat, niFont;

    TextView ntWapp, ntWbapp, ntSaved,
            ntDark, ntLang, ntShare, ntRate, ntPrivacy, ntMore, ntHow, ntWeb, ntChat, ntFont;

    SwitchCompat modeSwitch;

    TabLayout tabLayout;
    ViewPager viewPager;
    String[] tabs;
    Dialog dialog, dialogLang;
    LinearLayout container;

    private AppUpdateManager mAppUpdateManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Utils.setLanguage(MainActivity.this, SharedPrefs.getLang(MainActivity.this));
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabs = new String[5];
        tabs[0] = getResources().getString(R.string.d_chat);
        tabs[1] = getResources().getString(R.string.dmsg);
        tabs[2] = getResources().getString(R.string.wapp);
        tabs[3] = getResources().getString(R.string.saved);
        tabs[4] = getResources().getString(R.string.wbapp);

        tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(getTabViewUn(i));
        }

        setupTabIcons();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                TabLayout.Tab tabs = tabLayout.getTabAt(tab.getPosition());
                tabs.setCustomView(null);
                tabs.setCustomView(getTabView(tab.getPosition()));

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                navigate(null);

                if (tab.getPosition() == 0) {
                    // Direct Chat
                }
                if (tab.getPosition() == 1) {
                    ((DeletedMsgFrag) (MainActivity.this.getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + tab.getPosition()))).loadDeletedMsgFromLocalDatabase();
                }
                if (tab.getPosition() == 2) {
                    if (isOpenWapp) {
                        isOpenWapp = false;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            if (SharedPrefs.getshouldPopulate(MainActivity.this)) {
                                ((RecentWapp) (MainActivity.this.getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + tab.getPosition()))).populateGrid();
                            }
                        } else if (!SharedPrefs.getWATree(MainActivity.this).equals("")) {
                            ((RecentWapp) (MainActivity.this.getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + tab.getPosition()))).populateGrid();
                        }
                    }
                }
                if (tab.getPosition() == 4) {
                    if (isOpenWbApp) {
                        isOpenWbApp = false;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            if (SharedPrefs.getshouldPopulateWB(MainActivity.this))
                                ((RecentWappBus) MainActivity.this.getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + tab.getPosition())).populateGrid();
                        }
                        if (!SharedPrefs.getWBTree(MainActivity.this).equals("")) {
                            ((RecentWappBus) MainActivity.this.getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + tab.getPosition())).populateGrid();
                        }
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TabLayout.Tab tabs = tabLayout.getTabAt(tab.getPosition());
                tabs.setCustomView(null);
                tabs.setCustomView(getTabViewUn(tab.getPosition()));
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        int viewPagerDefaultId = 0;
        try {
            viewPagerDefaultId = SharedPrefs.getCurrentTabSelected(this);
            viewPager.setCurrentItem(viewPagerDefaultId);
        } catch (Exception e) {
            Log.e("onCreate: ", "error getting viewPagerDefaultId");
        }

        navIV = findViewById(R.id.navIV);
        navIV.setOnClickListener(v -> {
            slidingRootNav.openMenu(true);
        });

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject();

        nTop = findViewById(R.id.nTop);
        Glide.with(this)
                .load(R.drawable.mtop)
                .into(nTop);

        initDrawer();

        wAppAlert();
        whatsIV = findViewById(R.id.whatsIV);
        whatsIV.setOnClickListener(v -> {
            dialog.show();
        });

        shareIV = findViewById(R.id.shareIV);
        shareIV.setOnClickListener(v -> {
            shareApp();
        });

        langAlert();

        container = findViewById(R.id.banner_container);
        if (AdController.isLoadIronSourceAd) {
            AdController.inItIron(MainActivity.this);
        } else {
            /*admob*/
            AdController.loadBannerAd(MainActivity.this, container);
            AdController.loadInterAd(MainActivity.this);
        }

        // Start Media Observer
        startMediaObserverServiceApp(false);

        mAppUpdateManager = AppUpdateManagerFactory.create(this);

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(result -> {
            if(result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))
            {
                try
                {
                    mAppUpdateManager.startUpdateFlowForResult(result,AppUpdateType.FLEXIBLE, MainActivity.this
                            ,RC_APP_UPDATE);

                } catch (IntentSender.SendIntentException e)
                {
                    e.printStackTrace();
                }
            }
        });

//        if (!Utils.isNotificationServiceRunning(MainActivity.this)) {
//            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
//            alertDialog.setTitle(R.string.confirm);
//            alertDialog.setMessage(R.string.notification_permission_msg);
//            alertDialog.setPositiveButton(R.string.notification_permission_retry, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
//                }
//            });
//            alertDialog.setNegativeButton(R.string.notification_permission_deny, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            });
//            alertDialog.show();
//        }

//        try {
//            final Intent intent = new Intent();
//            String manufacturer = Build.MANUFACTURER;
//            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
//                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
//            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
//                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
//                //intent.setComponent(new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"));
//            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
//                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
//            } else if ("huawei".equalsIgnoreCase(manufacturer)) {
//                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
//            } else {
//                // applySubmit(false);
//                return;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    private final InstallStateUpdatedListener installStateUpdatedListener = state -> {
        if(state.installStatus() == InstallStatus.DOWNLOADED)
        {
            showCompletedUpdate();
        }
    };

    private void showCompletedUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"New app is ready!",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Install", view -> mAppUpdateManager.completeUpdate());
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RC_APP_UPDATE && resultCode != RESULT_OK)
        {
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startMediaObserverServiceApp(boolean shouldStart) {
        if (shouldStart) {
            // Media Observer Service Start
            Intent mediaObserverService = new Intent(this, MediaObserverService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(mediaObserverService);
            } else {
                startService(mediaObserverService);
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        mAppUpdateManager.registerListener(installStateUpdatedListener);

        if (AdController.isLoadIronSourceAd) {
            AdController.destroyIron();
            AdController.ironBanner(MainActivity.this, container);
            // call the IronSource onResume method
            IronSource.onResume(this);
        }
    }

    @Override
    protected void onStop() {
        if(mAppUpdateManager!=null) mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (AdController.isLoadIronSourceAd) {
            // call the IronSource onPause method
            IronSource.onPause(this);
        }
        try {
            SharedPrefs.setCurrentTabSelected(this, viewPager.getCurrentItem());
        } catch (Exception e) {
            Log.e("onDestroy", "viewPager Error: " + e);
        }
        super.onPause();
    }

    boolean isOpenWapp = false, isOpenWbApp = false;

    void wAppAlert() {
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.popup_lay);

        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RelativeLayout btnWapp = dialog.findViewById(R.id.btn_wapp);
        RelativeLayout btnWappBus = dialog.findViewById(R.id.btn_wapp_bus);

        btnWapp.setOnClickListener(arg0 -> {
            try {
                isOpenWapp = true;
                startActivity(getPackageManager().getLaunchIntentForPackage("com.whatsapp"));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Please Install WhatsApp For Download Status!!!!!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();

        });

        btnWappBus.setOnClickListener(arg0 -> {
            try {
                isOpenWbApp = true;
                startActivity(getPackageManager().getLaunchIntentForPackage("com.whatsapp.w4b"));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Please Install WhatsApp Business For Download Status!!!!!", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

    }


    void langAlert() {
        dialogLang = new Dialog(MainActivity.this);
        dialogLang.setContentView(R.layout.lang_lay);

        dialogLang.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView txtEn = dialogLang.findViewById(R.id.txt_en);
        TextView txtHi = dialogLang.findViewById(R.id.txt_hi);
        TextView txtAr = dialogLang.findViewById(R.id.txt_ar);

        txtEn.setOnClickListener(arg0 -> {
            SharedPrefs.setLang(MainActivity.this, "en");
            dialogLang.dismiss();
            refresh();
        });

        txtHi.setOnClickListener(arg0 -> {
            SharedPrefs.setLang(MainActivity.this, "hi");
            dialogLang.dismiss();
            refresh();
        });

        txtAr.setOnClickListener(arg0 -> {
            SharedPrefs.setLang(MainActivity.this, "ar");
            dialogLang.dismiss();
            refresh();
        });

    }

    void refresh() {
        finish();
        startActivity(getIntent());
    }


    @Override
    protected void onDestroy() {
        try {
            SharedPrefs.setCurrentTabSelected(this, viewPager.getCurrentItem());
        } catch (Exception e) {
            Log.e("onDestroy", "viewPager Error: " + e);
        }
        super.onDestroy();
    }

    ViewPagerAdapter adapter;

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(
                getSupportFragmentManager());

        adapter.addFragment(new DirectChatFragment(), "Direct Chat");
        adapter.addFragment(new DeletedMsgFrag(), "Deleted Message");
        adapter.addFragment(new RecentWapp(), "Whatsapp");
        adapter.addFragment(new MyPhotos(), "Photos");
        adapter.addFragment(new RecentWappBus(), "WA Business");

        viewPager.setAdapter(adapter);
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return this.mFragmentList.get(arg0);
        }

        @Override
        public int getCount() {
            return this.mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            this.mFragmentList.add(fragment);
            this.mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return this.mFragmentTitleList.get(position);
        }
    }

    private void setupTabIcons() {
        View v = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView txt = v.findViewById(R.id.tab);
        txt.setText(tabs[0]);
        txt.setTextColor(getResources().getColor(R.color.tab_txt_press));
        txt.setBackgroundResource(R.drawable.press_tab);
        FrameLayout.LayoutParams tabp = new FrameLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels * 440 / 1080,
                getResources().getDisplayMetrics().heightPixels * 140 / 1920);
        txt.setLayoutParams(tabp);
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        tab.setCustomView(null);
        tab.setCustomView(v);
    }

    public View getTabView(int pos) {
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
        TextView txt = v.findViewById(R.id.tab);
        txt.setText(tabs[pos]);
        txt.setTextColor(getResources().getColor(R.color.tab_txt_press));
        txt.setBackgroundResource(R.drawable.press_tab);
        FrameLayout.LayoutParams tab = new FrameLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels * 440 / 1080,
                getResources().getDisplayMetrics().heightPixels * 140 / 1920);
        txt.setLayoutParams(tab);
        return v;
    }

    public View getTabViewUn(int pos) {
        View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_tab, null);
        TextView txt = v.findViewById(R.id.tab);
        txt.setText(tabs[pos]);
        txt.setTextColor(getResources().getColor(R.color.tab_txt_unpress));
        txt.setBackgroundResource(R.drawable.unpress_tab);
        FrameLayout.LayoutParams tab = new FrameLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels * 440 / 1080,
                getResources().getDisplayMetrics().heightPixels * 140 / 1920);
        txt.setLayoutParams(tab);
        return v;
    }

    public void initDrawer() {
        nWapp = findViewById(R.id.nWapp);
//        nWbapp = findViewById(R.id.nWbapp);
//        nSaved = findViewById(R.id.nSaved);
        nDark = findViewById(R.id.nDark);
        nLang = findViewById(R.id.nLang);
        nShare = findViewById(R.id.nShare);
        nRate = findViewById(R.id.nRate);
        nPrivacy = findViewById(R.id.nPrivacy);
//        nMore = findViewById(R.id.nMore);
        nHow = findViewById(R.id.nHow);
        nWeb = findViewById(R.id.nWeb);
//        nChat = findViewById(R.id.nChat);
        nFont = findViewById(R.id.nFont);

        niWapp = findViewById(R.id.niWapp);
//        niWbapp = findViewById(R.id.niWbapp);
//        niSaved = findViewById(R.id.niSaved);
        niDark = findViewById(R.id.niDark);
        niLang = findViewById(R.id.niLang);
        niShare = findViewById(R.id.niShare);
        niRate = findViewById(R.id.niRate);
        niPrivacy = findViewById(R.id.niPrivacy);
//        niMore = findViewById(R.id.niMore);
        niHow = findViewById(R.id.niHow);
        niWeb = findViewById(R.id.niWeb);
//        niChat = findViewById(R.id.niChat);
        niFont = findViewById(R.id.niFont);

        ntWapp = findViewById(R.id.ntWapp);
//        ntWbapp = findViewById(R.id.ntWbapp);
//        ntSaved = findViewById(R.id.ntSaved);
        ntDark = findViewById(R.id.ntDark);
        ntLang = findViewById(R.id.ntLang);
        ntShare = findViewById(R.id.ntShare);
        ntRate = findViewById(R.id.ntRate);
        ntPrivacy = findViewById(R.id.ntPrivacy);
//        ntMore = findViewById(R.id.ntMore);
        ntHow = findViewById(R.id.ntHow);
        ntWeb = findViewById(R.id.ntWeb);
//        ntChat = findViewById(R.id.ntChat);
        ntFont = findViewById(R.id.ntFont);


        nWapp.setOnClickListener(new ClickListener());
//        nWbapp.setOnClickListener(new ClickListener());
//        nSaved.setOnClickListener(new ClickListener());
        nDark.setOnClickListener(new ClickListener());
        nLang.setOnClickListener(new ClickListener());
        nShare.setOnClickListener(new ClickListener());
        nRate.setOnClickListener(new ClickListener());
        nPrivacy.setOnClickListener(new ClickListener());
//        nMore.setOnClickListener(new ClickListener());
        nHow.setOnClickListener(new ClickListener());
        nWeb.setOnClickListener(new ClickListener());
//        nChat.setOnClickListener(new ClickListener());
        nFont.setOnClickListener(new ClickListener());

        modeSwitch = findViewById(R.id.modeSwitch);
        modeSwitch.setChecked(SharedPrefs.getAppNightDayMode(MainActivity.this) == AppCompatDelegate.MODE_NIGHT_YES);

        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                SharedPrefs.setInt(this, SharedPrefs.PREF_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                SharedPrefs.setInt(this, SharedPrefs.PREF_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

    }

    void setUnpress() {
        setPress(niWapp, ntWapp, R.color.drawer_unpress);
//        setPress(niWbapp, ntWbapp, R.color.drawer_unpress);
//        setPress(niSaved, ntSaved, R.color.drawer_unpress);
//        setPress(niSaved, ntSaved, R.color.drawer_unpress);
        setPress(niLang, ntLang, R.color.drawer_unpress);
        setPress(niHow, ntHow, R.color.drawer_unpress);
        setPress(niShare, ntShare, R.color.drawer_unpress);
        setPress(niRate, ntRate, R.color.drawer_unpress);
        setPress(niPrivacy, ntPrivacy, R.color.drawer_unpress);
//        setPress(niMore, ntMore, R.color.drawer_unpress);
        setPress(niWeb, ntWeb, R.color.drawer_unpress);
//        setPress(niChat, ntChat, R.color.drawer_unpress);
        setPress(niFont, ntFont, R.color.drawer_unpress);
    }

    void setPress(ImageView imageView, TextView textView, int color) {
        imageView.setColorFilter(ContextCompat.getColor(MainActivity.this, color), android.graphics.PorterDuff.Mode.SRC_IN);
        textView.setTextColor(getResources().getColor(color));
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.nWapp:
                    setUnpress();
                    setPress(niWapp, ntWapp, R.color.drawer_press);
                    viewPager.setCurrentItem(0);
                    slidingRootNav.closeMenu();
                    navigate(null);
                    break;

//                case R.id.nWbapp:
//                    setUnpress();
//                    setPress(niWbapp, ntWbapp, R.color.drawer_press);
//                    viewPager.setCurrentItem(1);
//                    slidingRootNav.closeMenu();
//                    navigate(null);
//                    break;
//
//                case R.id.nSaved:
//                    setUnpress();
//                    setPress(niSaved, ntSaved, R.color.drawer_press);
//                    if (Utils.hasPermissions(MainActivity.this, Utils.permissions)) {
//                        ActivityCompat.requestPermissions(MainActivity.this, Utils.permissions, Utils.perRequest);
//                    } else {
//                        navigate(new Intent(MainActivity.this, MyStatusActivity.class));
//                    }
//                    slidingRootNav.closeMenu();
//                    break;

                case R.id.nWeb:
                    setUnpress();
                    setPress(niWeb, ntWeb, R.color.drawer_press);
                    if (Utils.hasPermissions(MainActivity.this, Utils.permissions)) {
                        ActivityCompat.requestPermissions(MainActivity.this, Utils.permissions, Utils.perRequest);
                    } else {
                        navigate(new Intent(MainActivity.this, WAWebActivity.class));
                    }
                    slidingRootNav.closeMenu();
                    break;

//                case R.id.nChat:
//                    setUnpress();
//                    setPress(niChat, ntChat, R.color.drawer_press);
//                    if (Utils.hasPermissions(MainActivity.this, Utils.permissions)) {
//                        ActivityCompat.requestPermissions(MainActivity.this, Utils.permissions, Utils.perRequest);
//                    } else {
//                        navigate(new Intent(MainActivity.this, DChatActivity.class));
//                    }
//                    slidingRootNav.closeMenu();
//                    break;

                case R.id.nFont:
                    setUnpress();
                    setPress(niFont, ntFont, R.color.drawer_press);
                    if (Utils.hasPermissions(MainActivity.this, Utils.permissions)) {
                        ActivityCompat.requestPermissions(MainActivity.this, Utils.permissions, Utils.perRequest);
                    } else {
                        navigate(new Intent(MainActivity.this, FontActivity.class));
                    }
                    slidingRootNav.closeMenu();
                    break;

                case R.id.nDark:
                    slidingRootNav.closeMenu();
                    navigate(null);
                    break;

                case R.id.nLang:
                    setUnpress();
                    setPress(niLang, ntLang, R.color.drawer_press);
                    dialogLang.show();
                    slidingRootNav.closeMenu();
                    break;

                case R.id.nHow:
                    setUnpress();
                    setPress(niHow, ntHow, R.color.drawer_press);
                    navigate(new Intent(MainActivity.this, HelpActivity.class));
                    slidingRootNav.closeMenu();
                    break;

                case R.id.nShare:
                    setUnpress();
                    setPress(niShare, ntShare, R.color.drawer_press);
                    slidingRootNav.closeMenu();
                    shareApp();
                    break;

                case R.id.nRate:
                    setUnpress();
                    setPress(niRate, ntRate, R.color.drawer_press);
                    slidingRootNav.closeMenu();
                    rateUs();
                    break;

//                case R.id.nMore:
//                    setUnpress();
//                    setPress(niMore, ntMore, R.color.drawer_press);
//                    slidingRootNav.closeMenu();
//                    moreApp();
//                    break;

                case R.id.nPrivacy:
                    setUnpress();
                    setPress(niPrivacy, ntPrivacy, R.color.drawer_press);
                    slidingRootNav.closeMenu();
                    navigate(new Intent(MainActivity.this, PolicyActivity.class));
                    break;

            }
        }
    }

    void navigate(Intent intent) {
        AdController.adCounter++;
        Log.e("navigate: ", "" + AdController.adCounter);
        if (AdController.isLoadIronSourceAd) {
            AdController.ironShowInterstitial(MainActivity.this, intent, 0);
        } else {
            AdController.showInterAd(MainActivity.this, intent, 0);
        }
    }

    public void shareApp() {
        Intent myapp = new Intent(Intent.ACTION_SEND);
        myapp.setType("text/plain");
        myapp.putExtra(Intent.EXTRA_TEXT, "Download this awesome app\n https://play.google.com/store/apps/details?id=" + getPackageName() + " \n");
        startActivity(myapp);
    }

    public void rateUs() {
        try {
            Intent rateIntent=new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
            rateIntent.setPackage("com.android.vending");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
        }
    }

    public void moreApp() {
        startActivity(new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/dev?id=7081479513420377164&hl=en")));
    }

    private long mLastBackClick = 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mLastBackClick < 11000) {
            super.onBackPressed();
        } else {
            AdController.adCounter++;
            if (AdController.adCounter == AdController.adDisplayCounter) {
                AdController.showInterAd(MainActivity.this, null, 0);
            } else {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.exit_alert), Toast.LENGTH_SHORT).show();
                mLastBackClick = System.currentTimeMillis();
            }
        }
    }

}