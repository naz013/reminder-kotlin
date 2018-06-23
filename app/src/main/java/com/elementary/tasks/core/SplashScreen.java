package com.elementary.tasks.core;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.elementary.tasks.core.async.EnableThread;
import com.elementary.tasks.core.data.AppDb;
import com.elementary.tasks.core.services.PermanentReminderReceiver;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.groups.GroupsUtil;
import com.elementary.tasks.intro.IntroActivity;
import com.elementary.tasks.navigation.MainActivity;

import androidx.annotation.Nullable;

public class SplashScreen extends ThemedActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPrefs();
        if (Prefs.getInstance(this).isSbNotificationEnabled()) {
            Notifier.updateReminderPermanent(this, PermanentReminderReceiver.ACTION_SHOW);
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
        gotoApp();
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
        if (AppDb.getAppDatabase(this).groupDao().getAll().size() == 0) {
            GroupsUtil.initDefault(this);
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