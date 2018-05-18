package com.elementary.tasks.birthdays;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.BaseNotificationActivity;
import com.elementary.tasks.core.async.BackupTask;
import com.elementary.tasks.core.utils.Configs;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.LED;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.Sound;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ActivityShowBirthdayBinding;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
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

public class ShowBirthdayActivity extends BaseNotificationActivity {

    private static final int CALL_PERM = 612;
    private static final int SMS_PERM = 613;

    private ActivityShowBirthdayBinding binding;
    @Nullable
    private BirthdayItem mBirthdayItem;
    private boolean mIsResumed;
    @Nullable
    private String wearMessage;

    public static Intent getLaunchIntent(Context context, String uuId) {
        Intent resultIntent = new Intent(context, ShowBirthdayActivity.class);
        resultIntent.putExtra(Constants.INTENT_ID, uuId);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        return resultIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsResumed = getIntent().getBooleanExtra(Constants.INTENT_NOTIFICATION, false);
        mBirthdayItem = RealmDb.getInstance().getBirthday(getIntent().getStringExtra(Constants.INTENT_ID));
        super.onCreate(savedInstanceState);
        if (mBirthdayItem == null) finish();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_show_birthday);
        binding.card.setCardBackgroundColor(getThemeUtil().getCardStyle());
        if (Module.isLollipop()) {
            binding.card.setCardElevation(Configs.CARD_ELEVATION);
        }
        loadImage(binding.bgImage);
        colorify(binding.buttonOk, binding.buttonCall, binding.buttonSend);

        binding.buttonOk.setOnClickListener(view -> ok());
        binding.buttonCall.setOnClickListener(view -> call());
        binding.buttonSend.setOnClickListener(view -> sendSMS());

        binding.buttonOk.setImageResource(R.drawable.ic_done_black_24dp);
        binding.buttonCall.setImageResource(R.drawable.ic_call_black_24dp);
        binding.buttonSend.setImageResource(R.drawable.ic_send_black_24dp);

