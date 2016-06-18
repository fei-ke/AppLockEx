package com.fei_ke.applockex;

import android.content.Intent;
import android.os.Bundle;
import android.os.IALEService;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.fei_ke.applockex.hook.ALEService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        startService(new Intent(this, WearableService.class));

    }

    @Override
    protected void onResume() {
        super.onResume();

        IALEService service = ALEService.getService();
        Log.i(TAG, "onCreate: " + service);
        try {
            textView.setText("SafeLocation: " + service.isSafeLocation() +
                    "\nNeedLock: " + service.isAppNeedLock(getPackageName()));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
