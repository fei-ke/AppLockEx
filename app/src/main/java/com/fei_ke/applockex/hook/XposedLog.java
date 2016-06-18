package com.fei_ke.applockex.hook;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by fei on 16/6/18.
 */
public class XposedLog {
    public static void log(String text) {
        XposedBridge.log(text);
    }

    public static void log(Throwable t) {
        XposedBridge.log(t);
    }
}
