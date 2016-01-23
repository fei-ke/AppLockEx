package com.fei_ke.applockex;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences preferences = getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_WORLD_READABLE);

        SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id.buttonTest);

        switchCompat.setChecked(preferences.getBoolean(Constants.PRE_KEY_IS_SAFE_LOCATION, false));

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean(Constants.PRE_KEY_IS_SAFE_LOCATION, isChecked).commit();
            }
        });
    }
}
