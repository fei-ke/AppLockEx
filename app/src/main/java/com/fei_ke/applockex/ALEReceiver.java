package com.fei_ke.applockex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by fei on 16/2/6.
 */
public class ALEReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ALEServer.class));
    }
}
