package com.fei_ke.applockex;

/**
 * 一些常量
 * Created by fei-ke on 2016/1/23.
 */
public class Constants {
    public static final String APP_LPCK_PKG_NAME = "com.samsung.android.applock";

    public static final String KEY_IS_SAFE_LOCATION = "is_safe_location";


    public static final String CONTENT_URI = "content://com.fei_ke.applockex.ALEProvider";

    public static final String URI_IS_APP_LOCK = CONTENT_URI + "/isAppLocked";

    public static final String URI_UPDATE_UNLOCK_TIME = CONTENT_URI + "/updateUnlockTime";
    public static final String URI_UPDATE_IS_SAFE_LOCATION = CONTENT_URI + "/updateIsSafeLocation";

    public static final String KEY_LAST_UNLOCK_TIME = "last_unlock_time";
}
