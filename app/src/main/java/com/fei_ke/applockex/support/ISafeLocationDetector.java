package com.fei_ke.applockex.support;

import android.content.Context;

import java.util.List;

/**
 * 检测
 * Created by fei on 16/2/14.
 */
public interface ISafeLocationDetector {
    int TYPE_WEARABLE = 0;
    int TYPE_WIFI = 1;
    int TYPE_AUDIO = 2;

    void start(Context context);

    void stop();

    boolean isSafeLocation(String name);

    List<String> getConnectedList();

}
