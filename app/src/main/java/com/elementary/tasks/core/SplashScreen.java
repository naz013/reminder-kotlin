package com.elementary.tasks.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.navigation.MainActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPrefs();
        runApplication();
    }

    private void initPrefs() {
        Prefs prefs = Prefs.getInstance(this);
        prefs.initPrefs(this);
        prefs.checkPrefs();
    }

    private void runApplication() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}