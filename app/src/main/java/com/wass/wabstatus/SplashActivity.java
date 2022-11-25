package com.wass.wabstatus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.wass.wabstatus.util.SharedPrefs;
import com.wass.wabstatus.util.Utils;

public class SplashActivity extends AppCompatActivity {

    String[] permissionsList = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int REQUEST_CODE_NOTIFICATION_LISTENER = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppCompatDelegate.setDefaultNightMode(SharedPrefs.getAppNightDayMode(this));

        Utils.setLanguage(SplashActivity.this, SharedPrefs.getLang(SplashActivity.this));

        if (Utils.hasNoPermissions(this, permissionsList)) {
            ActivityCompat.requestPermissions(this, Utils.permissions, Utils.perRequest);
        } else {
            gotoNext();
        }
    }

    void gotoNext() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }, 600);

    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utils.perRequest) {
            if (Utils.hasNoPermissions(this, permissionsList)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
                alertDialog.setTitle(R.string.allow_storage_access);
                alertDialog.setMessage(R.string.storage_permission_msg);
                alertDialog.setPositiveButton(R.string.notification_permission_retry, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        int checkVal = (SplashActivity.this).checkCallingOrSelfPermission(permissionsList[0]);
                        int checkVal1 = (SplashActivity.this).checkCallingOrSelfPermission(permissionsList[1]);

                        if(checkVal == PackageManager.PERMISSION_DENIED || checkVal1 ==PackageManager.PERMISSION_DENIED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, permissions[0])
                                    || ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, permissions[1])) {
                                ActivityCompat.requestPermissions(SplashActivity.this, Utils.permissions, Utils.perRequest);
                            } else {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, Utils.perRequest);
                            }
                        }
                    }
                });
                alertDialog.setNegativeButton(R.string.notification_permission_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).show();

            } else {
                gotoNext();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.perRequest) {
            if (Utils.hasNoPermissions(this, permissionsList)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
                alertDialog.setTitle(R.string.allow_storage_access);
                alertDialog.setMessage(R.string.storage_permission_msg);
                alertDialog.setPositiveButton(R.string.notification_permission_retry, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, permissionsList[0])
                                || ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this, permissionsList[1])) {
                            ActivityCompat.requestPermissions(SplashActivity.this, Utils.permissions, Utils.perRequest);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent,Utils.perRequest);
                        }
                    }
                });
                alertDialog.setNegativeButton(R.string.notification_permission_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).show();

            } else {
                gotoNext();
            }
        }

        if (requestCode == REQUEST_CODE_NOTIFICATION_LISTENER) {
            if (!Utils.isNotificationServiceRunning(this)) {
                //show dialog
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
                alertDialog.setTitle(R.string.confirm);
                alertDialog.setMessage(R.string.notification_permission_msg);
                alertDialog.setPositiveButton(R.string.notification_permission_retry, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                        startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS), REQUEST_CODE_NOTIFICATION_LISTENER);
                    }
                });
                alertDialog.setNegativeButton(R.string.notification_permission_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        gotoNext();
                    }
                });
                alertDialog.show();
            } else {
                gotoNext();
            }
        }
    }
}