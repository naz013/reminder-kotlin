package com.elementary.tasks.creators;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.app_widgets.UpdatesHelper;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.file_explorer.FileExplorerActivity;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.Recognize;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.TextViewWithIcon;
import com.elementary.tasks.creators.fragments.ApplicationFragment;
import com.elementary.tasks.creators.fragments.DateFragment;
import com.elementary.tasks.creators.fragments.EmailFragment;
import com.elementary.tasks.creators.fragments.LocationFragment;
import com.elementary.tasks.creators.fragments.LocationOutFragment;
import com.elementary.tasks.creators.fragments.MonthFragment;
import com.elementary.tasks.creators.fragments.PlacesFragment;
import com.elementary.tasks.creators.fragments.ReminderInterface;
import com.elementary.tasks.creators.fragments.ShopFragment;
import com.elementary.tasks.creators.fragments.SkypeFragment;
import com.elementary.tasks.creators.fragments.TimerFragment;
import com.elementary.tasks.creators.fragments.TypeFragment;
import com.elementary.tasks.creators.fragments.WeekFragment;
import com.elementary.tasks.creators.fragments.YearFragment;
import com.elementary.tasks.databinding.ActivityCreateReminderBinding;
import com.elementary.tasks.databinding.DialogSelectExtraBinding;
import com.elementary.tasks.databinding.DialogWithSeekAndTitleBinding;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.groups.Position;
import com.elementary.tasks.reminder.DeleteFilesAsync;
import com.elementary.tasks.reminder.models.Reminder;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class CreateReminderActivity extends ThemedActivity implements ReminderInterface, View.OnLongClickListener {

    private static final int DATE = 0;
    private static final int TIMER = 1;
    private static final int WEEK = 2;
    private static final int GPS = 3;
    private static final int SKYPE = 4;
    private static final int APP = 5;
    private static final int MONTH = 6;
    private static final int YEAR = 7;
    private static final int GPS_OUT = 8;
    private static final int SHOP = 9;
    private static final int EMAIL = 10;
    private static final int GPS_PLACE = 11;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int MENU_ITEM_DELETE = 12;
    private static final int CONTACTS_REQUEST_E = 501;
    private static final String TAG = "CreateReminderActivity";
    private static final String SHOWCASE = "reminder_showcase";

    private ActivityCreateReminderBinding binding;
    private Toolbar toolbar;
    private Spinner spinner;

    private TypeFragment fragment;

    private boolean useGlobal = true;
    private boolean vibration;
    private boolean voice;
    private boolean notificationRepeat;
    private boolean wake;
    private boolean unlock;
    private boolean auto;
    private boolean hasAutoExtra;
    private boolean isExportToTasks;
    private int repeatLimit = -1;
    private int volume = 25;
    private String groupId;
    private String melodyPath;
    private String autoLabel;
    private int ledColor = -1;
    private boolean isEditing;

    private Reminder mReminder;

    private AdapterView.OnItemSelectedListener mOnTypeSelectListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            getPrefs().setLastUsedReminder(position);
            switch (position) {
                case DATE:
                    replaceFragment(new DateFragment());
                    break;
                case TIMER:
                    replaceFragment(new TimerFragment());
                    break;
                case WEEK:
                    replaceFragment(new WeekFragment());
                    break;
                case GPS:
                    if (hasGpsPermission(GPS)) {
                        replaceFragment(new LocationFragment());
                    } else {
                        spinner.setSelection(DATE);
                    }
                    break;
                case SKYPE:
                    replaceFragment(new SkypeFragment());
                    break;
                case APP:
                    replaceFragment(new ApplicationFragment());
                    break;
                case MONTH:
                    replaceFragment(new MonthFragment());
                    break;
                case GPS_OUT:
                    if (hasGpsPermission(GPS_OUT)) {
                        replaceFragment(new LocationOutFragment());
                    } else {
                        spinner.setSelection(DATE);
                    }
                    break;
                case SHOP:
                    replaceFragment(new ShopFragment());
                    break;
                case EMAIL:
                    if (Permissions.checkPermission(CreateReminderActivity.this, Permissions.READ_CONTACTS)) {
                        replaceFragment(new EmailFragment());
                    } else {
                        Permissions.requestPermission(CreateReminderActivity.this, CONTACTS_REQUEST_E, Permissions.READ_CONTACTS);
                    }
                    break;
                case GPS_PLACE:
                    if (hasGpsPermission(GPS_PLACE)) {
                        replaceFragment(new PlacesFragment());
                    } else {
                        spinner.setSelection(DATE);
                    }
                    break;
                case YEAR:
                    replaceFragment(new YearFragment());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private boolean hasGpsPermission(int code) {
        if (!Permissions.checkPermission(CreateReminderActivity.this, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION)) {
            Permissions.requestPermission(CreateReminderActivity.this, code, Permissions.ACCESS_COARSE_LOCATION, Permissions.ACCESS_FINE_LOCATION);
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadReminder();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_reminder);
        isExportToTasks = Google.getInstance(this) != null;
        initActionBar();
        initNavigation();
        initLongClick();
        editReminder();
    }

    private void initLongClick() {
        binding.customButton.setOnLongClickListener(this);
        binding.groupButton.setOnLongClickListener(this);
        binding.voiceButton.setOnLongClickListener(this);
        binding.exclusionButton.setOnLongClickListener(this);
        binding.melodyButton.setOnLongClickListener(this);
        binding.repeatButton.setOnLongClickListener(this);
    }

    @Override
    protected String getStats() {
        return "Reminder " + (mReminder != null ? "edit" : "create");
    }

    private void loadReminder() {
        Intent intent = getIntent();
        String id = intent.getStringExtra(Constants.INTENT_ID);
        if (id != null) {
            mReminder = RealmDb.getInstance().getReminder(id);
            isEditing = true;
            if (mReminder == null) {
                return;
            }
            getControl().pause();
        } else if (intent.getData() != null) {
            try {
                Uri name = intent.getData();
                String scheme = name.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    ContentResolver cr = getContentResolver();
                    mReminder = BackupTool.getInstance().getReminder(cr, name);
                } else {
                    mReminder = BackupTool.getInstance().getReminder(name.getPath(), null);
                }
            } catch (IOException | IllegalStateException e) {
                LogUtil.d(TAG, "loadReminder: " + e.getLocalizedMessage());
            }
        }
    }

    private EventControl getControl() {
        return EventControlFactory.getController(this, mReminder);
    }

    private void editReminder() {
        if (mReminder == null) return;
        binding.taskSummary.setText(mReminder.getSummary());
        showGroup(RealmDb.getInstance().getGroup(mReminder.getGroupUuId()));
        initParams();
        switch (mReminder.getType()) {
            case Reminder.BY_DATE:
            case Reminder.BY_DATE_CALL:
            case Reminder.BY_DATE_SMS:
                spinner.setSelection(DATE);
                break;
            case Reminder.BY_TIME:
                spinner.setSelection(TIMER);
                break;
            case Reminder.BY_WEEK:
            case Reminder.BY_WEEK_CALL:
            case Reminder.BY_WEEK_SMS:
                spinner.setSelection(WEEK);
                break;
            case Reminder.BY_LOCATION:
            case Reminder.BY_LOCATION_CALL:
            case Reminder.BY_LOCATION_SMS:
                spinner.setSelection(GPS);
                break;
            case Reminder.BY_SKYPE:
            case Reminder.BY_SKYPE_CALL:
            case Reminder.BY_SKYPE_VIDEO:
                spinner.setSelection(SKYPE);
                break;
            case Reminder.BY_DATE_APP:
            case Reminder.BY_DATE_LINK:
                spinner.setSelection(APP);
                break;
            case Reminder.BY_MONTH:
            case Reminder.BY_MONTH_CALL:
            case Reminder.BY_MONTH_SMS:
                spinner.setSelection(MONTH);
                break;
            case Reminder.BY_OUT:
            case Reminder.BY_OUT_SMS:
            case Reminder.BY_OUT_CALL:
                spinner.setSelection(GPS_OUT);
                break;
            case Reminder.BY_DATE_SHOP:
                spinner.setSelection(SHOP);
                break;
            case Reminder.BY_DATE_EMAIL:
                spinner.setSelection(EMAIL);
                break;
            case Reminder.BY_DAY_OF_YEAR:
            case Reminder.BY_DAY_OF_YEAR_CALL:
            case Reminder.BY_DAY_OF_YEAR_SMS:
                spinner.setSelection(YEAR);
                break;
            default:
                if (Module.isPro()) {
                    switch (mReminder.getType()) {
                        case Reminder.BY_PLACES:
                        case Reminder.BY_PLACES_SMS:
                        case Reminder.BY_PLACES_CALL:
                            spinner.setSelection(GPS_PLACE);
                            break;
                    }
                }
                break;
        }
    }

    private void initParams() {
        useGlobal = mReminder.isUseGlobal();
        auto = mReminder.isAuto();
        wake = mReminder.isAwake();
        unlock = mReminder.isUnlock();
        notificationRepeat = mReminder.isRepeatNotification();
        voice = mReminder.isNotifyByVoice();
        vibration = mReminder.isVibrate();
        volume = mReminder.getVolume();
        repeatLimit = mReminder.getRepeatLimit();
        melodyPath = mReminder.getMelodyPath();
        ledColor = mReminder.getColor();
        updateMelodyIndicator();
    }

    private void updateMelodyIndicator() {
        if (melodyPath != null) {
            binding.melodyButton.setVisibility(View.VISIBLE);
        } else {
            binding.melodyButton.setVisibility(View.GONE);
        }
    }

    private void initNavigation() {
        spinner = binding.navSpinner;
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        if (getThemeUtil().isDark()) {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date), R.drawable.ic_meeting_deadlines_white));
            navSpinner.add(new SpinnerItem(getString(R.string.timer), R.drawable.ic_timer_white));
            navSpinner.add(new SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm_white));
            navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_map_white));
            navSpinner.add(new SpinnerItem(getString(R.string.skype), R.drawable.ic_skype_white));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application), R.drawable.ic_software_white));
            navSpinner.add(new SpinnerItem(getString(R.string.day_of_month), R.drawable.ic_calendar_white));
            navSpinner.add(new SpinnerItem(getString(R.string.yearly), R.drawable.ic_confetti_white));
            navSpinner.add(new SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_cart_white));
            navSpinner.add(new SpinnerItem(getString(R.string.e_mail), R.drawable.ic_email_white));
            if (Module.isPro())
                navSpinner.add(new SpinnerItem(getString(R.string.places), R.drawable.ic_map_marker_white));
        } else {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date), R.drawable.ic_meeting_deadlines));
            navSpinner.add(new SpinnerItem(getString(R.string.timer), R.drawable.ic_timer));
            navSpinner.add(new SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm));
            navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_map));
            navSpinner.add(new SpinnerItem(getString(R.string.skype), R.drawable.ic_skype));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application), R.drawable.ic_software));
            navSpinner.add(new SpinnerItem(getString(R.string.day_of_month), R.drawable.ic_calendar));
            navSpinner.add(new SpinnerItem(getString(R.string.yearly), R.drawable.ic_confetti_black));
            navSpinner.add(new SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_cart));
            navSpinner.add(new SpinnerItem(getString(R.string.e_mail), R.drawable.ic_email));
            if (Module.isPro())
                navSpinner.add(new SpinnerItem(getString(R.string.places), R.drawable.ic_map_marker));
        }
        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(mOnTypeSelectListener);
        int lastPos = getPrefs().getLastUsedReminder();
        if (lastPos >= navSpinner.size()) lastPos = 0;
        spinner.setSelection(lastPos);
    }

    private void initActionBar() {
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.voiceButton.setOnClickListener(v -> openRecognizer());
        binding.customButton.setOnClickListener(v -> openCustomizationDialog());
        binding.groupButton.setOnClickListener(v -> changeGroup());
        binding.melodyButton.setOnClickListener(view -> showCurrentMelody());
        GroupItem groupItem = RealmDb.getInstance().getDefaultGroup();
        if (groupItem != null) {
            binding.groupButton.setText(groupItem.getTitle());
            groupId = groupItem.getUuId();
        }
    }

    private void changeGroup() {
        List<GroupItem> list = new ArrayList<>();
        Position position = new Position();
        final List<String> categories = RealmDb.getInstance().getAllGroupsNames(list, groupId, position);
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(R.string.choose_group);
        builder.setSingleChoiceItems(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, categories), position.getI(), (dialog, which) -> {
            dialog.dismiss();
            showGroup(list.get(which));
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showGroup(GroupItem item) {
        if (item == null) return;
        binding.groupButton.setText(item.getTitle());
        groupId = item.getUuId();
    }

    private void openCustomizationDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(R.string.personalization);
        DialogSelectExtraBinding b = getCustomizationView();
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialog, which) -> saveExtraResults(b));
        builder.create().show();
    }

    private void saveExtraResults(DialogSelectExtraBinding b) {
        useGlobal = b.extraSwitch.isChecked();
        auto = b.autoCheck.isChecked();
        wake = b.wakeCheck.isChecked();
        unlock = b.unlockCheck.isChecked();
        notificationRepeat = b.repeatCheck.isChecked();
        voice = b.voiceCheck.isChecked();
        vibration = b.vibrationCheck.isChecked();
    }

    private DialogSelectExtraBinding getCustomizationView() {
        DialogSelectExtraBinding binding = DialogSelectExtraBinding.inflate(getLayoutInflater());
        binding.extraSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.autoCheck.setEnabled(!isChecked);
            binding.repeatCheck.setEnabled(!isChecked);
            binding.unlockCheck.setEnabled(!isChecked);
            binding.vibrationCheck.setEnabled(!isChecked);
            binding.voiceCheck.setEnabled(!isChecked);
            binding.wakeCheck.setEnabled(!isChecked);
        });
        binding.voiceCheck.setChecked(voice);
        binding.vibrationCheck.setChecked(vibration);
        binding.unlockCheck.setChecked(unlock);
        binding.repeatCheck.setChecked(notificationRepeat);
        binding.autoCheck.setChecked(auto);
        binding.wakeCheck.setChecked(wake);
        binding.extraSwitch.setChecked(useGlobal);
        binding.autoCheck.setEnabled(!useGlobal);
        binding.repeatCheck.setEnabled(!useGlobal);
        binding.unlockCheck.setEnabled(!useGlobal);
        binding.vibrationCheck.setEnabled(!useGlobal);
        binding.voiceCheck.setEnabled(!useGlobal);
        binding.wakeCheck.setEnabled(!useGlobal);
        if (hasAutoExtra && autoLabel != null) {
            binding.autoCheck.setVisibility(View.VISIBLE);
            binding.autoCheck.setText(autoLabel);
        } else {
            binding.autoCheck.setVisibility(View.GONE);
        }
        return binding;
    }

    private void openRecognizer() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, true);
    }

    public void replaceFragment(TypeFragment fragment) {
        this.fragment = fragment;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.main_container, fragment, null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                save();
                return true;
            case R.id.action_custom_melody:
                if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL)) {
                    startActivityForResult(new Intent(this, FileExplorerActivity.class),
                            Constants.REQUEST_CODE_SELECTED_MELODY);
                } else {
                    Permissions.requestPermission(this, 330, Permissions.READ_EXTERNAL);
                }
                return true;
            case R.id.action_custom_color:
                chooseLedColor();
                return true;
            case R.id.action_volume:
                selectVolume();
                return true;
            case MENU_ITEM_DELETE:
                deleteReminder();
                return true;
            case android.R.id.home:
                closeScreen();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeScreen() {
        if (mReminder != null && getPrefs().isAutoSaveEnabled()) {
            if (!mReminder.isActive()) {
                askAboutEnabling();
            } else {
                save();
            }
        } else if (isEditing && mReminder != null) {
            if (!mReminder.isActive()) {
                getControl().resume();
            }
            finish();
        } else {
            finish();
        }
    }

    private void deleteReminder() {
        if (mReminder != null) {
            if (mReminder.isRemoved()) {
                RealmDb.getInstance().deleteReminder(mReminder.getUuId());
                new DeleteFilesAsync(this).execute(mReminder.getUuId());
            } else {
                EventControl control = EventControlFactory.getController(this, mReminder.setRemoved(true));
                control.stop();
            }
            finish();
        }
    }

    private void selectVolume() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(R.string.loudness);
        DialogWithSeekAndTitleBinding b = DialogWithSeekAndTitleBinding.inflate(getLayoutInflater());
        b.seekBar.setMax(25);
        b.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                b.titleView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        b.seekBar.setProgress(volume);
        b.titleView.setText(String.valueOf(volume));
        builder.setView(b.getRoot());
        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
            volume = b.seekBar.getProgress();
            String str = String.format(getString(R.string.selected_loudness_x_for_reminder), String.valueOf(volume));
            showSnackbar(str, getString(R.string.cancel), v -> volume = 25);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void chooseLedColor() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.led_color));
        String[] colors = new String[LED.NUM_OF_LEDS];
        for (int i = 0; i < LED.NUM_OF_LEDS; i++) {
            colors[i] = LED.getTitle(this, i);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, colors);
        builder.setSingleChoiceItems(adapter, ledColor, (dialog, which) -> {
            if (which != -1) {
                ledColor = which;
                String selColor = LED.getTitle(this, which);
                String str = String.format(getString(R.string.led_color_x), selColor);
                showSnackbar(str, getString(R.string.cancel), v -> ledColor = -1);
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.setNegativeButton(R.string.disable, (dialog, which) -> {
            ledColor = -1;
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void askAboutEnabling() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(R.string.this_reminder_is_disabled);
        builder.setMessage(R.string.would_you_like_to_enable_it);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            dialog.dismiss();
            save();
        });
        builder.setNegativeButton(R.string.no, (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.create().show();
    }

    private void save() {
        if (fragment.save()) {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_reminder, menu);
        if (mReminder != null && isEditing) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null) {
                Reminder model = new Recognize(this).findResults(matches);
                if (model != null) {
                    processModel(model);
                } else {
                    String text = matches.get(0).toString();
                    binding.taskSummary.setText(StringUtils.capitalize(text));
                }
            }
        }
        if (requestCode == Constants.REQUEST_CODE_SELECTED_MELODY && resultCode == RESULT_OK) {
            melodyPath = data.getStringExtra(Constants.FILE_PICKED);
            updateMelodyIndicator();
            showCurrentMelody();
        }
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showCurrentMelody() {
        if (melodyPath != null) {
            File musicFile = new File(melodyPath);
            showSnackbar(String.format(getString(R.string.melody_x), musicFile.getName()),
                    getString(R.string.delete), view -> removeMelody());
        }
    }

    private void removeMelody() {
        melodyPath = null;
        updateMelodyIndicator();
    }

    private void processModel(Reminder model) {
        this.mReminder = model;
        editReminder();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CONTACTS_REQUEST_E:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    spinner.setSelection(EMAIL);
                } else {
                    spinner.setSelection(DATE);
                }
                break;
            case GPS_PLACE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    spinner.setSelection(GPS_PLACE);
                } else {
                    spinner.setSelection(DATE);
                }
                break;
            case GPS_OUT:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    spinner.setSelection(GPS_OUT);
                } else {
                    spinner.setSelection(DATE);
                }
                break;
            case GPS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    spinner.setSelection(GPS);
                } else {
                    spinner.setSelection(DATE);
                }
                break;
        }
        if (Module.isMarshmallow() && fragment != null)
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showShowcase();
    }

    public void showShowcase() {
        if (!getPrefs().isShowcase(SHOWCASE)) {
            getPrefs().setShowcase(SHOWCASE, true);
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(350);
            config.setMaskColor(getThemeUtil().getColor(getThemeUtil().colorAccent()));
            config.setContentTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            config.setDismissTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this);
            sequence.setConfig(config);
            sequence.addSequenceItem(binding.navSpinner,
                    getString(R.string.click_to_select_reminder_type),
                    getString(R.string.got_it));
            sequence.addSequenceItem(binding.voiceButton,
                    getString(R.string.to_insert_task_by_voice),
                    getString(R.string.got_it));
            sequence.addSequenceItem(binding.customButton,
                    getString(R.string.click_to_customize),
                    getString(R.string.got_it));
            sequence.addSequenceItem(binding.groupButton,
                    getString(R.string.click_to_change_reminder_group),
                    getString(R.string.got_it));
            sequence.start();
        }
    }

    @Override
    public String getMelodyPath() {
        return melodyPath;
    }

    @Override
    public void showSnackbar(String title, String actionName, View.OnClickListener listener) {
        Snackbar.make(binding.mainContainer, title, Snackbar.LENGTH_SHORT).setAction(actionName, listener).show();
    }

    @Override
    public void showSnackbar(String title) {
        Snackbar.make(binding.mainContainer, title, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public String getSummary() {
        return binding.taskSummary.getText().toString().trim();
    }

    @Override
    public Reminder getReminder() {
        return mReminder;
    }

    @Override
    public boolean getUseGlobal() {
        return useGlobal;
    }

    @Override
    public boolean getVoice() {
        return voice;
    }

    @Override
    public boolean getVibration() {
        return vibration;
    }

    @Override
    public boolean getNotificationRepeat() {
        return notificationRepeat;
    }

    @Override
    public boolean getWake() {
        return wake;
    }

    @Override
    public boolean getUnlock() {
        return unlock;
    }

    @Override
    public boolean getAuto() {
        return auto;
    }

    @Override
    public int getVolume() {
        return volume;
    }

    @Override
    public int getLedColor() {
        return ledColor;
    }

    public int getRepeatLimit() {
        return repeatLimit;
    }

    @Override
    public String getGroup() {
        return groupId;
    }

    @Override
    public void setRepeatLimit(int repeatLimit) {
        this.repeatLimit = repeatLimit;
    }

    @Override
    public void setEventHint(String hint) {
        if (binding != null) binding.taskSummary.setHint(hint);
    }

    @Override
    public boolean isExportToCalendar() {
        return getPrefs().isCalendarEnabled() || getPrefs().isStockCalendarEnabled();
    }

    @Override
    public boolean isExportToTasks() {
        return isExportToTasks;
    }

    @Override
    public void setExclusionAction(View.OnClickListener listener) {
        if (binding == null) return;
        if (listener == null) {
            binding.exclusionButton.setVisibility(View.GONE);
        } else {
            binding.exclusionButton.setVisibility(View.VISIBLE);
            binding.exclusionButton.setOnClickListener(listener);
        }
    }

    @Override
    public void setRepeatAction(View.OnClickListener listener) {
        if (binding == null) return;
        if (listener == null) {
            binding.repeatButton.setVisibility(View.GONE);
        } else {
            binding.repeatButton.setVisibility(View.VISIBLE);
            binding.repeatButton.setOnClickListener(listener);
        }
    }

    @Override
    public void setFullScreenMode(boolean b) {
        if (b) {
            ViewUtils.collapse(toolbar);
        } else {
            ViewUtils.expand(toolbar);
        }
    }

    @Override
    public void setHasAutoExtra(boolean hasAutoExtra, String label) {
        this.hasAutoExtra = hasAutoExtra;
        this.autoLabel = label;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdatesHelper.getInstance(this).updateWidget();
    }

    @Override
    public void onBackPressed() {
        if (fragment.onBackPressed()) {
            closeScreen();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.customButton:
                Toast.makeText(this, getString(R.string.acc_customize_reminder), Toast.LENGTH_SHORT).show();
                break;
            case R.id.groupButton:
                Toast.makeText(this, getString(R.string.change_group), Toast.LENGTH_SHORT).show();
                break;
            case R.id.voiceButton:
                Toast.makeText(this, getString(R.string.acc_type_by_voice), Toast.LENGTH_SHORT).show();
                break;
            case R.id.exclusionButton:
                Toast.makeText(this, getString(R.string.acc_customize_exclusions), Toast.LENGTH_SHORT).show();
                break;
            case R.id.melodyButton:
                Toast.makeText(this, getString(R.string.acc_select_melody), Toast.LENGTH_SHORT).show();
                break;
            case R.id.repeatButton:
                Toast.makeText(this, getString(R.string.repeat_limit), Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private static class SpinnerItem {
        private String title;
        private int icon;

        SpinnerItem(String title, int icon) {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle() {
            return this.title;
        }

        public int getIcon() {
            return this.icon;
        }
    }

    private class TitleNavigationAdapter extends BaseAdapter {

        private TextViewWithIcon txtTitle;
        private ArrayList<SpinnerItem> spinnerNavItem;
        private Context context;

        TitleNavigationAdapter(Context context, ArrayList<SpinnerItem> spinnerNavItem) {
            this.spinnerNavItem = spinnerNavItem;
            this.context = context;
        }

        @Override
        public int getCount() {
            return spinnerNavItem.size();
        }

        @Override
        public Object getItem(int index) {
            return spinnerNavItem.get(index);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater)
                        context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.list_item_navigation, null);
            }
            txtTitle = convertView.findViewById(R.id.txtTitle);
            txtTitle.setIcon(0);
            txtTitle.setText(spinnerNavItem.get(position).getTitle());
            txtTitle.setTextColor(context.getResources().getColor(R.color.whitePrimary));
            return convertView;
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater mInflater = (LayoutInflater)
                        context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                convertView = mInflater.inflate(R.layout.list_item_navigation, null);
            }
            RelativeLayout itemBg = convertView.findViewById(R.id.itemBg);
            itemBg.setBackgroundColor(getThemeUtil().getSpinnerStyle());
            txtTitle = convertView.findViewById(R.id.txtTitle);
            txtTitle.setIcon(spinnerNavItem.get(position).getIcon());
            if (getThemeUtil().isDark()) {
                txtTitle.setTextColor(getThemeUtil().getColor(R.color.whitePrimary));
            } else {
                txtTitle.setTextColor(getThemeUtil().getColor(R.color.blackPrimary));
            }
            txtTitle.setText(spinnerNavItem.get(position).getTitle());
            return convertView;
        }
    }
}
