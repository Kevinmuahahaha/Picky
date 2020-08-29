package com.yybb.picky.ui.utils;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.yybb.picky.R;

import java.util.concurrent.atomic.AtomicInteger;


public class msg  {
    private static final String CHANNEL_ID = "PICKY_NOTIFICATION_CHANNEL_FROM_MSG";
    public static Context context = null;
    public static String humanReadableByteCountBin(long bytes) {
        long b = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        return b < 1024L ? bytes + " B"
                : b <= 0xfffccccccccccccL >> 40 ? String.format("%.1f KiB", bytes / 0x1p10)
                : b <= 0xfffccccccccccccL >> 30 ? String.format("%.1f MiB", bytes / 0x1p20)
                : b <= 0xfffccccccccccccL >> 20 ? String.format("%.1f GiB", bytes / 0x1p30)
                : b <= 0xfffccccccccccccL >> 10 ? String.format("%.1f TiB", bytes / 0x1p40)
                : b <= 0xfffccccccccccccL ? String.format("%.1f PiB", (bytes >> 10) / 0x1p40)
                : String.format("%.1f EiB", (bytes >> 20) / 0x1p40);
    }
    public static void textf(String info){ // force show text
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                msg.text(info);
            }
        });
    }
    public static void shrtf(String info){ // force show text
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                msg.shrt(info);
            }
        });
    }
    public static void text(String TEXT){
        if( context == null ){
            return;
        }
        Toast.makeText(context, TEXT, Toast.LENGTH_LONG).show();
    }
    public static void shrt(String TEXT){
        if( context == null ){
            return;
        }
        Toast.makeText(context, TEXT, Toast.LENGTH_SHORT).show();
    }
    public static void popup(String TEXT){
        new AlertDialog.Builder(context)
                .setTitle("INFO")
                .setMessage(TEXT)
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                //.setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .show();
    }
    public static void notify(Context passed_context, String info) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Picky";
            String description = "placeholder";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) passed_context.getSystemService(NotificationManager.class);
            channel.enableVibration(true); //Set if it is necesssary
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(passed_context, CHANNEL_ID)
                    .setContentTitle("Repo Notification")
                    .setSmallIcon(R.drawable.placeholder_block_blue)
                    .setContentText(info)
                    //.setStyle(new NotificationCompat.BigTextStyle()
                    //      .bigText("Much longer text that cannot fit one line..."))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager_send = NotificationManagerCompat.from(passed_context);
            notificationManager_send.notify(NotificationID.getID(),builder.build());
        }
    }
    private static class NotificationID {
        private final static AtomicInteger c = new AtomicInteger(0);
        public static int getID() {
            return c.incrementAndGet();
        }
    }
}
