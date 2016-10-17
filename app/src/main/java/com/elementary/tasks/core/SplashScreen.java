package com.elementary.tasks.core;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.elementary.tasks.navigation.MainActivity;
import com.elementary.tasks.core.utils.Prefs;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPrefs();
        runApplication();
    }

    private void initPrefs() {
        Prefs prefs = Prefs.getInstance(this);
        if (!prefs.hasScreenOrientation()) prefs.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        if (!prefs.hasAppTheme()) prefs.setAppTheme(8);
    }

    private void runApplication() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}