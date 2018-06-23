package com.elementary.tasks.reminder.preview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.elementary.tasks.BuildConfig;
import com.elementary.tasks.R;
import com.elementary.tasks.core.BaseNotificationActivity;
import com.elementary.tasks.core.async.BackupTask;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.data.models.ShopItem;
import com.elementary.tasks.core.services.RepeatNotificationReceiver;
import com.elementary.tasks.core.services.SendReceiver;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.view_models.reminders.ReminderViewModel;
import com.elementary.tasks.reminder.work.BackupReminderTask;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;
import com.elementary.tasks.databinding.ActivityReminderDialogBinding;
import com.elementary.tasks.reminder.lists.ShopListRecyclerAdapter;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
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
    private ReminderViewModel viewModel;

    private ShopListRecyclerAdapter shoppingAdapter;

    private RepeatNotificationReceiver repeater = new RepeatNotificationReceiver();
    private BroadcastReceiver sentReceiver;

    @Nullable
    private Reminder mReminder;
    @Nullable
    private EventControl mControl;
    private boolean mIsResumed;

    public static Intent getLaunchIntent(Context context, int id) {
        Intent resultIntent = new Intent(context, ReminderDialogActivity.class);
        resultIntent.putExtra(Constants.INTENT_ID, id);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        return resultIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsResumed = getIntent().getBooleanExtra(Constants.INTENT_NOTIFICATION, false);
        int id = getIntent().getIntExtra(Constants.INTENT_ID, 0);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_dialog);

        binding.card.setCardBackgroundColor(getThemeUtil().getCardStyle());
        if (Module.isLollipop()) binding.card.setCardElevation(Configs.CARD_ELEVATION);
        binding.container.setVisibility(View.GONE);
        binding.subjectContainer.setVisibility(View.GONE);
        loadImage(binding.bgImage);
        colorify(binding.buttonOk, binding.buttonCall, binding.buttonCancel, binding.buttonDelay,
                binding. buttonDelayFor, binding.buttonNotification, binding.buttonEdit);
        setTextDrawable(binding.buttonDelay, String.valueOf(getPrefs().getSnoozeTime()));
        setTextDrawable(binding.buttonDelayFor, "...");
        binding.buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        binding.buttonEdit.setImageResource(R.drawable.ic_create_black_24dp);
        binding.buttonCancel.setImageResource(R.drawable.ic_clear_black_24dp);
        binding.buttonRefresh.hide();
        binding.buttonCall.setImageResource(R.drawable.ic_call_black_24dp);
        binding.buttonNotification.setImageResource(R.drawable.ic_favorite_black_24dp);

        initViewModel(id);
    }

    private void initViewModel(int id) {
        ReminderViewModel.Factory factory = new ReminderViewModel.Factory(getApplication(), id);
        viewModel = ViewModelProviders.of(this, factory).get(ReminderViewModel.class);
        viewModel.reminder.observe(this, reminder -> {
            if (reminder != null) {
                showInfo(reminder);
            } else {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();

            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case DELETED:
                        break;
                }
            }
        });
    }

    private void showInfo(Reminder reminder) {
        this.mReminder = reminder;
        this.mControl = EventControlFactory.getController(reminder);
        LogUtil.d(TAG, "showInfo: " + TimeUtil.getFullDateTime(reminder.getEventTime()));
        if (reminder.getAttachmentFile() != null) showAttachmentButton();

        CircleImageView contactPhoto = binding.contactPhoto;
        contactPhoto.setBorderColor(getThemeUtil().getColor(getThemeUtil().colorPrimary()));
        contactPhoto.setVisibility(View.GONE);

        binding.todoList.setLayoutManager(new LinearLayoutManager(this));
        binding.todoList.setVisibility(View.GONE);

        binding.remText.setText("");

        if (!TextUtils.isEmpty(reminder.getEventTime()) && Reminder.isGpsType(reminder.getType())) {
            binding.reminderTime.setText(TimeUtil.getFullDateTime(TimeUtil.getDateTimeFromGmt(reminder.getEventTime()),
                    Prefs.getInstance(this).is24HourFormatEnabled(), false));
            binding.reminderTime.setVisibility(View.VISIBLE);
        } else {
            binding.reminderTime.setVisibility(View.GONE);
        }

        if (Reminder.isKind(reminder.getType(), Reminder.Kind.CALL) || Reminder.isSame(reminder.getType(), Reminder.BY_SKYPE_VIDEO)) {
            if (!Reminder.isBase(reminder.getType(), Reminder.BY_SKYPE)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getIdFromNumber(reminder.getTarget(), this);
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
                String name = Contacts.getNameFromNumber(reminder.getTarget(), this);
                binding.remText.setText(R.string.make_call);
                String userTitle = (name != null ? name : "") + "\n" + reminder.getTarget();
                binding.contactInfo.setText(userTitle);
                binding.contactInfo.setContentDescription(userTitle);
                binding.messageView.setText(getSummary());
                binding.messageView.setContentDescription(getSummary());
            } else {
                if (Reminder.isSame(reminder.getType(), Reminder.BY_SKYPE_VIDEO)) {
                    binding.remText.setText(R.string.video_call);
                } else {
                    binding.remText.setText(R.string.skype_call);
                }
                binding.contactInfo.setText(reminder.getTarget());
                binding.contactInfo.setContentDescription(reminder.getTarget());
                binding.messageView.setText(getSummary());
                binding.messageView.setContentDescription(getSummary());
                if (TextUtils.isEmpty(getSummary())) {
                    binding.messageView.setVisibility(View.GONE);
                    binding.someView.setVisibility(View.GONE);
                }
            }
            binding.container.setVisibility(View.VISIBLE);
        } else if (Reminder.isKind(reminder.getType(), Reminder.Kind.SMS) || Reminder.isSame(reminder.getType(), Reminder.BY_SKYPE)) {
            if (!Reminder.isSame(reminder.getType(), Reminder.BY_SKYPE)) {
                contactPhoto.setVisibility(View.VISIBLE);
                long conID = Contacts.getIdFromNumber(reminder.getTarget(), this);
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
                String name = Contacts.getNameFromNumber(reminder.getTarget(), this);
                binding.remText.setText(R.string.send_sms);
                String userInfo = (name != null ? name : "") + "\n" + reminder.getTarget();
                binding.contactInfo.setText(userInfo);
                binding.contactInfo.setContentDescription(userInfo);
                binding.messageView.setText(getSummary());
                binding.messageView.setContentDescription(getSummary());
            } else {
                binding.remText.setText(R.string.skype_chat);
                binding.contactInfo.setText(reminder.getTarget());
                binding.contactInfo.setContentDescription(reminder.getTarget());
                binding.messageView.setText(getSummary());
                binding.messageView.setContentDescription(getSummary());
            }
            if (!getPrefs().isAutoSmsEnabled()) {
                binding.buttonCall.show();
                binding. buttonCall.setImageResource(R.drawable.ic_send_black_24dp);
                binding.buttonCall.setContentDescription(getString(R.string.acc_button_send_message));
            } else {
                binding. buttonCall.hide();
                binding.buttonDelay.hide();
                binding.buttonDelayFor.hide();
            }
            binding.container.setVisibility(View.VISIBLE);
        } else if (Reminder.isSame(reminder.getType(), Reminder.BY_DATE_EMAIL)) {
            binding.buttonCall.show();
            binding.buttonCall.setImageResource(R.drawable.ic_send_black_24dp);
            binding.buttonCall.setContentDescription(getString(R.string.acc_button_send_message));
            binding.remText.setText(R.string.e_mail);
            int conID = Contacts.getIdFromMail(reminder.getTarget(), this);
            if (conID != 0) {
                Uri photo = Contacts.getPhoto(conID);
                if (photo != null) contactPhoto.setImageURI(photo);
                else contactPhoto.setVisibility(View.GONE);
                String name = Contacts.getNameFromMail(reminder.getTarget(), this);
                String userInfo = (name != null ? name : "") + "\n" + reminder.getTarget();
                binding.contactInfo.setText(userInfo);
                binding.contactInfo.setContentDescription(userInfo);
            } else {
                binding.contactInfo.setText(reminder.getTarget());
                binding.contactInfo.setContentDescription(reminder.getTarget());
            }
            binding.messageView.setText(getSummary());
            binding.messageView.setContentDescription(getSummary());
            binding.subjectView.setText(reminder.getSubject());
            binding.subjectView.setContentDescription(reminder.getSubject());
            binding.container.setVisibility(View.VISIBLE);
            binding.subjectContainer.setVisibility(View.VISIBLE);
        } else if (Reminder.isSame(reminder.getType(), Reminder.BY_DATE_APP)) {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(reminder.getTarget(), 0);
            } catch (final PackageManager.NameNotFoundException ignored) {
            }
            String nameA = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
            String label = getSummary() + "\n\n" + nameA + "\n" + reminder.getTarget();
            binding.remText.setText(label);
            binding.remText.setContentDescription(label);
            binding.buttonCall.show();
            binding.buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
            binding.buttonCall.setContentDescription(getString(R.string.acc_button_open_application));
        } else if (Reminder.isSame(reminder.getType(), Reminder.BY_DATE_LINK)) {
            String label = getSummary() + "\n\n" + reminder.getTarget();
            binding.remText.setText(label);
            binding.remText.setContentDescription(label);
            binding.buttonCall.show();
            binding.buttonCall.setImageResource(R.drawable.ic_open_in_browser_black_24dp);
            binding.buttonCall.setContentDescription(getString(R.string.acc_button_open_link_in_browser));
        } else if (Reminder.isSame(reminder.getType(), Reminder.BY_DATE_SHOP)) {
            binding.remText.setText(getSummary());
            binding.remText.setContentDescription(getSummary());
            binding.buttonCall.hide();
            loadData();
        } else {
            binding.remText.setText(getSummary());
            binding.remText.setContentDescription(getSummary());
            binding.buttonCall.hide();
        }

        if (Reminder.isBase(reminder.getType(), Reminder.BY_TIME)) {
            binding.buttonRefresh.show();
            binding.buttonRefresh.setOnClickListener(v -> startAgain());
        } else {
            binding.buttonRefresh.hide();
        }

        if (Reminder.isGpsType(reminder.getType())) {
            binding.buttonDelay.hide();
            binding.buttonDelayFor.hide();
        }

        if (!mControl.canSkip()) {
            binding.buttonCancel.hide();
        } else {
            binding.buttonCancel.show();
        }

        binding.buttonCancel.setOnClickListener(v -> cancel());
        binding.buttonNotification.setOnClickListener(v -> favourite());
        binding.buttonOk.setOnClickListener(v -> ok());
        binding.buttonEdit.setOnClickListener(v -> editReminder());
        binding.buttonDelay.setOnClickListener(v -> delay());
        binding.buttonDelayFor.setOnClickListener(v -> {
            showDialog();
            repeater.cancelAlarm(this, getId());
            discardNotification(getId());
        });
        binding.buttonCall.setOnClickListener(v -> call());
        if (Reminder.isKind(reminder.getType(), Reminder.Kind.SMS) && isAutoEnabled()) {
            sendSMS();
        } else if (Reminder.isKind(reminder.getType(), Reminder.Kind.CALL) && isAutoCallEnabled()) {
            call();
        } else if (isAppType() && isAutoLaunchEnabled()) {
            openApplication();
        } else {
            showReminder();
        }
        if (isRepeatEnabled()) {
            repeater.setAlarm(this, getId());
        }
        if (isTtsEnabled()) {
            startTts();
        }
    }

    private void startAgain() {
        if (mControl != null) {
            mControl.next();
            mControl.onOff();
            removeFlags();
            cancelTasks();
        }
        finish();
    }

    private void showAttachmentButton() {
        if (binding.buttonAttachment != null) {
            binding.buttonAttachment.show();
            binding.buttonAttachment.setOnClickListener(view -> showFile());
        }
    }

    private void showFile() {
        if (mReminder == null) return;
        String path = mReminder.getAttachmentFile();
        if (path == null) return;
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Module.isNougat()) {
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(path));
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.parse("file://" + path), mime.getMimeTypeFromExtension(fileExt(mReminder.getAttachmentFile()).substring(1)));
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.cant_find_app_for_that_file_type, Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private String fileExt(@Nullable String url) {
        if (url == null) return "";
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return "";
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    private boolean isAppType() {
        return mReminder != null && (Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_LINK) ||
                Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_APP));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sentReceiver != null) {
            unregisterReceiver(sentReceiver);
        }
        removeFlags();
        if (getPrefs().isAutoBackupEnabled()) {
            new BackupTask(this).execute();
        }
        new BackupReminderTask(this).execute();
    }

    @Override
    public void onBackPressed() {
        discardMedia();
        if (getPrefs().isFoldingEnabled()) {
            repeater.cancelAlarm(this, getId());
            removeFlags();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show();
        }
    }

    public void openApplication() {
        if (mReminder == null) return;
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
        repeater.cancelAlarm(this, getId());
    }

    public void showDialog() {
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
        AlertDialog.Builder builder = Dialogues.getDialog(this);
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
            if (mControl != null) mControl.setDelay(x);
            Toast.makeText(this, getString(R.string.reminder_snoozed), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            removeFlags();
            finish();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendSMS() {
        if (mReminder == null || TextUtils.isEmpty(getSummary())) return;
        if (!Permissions.checkPermission(this, Permissions.SEND_SMS)) {
            Permissions.requestPermission(this, SMS_PERM, Permissions.SEND_SMS);
            return;
        }
        showProgressDialog(getString(R.string.sending_message));
        String SENT = "SMS_SENT";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        registerReceiver(sentReceiver = new SendReceiver(mSendListener), new IntentFilter(SENT));
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(mReminder.getTarget(), null, getSummary(), sentPI, null);
    }

    private void showReminder() {
        if (!isTtsEnabled()) {
            showReminderNotification(this);
        } else {
            showTTSNotification(this);
        }
    }

    private boolean isAutoCallEnabled() {
        if (mReminder == null) return false;
        boolean is = getPrefs().isAutoCallEnabled();
        if (!isGlobal()) {
            is = mReminder.isAuto();
        }
        return is;
    }

    private boolean isAutoLaunchEnabled() {
        if (mReminder == null) return false;
        boolean is = getPrefs().isAutoLaunchEnabled();
        if (!isGlobal()) {
            is = mReminder.isAuto();
        }
        return is;
    }

    private boolean isAutoEnabled() {
        if (mReminder == null) return false;
        boolean is = getPrefs().isAutoSmsEnabled();
        if (!isGlobal()) {
            is = mReminder.isAuto();
        }
        return is;
    }

    private void editReminder() {
        if (mReminder == null || mControl == null) return;
        mControl.stop();
        removeFlags();
        cancelTasks();
        startActivity(new Intent(this, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, mReminder.getUuId()));
        finish();
    }

    private boolean isRepeatEnabled() {
        if (mReminder == null) return false;
        boolean isRepeat = getPrefs().isNotificationRepeatEnabled();
        if (!isGlobal()) {
            isRepeat = mReminder.isRepeatNotification();
        }
        return isRepeat;
    }

    private boolean isTtsEnabled() {
        if (mReminder == null) return false;
        boolean isTTS = getPrefs().isTtsEnabled();
        if (!isGlobal()) {
            isTTS = mReminder.isNotifyByVoice();
        }
        LogUtil.d(TAG, "isTtsEnabled: " + isTTS);
        return isTTS;
    }

    private void loadData() {
        if (mReminder == null) return;
        shoppingAdapter = new ShopListRecyclerAdapter(this, mReminder.getShoppings(),
                new ShopListRecyclerAdapter.ActionListener() {
                    @Override
                    public void onItemCheck(int position, boolean isChecked) {
                        ShopItem item = shoppingAdapter.getItem(position);
                        item.setChecked(!item.isChecked());
                        shoppingAdapter.updateData();
                        viewModel.saveReminder(mReminder.setShoppings(shoppingAdapter.getData()));
                    }

                    @Override
                    public void onItemDelete(int position) {
                        shoppingAdapter.delete(position);
                        viewModel.saveReminder(mReminder.setShoppings(shoppingAdapter.getData()));
                    }
                });
        binding.todoList.setAdapter(shoppingAdapter);
        binding.todoList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void call() {
        if (mReminder == null || mControl == null) return;
        mControl.next();
        removeFlags();
        cancelTasks();
        if (Reminder.isKind(mReminder.getType(), Reminder.Kind.SMS)) {
            sendSMS();
        } else if (Reminder.isBase(mReminder.getType(), Reminder.BY_SKYPE)) {
            if (!SuperUtil.isSkypeClientInstalled(this)) {
                showInstallSkypeDialog();
                return;
            }
            if (Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE_CALL)) {
                TelephonyUtil.skypeCall(mReminder.getTarget(), this);
            } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE_VIDEO)) {
                TelephonyUtil.skypeVideoCall(mReminder.getTarget(), this);
            } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_SKYPE)) {
                TelephonyUtil.skypeChat(mReminder.getTarget(), this);
            }
        } else if (isAppType()) {
            openApplication();
        } else if (Reminder.isSame(mReminder.getType(), Reminder.BY_DATE_EMAIL)) {
            TelephonyUtil.sendMail(this, mReminder.getTarget(),
                    mReminder.getSubject(), getSummary(), mReminder.getAttachmentFile());
        } else {
            makeCall();
        }
        if (!Reminder.isKind(mReminder.getType(), Reminder.Kind.SMS)) {
            finish();
        }
    }

    private void showInstallSkypeDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setMessage(R.string.skype_is_not_installed);
        builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            dialogInterface.dismiss();
            SuperUtil.installSkype(this);
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create().show();
    }

    private void makeCall() {
        if (mReminder == null) return;
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE)) {
            TelephonyUtil.makeCall(mReminder.getTarget(), this);
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE);
        }
    }

    @Override
    protected void delay() {
        if (mControl != null) {
            int delay = getPrefs().getSnoozeTime();
            mControl.setDelay(delay);
            removeFlags();
            cancelTasks();
        }
        finish();
    }

    @Override
    protected void cancel() {
        if (mControl != null) {
            mControl.stop();
            removeFlags();
            cancelTasks();
        }
        finish();
    }

    @Override
    protected void favourite() {
        if (mControl != null) {
            mControl.next();
            removeFlags();
            cancelTasks();
            showFavouriteNotification();
        }
        finish();
    }

    @Override
    protected void ok() {
        if (mControl != null) {
            mControl.next();
            removeFlags();
            cancelTasks();
        }
        finish();
    }

    @Override
    protected void showSendingError() {
        showReminder();
        binding.remText.setText(getString(R.string.error_sending));
        binding.remText.setContentDescription(getString(R.string.error_sending));
        binding.buttonCall.setImageResource(R.drawable.ic_refresh);
        binding.buttonCall.setContentDescription(getString(R.string.acc_button_retry_to_send_message));
        if (binding.buttonCall.getVisibility() == View.GONE) {
            binding.buttonCall.show();
        }
    }

    @Override
    protected String getMelody() {
        if (mReminder == null) return "";
        return mReminder.getMelodyPath();
    }

    @Override
    protected boolean isScreenResumed() {
        return mIsResumed;
    }

    @Override
    protected boolean isVibrate() {
        if (mReminder == null) return false;
        boolean isVibrate = getPrefs().isVibrateEnabled();
        if (!isGlobal()) isVibrate = mReminder.isVibrate();
        return isVibrate;
    }

    @Override
    protected String getSummary() {
        if (mReminder == null) return "";
        return mReminder.getSummary();
    }

    @Override
    protected String getUuId() {
        if (mReminder == null) return "";
        return mReminder.getUuId();
    }

    @Override
    protected int getId() {
        if (mReminder == null) return 0;
        return mReminder.getUniqueId();
    }

    @Override
    protected int getLedColor() {
        if (mReminder == null) return 0;
        if (Module.isPro()) {
            if (mReminder.getColor() != -1) {
                return LED.getLED(mReminder.getColor());
            } else {
                return LED.getLED(getPrefs().getLedColor());
            }
        }
        return LED.getLED(0);
    }

    @Override
    protected boolean isAwakeDevice() {
        if (mReminder == null) return false;
        boolean is = getPrefs().isDeviceAwakeEnabled();
        if (!isGlobal()) is = mReminder.isAwake();
        return is;
    }

    @Override
    protected boolean isGlobal() {
        return mReminder != null && mReminder.isUseGlobal();
    }

    @Override
    protected boolean isUnlockDevice() {
        if (mReminder == null) return false;
        boolean is = getPrefs().isDeviceUnlockEnabled();
        if (!isGlobal()) is = mReminder.isUnlock();
        return is;
    }

    @Override
    protected int getMaxVolume() {
        if (mReminder == null) return 25;
        if (!isGlobal() && mReminder.getVolume() != -1) return mReminder.getVolume();
        else return getPrefs().getLoudness();
    }

    private void showFavouriteNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER);
        builder.setContentTitle(getSummary());
        String appName;
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
        } else {
            appName = getString(R.string.app_name);
        }
        builder.setContentText(appName);
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
            builder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white);
        }
        boolean isWear = getPrefs().isWearEnabled();
        if (isWear && Module.isJellyMR2()) {
            builder.setOnlyAlertOnce(true);
            builder.setGroup("GROUP");
            builder.setGroupSummary(true);
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
        mNotifyMgr.notify(getId(), builder.build());
        if (isWear) {
            showWearNotification(appName);
        }
    }

    private void showReminderNotification(Activity activity) {
        LogUtil.d(TAG, "showReminderNotification: ");
        Intent notificationIntent = new Intent(this, activity.getClass());
        notificationIntent.putExtra(Constants.INTENT_ID, getUuId());
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent intent = PendingIntent.getActivity(this, getId(), notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER);
        builder.setContentTitle(getSummary());
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (getPrefs().isManualRemoveEnabled()) {
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        String appName;
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
            if (getPrefs().isLedEnabled()) {
                builder.setLights(getLedColor(), 500, 1000);
            }
        } else {
            appName = getString(R.string.app_name);
        }
        builder.setContentText(appName);
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
            builder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white);
        }
        if (getSound() != null && !isScreenResumed() && (!SuperUtil.isDoNotDisturbEnabled(this) ||
                (SuperUtil.checkNotificationPermission(this) && getPrefs().isSoundInSilentModeEnabled()))) {
            Uri soundUri = getSoundUri();
            LogUtil.d(TAG, "showReminderNotification: " + soundUri);
            getSound().playAlarm(soundUri, getPrefs().isInfiniteSoundEnabled());
        }
        if (isVibrate()) {
            long[] pattern;
            if (getPrefs().isInfiniteVibrateEnabled()) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        boolean isWear = getPrefs().isWearEnabled();
        if (isWear && Module.isJellyMR2()) {
            builder.setOnlyAlertOnce(true);
            builder.setGroup("GROUP");
            builder.setGroupSummary(true);
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
        mNotifyMgr.notify(getId(), builder.build());
        if (isWear) {
            showWearNotification(appName);
        }
    }

    protected void showTTSNotification(Activity activityClass) {
        LogUtil.d(TAG, "showTTSNotification: ");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER);
        builder.setContentTitle(getSummary());
        Intent notificationIntent = new Intent(this, activityClass.getClass());
        notificationIntent.putExtra(Constants.INTENT_ID, getUuId());
        notificationIntent.putExtra(Constants.INTENT_NOTIFICATION, true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent intent = PendingIntent.getActivity(this, getId(), notificationIntent, 0);
        builder.setContentIntent(intent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (getPrefs().isManualRemoveEnabled()) {
            builder.setOngoing(false);
        } else {
            builder.setOngoing(true);
        }
        String appName;
        if (Module.isPro()) {
            appName = getString(R.string.app_name_pro);
            if (getPrefs().isLedEnabled()) {
                builder.setLights(getLedColor(), 500, 1000);
            }
        } else {
            appName = getString(R.string.app_name);
        }
        builder.setContentText(appName);
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_notifications_white_24dp);
            builder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
        } else {
            builder.setSmallIcon(R.drawable.ic_notification_nv_white);
        }
        if (!isScreenResumed() && (!SuperUtil.isDoNotDisturbEnabled(this) ||
                (SuperUtil.checkNotificationPermission(this) && getPrefs().isSoundInSilentModeEnabled()))) {
            playDefaultMelody();
        }
        if (isVibrate()) {
            long[] pattern;
            if (getPrefs().isInfiniteVibrateEnabled()) {
                pattern = new long[]{150, 86400000};
            } else {
                pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            }
            builder.setVibrate(pattern);
        }
        boolean isWear = getPrefs().isWearEnabled();
        if (isWear && Module.isJellyMR2()) {
            builder.setOnlyAlertOnce(true);
            builder.setGroup("GROUP");
            builder.setGroupSummary(true);
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(this);
        mNotifyMgr.notify(getId(), builder.build());
        if (isWear) {
            showWearNotification(appName);
        }
    }

    private void playDefaultMelody() {
        if (getSound() == null) return;
        LogUtil.d(TAG, "playDefaultMelody: ");
        try {
            AssetFileDescriptor afd = getAssets().openFd("sounds/beep.mp3");
            getSound().playAlarm(afd);
        } catch (IOException e) {
            e.printStackTrace();
            getSound().playAlarm(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
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
