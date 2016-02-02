package com.fei_ke.applockex.hook;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.animation.TranslateAnimation;

import com.fei_ke.applockex.Constants;

import java.lang.ref.WeakReference;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by fei-ke on 2016/1/23.
 * hook class
 */
public class HookAppLock implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static String MODULE_PATH = null;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;

        XposedHelpers.findAndHookMethod(ActivityManager.class, "isAppLockedPackage", String.class,
                new XC_MethodHook() {
                    private WeakReference<Context> contextReference;

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean result = (boolean) param.getResult();
                        if (result) {
                            if (contextReference == null || contextReference.get() == null) {
                                Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                                contextReference = new WeakReference<Context>(context);
                                //XposedBridge.log("ActivityManager:Context= " + context);
                            }
                            String pkgName = (String) param.args[0];
                            boolean shouldLock = getShouldLockNextTime(contextReference.get(), pkgName);
                            param.setResult(shouldLock);
                        }
                    }
                });
        XposedHelpers.findAndHookMethod("com.android.internal.app.AppLockPolicy", null, "isAppLockedPackage", String.class,
                new XC_MethodHook() {
                    private Context context;

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        boolean result = (boolean) param.getResult();
                        if (result) {
                            if (context == null) {
                                context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                                //XposedBridge.log("AppLockPolicy:Context= " + context);
                            }

                            String pkgName = (String) param.args[0];
                            boolean shouldLock = getShouldLockNextTime(context, pkgName);
                            param.setResult(shouldLock);
                        }
                    }
                });


    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals(Constants.APP_LPCK_PKG_NAME)) {
            hookLog(loadPackageParam);
            hookAppLockFuc(loadPackageParam);
        }
    }

    //@Override
    //public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
    //    if (!Constants.APP_LPCK_PKG_NAME.equals(resparam.packageName)) return;
    //
    //    XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
    //    resparam.res.setReplacement(Constants.APP_LPCK_PKG_NAME, "anim", "in_from_bottom_to_top", modRes.fwd(R.anim.in_anim));
    //}

    private void hookAppLockFuc(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        XposedHelpers.findAndHookMethod("com.samsung.android.applock.AppLockConfirmActivity", loadPackageParam.classLoader, "verifySuccess",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        //update unlock time
                        Context context = (Context) param.thisObject;
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(Constants.KEY_LAST_UNLOCK_TIME, System.currentTimeMillis());
                        context.getContentResolver().update(Uri.parse(Constants.URI_UPDATE_UNLOCK_TIME), contentValues, null, null);
                    }
                });

        XposedHelpers.findAndHookMethod("com.samsung.android.applock.checkservice.AppLockCheckService", loadPackageParam.classLoader, "onCreate",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        TranslateAnimation inAnim = new TranslateAnimation(0, 0, 0, 0);
                        inAnim.setFillAfter(true);
                        inAnim.setFillAfter(true);
                        inAnim.setFillEnabled(true);
                        XposedHelpers.setObjectField(param.thisObject, "mTranslateIn", inAnim);
                    }
                });

    }


    private boolean getShouldLockNextTime(Context context, String packageName) {
        //XposedBridge.log(Process.myPid() + "-" + Process.myTid() + "-" + Process.myUid());

        Cursor cursor = context.getContentResolver().query(
                Uri.parse(Constants.URI_IS_APP_LOCK), null, packageName, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            boolean shouldLock = cursor.getInt(0) == 1;
            cursor.close();
            return shouldLock;
        } else {
            return true;
        }
    }

    private void hookLog(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //Class<?> classLog = XposedHelpers.findClass("android.util.secutil.Log", loadPackageParam.classLoader);
        //XposedHelpers.findAndHookMethod(classLog, "v", String.class, String.class, new XC_MethodHook() {
        //    @Override
        //    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        //        XposedBridge.log(param.args[0].toString() + " : " + param.args[1].toString());
        //    }
        //});
        //XposedHelpers.findAndHookMethod(classLog, "d", String.class, String.class, new XC_MethodHook() {
        //    @Override
        //    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        //        XposedBridge.log(param.args[0].toString() + " : " + param.args[1].toString());
        //    }
        //});
        //XposedHelpers.findAndHookMethod(classLog, "i", String.class, String.class, new XC_MethodHook() {
        //    @Override
        //    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        //        XposedBridge.log(param.args[0].toString() + " : " + param.args[1].toString());
        //    }
        //});
        //XposedHelpers.findAndHookMethod(classLog, "w", String.class, String.class, new XC_MethodHook() {
        //    @Override
        //    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        //        XposedBridge.log(param.args[0].toString() + " : " + param.args[1].toString());
        //    }
        //});

        //XposedHelpers.findAndHookMethod("com.samsung.android.applock.AppLockConfirmActivity", loadPackageParam.classLoader, "showAppInfor", new XC_MethodHook() {
        //    @Override
        //    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        //        View mWholeLayout = (View) XposedHelpers.getObjectField(param.thisObject, "mWholeLayout");
        //        mWholeLayout.setBackgroundColor(Color.GREEN);
        //        View mContentLayout = (View) XposedHelpers.getObjectField(param.thisObject, "mContentLayout");
        //        mContentLayout.setBackgroundColor(Color.BLUE);
        //        XposedBridge.log("设置背景颜色");
        //    }
        //});


    }


}
