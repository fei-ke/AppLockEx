package com.fei_ke.applockex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fei_ke.applockex.hook.ALEServiceProxy;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ALEServer.class));
        ALEServiceProxy proxy = new ALEServiceProxy(getApplication());
        Log.i(TAG, "onCreate: " + proxy.isAppNeedLock("xxx"));
    }
}
