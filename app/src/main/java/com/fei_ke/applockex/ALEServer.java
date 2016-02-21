package com.fei_ke.applockex;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fei_ke.applockex.support.WearableDetector;

/**
 * 主要检测屏幕关闭事件,更新解锁时间
 * Created by fei on 16/2/2.
 */
public class ALEServer extends Service {
    private static final String TAG = "ALEServer";
    private BroadcastReceiver mReceiver;
    private long lastUnlockTime = 0;
    private WearableDetector wearableDetector;
    private static final String MY_WATCH_NAME = "HUAWEI WATCH 0C14";

    @Override
    public void onCreate() {
        super.onCreate();
        wearableDetector = new WearableDetector();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        if (mReceiver == null) {
            mReceiver = new ScreenOffDetector();
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mReceiver, filter);
        }
        wearableDetector.start(this);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        wearableDetector.stop();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ALEBinder();
    }


    private boolean isSafeLocation() {
        return wearableDetector.isSafeLocation(MY_WATCH_NAME);
    }

    public void updateUnlockTime(long time) {
        Log.d(TAG, "updateUnlockTime() called with: " + "time = [" + time + "]");
        lastUnlockTime = time;
    }

    private class ScreenOffDetector extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUnlockTime(0);
        }
    }

    public class ALEBinder extends Binder {
        public boolean isNeedLock(String pkgName) {
            Log.d(TAG, "isNeedLock() called with: " + "pkgName = [" + pkgName + "]");
            boolean safeLocation = isSafeLocation();
            Log.d(TAG, "isNeedLock() lastUnlockTime: " + lastUnlockTime + ", isSafeLocation: " + safeLocation);
            return lastUnlockTime == 0 && !safeLocation;
        }

        public void updateUnlockTime(long time) {
            ALEServer.this.updateUnlockTime(time);
        }
    }

}