        CircleImageView contactPhoto = binding.contactPhoto;
        contactPhoto.setBorderColor(getThemeUtil().getColor(getThemeUtil().colorPrimary()));
        contactPhoto.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(mBirthdayItem.getNumber()) && checkContactPermission()) {
            mBirthdayItem.setNumber(Contacts.getNumber(mBirthdayItem.getName(), this));
        }
        if (mBirthdayItem.getContactId() == 0 && !TextUtils.isEmpty(mBirthdayItem.getNumber()) && checkContactPermission()) {
            mBirthdayItem.setContactId(Contacts.getIdFromNumber(mBirthdayItem.getNumber(), this));
        }
        Uri photo = Contacts.getPhoto(mBirthdayItem.getContactId());
        if (photo != null) {
            contactPhoto.setImageURI(photo);
        } else {
            contactPhoto.setVisibility(View.GONE);
        }
        String years = TimeUtil.getAgeFormatted(this, mBirthdayItem.getDate());
        RoboTextView userName = binding.userName;
        userName.setText(mBirthdayItem.getName());
        userName.setContentDescription(mBirthdayItem.getName());
        RoboTextView userNumber = findViewById(R.id.userNumber);
        RoboTextView userYears = findViewById(R.id.userYears);
        userYears.setText(years);
        userYears.setContentDescription(years);
        wearMessage = mBirthdayItem.getName() + "\n" + years;
        if (TextUtils.isEmpty(mBirthdayItem.getNumber())) {
            binding.buttonCall.hide();
            binding.buttonSend.hide();
            userNumber.setVisibility(View.GONE);
        } else {
            userNumber.setText(mBirthdayItem.getNumber());
            userNumber.setContentDescription(mBirthdayItem.getNumber());
        }
        showNotification(TimeUtil.getAge(mBirthdayItem.getDate()), mBirthdayItem.getName());
        if (isTtsEnabled()) {
            startTts();
        }
    }

    private boolean checkContactPermission() {
        return Permissions.checkPermission(this, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
    }

    public void showNotification(int years, String name) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Notifier.CHANNEL_REMINDER);
        builder.setContentTitle(name);
        builder.setContentText(TimeUtil.getAgeFormatted(this, years));
        if (Module.isLollipop()) {
            builder.setSmallIcon(R.drawable.ic_cake_white_24dp);
            builder.setColor(ViewUtils.getColor(this, R.color.bluePrimary));
        } else {
            builder.setSmallIcon(R.drawable.ic_cake_nv_white);
        }
        if (!isScreenResumed() && (!SuperUtil.isDoNotDisturbEnabled(this) ||
                (SuperUtil.checkNotificationPermission(this) && isBirthdaySilentEnabled()))) {
            Sound sound = getSound();
            if (sound != null) getSound().playAlarm(getSoundUri(), isBirthdayInfiniteSound());
        }
        if (isVibrate()) {
            long[] pattern = new long[]{150, 400, 100, 450, 200, 500, 300, 500};
            if (isBirthdayInfiniteVibration()) {
                pattern = new long[]{150, 86400000};
            }
            builder.setVibrate(pattern);
        }
        if (Module.isPro()) {
            builder.setLights(getLedColor(), 500, 1000);
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
            showWearNotification(name);
        }
    }

    private boolean isBirthdaySilentEnabled() {
        boolean is = getPrefs().isSoundInSilentModeEnabled();
        if (Module.isPro() && !isGlobal()) {
            is = getPrefs().isBirthdaySilentEnabled();
        }
        return is;
    }

    private boolean isTtsEnabled() {
        boolean is = getPrefs().isTtsEnabled();
        if (Module.isPro() && !isGlobal()) {
            is = getPrefs().isBirthdayTtsEnabled();
        }
        return is;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeFlags();
        if (getPrefs().isAutoBackupEnabled()) {
            new BackupTask(this).execute();
        }
        new BackupBirthdaysTask(this).execute();
    }

    @Override
    public void onBackPressed() {
        discardMedia();
        if (getPrefs().isFoldingEnabled()) {
            removeFlags();
            finish();
        } else {
            Toast.makeText(ShowBirthdayActivity.this, getString(R.string.select_one_of_item), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void call() {
        makeCall();
    }

    private void makeCall() {
        if (Permissions.checkPermission(this, Permissions.CALL_PHONE) && mBirthdayItem != null) {
            TelephonyUtil.makeCall(mBirthdayItem.getNumber(), this);
            updateBirthday(mBirthdayItem);
        } else {
            Permissions.requestPermission(this, CALL_PERM, Permissions.CALL_PHONE);
        }
    }

    @Override
    protected void delay() {
        close();
    }

    @Override
    protected void cancel() {
        sendSMS();
    }

    private void sendSMS() {
        if (Permissions.checkPermission(ShowBirthdayActivity.this, Permissions.SEND_SMS) && mBirthdayItem != null) {
            TelephonyUtil.sendSms(mBirthdayItem.getNumber(), ShowBirthdayActivity.this);
            updateBirthday(mBirthdayItem);
        } else {
            Permissions.requestPermission(ShowBirthdayActivity.this, SMS_PERM, Permissions.SEND_SMS);
        }
    }

    @Override
    protected void favourite() {
        close();
    }

    @Override
    protected void ok() {
        updateBirthday(mBirthdayItem);
    }

    private void updateBirthday(@Nullable BirthdayItem birthdayItem) {
        if (birthdayItem != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int year = calendar.get(Calendar.YEAR);
            birthdayItem.setShowedYear(year);
            RealmDb.getInstance().saveObject(birthdayItem);
        }
        close();
    }

    private void close() {
        removeFlags();
        discardNotification(getId());
        finish();
    }

    @Override
    protected void showSendingError() {
        binding.buttonCall.setImageResource(R.drawable.ic_refresh);
        binding.buttonCall.setContentDescription(getString(R.string.acc_button_retry_to_send_message));
        if (binding.buttonCall.getVisibility() == View.GONE) {
            binding.buttonCall.show();
        }
    }

    @Override
    protected Locale getTtsLocale() {
        Locale locale = new Language().getLocale(this, false);
        if (Module.isPro() && !isGlobal()) {
            locale = new Language().getLocale(this, true);
        }
        return locale;
    }

    @Override
    protected String getMelody() {
        if (Module.isPro() && !isGlobal()) {
            return getPrefs().getBirthdayMelody();
        } else {
            return getPrefs().getMelodyFile();
        }
    }

    @Override
    protected boolean isBirthdayInfiniteVibration() {
        boolean vibrate = getPrefs().isInfiniteVibrateEnabled();
        if (Module.isPro() && !isGlobal()) {
            vibrate = getPrefs().isBirthdayInfiniteVibrationEnabled();
        }
        return vibrate;
    }

    @Override
    protected boolean isBirthdayInfiniteSound() {
        boolean isLooping = getPrefs().isInfiniteSoundEnabled();
        if (Module.isPro() && !isGlobal()) {
            isLooping = getPrefs().isBirthdayInfiniteSoundEnabled();
        }
        return isLooping;
    }

    @Override
    protected boolean isScreenResumed() {
        return mIsResumed;
    }

    @Override
    protected boolean isVibrate() {
        boolean vibrate = getPrefs().isVibrateEnabled();
        if (Module.isPro() && !isGlobal()) {
            vibrate = getPrefs().isBirthdayVibrationEnabled();
        }
        return vibrate;
    }

    @Override
    protected String getSummary() {
        return wearMessage;
    }

    @Override
    protected String getUuId() {
        if (mBirthdayItem != null) {
            return mBirthdayItem.getUuId();
        } else return "";
    }

    @Override
    protected int getId() {
        if (mBirthdayItem != null) {
            return mBirthdayItem.getUniqueId();
        } else return 0;
    }

    @Override
    protected int getLedColor() {
        int ledColor = LED.getLED(getPrefs().getLedColor());
        if (Module.isPro() && !isGlobal()) {
            ledColor = LED.getLED(getPrefs().getBirthdayLedColor());
        }
        return ledColor;
    }

    @Override
    protected boolean isAwakeDevice() {
        boolean isWake = getPrefs().isDeviceAwakeEnabled();
        if (Module.isPro() && !isGlobal()) {
            isWake = getPrefs().isBirthdayWakeEnabled();
        }
        return isWake;
    }

    @Override
    protected int getMaxVolume() {
        return getPrefs().getLoudness();
    }

    @Override
    protected boolean isGlobal() {
        return getPrefs().isBirthdayGlobalEnabled();
    }

    @Override
    protected boolean isUnlockDevice() {
        return getPrefs().isDeviceUnlockEnabled();
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
