// AppLockEx.aidl
package android.os;


interface IALEService {
    boolean isAppNeedLock(String pkgName);
    void updateUnlockTime(long time);
    void updateIsSafeLocation(boolean isSafeLocation);
    boolean isSafeLocation();
}
