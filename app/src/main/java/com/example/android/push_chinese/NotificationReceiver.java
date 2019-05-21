package com.example.android.push_chinese;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("RECEIVED", "Receiving somthn");

        String whichAction = intent.getAction();
        Log.w("RECEIVED", whichAction);

        if (whichAction.equals("action1")) {

        }
    }
}
