package com.elementary.tasks.reminder;

import android.content.BroadcastReceiver;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.backdoor.shared.SharedConst;
import com.elementary.tasks.R;
import com.elementary.tasks.core.BaseNotificationActivity;
import com.elementary.tasks.core.services.RepeatNotificationReceiver;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.ActivityReminderDialogBinding;
import com.elementary.tasks.reminder.models.Reminder;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

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

    private ActivityReminderDialogBinding binding;
    private FloatingActionButton buttonCall, buttonDelay, buttonCancel;
    private TextView remText;
    private RecyclerView todoList;

    private RepeatNotificationReceiver repeater = new RepeatNotificationReceiver();
    private BroadcastReceiver deliveredReceiver, sentReceiver;

    private Reminder mReminder;
    private boolean mIsResumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mIsResumed = getIntent().getBooleanExtra(Constants.INTENT_NOTIFICATION, false);
        mReminder = RealmDb.getInstance().getReminder(getIntent().getStringExtra(Constants.INTENT_ID));
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminder_dialog);
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

    }

    @Override
    protected void delay() {

    }

    @Override
    protected void cancel() {

    }

    @Override
    protected void favourite() {

    }

    @Override
    protected void ok() {

    }

    @Override
    protected void showSendingError() {

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
        return mReminder.isVibrate();
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
        return mReminder.getColor();
    }

    @Override
    protected boolean isAwakeDevice() {
        return mReminder.isAwake();
    }

    @Override
    protected boolean isGlobal() {
        return mReminder.isUseGlobal();
    }

    @Override
    protected boolean isUnlockDevice() {
        return mReminder.isUnlock();
    }
}
