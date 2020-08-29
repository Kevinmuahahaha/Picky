package com.yybb.picky.ui.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class permissionChecker {
    public static int PERMISSION_ALL = 1;
    public static String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,     //0     Do
            android.Manifest.permission.WAKE_LOCK,                  //1     not
            android.Manifest.permission.INTERNET,                   //2     change
            android.Manifest.permission.ACCESS_NETWORK_STATE,       //3     the order!!
            android.Manifest.permission.READ_EXTERNAL_STORAGE       //4     append after
    };
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    msg.text("Cannot: " + permission);
                    return false;
                }
            }
        }
        return true;
    }

    public static void requestWrite(Activity activity){
        int write_permission = ActivityCompat.checkSelfPermission(activity.getApplicationContext(), PERMISSIONS[0] );

        if ( write_permission != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
    }
    public static void requestRead(Activity activity){
        int write_permission = ActivityCompat.checkSelfPermission(activity.getApplicationContext(), PERMISSIONS[4] );
        if ( write_permission != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
    }
    public static boolean canWrite(Activity activity){
        return ActivityCompat.checkSelfPermission(activity.getApplicationContext(), PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean canRead(Activity activity){
        return ActivityCompat.checkSelfPermission(activity.getApplicationContext(), PERMISSIONS[4]) == PackageManager.PERMISSION_GRANTED;
    }

}
