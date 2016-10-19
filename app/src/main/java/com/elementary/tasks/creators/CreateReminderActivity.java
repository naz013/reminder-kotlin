package com.elementary.tasks.creators;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.elementary.tasks.R;
import com.elementary.tasks.core.LED;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.views.roboto.RoboEditText;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.creators.fragments.ReminderInterface;
import com.elementary.tasks.creators.fragments.TypeFragment;
import com.elementary.tasks.databinding.ActivityCreateReminderBinding;
import com.elementary.tasks.databinding.DialogSelectExtraBinding;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.ArrayList;

public class CreateReminderActivity extends ThemedActivity implements ReminderInterface {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 109;
    private static final int MENU_ITEM_DELETE = 12;

    private ActivityCreateReminderBinding mBinding;
    private Toolbar toolbar;
    private Spinner spinner;
    private RoboEditText taskField;

    private TypeFragment fragment;

    private boolean useGlobal;
    private boolean vibration;
    private boolean voice;
    private boolean notificationRepeat;
    private boolean wake;
    private boolean unlock;
    private boolean auto;
    private int repeats = -1;
    private int volume;
    private String groupId;
    private int ledColor = -1;

    private Reminder mReminder;

    private AdapterView.OnItemSelectedListener mOnTypeSelectListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_create_reminder);
        initActionBar();
        initNavigation();
    }

    private void initNavigation() {
        spinner = mBinding.navSpinner;
        ArrayList<SpinnerItem> navSpinner = new ArrayList<>();
        if (themeUtil.isDark()) {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date), R.drawable.ic_calendar_white));
            navSpinner.add(new SpinnerItem(getString(R.string.timer), R.drawable.ic_timer_white));
            navSpinner.add(new SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm_white));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call), R.drawable.ic_phone_white));
            navSpinner.add(new SpinnerItem(getString(R.string.sms), R.drawable.ic_message_white));
            navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_map_white));
            navSpinner.add(new SpinnerItem(getString(R.string.skype), R.drawable.ic_skype_white));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application), R.drawable.ic_application_white));
            navSpinner.add(new SpinnerItem(getString(R.string.day_of_month), R.drawable.ic_calendar_white));
            navSpinner.add(new SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_white_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_cart_white));
            navSpinner.add(new SpinnerItem(getString(R.string.e_mail), R.drawable.ic_email_white));
            if (Module.isPro())
                navSpinner.add(new SpinnerItem(getString(R.string.places), R.drawable.ic_map_marker_white));
        } else {
            navSpinner.add(new SpinnerItem(getString(R.string.by_date), R.drawable.ic_calendar));
            navSpinner.add(new SpinnerItem(getString(R.string.timer), R.drawable.ic_timer));
            navSpinner.add(new SpinnerItem(getString(R.string.alarm), R.drawable.ic_alarm));
            navSpinner.add(new SpinnerItem(getString(R.string.make_call), R.drawable.ic_phone));
            navSpinner.add(new SpinnerItem(getString(R.string.sms), R.drawable.ic_message));
            navSpinner.add(new SpinnerItem(getString(R.string.location), R.drawable.ic_map));
            navSpinner.add(new SpinnerItem(getString(R.string.skype), R.drawable.ic_skype));
            navSpinner.add(new SpinnerItem(getString(R.string.launch_application), R.drawable.ic_application));
            navSpinner.add(new SpinnerItem(getString(R.string.day_of_month), R.drawable.ic_calendar));
            navSpinner.add(new SpinnerItem(getString(R.string.place_out), R.drawable.ic_beenhere_black_24dp));
            navSpinner.add(new SpinnerItem(getString(R.string.shopping_list), R.drawable.ic_cart));
            navSpinner.add(new SpinnerItem(getString(R.string.e_mail), R.drawable.ic_email));
            if (Module.isPro())
                navSpinner.add(new SpinnerItem(getString(R.string.places), R.drawable.ic_map_marker));
        }

        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(mOnTypeSelectListener);
    }

    private void initActionBar() {
        toolbar = mBinding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        taskField = mBinding.taskSummary;
        mBinding.voiceButton.setOnClickListener(v -> openRecognizer());
        mBinding.customButton.setOnClickListener(v -> openCustomizationDialog());
        mBinding.groupButton.setOnClickListener(v -> changeGroup());
    }

    private void changeGroup() {

    }

    private void openCustomizationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
            binding.autoCheck.setEnabled(isChecked);
            binding.repeatCheck.setEnabled(isChecked);
            binding.unlockCheck.setEnabled(isChecked);
            binding.vibrationCheck.setEnabled(isChecked);
            binding.voiceCheck.setEnabled(isChecked);
            binding.wakeCheck.setEnabled(isChecked);
        });
        binding.voiceCheck.setChecked(voice);
        binding.vibrationCheck.setChecked(vibration);
        binding.unlockCheck.setChecked(unlock);
        binding.repeatCheck.setChecked(notificationRepeat);
        binding.autoCheck.setChecked(auto);
        binding.wakeCheck.setChecked(wake);
        binding.extraSwitch.setChecked(useGlobal);
        binding.autoCheck.setEnabled(useGlobal);
        binding.repeatCheck.setEnabled(useGlobal);
        binding.unlockCheck.setEnabled(useGlobal);
        binding.vibrationCheck.setEnabled(useGlobal);
        binding.voiceCheck.setEnabled(useGlobal);
        binding.wakeCheck.setEnabled(useGlobal);
        return binding;
    }

    private void openRecognizer() {
        SuperUtil.startVoiceRecognitionActivity(this, VOICE_RECOGNITION_REQUEST_CODE, true);
    }

    public void replaceFragment(TypeFragment fragment, String title) {
        this.fragment = fragment;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.main_container, fragment, title);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        toolbar.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                save();
                return true;
            case R.id.action_custom_melody:
                if(Permissions.checkPermission(this,
                        Permissions.READ_EXTERNAL)) {
//                    startActivityForResult(new Intent(ReminderActivity.this, FileExploreActivity.class),
//                            Constants.REQUEST_CODE_SELECTED_MELODY);
                } else {
                    Permissions.requestPermission(this, 330,
                            Permissions.READ_EXTERNAL);
                }
                return true;
            case R.id.action_custom_radius:
                selectRadius();
                return true;
            case R.id.action_custom_color:
                chooseLedColor();
                return true;
            case R.id.action_volume:
                selectVolume();
                return true;
            case R.id.action_limit:
                changeLimit();
                return true;
            case MENU_ITEM_DELETE:
                deleteReminder();
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteReminder() {

    }

    private void changeLimit() {

    }

    private void selectVolume() {

    }

    private void chooseLedColor() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void selectRadius() {

    }

    private void save() {
        fragment.save();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_reminder, menu);
