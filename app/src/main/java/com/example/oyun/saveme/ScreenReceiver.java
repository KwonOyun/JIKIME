package com.example.oyun.saveme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.KeyEvent;

public class ScreenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn = true;
    public static int count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("test","onReceive");

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            wasScreenOn = false;
            Log.e("test","wasScreenOn"+wasScreenOn);
            count++;
            if(count > 5){
                Intent i = new Intent();
                i.setClass(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                count = 0;
            }

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            wasScreenOn = true;
            Log.e("test","wasScreenOn"+wasScreenOn);
            count++;
            if(count > 5){
                Intent i = new Intent();
                i.setClass(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                count = 0;
            }
        }else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            Log.e("test","userpresent");
        }
    }
}