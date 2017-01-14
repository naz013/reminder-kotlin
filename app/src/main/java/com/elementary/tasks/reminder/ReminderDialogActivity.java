package com.elementary.tasks.reminder;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.backdoor.shared.SharedConst;
import com.elementary.tasks.R;
import com.elementary.tasks.core.BaseNotificationActivity;
import com.elementary.tasks.core.async.BackupTask;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlImpl;
import com.elementary.tasks.core.services.RepeatNotificationReceiver;
import com.elementary.tasks.core.services.SendReceiver;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.ActivityReminderDialogBinding;
import com.elementary.tasks.reminder.models.Reminder;
import com.elementary.tasks.reminder.models.ShopItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class ReminderDialogActivity extends BaseNotificationActivity {

    private static final String TAG = "ReminderDialogActivity";
    private static final int CALL_PERM = 612;
    private static final int SMS_PERM = 613;
    private ActivityReminderDialogBinding binding;
    private FloatingActionButton buttonDelay;
    private FloatingActionButton buttonCancel;
    private RecyclerView todoList;

    private ShopListRecyclerAdapter shoppingAdapter;

    private RepeatNotificationReceiver repeater = new RepeatNotificationReceiver();
    private BroadcastReceiver sentReceiver;

    private Reminder mReminder;
    private EventControl mControl;
    private boolean mIsResumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsResumed = getIntent().getBooleanExtra(Constants.INTENT_NOTIFICATION, false);
        mReminder = RealmDb.getInstance().getReminder(getIntent().getStringExtra(Constants.INTENT_ID));
        mControl = EventControlImpl.getController(this, mReminder);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + TimeUtil.getFullDateTime(mReminder.getEventTime()));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_dialog);
        binding.card.setCardBackgroundColor(themeUtil.getCardStyle());
        if (Module.isLollipop()) binding.card.setCardElevation(Configs.CARD_ELEVATION);
        binding.singleContainer.setVisibility(View.VISIBLE);
        binding.container.setVisibility(View.GONE);
        binding.subjectContainer.setVisibility(View.GONE);
        loadImage(binding.bgImage);
        buttonCancel = (FloatingActionButton) findViewById(R.id.buttonCancel);
        FloatingActionButton buttonCall = (FloatingActionButton) findViewById(R.id.buttonCall);
        buttonDelay = (FloatingActionButton) findViewById(R.id.buttonDelay);
        FloatingActionButton buttonDelayFor = (FloatingActionButton) findViewById(R.id.buttonDelayFor);
        FloatingActionButton buttonNotification = (FloatingActionButton) findViewById(R.id.buttonNotification);
        colorify(binding.buttonOk, buttonCall, buttonCancel, buttonDelay, buttonDelayFor,
                buttonNotification, binding.buttonEdit);
        setTextDrawable(buttonDelay, String.valueOf(mPrefs.getSnoozeTime()));
        setTextDrawable(buttonDelayFor, "...");
        binding.buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        binding.buttonEdit.setImageResource(R.drawable.ic_create_black_24dp);
        buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp);
        buttonCall.setImageResource(R.drawable.ic_call_black_24dp);
        buttonNotification.setImageResource(R.drawable.ic_favorite_black_24dp);

        CircleImageView contactPhoto = binding.contactPhoto;
        contactPhoto.setBorderColor(themeUtil.getColor(themeUtil.colorPrimary()));
        contactPhoto.setVisibility(View.GONE);

        todoList = (RecyclerView) findViewById(R.id.todoList);
        todoList.setLayoutManager(new LinearLayoutManager(this));
        todoList.setVisibility(View.GONE);

        TextView remText = (TextView) findViewById(R.id.remText);
        TextView contactInfo = (TextView) findViewById(R.id.contactInfo);
        TextView subjectView = (TextView) findViewById(R.id.subjectView);
        TextView messageView = (TextView) findViewById(R.id.messageView);
        remText.setText("");

        if (Reminder.isKind(mReminder.getType(), Reminder.Kind.CALL) || Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE_VIDEO)) {
            if (!Reminder.isBase(mReminder.getType(), Reminder.BY_SKYPE)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getIdFromNumber(mReminder.getTarget(), ReminderDialogActivity.this);
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
                String name = Contacts.getNameFromNumber(mReminder.getTarget(), ReminderDialogActivity.this);
                if (name == null) name = "";
                remText.setText(R.string.make_call);
                contactInfo.setText(name + "\n" + mReminder.getTarget());
                messageView.setText(getSummary());
            } else {
                if (Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE_VIDEO)) {
                    remText.setText(R.string.video_call);
                } else {
                    remText.setText(R.string.skype_call);
                }
                contactInfo.setText(mReminder.getTarget());
                messageView.setText(getSummary());
                if (TextUtils.isEmpty(getSummary())) {
                    messageView.setVisibility(View.GONE);
                    binding.someView.setVisibility(View.GONE);
                }
            }
            binding.container.setVisibility(View.VISIBLE);
        } else if (Reminder.isKind(mReminder.getType(), Reminder.Kind.SMS) || Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE)) {
            if (!Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getIdFromNumber(mReminder.getTarget(), ReminderDialogActivity.this);
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
                String name = Contacts.getNameFromNumber(mReminder.getTarget(), ReminderDialogActivity.this);
                if (name == null) name = "";
                remText.setText(R.string.send_sms);
                contactInfo.setText(name + "\n" + mReminder.getTarget());
                messageView.setText(getSummary());
            } else {
                remText.setText(R.string.skype_chat);
                contactInfo.setText(mReminder.getTarget());
                messageView.setText(getSummary());
            }
            if (!mPrefs.isAutoSmsEnabled()) {
                buttonCall.setVisibility(View.VISIBLE);
                buttonCall.setImageResource(R.drawable.ic_send_black_24dp);
            } else {
                buttonCall.setVisibility(View.GONE);
                buttonDelay.setVisibility(View.GONE);
                buttonDelayFor.setVisibility(View.GONE);
            }
            binding.container.setVisibility(View.VISIBLE);
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_EMAIL)) {
            buttonCall.setVisibility(View.VISIBLE);
            buttonCall.setImageResource(R.drawable.ic_send_black_24dp);
            remText.setText(R.string.e_mail);
            int conID = Contacts.getIdFromMail(mReminder.getTarget(), this);
            if (conID != 0) {
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
                String name = Contacts.getNameFromMail(mReminder.getTarget(), ReminderDialogActivity.this);
                if (name == null) name = "";
                contactInfo.setText(name + "\n" + mReminder.getTarget());
            } else {
                contactInfo.setText(mReminder.getTarget());
            }
            messageView.setText(getSummary());
            subjectView.setText(mReminder.getSubject());
            binding.container.setVisibility(View.VISIBLE);
            binding.subjectContainer.setVisibility(View.VISIBLE);
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_APP)) {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(mReminder.getTarget(), 0);
            } catch (final PackageManager.NameNotFoundException ignored) {
            }
            final String nameA = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
            remText.setText(getSummary() + "\n\n" + nameA + "\n" + mReminder.getTarget());
            buttonCall.setVisibility(View.VISIBLE);
            buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_LINK)) {
            remText.setText(getSummary() + "\n\n" + mReminder.getTarget());
            buttonCall.setVisibility(View.VISIBLE);
            buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_SHOP)) {
            remText.setText(getSummary());
            buttonCall.setVisibility(View.GONE);
            loadData();
        } else {
            remText.setText(getSummary());
            buttonCall.setVisibility(View.GONE);
        }

        if (Reminder.isGpsType(mReminder.getType())){
            buttonDelay.setVisibility(View.GONE);
            buttonDelayFor.setVisibility(View.GONE);
        }

        if (!mControl.canSkip()) {
            buttonCancel.setVisibility(View.GONE);
        } else {
            buttonCancel.setVisibility(View.VISIBLE);
        }

        buttonCancel.setOnClickListener(v -> cancel());
        buttonNotification.setOnClickListener(v -> favourite());
        binding.buttonOk.setOnClickListener(v -> ok());
        binding.buttonEdit.setOnClickListener(v -> editReminder());
        buttonDelay.setOnClickListener(v -> delay());
        buttonDelayFor.setOnClickListener(v -> {
            showDialog();
            repeater.cancelAlarm(ReminderDialogActivity.this, getId());
            discardNotification(getId());
        });
        buttonCall.setOnClickListener(v -> call());
        if (Reminder.isKind(mReminder.getType(), Reminder.Kind.SMS)) {
            if (isAutoEnabled()) {
                sendSMS();
            } else {
                showReminder();
            }
        } else if (isAppType()) {
            if (isAutoLaunchEnabled()) {
                openApplication();
            } else {
                showReminder();
            }
        } else {
            showReminder();
        }
        if (isRepeatEnabled()) {
            repeater.setAlarm(ReminderDialogActivity.this, getUuId(), getId());
        }
        if (isTtsEnabled()) {
            startTts();
        }
    }

    private boolean isAppType() {
        return Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_LINK) || Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_APP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SuperUtil.isGooglePlayServicesAvailable(this)) {
            mTracker.setScreenName("Reminder " + mReminder.getType());
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        if (mPrefs.isWearEnabled()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPrefs.isWearEnabled()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver);
        }
