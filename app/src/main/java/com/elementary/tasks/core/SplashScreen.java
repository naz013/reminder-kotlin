package com.elementary.tasks.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.elementary.tasks.core.services.PermanentBirthdayService;
import com.elementary.tasks.core.services.PermanentReminderService;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.navigation.MainActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPrefs();
        initGroups();
        runApplication();
        if (Prefs.getInstance(this).isSbNotificationEnabled()) {
            startService(new Intent(this, PermanentReminderService.class).setAction(PermanentReminderService.ACTION_SHOW));
        }
        if (Prefs.getInstance(this).isBirthdayPermanentEnabled()) {
            startService(new Intent(this, PermanentBirthdayService.class).setAction(PermanentBirthdayService.ACTION_SHOW));
        }
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