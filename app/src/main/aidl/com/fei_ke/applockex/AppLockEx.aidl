// AppLockEx.aidl
package com.fei_ke.applockex;


interface AppLockEx {
    boolean isAppNeedLock(String pkgName);
    void updateUnlockTime(long time);
}
