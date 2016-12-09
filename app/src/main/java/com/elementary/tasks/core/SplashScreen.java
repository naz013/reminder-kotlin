package com.elementary.tasks.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.navigation.MainActivity;

import java.util.Random;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPrefs();
        initGroups();
        runApplication();
    }

    private void initGroups() {
        if (RealmDb.getInstance().getAllGroups().size() == 0) {
            addDefaultGroups();
        }
    }

    private void addDefaultGroups() {
        Random random = new Random();
        RealmDb.getInstance().saveObject(new GroupItem("General", random.nextInt(16)));
        RealmDb.getInstance().saveObject(new GroupItem("Work", random.nextInt(16)));
        RealmDb.getInstance().saveObject(new GroupItem("Personal", random.nextInt(16)));
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