package com.elementary.tasks.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.elementary.tasks.core.services.GcmListenerService;
import com.elementary.tasks.core.services.PermanentReminderService;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.login.LoginActivity;
import com.elementary.tasks.navigation.MainActivity;
import com.google.firebase.messaging.FirebaseMessaging;

import io.fabric.sdk.android.Fabric;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        initPrefs();
        if (Prefs.getInstance(this).isGcmEnabled()) {
            FirebaseMessaging.getInstance().subscribeToTopic(GcmListenerService.TOPIC_NAME);
        }
        if (Prefs.getInstance(this).isSbNotificationEnabled()) {
            startService(new Intent(this, PermanentReminderService.class).setAction(PermanentReminderService.ACTION_SHOW));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Prefs.getInstance(this).isUserLogged()) {
            openLoginScreen();
        } else {
            initGroups();
            runApplication();
        }
    }

    private void openLoginScreen() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void initGroups() {
        if (RealmDb.getInstance().getAllGroups().size() == 0) {
            RealmDb.getInstance().setDefaultGroups(this);
        }
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