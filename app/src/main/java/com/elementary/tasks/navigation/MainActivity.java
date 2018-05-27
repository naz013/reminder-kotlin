package com.elementary.tasks.navigation;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.async.BackupSettingTask;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Recognize;
import com.elementary.tasks.core.utils.RemotePrefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.FilterView;
import com.elementary.tasks.core.views.ReturnScrollListener;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ActivityMainBinding;
import com.elementary.tasks.reminder.lists.ArchiveFragment;
import com.elementary.tasks.navigation.fragments.BackupsFragment;
import com.elementary.tasks.navigation.fragments.CalendarFragment;
import com.elementary.tasks.navigation.fragments.DayViewFragment;
import com.elementary.tasks.navigation.fragments.FeedbackFragment;
import com.elementary.tasks.navigation.fragments.GoogleTasksFragment;
import com.elementary.tasks.groups.list.GroupsFragment;
import com.elementary.tasks.navigation.fragments.HelpFragment;
import com.elementary.tasks.navigation.fragments.MapFragment;
import com.elementary.tasks.navigation.fragments.NotesFragment;
import com.elementary.tasks.places.list.PlacesFragment;
import com.elementary.tasks.reminder.lists.RemindersFragment;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;
import com.elementary.tasks.navigation.settings.SettingsFragment;
import com.elementary.tasks.navigation.settings.images.MainImageActivity;
import com.elementary.tasks.navigation.settings.images.SaveAsync;
import com.elementary.tasks.notes.QuickNoteCoordinator;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends ThemedActivity implements NavigationView.OnNavigationItemSelectedListener,
        FragmentCallback, RemotePrefs.SaleObserver, RemotePrefs.UpdateObserver {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int PRESS_AGAIN_TIME = 2000;
    private static final String CURRENT_SCREEN = "current_screen";

    private ActivityMainBinding binding;
    private ImageView mMainImageView;
    private RoboTextView mSaleBadge;
    private RoboTextView mUpdateBadge;
    private NavigationView mNavigationView;
    @Nullable
    private Fragment fragment;
    private QuickNoteCoordinator mNoteView;
    private ReturnScrollListener returnScrollListener;
    private RecyclerView.OnScrollListener listener;
    private RecyclerView mPrevList;

    private int prevItem;
    private int beforeSettings;
    private boolean isBackPressed;
    private long pressedTime;

    private QuickNoteCoordinator.Callback mQuickCallback = new QuickNoteCoordinator.Callback() {
        @Override
        public void onOpen() {
            binding.fab.setImageResource(R.drawable.ic_clear_white_24dp);
        }

        @Override
        public void onClose() {
            binding.fab.setImageResource(R.drawable.ic_add_white_24dp);
        }
    };

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
        mNoteView = new QuickNoteCoordinator(this, binding, mQuickCallback);
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
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        binding.toolbar.setNavigationOnClickListener(v -> onDrawerClick());
    }

    private void onDrawerClick() {
        if (this.fragment instanceof BaseSettingsFragment) {
            onBackPressed();
        } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void replaceFragment(Fragment fragment, String title) {
        this.fragment = fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_container, fragment, title);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(title);
        ft.commit();
        binding.toolbar.setTitle(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getPrefs().isUiChanged()) {
            getPrefs().setUiChanged(false);
            recreate();
        }
        if (!getPrefs().isBetaWarmingShowed()) {
            showBetaDialog();
        }
        if (isRateDialogShowed()) {
            showRateDialog();
        }
        showMainImage();
        RemotePrefs.getInstance(this).addUpdateObserver(this);
        if (!Module.isPro()) {
            RemotePrefs.getInstance(this).addSaleObserver(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!Module.isPro()) {
            RemotePrefs.getInstance(this).removeSaleObserver(this);
        }
        RemotePrefs.getInstance(this).removeUpdateObserver(this);
    }

    private boolean isRateDialogShowed() {
        int count = getPrefs().getRateCount();
        count++;
        getPrefs().setRateCount(count);
        return count == 10;
    }

    private void showRateDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(R.string.rate);
        builder.setMessage(R.string.can_you_rate_this_application);
        builder.setPositiveButton(R.string.rate, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            SuperUtil.launchMarket(MainActivity.this);
        });
        builder.setNegativeButton(R.string.never, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.setNeutralButton(R.string.later, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            getPrefs().setRateCount(0);
        });
        builder.create().show();
    }

    private void showBetaDialog() {
        getPrefs().setBetaWarmingShowed(true);
        String appVersion = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (!appVersion.contains("beta")) {
            return;
        }
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle("Beta");
        builder.setMessage("This version of application may work unstable!");
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void showMainImage() {
        String path = getPrefs().getImagePath();
        if (path != null && !path.isEmpty() && !path.contains("{")) {
            String fileName = path;
            if (path.contains("=")) {
                int index = path.indexOf("=");
                fileName = path.substring(index);
            }
            File file = new File(MemoryUtil.getImageCacheDir(), fileName + ".jpg");
            boolean readPerm = Permissions.checkPermission(this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            if (readPerm && file.exists()) {
                Glide.with(this).load(file).into(mMainImageView);
                mMainImageView.setVisibility(View.VISIBLE);
            } else {
                Glide.with(this).load(path).into(mMainImageView);
                mMainImageView.setVisibility(View.VISIBLE);
                if (readPerm) {
                    new SaveAsync(this).execute(path);
                }
            }
        } else {
            mMainImageView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getPrefs().isAutoBackupEnabled() && getPrefs().isSettingsBackupEnabled()
                && Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
            new BackupSettingTask(this).execute();
        }
    }

    @Override
    public void onTitleChange(String title) {
        binding.toolbar.setTitle(title);
    }

    @Override
    public void onFragmentSelect(Fragment fragment) {
        this.fragment = fragment;
        if (this.fragment instanceof BaseSettingsFragment) {
            binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        } else {
            binding.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        }
    }

    @Override
    public void setClick(View.OnClickListener listener) {
        if (listener == null) {
            hideFab();
        } else {
            showFab();
            binding.fab.setOnClickListener(view -> {
                if (mNoteView.isNoteVisible()) {
                    mNoteView.hideNoteView();
                    return;
                }
                listener.onClick(view);
            });
        }
    }

    @Override
    public void onThemeChange(@ColorInt int primary, @ColorInt int primaryDark, @ColorInt int accent) {
        if (primary == 0) {
            primary = getThemeUtil().getColor(getThemeUtil().colorPrimary());
        }
        if (primaryDark == 0) {
            primaryDark = getThemeUtil().getColor(getThemeUtil().colorPrimaryDark());
        }
        if (accent == 0) {
            accent = getThemeUtil().getColor(getThemeUtil().colorAccent());
        }
        binding.toolbar.setBackgroundColor(primary);
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(primaryDark);
        }
        binding.fab.setBackgroundTintList(ViewUtils.getFabState(accent, accent));
    }

    @Override
    public void refreshMenu() {
        setMenuVisible();
    }

    @Override
    public void onScrollChanged(RecyclerView recyclerView) {
        if (listener != null && mPrevList != null) {
            mPrevList.removeOnScrollListener(listener);
        }
        if (recyclerView != null) {
            returnScrollListener = new ReturnScrollListener.Builder(ReturnScrollListener.QuickReturnViewType.FOOTER)
                    .footer(binding.fab)
                    .minFooterTranslation(MeasureUtils.dp2px(this, 88))
                    .isSnappable(true)
                    .build();
            listener = getOnScrollListener();
            if (Module.isLollipop()) {
                recyclerView.addOnScrollListener(listener);
            } else {
                recyclerView.setOnScrollListener(listener);
            }
            mPrevList = recyclerView;
        }
    }

    @NonNull
    private RecyclerView.OnScrollListener getOnScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                returnScrollListener.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                returnScrollListener.onScrolled(recyclerView, dx, dy);
            }
        };
    }

    @Override
    public void addFilters(List<FilterView.Filter> filters, boolean clear) {
        if (filters == null || filters.size() == 0) {
            hideFilters();
            if (clear) {
                binding.filterView.clear();
            }
        } else {
            if (clear) {
                binding.filterView.clear();
            }
            for (FilterView.Filter filter : filters) {
                binding.filterView.addFilter(filter);
            }
            ViewUtils.expand(binding.filterView);
        }
    }

    @Override
    public void hideFilters() {
        if (isFiltersVisible()) {
            ViewUtils.collapse(binding.filterView);
        }
    }

    @Override
    public boolean isFiltersVisible() {
        return binding.filterView.getVisibility() == View.VISIBLE;
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
        mNavigationView = binding.navView;
        mNavigationView.setNavigationItemSelectedListener(this);
        View view = mNavigationView.getHeaderView(0);
        mSaleBadge = view.findViewById(R.id.sale_badge);
        mUpdateBadge = view.findViewById(R.id.update_badge);
        mSaleBadge.setVisibility(View.INVISIBLE);
        mUpdateBadge.setVisibility(View.INVISIBLE);
        mMainImageView = view.findViewById(R.id.headerImage);
        mMainImageView.setOnClickListener(view1 -> openImageScreen());
        view.findViewById(R.id.headerItem).setOnClickListener(view12 -> openImageScreen());
        RoboTextView nameView = view.findViewById(R.id.appNameBanner);
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
        if (!Module.isPro() && !SuperUtil.isAppInstalled(this, "com.cray.software.justreminderpro")) {
            menu.getItem(13).setVisible(true);
        } else {
            menu.getItem(13).setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (isFiltersVisible()) {
            addFilters(null, true);
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
            binding.fab.show();
        }
    }

    private void hideFab() {
        if (binding.fab.getVisibility() != View.GONE) {
            binding.fab.hide();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            new Recognize(this).parseResults(matches, false);
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Module.isMarshmallow() && fragment != null) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
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
                replaceFragment(new SettingsFragment(), getString(R.string.action_settings));
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
        Dialogues.getDialog(this)
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

    @Override
    public void onSale(String discount, String expiryDate) {
        String expiry = TimeUtil.getFireFormatted(this, expiryDate);
        if (TextUtils.isEmpty(expiry)) {
            mSaleBadge.setVisibility(View.INVISIBLE);
        } else {
            mSaleBadge.setVisibility(View.VISIBLE);
            mSaleBadge.setText("SALE" + " " + getString(R.string.app_name_pro) + " -" + discount + getString(R.string.p_until) + " " + expiry);
        }
    }

    @Override
    public void noSale() {
        mSaleBadge.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUpdate(String version) {
        mUpdateBadge.setVisibility(View.VISIBLE);
        mUpdateBadge.setText(getString(R.string.update_available) + ": " + version);
        mUpdateBadge.setOnClickListener(v -> SuperUtil.launchMarket(MainActivity.this));
    }

    @Override
    public void noUpdate() {
        mUpdateBadge.setVisibility(View.INVISIBLE);
    }
}