//        notifier.recreatePermanent();
        removeFlags();
        if (mPrefs.isAutoBackupEnabled()) {
            new BackupTask(this).execute();
        }
    }

    @Override
    public void onBackPressed() {
        discardMedia();
        if (mPrefs.isFoldingEnabled()){
            repeater.cancelAlarm(ReminderDialogActivity.this, getId());
            removeFlags();
            finish();
        } else {
            Toast.makeText(ReminderDialogActivity.this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show();
        }
    }

    public void openApplication() {
        if (Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_APP)) {
            TelephonyUtil.openApp(mReminder.getTarget(), this);
        } else {
            TelephonyUtil.openLink(mReminder.getTarget(), this);
        }
        cancelTasks();
        finish();
    }

    private void cancelTasks() {
        discardNotification(getId());
        repeater.cancelAlarm(ReminderDialogActivity.this, getId());
    }

    public void showDialog(){
        final CharSequence[] items = {String.format(getString(R.string.x_minutes), String.valueOf(5)),
                String.format(getString(R.string.x_minutes), String.valueOf(10)),
                String.format(getString(R.string.x_minutes), String.valueOf(15)),
                String.format(getString(R.string.x_minutes), String.valueOf(30)),
                String.format(getString(R.string.x_minutes), String.valueOf(45)),
                String.format(getString(R.string.x_minutes), String.valueOf(60)),
                String.format(getString(R.string.x_minutes), String.valueOf(90)),
                String.format(getString(R.string.x_hours), String.valueOf(2)),
                String.format(getString(R.string.x_hours), String.valueOf(6)),
                String.format(getString(R.string.x_hours), String.valueOf(24)),
                String.format(getString(R.string.x_days), String.valueOf(2)),
                String.format(getString(R.string.x_days), String.valueOf(7))};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_time));
        builder.setItems(items, (dialog, item1) -> {
            int x = 0;
            if (item1 == 0) {
                x = 5;
            } else if (item1 == 1) {
                x = 10;
            } else if (item1 == 2) {
                x = 15;
            } else if (item1 == 3) {
                x = 30;
            } else if (item1 == 4) {
                x = 45;
            } else if (item1 == 5) {
                x = 60;
            } else if (item1 == 6) {
                x = 90;
            } else if (item1 == 7) {
                x = 120;
            } else if (item1 == 8) {
                x = 60 * 6;
            } else if (item1 == 9) {
                x = 60 * 24;
            } else if (item1 == 10) {
                x = 60 * 24 * 2;
            } else if (item1 == 11) {
                x = 60 * 24 * 7;
            }
            mControl.setDelay(x);
            Toast.makeText(ReminderDialogActivity.this, getString(R.string.reminder_snoozed), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            removeFlags();
            finish();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendSMS() {
        if (!Permissions.checkPermission(this, Permissions.SEND_SMS)) {
            Permissions.requestPermission(this, SMS_PERM, Permissions.SEND_SMS);
        }
        showProgressDialog(getString(R.string.sending_message));
        String SENT = "SMS_SENT";
        PendingIntent sentPI = PendingIntent.getBroadcast(ReminderDialogActivity.this, 0, new Intent(SENT), 0);
        registerReceiver(sentReceiver = new SendReceiver(mSendListener), new IntentFilter(SENT));
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(mReminder.getTarget(), null, getSummary(), sentPI, null);
    }

    private void showReminder(){
        if (!isTtsEnabled()) {
            showReminderNotification(this);
        } else {
            showTTSNotification(this);
        }
    }

    private boolean isAutoLaunchEnabled() {
        boolean isRepeat = mPrefs.isAutoLaunchEnabled();
        if (!isGlobal()) {
            isRepeat = mReminder.isAuto();
        }
        return isRepeat;
    }

    private boolean isAutoEnabled() {
        boolean isRepeat = mPrefs.isAutoSmsEnabled();
        if (!isGlobal()) {
            isRepeat = mReminder.isAuto();
        }
        return isRepeat;
    }

    private void editReminder() {
        mControl.stop();
        removeFlags();
        cancelTasks();
        startActivity(new Intent(this, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, mReminder.getUuId()));
        finish();
    }

    private boolean isRepeatEnabled() {
        boolean isRepeat = mPrefs.isNotificationRepeatEnabled();
        if (!isGlobal()) {
            isRepeat = mReminder.isRepeatNotification();
        }
        return isRepeat;
    }

    private boolean isTtsEnabled() {
        boolean isTTS = mPrefs.isTtsEnabled();
        if (!isGlobal()) {
            isTTS = mReminder.isNotifyByVoice();
        }
        return isTTS;
    }

    private void loadData() {
        shoppingAdapter = new ShopListRecyclerAdapter(this, mReminder.getShoppings(),
                new ShopListRecyclerAdapter.ActionListener() {
                    @Override
                    public void onItemCheck(int position, boolean isChecked) {
                        ShopItem item = shoppingAdapter.getItem(position);
                        item.setChecked(!item.isChecked());
                        shoppingAdapter.updateData();
                        RealmDb.getInstance().saveObject(mReminder.setShoppings(shoppingAdapter.getData()));
                    }

                    @Override
                    public void onItemDelete(int position) {
                        shoppingAdapter.delete(position);
                        RealmDb.getInstance().saveObject(mReminder.setShoppings(shoppingAdapter.getData()));
                    }
                });
        todoList.setAdapter(shoppingAdapter);
        todoList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void sendDataToWear() {
        boolean silentSMS = mPrefs.isAutoSmsEnabled();
        if (Reminder.isKind(mReminder.getType(), Reminder.Kind.SMS) && silentSMS)
            return;
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.WEAR_REMINDER);
        DataMap map = putDataMapReq.getDataMap();
        map.putInt(SharedConst.KEY_TYPE, mReminder.getType());
        map.putString(SharedConst.KEY_TASK, getSummary());
        map.putInt(SharedConst.KEY_COLOR, themeUtil.colorAccent());
        map.putBoolean(SharedConst.KEY_THEME, themeUtil.isDark());
        map.putBoolean(SharedConst.KEY_REPEAT, buttonCancel.getVisibility() == View.VISIBLE);
        map.putBoolean(SharedConst.KEY_TIMED, buttonDelay.getVisibility() == View.VISIBLE);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    @Override
    protected void call() {
        if (Reminder.isKind(mReminder.getType(), Reminder.Kind.SMS)){
            sendSMS();
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE_CALL)){
            TelephonyUtil.skypeCall(mReminder.getTarget(), this);
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE_VIDEO)){
            TelephonyUtil.skypeVideoCall(mReminder.getTarget(), this);
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE)){
            TelephonyUtil.skypeChat(mReminder.getTarget(), this);
        } else if (isAppType()){
            openApplication();
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_EMAIL)){
            TelephonyUtil.sendMail(ReminderDialogActivity.this, mReminder.getTarget(), mReminder.getSubject(), getSummary(), mReminder.getAttachmentFile());
        } else {
            makeCall();
        }
        removeFlags();
        cancelTasks();
        if (!Reminder.isKind(mReminder.getType(), Reminder.Kind.SMS)){
            finish();
        }
    }

    private void makeCall() {
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mReminder.getTarget(), ReminderDialogActivity.this);
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE);
        }
    }

    @Override
    protected void delay() {
        int delay = mPrefs.getSnoozeTime();
        mControl.setDelay(delay);
        removeFlags();
        cancelTasks();
        finish();
    }

    @Override
    protected void cancel() {
        mControl.stop();
        removeFlags();
        cancelTasks();
        finish();
    }

    @Override
    protected void favourite() {
        mControl.next();
        removeFlags();
        cancelTasks();
        showFavouriteNotification();
        finish();
    }

    @Override
    protected void ok() {
        mControl.next();
        removeFlags();
        cancelTasks();
        finish();
    }

    @Override
    protected void showSendingError() {
        showReminder();
        binding.remText.setText(getString(R.string.error_sending));
        binding.buttonCall.setImageResource(R.drawable.ic_refresh);
        if (binding.buttonCall.getVisibility() == View.GONE) {
            binding.buttonCall.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected String getMelody() {
        return mReminder.getMelodyPath();
    }

    @Override
    protected boolean isScreenResumed() {
        return mIsResumed;
    }

    @Override
    protected boolean isVibrate() {
        boolean isVibrate = mPrefs.isVibrateEnabled();
        if (!isGlobal()) isVibrate = mReminder.isVibrate();
        return isVibrate;
    }

    @Override
    protected String getSummary() {
        return mReminder.getSummary();
    }

    @Override
    protected String getUuId() {
        return mReminder.getUuId();
    }

    @Override
    protected int getId() {
        return mReminder.getUniqueId();
    }

    @Override
    protected int getLedColor() {
        int ledColor = mReminder.getColor();
        if (ledColor == -1) {
            ledColor = mPrefs.getLedColor();
        }
        return ledColor;
    }

    @Override
    protected boolean isAwakeDevice() {
        boolean is = mPrefs.isDeviceAwakeEnabled();
        if (!isGlobal()) is = mReminder.isAwake();
        return is;
    }

    @Override
    protected boolean isGlobal() {
        return mReminder.isUseGlobal();
    }

    @Override
    protected boolean isUnlockDevice() {
        boolean is = mPrefs.isDeviceUnlockEnabled();
        if (!isGlobal()) is = mReminder.isUnlock();
        return is;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CALL_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall();
                }
                break;
            case SMS_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMS();
                }
                break;
        }
    }
}
