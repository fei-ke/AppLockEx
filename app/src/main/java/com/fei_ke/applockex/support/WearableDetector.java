package com.fei_ke.applockex.support;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fei on 16/2/14.
 */
public class WearableDetector implements ISafeLocationDetector, GoogleApiClient.ConnectionCallbacks, NodeApi.NodeListener {
    private static final String TAG = "WearableDetector";

    private GoogleApiClient mGoogleApiClient;
    private List<Node> connectedNodes = new ArrayList<>();

    @Override
    public void start(Context context) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
        }
        tryConnectGoogleApi();
    }

    private void tryConnectGoogleApi() {
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void stop() {
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

    @Override
    public boolean isSafeLocation(String name) {
        Log.i(TAG, "isSafeLocation: " + connectedNodes);
        for (Node node : connectedNodes) {
            if (node.getDisplayName().equals(name) && node.isNearby()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getConnectedList() {
        List<String> list = new ArrayList<>();
        for (Node node : connectedNodes) {
            list.add(node.getDisplayName());
        }
        return list;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.NodeApi.addListener(mGoogleApiClient, this);

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                if (nodes != null) {
                    connectedNodes.clear();
                    connectedNodes.addAll(nodes);
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectedNodes.clear();
    }

    @Override
    public void onPeerConnected(Node node) {
        Log.d(TAG, "onPeerConnected() called with: " + "node = [" + node + "]");
        connectedNodes.add(node);
    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d(TAG, "onPeerDisconnected() called with: " + "node = [" + node + "]");
        connectedNodes.remove(node);
    }
}
