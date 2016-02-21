package com.fei_ke.applockex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fei_ke.applockex.support.WearableDetector;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private WearableDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ALEServer.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detector.stop();
    }
}
