package com.fei_ke.applockex.hook;

import android.content.Context;
import android.os.Build;
import android.os.IALEService;
import android.os.IBinder;
import android.os.RemoteException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by fei on 16/6/18.
 */
public class ALEService extends IALEService.Stub {
    public static final String SERVICE_NAME = "packagename.service";
    private static IALEService service;

    private final Context context;

    private long lastUnlockTime = 0;
    private boolean isSafeLocation = false;

    public static void inject() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("android.app.ActivityThread", null), "systemMain",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            ClassLoader loader = Thread.currentThread().getContextClassLoader();

                            XposedBridge.hookAllMethods(
                                    XposedHelpers.findClass("com.android.server.am.ActivityManagerService", loader),
                                    "setSystemProcess", new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param) {
                                            try {
                                                register(getContext(param.thisObject));
                                            } catch (Throwable ex) {
                                                XposedLog.log("add ALEService fail!!");
                                            }
                                        }
                                    });
                        }
                    });
        } else {
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActivityManagerService", null),
                    "main", new XC_MethodHook() {
                        @Override
                        protected final void afterHookedMethod(final MethodHookParam param) {
                            register((Context) param.getResult());
                        }
                    });
        }
    }

    private static void register(Context context) {
        try {
            Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
            Method addService = ServiceManager.getDeclaredMethod("addService", String.class, IBinder.class);
            addService.invoke(null, SERVICE_NAME, new ALEService(context));
            XposedLog.log("add ALEService success!!");
        } catch (Throwable ex) {
            XposedLog.log("add ALEService fail!!");
        }
    }

    private static Context getContext(Object object) throws NoSuchFieldException, IllegalAccessException {
        Field mContext = object.getClass().getDeclaredField("mContext");
        mContext.setAccessible(true);
        return (Context) mContext.get(object);
    }

    public static IALEService getService() {
        if (service == null) {
            try {
                Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
                Method getService = ServiceManager.getDeclaredMethod("getService", String.class);
                service = IALEService.Stub.asInterface((IBinder) getService.invoke(null, SERVICE_NAME));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        return service;
    }

    public ALEService(Context context) {
        this.context = context;
    }

    @Override
    public boolean isAppNeedLock(String pkgName) throws RemoteException {
        return lastUnlockTime == 0 && !isSafeLocation;
    }

    @Override
    public void updateUnlockTime(long time) throws RemoteException {
        lastUnlockTime = time;
    }

    @Override
    public void updateIsSafeLocation(boolean isSafeLocation) throws RemoteException {
        this.isSafeLocation = isSafeLocation;
    }

    @Override
    public boolean isSafeLocation() throws RemoteException {
        return isSafeLocation;
    }

}
