package com.fei_ke.applockex;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * 主要检测屏幕关闭事件,更新解锁时间
 * Created by fei on 16/2/2.
 */
public class ALEServer extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        NodeApi.NodeListener {
    private static final String TAG = "ALEServer";
    private BroadcastReceiver mReceiver;
    private GoogleApiClient mGoogleApiClient;

    private long lastUnlockTime = 0;
    private boolean isSafeLocation = false;

    private static final String MY_WATCH_NAME = "HUAWEI WATCH 0C14";

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        tryConnectGoogleApi();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AppLockEx.Stub() {

            @Override
            public boolean isAppNeedLock(String pkgName) throws RemoteException {
                Log.d(TAG, "isAppNeedLock() called with: " + "pkgName = [" + pkgName + "]");
                Log.i(TAG, "lastUnlockTime: " + lastUnlockTime + " isSafeLocation: " + isSafeLocation);
                Log.d(TAG, "isAppNeedLock() returned: " + (lastUnlockTime == 0 && !isSafeLocation));
                return lastUnlockTime == 0 && !isSafeLocation;
            }

            @Override
            public void updateUnlockTime(long time) throws RemoteException {
                ALEServer.this.updateUnLockTime(time);
            }
        };
    }

    protected void tryConnectGoogleApi() {
        Log.d(TAG, "tryConnectGoogleApi() called with: " + "");
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: " + "intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        if (mReceiver == null) {
            mReceiver = new ScreenOffDetector();
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mReceiver, filter);
        }
        tryConnectGoogleApi();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.d(TAG, "onPeerConnected() called with: " + "peer = [" + peer + "]");
        isSafeLocation(peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.d(TAG, "onPeerDisconnected() called with: " + "peer = [" + peer + "]");
        isSafeLocation(peer);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.NodeApi.addListener(mGoogleApiClient, this);

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                for (Node node : nodes) {
                    isSafeLocation(node);
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        updateIsSafeLocation(false);
    }

    private void isSafeLocation(Node node) {
        if (node.getDisplayName().equals(MY_WATCH_NAME)) {
            updateIsSafeLocation(node.isNearby());
        }
    }

    void updateUnLockTime(long time) {
        Log.d(TAG, "updateUnLockTime() called with: " + "time = [" + time + "]");

        lastUnlockTime = time;

        //ContentValues contentValues = new ContentValues();
        //contentValues.put(Constants.KEY_LAST_UNLOCK_TIME, time);
        //getContentResolver().update(Uri.parse(Constants.URI_UPDATE_UNLOCK_TIME), contentValues, null, null);
    }

    private void updateIsSafeLocation(boolean isSafeLocation) {
        this.isSafeLocation = isSafeLocation;

        //ContentValues contentValues = new ContentValues();
        //contentValues.put(Constants.KEY_IS_SAFE_LOCATION, isSafeLocation);
        //getContentResolver().update(Uri.parse(Constants.URI_UPDATE_IS_SAFE_LOCATION), contentValues, null, null);
    }


    private class ScreenOffDetector extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUnLockTime(0);
        }
    }

}
