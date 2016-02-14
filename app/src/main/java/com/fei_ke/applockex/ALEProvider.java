package com.fei_ke.applockex;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 提供应用是否需要加锁,上一次解锁成功的时间等等
 * Created by fei on 16/2/2.
 */
public class ALEProvider extends ContentProvider {
    private static final String TAG = "ALEProvider";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private long lastUnlockTime = 0;
    private boolean isSafeLocation = false;

    @Override
    public boolean onCreate() {
        if (DEBUG) {
            Log.d(TAG, "onCreate() called with: " + "");
        }

        Intent intent = new Intent(getContext(), ALEServer.class);
        if (getContext() != null) {
            getContext().startService(intent);
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (Constants.URI_IS_APP_LOCK.equals(uri.toString())) {
            if (DEBUG) {
                Log.i(TAG, "query: selection : " + selection);
            }
            MatrixCursor cursor = new MatrixCursor(new String[]{"isLocked"});
            cursor.newRow().add(isNeedLock() ? 1 : 0);
            return cursor;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String uriStr = uri.toString();
        if (Constants.URI_UPDATE_UNLOCK_TIME.equals(uriStr)) {
            this.lastUnlockTime = values.getAsLong(Constants.KEY_LAST_UNLOCK_TIME);
            if (DEBUG) {
                Log.i(TAG, "update: lastUnlockTime : " + lastUnlockTime);
            }
            return 1;
        } else if (Constants.URI_UPDATE_IS_SAFE_LOCATION.equals(uriStr)) {
            isSafeLocation = values.getAsBoolean(Constants.KEY_IS_SAFE_LOCATION);
            if (DEBUG) {
                Log.i(TAG, "update: isSafeLocation : " + isSafeLocation);
            }
            return 1;
        }
        return 0;
    }

    private synchronized boolean isNeedLock() {
        if (DEBUG) {
            Log.i(TAG, "lastUnlockTime: " + lastUnlockTime + ",isSafeLocation: " + isSafeLocation);
        }
        return lastUnlockTime == 0 && !isSafeLocation;
    }
}
