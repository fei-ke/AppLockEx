package com.fei_ke.applockex.hook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.fei_ke.applockex.AppLockEx;
import com.fei_ke.applockex.BuildConfig;

/**
 * Created by fei on 16/2/8.
 */
public class ALEServiceProxy {
    private static final String TAG = "ALEServiceProxy";
    private AppLockEx appLockEx;

    public ALEServiceProxy(Context context) {
        Intent service = new Intent("com.fei_ke.applockex.ALEService");
        service.setPackage(BuildConfig.APPLICATION_ID);
        context.bindService(service, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected() called with: " + "name = [" + name + "], service = [" + service + "]");
                appLockEx = (AppLockEx) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected() called with: " + "name = [" + name + "]");
                appLockEx = null;
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public boolean isAppNeedLock(String pkgName) {
        try {
            return appLockEx.isAppNeedLock(pkgName);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public void updateUnlockTime(long time) {
        try {
            appLockEx.updateUnlockTime(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
