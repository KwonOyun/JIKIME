package com.example.oyun.saveme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.net.Uri;

public class MyReceiver extends BroadcastReceiver {

    MainActivity main = new MainActivity();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals("call_police")) {

            Intent call_intent = new Intent();
            call_intent.setAction(Intent.ACTION_CALL);
            call_intent.setData(Uri.parse("tel:010-6647-6303"));
            try {
                context.startActivity(call_intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        else if(action.equals("message")) {
            Toast.makeText(context, "message", Toast.LENGTH_LONG).show();
            main.SendMessage();  //구조문자 보내기
            main.SendLocation();  //위치 전송
        }
        else if(action.equals("siren_message")) {
            Toast.makeText(context, "siren_message", Toast.LENGTH_LONG).show();
            main.SoundOn();   //사이렌
            main.SendLocation();  //위치 전송
            main.SendMessage(); //구조문자 보내기
        }
        else if(action.equals("siren_flash")) {
            Toast.makeText(context, "siren_flash", Toast.LENGTH_LONG).show();
            main.SoundOn();  //사이렌
            main.turnOnFlash();  //점멸
        }

    }
}
