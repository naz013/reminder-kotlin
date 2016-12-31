package com.elementary.tasks.navigation;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.cloud.GoogleTasks;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.Recognize;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.databinding.ActivityMainBinding;
import com.elementary.tasks.navigation.fragments.ArchiveFragment;
import com.elementary.tasks.navigation.fragments.BackupsFragment;
import com.elementary.tasks.navigation.fragments.CalendarFragment;
import com.elementary.tasks.navigation.fragments.DayViewFragment;
import com.elementary.tasks.navigation.fragments.FeedbackFragment;
import com.elementary.tasks.navigation.fragments.FragmentCallback;
import com.elementary.tasks.navigation.fragments.GoogleTasksFragment;
import com.elementary.tasks.navigation.fragments.GroupsFragment;
import com.elementary.tasks.navigation.fragments.HelpFragment;
import com.elementary.tasks.navigation.fragments.MapFragment;
import com.elementary.tasks.navigation.fragments.NotesFragment;
import com.elementary.tasks.navigation.fragments.PlacesFragment;
import com.elementary.tasks.navigation.fragments.RemindersFragment;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;
import com.elementary.tasks.navigation.settings.SettingsFragment;

import java.util.ArrayList;

public class MainActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentCallback {

    private static final int PRESS_AGAIN_TIME = 2000;
    private static final String TAG = "MainActivity";
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;

    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private NavigationView mNavigationView;
    private Fragment fragment;

    private int prevItem;
    private int beforeSettings;
    private boolean isBackPressed;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initActionBar();
        initNavigation();
        initStartFragment();
    }

    private void initStartFragment() {
        prevItem = R.id.nav_current;
        mNavigationView.setCheckedItem(prevItem);
        replaceFragment(new RemindersFragment(), getString(R.string.events));
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

    @Override
    public void onThemeChange(@ColorInt int primary, @ColorInt int primaryDark, @ColorInt int accent) {
        if (primary == 0) primary = themeUtil.getColor(themeUtil.colorPrimary());
        if (primaryDark == 0) primaryDark = themeUtil.getColor(themeUtil.colorPrimaryDark());
        if (accent == 0) accent = themeUtil.getColor(themeUtil.colorAccent());
        toolbar.setBackgroundColor(primary);
        if (Module.isLollipop()) getWindow().setStatusBarColor(primaryDark);
        binding.fab.setBackgroundTintList(ViewUtils.getFabState(accent, accent));
    }

    @Override
    public void refreshMenu() {
        setMenuVisible();
    }

    private void initNavigation() {
        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = binding.navView;
        mNavigationView.setNavigationItemSelectedListener(this);
        setMenuVisible();
    }

    private void setMenuVisible() {
        Menu menu = mNavigationView.getMenu();
        menu.getItem(4).setVisible(new GoogleTasks(this).isLinked());
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
            if (fragment instanceof SettingsFragment) {
                if (beforeSettings != 0) {
                    mNavigationView.setCheckedItem(beforeSettings);
                    openScreen(beforeSettings);
                } else if (!isBackPressed) {
                    firstBackPress();
                }
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
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            new Recognize(this).parseResults(matches, false);
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (fragment != null) fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Module.isMarshmallow() && fragment != null) fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = binding.drawerLayout;
        drawer.closeDrawer(GravityCompat.START);
        if (prevItem == item.getItemId() && (item.getItemId() != R.id.nav_feedback ||
                item.getItemId() != R.id.nav_help)) {
            return false;
        }
        openScreen(item.getItemId());
        if (item.getItemId() != R.id.nav_feedback && item.getItemId() != R.id.nav_help) {
            prevItem = item.getItemId();
        }
        return true;
    }

    private void openScreen(int itemId) {
        beforeSettings = 0;
        switch (itemId) {
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
                beforeSettings = prevItem;
                replaceFragment(new SettingsFragment(), getString(R.string.settings));
                break;
            case R.id.nav_feedback:
                replaceFragment(new FeedbackFragment(), getString(R.string.feedback));
                break;
            case R.id.nav_help:
                replaceFragment(new HelpFragment(), getString(R.string.help));
                break;
        }
    }
}