//        if (isLocationAttached()){
//            menu.getItem(2).setVisible(true);
//        } else {
//            menu.getItem(2).setVisible(false);
//        }
//        if (isLocationAttached() || isLocationOutAttached()
//                || isShoppingAttached() || isPlacesAttached()){
//            menu.getItem(4).setVisible(false);
//        } else {
//            menu.getItem(4).setVisible(true);
//        }
//        if (Module.isPro() && SharedPrefs.getInstance(this).getBoolean(Prefs.LED_STATUS)){
//            menu.getItem(3).setVisible(true);
//        } else {
//            menu.getItem(3).setVisible(false);
//        }
        if (mReminder != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        if (isLocationAttached()){
//            menu.getItem(2).setVisible(true);
//        } else {
//            menu.getItem(2).setVisible(false);
//        }
//        if (isLocationAttached() || isLocationOutAttached()
//                || isShoppingAttached() || isPlacesAttached()){
//            menu.getItem(4).setVisible(false);
//        } else {
//            menu.getItem(4).setVisible(true);
//        }
//        if (Module.isPro() && SharedPrefs.getInstance(this).getBoolean(Prefs.LED_STATUS)){
//            menu.getItem(3).setVisible(true);
//        } else {
//            menu.getItem(3).setVisible(false);
//        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null){
                String text = matches.get(0).toString();
                taskField.setText(text);
            }
        }
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

    @Override
    public int getRepeats() {
        return repeats;
    }

    @Override
    public String getGroup() {
        return groupId;
    }

    private class SpinnerItem {
        private String title;
        private int icon;

        public SpinnerItem(String title, int icon) {
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

        private ImageView imgIcon;
        private RoboTextView txtTitle;
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
            imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
            txtTitle = (RoboTextView) convertView.findViewById(R.id.txtTitle);
            imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
            imgIcon.setVisibility(View.GONE);
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
            RelativeLayout itemBg = (RelativeLayout) convertView.findViewById(R.id.itemBg);
            itemBg.setBackgroundColor(themeUtil.getSpinnerStyle());
            imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
            txtTitle = (RoboTextView) convertView.findViewById(R.id.txtTitle);
            imgIcon.setImageResource(spinnerNavItem.get(position).getIcon());
            if (themeUtil.isDark()) {
                txtTitle.setTextColor(themeUtil.getColor(R.color.whitePrimary));
            } else {
                txtTitle.setTextColor(themeUtil.getColor(R.color.blackPrimary));
            }
            txtTitle.setText(spinnerNavItem.get(position).getTitle());
            return convertView;
        }
    }
}
