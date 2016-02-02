package com.fei_ke.applockex;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 提供应用是否需要加锁,上一次解锁成功的时间等等
 * Created by fei on 16/2/2.
 */
public class ALEProvider extends ContentProvider {
    private static final String TAG = "ALEProvider";

    private long lastUnlockTime = 0;
    private boolean isSafeLocation = false;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate() called with: " + "");

        Intent intent = new Intent(getContext(), ALEServer.class);
        getContext().startService(intent);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (Constants.URI_IS_APP_LOCK.equals(uri.toString())) {
            Log.i(TAG, "query: selection : " + selection);
            MatrixCursor cursor = new MatrixCursor(new String[]{"isLocked"});
            cursor.newRow().add(isNeedLock() ? 1 : 0);
            return cursor;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String uriStr = uri.toString();
        if (Constants.URI_UPDATE_UNLOCK_TIME.equals(uriStr)) {
            this.lastUnlockTime = values.getAsLong(Constants.KEY_LAST_UNLOCK_TIME);
            Log.i(TAG, "update: lastUnlockTime : " + lastUnlockTime);
            return 1;
        } else if (Constants.URI_UPDATE_IS_SAFE_LOCATION.equals(uriStr)) {
            isSafeLocation = values.getAsBoolean(Constants.KEY_IS_SAFE_LOCATION);
            Log.i(TAG, "update: isSafeLocation : " + isSafeLocation);
            return 1;
        }
        return 0;
    }

    private synchronized boolean isNeedLock() {
        Log.i(TAG, "lastUnlockTime: " + lastUnlockTime + ",isSafeLocation: " + isSafeLocation);
        return lastUnlockTime == 0 && !isSafeLocation;
    }
}
