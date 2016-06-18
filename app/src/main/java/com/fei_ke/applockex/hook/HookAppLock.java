package com.fei_ke.applockex.hook;

import android.graphics.Color;
import android.view.View;
import android.view.animation.TranslateAnimation;

import com.fei_ke.applockex.BuildConfig;
import com.fei_ke.applockex.Constants;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by fei-ke on 2016/1/23.
 * hook class
 */
public class HookAppLock implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static String MODULE_PATH = null;
    private static final boolean DEBUG = BuildConfig.DEBUG;


    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;

        ALEService.inject();

        XposedHelpers.findAndHookMethod("com.android.internal.app.AppLockPolicy", null, "isAppLockedPackage",
                String.class, new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean result = (boolean) param.getResult();
                        if (result) {
                            String pkgName = (String) param.args[0];
                            boolean shouldLock = ALEService.getService().isAppNeedLock(pkgName);
                            param.setResult(shouldLock);
                        }
                    }
                });
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(Constants.APP_LOCK_PKG_NAME)) {
            //hookLog(loadPackageParam);
            hookAppLock(loadPackageParam);
        }
    }

    //@Override
    //public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
    //    if (!Constants.APP_LOCK_PKG_NAME.equals(resparam.packageName)) return;
    //
    //    XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
    //    resparam.res.setReplacement(Constants.APP_LOCK_PKG_NAME, "anim", "in_from_bottom_to_top", modRes.fwd(R.anim.in_anim));
    //}

    private void hookAppLock(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod("com.samsung.android.applock.AppLockConfirmActivity",
                loadPackageParam.classLoader, "verifySuccess", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        //update unlock time
                        ALEService.getService().updateUnlockTime(System.currentTimeMillis());
                    }
                });

        XposedHelpers.findAndHookMethod("com.samsung.android.applock.checkservice.AppLockCheckService",
                loadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        //change lock view TranslateIn animation
                        TranslateAnimation inAnim = new TranslateAnimation(0, 0, 0, 0);
                        inAnim.setFillAfter(true);
                        inAnim.setFillAfter(true);
                        inAnim.setFillEnabled(true);
                        XposedHelpers.setObjectField(param.thisObject, "mTranslateIn", inAnim);
                    }
                });
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

        XposedHelpers.findAndHookMethod("com.samsung.android.applock.AppLockConfirmActivity",
                loadPackageParam.classLoader, "showAppInfor", new XC_MethodHook() {
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
}
