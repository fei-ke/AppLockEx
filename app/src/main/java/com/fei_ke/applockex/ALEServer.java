package com.fei_ke.applockex;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

/**
 * 主要检测屏幕关闭事件,更新解锁时间
 * Created by fei on 16/2/2.
 */
public class ALEServer extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "ALEServer";
    private BroadcastReceiver mReceiver;
    private GoogleApiClient mGoogleApiClient;

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
    public void onConnectedNodes(List<Node> connectedNodes) {
        Log.d(TAG, "onConnectedNodes() called with: " + "connectedNodes = [" + connectedNodes + "]");
        super.onConnectedNodes(connectedNodes);
        for (Node node : connectedNodes) {
            isSafeLocation(node);
        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        Log.d(TAG, "onPeerConnected() called with: " + "peer = [" + peer + "]");
        super.onPeerConnected(peer);
        isSafeLocation(peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        Log.d(TAG, "onPeerDisconnected() called with: " + "peer = [" + peer + "]");
        super.onPeerDisconnected(peer);
        isSafeLocation(peer);
    }

    @Override
    public void onConnected(Bundle bundle) {
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

    private void updateIsSafeLocation(boolean isSafeLocation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.KEY_IS_SAFE_LOCATION, isSafeLocation);
        getContentResolver().update(Uri.parse(Constants.URI_UPDATE_IS_SAFE_LOCATION), contentValues, null, null);
    }


    private static class ScreenOffDetector extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.KEY_LAST_UNLOCK_TIME, 0);
            context.getContentResolver().update(Uri.parse(Constants.URI_UPDATE_UNLOCK_TIME), contentValues, null, null);
        }
    }

}
