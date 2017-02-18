package com.elementary.tasks.navigation;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.async.BackupSettingTask;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Recognize;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.roboto.RoboTextView;
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
import com.elementary.tasks.navigation.settings.images.MainImageActivity;
import com.elementary.tasks.navigation.settings.images.SaveAsync;
import com.elementary.tasks.notes.QuickNoteCoordinator;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentCallback {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int PRESS_AGAIN_TIME = 2000;
    private static final String TAG = "MainActivity";
    private static final String CURRENT_SCREEN = "current_screen";

    private ActivityMainBinding binding;
    private Toolbar toolbar;
    private ImageView mMainImageView;
    private NavigationView mNavigationView;
    private Fragment fragment;
    private QuickNoteCoordinator mNoteView;

    private int prevItem;
    private int beforeSettings;
    private boolean isBackPressed;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.fab.setOnLongClickListener(view -> {
            mNoteView.switchQuickNote();
            return true;
        });
        initActionBar();
        initNavigation();
        mNoteView = new QuickNoteCoordinator(this, binding);
        if (savedInstanceState != null) {
            openScreen(savedInstanceState.getInt(CURRENT_SCREEN, R.id.nav_current));
        } else if (getIntent().getIntExtra(Constants.INTENT_POSITION, 0) != 0) {
            prevItem = getIntent().getIntExtra(Constants.INTENT_POSITION, 0);
            mNavigationView.setCheckedItem(prevItem);
            openScreen(prevItem);
        } else {
            initStartFragment();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_SCREEN, prevItem);
        super.onSaveInstanceState(outState);
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
        if (mPrefs.isUiChanged()) {
            mPrefs.setUiChanged(false);
            recreate();
        }
        if (!mPrefs.isBetaWarmingShowed()) {
            showBetaDialog();
        }
        if (isRateDialogShowed()) {
            showRateDialog();
        }
        showMainImage();
    }

    private boolean isRateDialogShowed() {
        int count = mPrefs.getRateCount();
        count++;
        mPrefs.setRateCount(count);
        return count == 10;
    }

    private void showRateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rate);
        builder.setMessage(R.string.can_you_rate_this_application);
        builder.setPositiveButton(R.string.rate, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            SuperUtil.launchMarket(MainActivity.this);
        });
        builder.setNegativeButton(R.string.never, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setNeutralButton(R.string.later, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            mPrefs.setRateCount(0);
        });
        builder.create().show();
    }

    private void showBetaDialog() {
        mPrefs.setBetaWarmingShowed(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Beta");
        builder.setMessage("This version of application may work unstable!");
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void showMainImage() {
        String path = mPrefs.getImagePath();
        if (!path.isEmpty()) {
            String fileName = path;
            if (path.contains("=")) {
                int index = path.indexOf("=");
                fileName = path.substring(index);
            }
            File file = new File(MemoryUtil.getImageCacheDir(), fileName + ".jpg");
            boolean readPerm = Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            if (readPerm && file.exists()) {
                Picasso.with(this)
                        .load(file)
                        .into(mMainImageView);
                mMainImageView.setVisibility(View.VISIBLE);
            } else {
                Picasso.with(this)
                        .load(path)
                        .into(mMainImageView);
                mMainImageView.setVisibility(View.VISIBLE);
                if (readPerm) new SaveAsync(this).execute(path);
            }
        } else mMainImageView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPrefs.isAutoBackupEnabled() && mPrefs.isSettingsBackupEnabled()
                && Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
            new BackupSettingTask(this).execute();
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
            binding.fab.setOnClickListener(view -> {
                if (mNoteView.isNoteVisible()) mNoteView.hideNoteView();
                listener.onClick(view);
            });
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

    @Override
    public void onVoiceAction() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, false);
    }

    @Override
    public void onMenuSelect(int menu) {
        prevItem = menu;
        mNavigationView.setCheckedItem(prevItem);
    }

    private void initNavigation() {
        DrawerLayout drawer = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = binding.navView;
        mNavigationView.setNavigationItemSelectedListener(this);
        View view = mNavigationView.getHeaderView(0);
        mMainImageView = (ImageView) view.findViewById(R.id.headerImage);
        mMainImageView.setOnClickListener(view1 -> openImageScreen());
        view.findViewById(R.id.headerItem).setOnClickListener(view12 -> openImageScreen());
        RoboTextView nameView = (RoboTextView) view.findViewById(R.id.appNameBanner);
        String appName = getString(R.string.app_name);
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
        }
        nameView.setText(appName.toUpperCase());
        setMenuVisible();
    }

    private void openImageScreen() {
        startActivity(new Intent(this, MainImageActivity.class));
    }

    private void setMenuVisible() {
        Menu menu = mNavigationView.getMenu();
        menu.getItem(4).setVisible(Google.getInstance(this) != null);
        if (!Module.isPro() && !SuperUtil.isAppInstalled(this, "com.cray.software.justreminderpro")){
            menu.getItem(13).setVisible(true);
        } else {
            menu.getItem(13).setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = binding.drawerLayout;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mNoteView.isNoteVisible()) {
            mNoteView.hideNoteView();
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
                    prevItem = beforeSettings;
                    mNavigationView.setCheckedItem(beforeSettings);
                    openScreen(beforeSettings);
                } else {
                    initStartFragment();
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
        new Handler().postDelayed(() -> {
            if (prevItem == item.getItemId() && (item.getItemId() != R.id.nav_feedback ||
                    item.getItemId() != R.id.nav_help && item.getItemId() != R.id.nav_pro)) {
                return;
            }
            openScreen(item.getItemId());
            if (item.getItemId() != R.id.nav_feedback && item.getItemId() != R.id.nav_help && item.getItemId() != R.id.nav_pro) {
                prevItem = item.getItemId();
            }
        }, 250);
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
            case R.id.nav_pro:
                showProDialog();
                break;
        }
    }

    private void openMarket() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + "com.cray.software.justreminderpro"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.could_not_launch_market, Toast.LENGTH_SHORT).show();
        }
    }

    private void showProDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.buy_pro))
                .setMessage(getString(R.string.pro_advantages) + "\n" +
                        getString(R.string.different_settings_for_birthdays) + "\n" +
                        getString(R.string.additional_reminder) + "\n" +
                        getString(R.string._led_notification_) + "\n" +
                        getString(R.string.led_color_for_each_reminder) + "\n" +
                        getString(R.string.styles_for_marker) + "\n" +
                        getString(R.string.option_for_image_blurring) + "\n" +
                        getString(R.string.additional_app_themes))
                .setPositiveButton(R.string.buy, (dialog, which) -> {
                    dialog.dismiss();
                    openMarket();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .create().show();
    }
}
