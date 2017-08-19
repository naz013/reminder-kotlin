package com.elementary.tasks.core;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.elementary.tasks.core.async.EnableThread;
import com.elementary.tasks.core.migration.MigrationTool;
import com.elementary.tasks.core.services.GcmListenerService;
import com.elementary.tasks.core.services.PermanentReminderService;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.intro.IntroActivity;
import com.elementary.tasks.navigation.MainActivity;
import com.google.firebase.messaging.FirebaseMessaging;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPrefs();
        if (Prefs.getInstance(this).isGcmEnabled()) {
            FirebaseMessaging.getInstance().subscribeToTopic(GcmListenerService.TOPIC_NAME);
        }
        if (Prefs.getInstance(this).isSbNotificationEnabled()) {
            startService(new Intent(this, PermanentReminderService.class).setAction(PermanentReminderService.ACTION_SHOW));
        }
    }

    private void checkIfAppUpdated() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (!Prefs.getInstance(this).getVersion(info.versionName)) {
                Prefs.getInstance(this).saveVersionBoolean(info.versionName);
                new EnableThread(this).start();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Prefs.getInstance(this).isMigrated()) {
            gotoApp();
        } else {
            new Thread(() -> {
                try {
                    MigrationTool.migrate(SplashScreen.this);
                } catch (Exception ignored) {
                }
                Prefs.getInstance(SplashScreen.this).setMigrated(true);
                runOnUiThread(this::gotoApp);
            }).start();
        }
    }

    private void gotoApp() {
        checkIfAppUpdated();
        if (!Prefs.getInstance(this).isUserLogged()) {
            openIntroScreen();
        } else {
            initGroups();
            runApplication();
        }
    }

    private void openIntroScreen() {
        startActivity(new Intent(this, IntroActivity.class));
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