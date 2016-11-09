package com.elementary.tasks.navigation;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.databinding.ActivityMainBinding;
import com.elementary.tasks.navigation.fragments.ArchiveFragment;
import com.elementary.tasks.navigation.fragments.BackupsFragment;
import com.elementary.tasks.navigation.fragments.CalendarFragment;
import com.elementary.tasks.navigation.fragments.DayViewFragment;
import com.elementary.tasks.navigation.fragments.FragmentCallback;
import com.elementary.tasks.navigation.fragments.GoogleTasksFragment;
import com.elementary.tasks.navigation.fragments.GroupsFragment;
import com.elementary.tasks.navigation.fragments.MapFragment;
import com.elementary.tasks.navigation.fragments.NotesFragment;
import com.elementary.tasks.navigation.fragments.PlacesFragment;
import com.elementary.tasks.navigation.fragments.RemindersFragment;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;
import com.elementary.tasks.navigation.settings.SettingsFragment;

public class MainActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentCallback {

    private static final int PRESS_AGAIN_TIME = 2000;
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private NavigationView mNavigationView;
    private Fragment fragment;

    private int prevItem;
    private boolean isBackPressed;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initActionBar();
        initNavigation();
    }

    private void initActionBar() {
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    public void replaceFragment(Fragment fragment, String title) {
        this.fragment = fragment;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.main_container, fragment, title);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(title);
        ft.commit();
        toolbar.setTitle(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Prefs.getInstance(this).isUiChanged()) {
            Prefs.getInstance(this).setUiChanged(false);
            recreate();
        }
    }

    @Override
    public void onTitleChange(String title) {
        toolbar.setTitle(title);
    }

    @Override
    public void onFragmentSelect(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void setClick(View.OnClickListener listener) {
        if (listener == null) {
            hideFab();
        } else {
            showFab();
            binding.fab.setOnClickListener(listener);
        }
    }

    private void initNavigation() {
        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = binding.navView;
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = binding.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (isBackPressed) {
                if (System.currentTimeMillis() - pressedTime < PRESS_AGAIN_TIME) {
                    finish();
                } else {
                    isBackPressed = false;
                    onBackPressed();
                }
            }
            if (fragment instanceof SettingsFragment && !isBackPressed) {
                firstBackPress();
            } else if (fragment instanceof BaseSettingsFragment) {
                super.onBackPressed();
            } else if (!isBackPressed) {
                firstBackPress();
            }
        }
    }

    private void firstBackPress() {
        isBackPressed = true;
        pressedTime = System.currentTimeMillis();
        Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
    }

    private void showFab() {
        if (binding.fab.getVisibility() != View.VISIBLE) {
            binding.fab.setVisibility(View.VISIBLE);
        }
    }

    private void hideFab() {
        if (binding.fab.getVisibility() != View.GONE) {
            binding.fab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fragment != null) fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = binding.drawerLayout;
        drawer.closeDrawer(GravityCompat.START);
        if (prevItem == item.getItemId() && (item.getItemId() != R.id.nav_feedback ||
                item.getItemId() != R.id.nav_help)) {
            return false;
        }
        if (item.getItemId() != R.id.nav_feedback && item.getItemId() != R.id.nav_help) {
            prevItem = item.getItemId();
        }
        switch (item.getItemId()) {
            case R.id.nav_current:
                replaceFragment(new RemindersFragment(), getString(R.string.tasks));
                break;
            case R.id.nav_notes:
                replaceFragment(new NotesFragment(), getString(R.string.notes));
                break;
            case R.id.nav_calendar:
                replaceFragment(new CalendarFragment(), getString(R.string.calendar));
                break;
            case R.id.nav_day_view:
                replaceFragment(new DayViewFragment(), getString(R.string.events));
                break;
            case R.id.nav_tasks:
                replaceFragment(new GoogleTasksFragment(), getString(R.string.google_tasks));
                break;
            case R.id.nav_groups:
                replaceFragment(new GroupsFragment(), getString(R.string.groups));
                break;
            case R.id.nav_map:
                replaceFragment(new MapFragment(), getString(R.string.map));
                break;
            case R.id.nav_places:
                replaceFragment(new PlacesFragment(), getString(R.string.places));
                break;
            case R.id.nav_backups:
                replaceFragment(new BackupsFragment(), getString(R.string.backup_files));
                break;
            case R.id.nav_archive:
                replaceFragment(new ArchiveFragment(), getString(R.string.trash));
                break;
            case R.id.nav_settings:
                replaceFragment(new SettingsFragment(), getString(R.string.settings));
                break;
            case R.id.nav_feedback:
                replaceFragment(new DayViewFragment(), getString(R.string.feedback));
                break;
            case R.id.nav_help:
                replaceFragment(new DayViewFragment(), getString(R.string.help));
                break;
        }
        return true;
    }
}
