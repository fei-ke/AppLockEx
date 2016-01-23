package com.fei_ke.applockex.hook;

import android.app.ActivityManager;
import android.graphics.Color;
import android.view.View;

import com.fei_ke.applockex.BuildConfig;
import com.fei_ke.applockex.Constants;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by fei-ke on 2016/1/23.
 * hook class
 */
public class HookAppLock implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private XSharedPreferences preferences;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(Constants.APP_LPCK_PKG_NAME)) {
            hookLog(loadPackageParam);
            //hook(loadPackageParam);
        }
    }


    private void hookLog(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> classLog = XposedHelpers.findClass("android.util.secutil.Log", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(classLog, "v", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(param.args[0].toString() + " : " + param.args[1].toString());
            }
        });
        XposedHelpers.findAndHookMethod(classLog, "d", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(param.args[0].toString() + " : " + param.args[1].toString());
            }
        });
        XposedHelpers.findAndHookMethod(classLog, "i", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(param.args[0].toString() + " : " + param.args[1].toString());
            }
        });
        XposedHelpers.findAndHookMethod(classLog, "w", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(param.args[0].toString() + " : " + param.args[1].toString());
            }
        });

        XposedHelpers.findAndHookMethod("com.samsung.android.applock.AppLockConfirmActivity", loadPackageParam.classLoader, "showAppInfor", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View mWholeLayout = (View) XposedHelpers.getObjectField(param.thisObject, "mWholeLayout");
                mWholeLayout.setBackgroundColor(Color.GREEN);
                View mContentLayout = (View) XposedHelpers.getObjectField(param.thisObject, "mContentLayout");
                mContentLayout.setBackgroundColor(Color.BLUE);
                XposedBridge.log("设置背景颜色");
            }
        });
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log("initZygote+++++++++++++++++++++++++");

        XposedHelpers.findAndHookMethod(ActivityManager.class, "isAppLockedPackage", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                hookIsAppLockedPackage(param);
            }
        });
        XposedHelpers.findAndHookMethod("com.android.internal.app.AppLockPolicy", null, "isAppLockedPackage", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                hookIsAppLockedPackage(param);
            }
        });

    }

    private void hookIsAppLockedPackage(XC_MethodHook.MethodHookParam param) {
        boolean result = (boolean) param.getResult();
        if (result && isSafeLocation()) {
            param.setResult(false);
        }
    }

    private boolean isSafeLocation() {
        if (preferences == null) {
            preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            preferences.makeWorldReadable();
        } else {
            preferences.reload();
        }
        return preferences.getBoolean(Constants.PRE_KEY_IS_SAFE_LOCATION, false);
    }
}
