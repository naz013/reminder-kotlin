package com.elementary.tasks.core.additional;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.telephony.SmsManager;
import android.view.WindowManager;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.views.roboto.RoboButton;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ActivityQuickSmsLayoutBinding;
import com.elementary.tasks.navigation.settings.additional.TemplateItem;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class QuickSmsActivity extends ThemedActivity {

    private static final int REQ_SMS = 425;

    private RecyclerView messagesList;
    private SelectableTemplatesAdapter mAdapter;
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        ActivityQuickSmsLayoutBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_quick_sms_layout);
        messagesList = binding.messagesList;
        messagesList.setLayoutManager(new LinearLayoutManager(this));
        RoboButton buttonSend = binding.buttonSend;
        buttonSend.setOnClickListener(v -> startSending());
        String name = Contacts.getNameFromNumber(number, this);
        RoboTextView contactInfo = binding.contactInfo;
        contactInfo.setText(SuperUtil.appendString(name, "\n", number));
        loadTemplates();
        if (mAdapter.getItemCount() > 0) {
            mAdapter.selectItem(0);
        }
    }

    private void initData() {
        Intent i = getIntent();
        number = i.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
    }

    private void startSending() {
        if (!Permissions.checkPermission(this, Permissions.SEND_SMS)) {
            Permissions.requestPermission(this, REQ_SMS, Permissions.SEND_SMS);
            return;
        }
        int position = mAdapter.getSelectedPosition();
        TemplateItem item = mAdapter.getItem(position);
        if (item != null) {
            LogUtil.d("TAG", "startSending: " + item.getTitle());
            sendSMS(number, item.getTitle());
        }
        removeFlags();
    }

    private void loadTemplates() {
        List<TemplateItem> list = RealmDb.getInstance().getAllTemplates();
        mAdapter = new SelectableTemplatesAdapter(list, this);
        messagesList.setAdapter(mAdapter);
    }

    public void removeFlags() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        finish();
    }

    private void sendSMS(String number, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(number, null, message, sentPI, deliveredPI);
    }

    @Override
    public void onBackPressed() {
        removeFlags();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case REQ_SMS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSending();
                }
                break;
        }
    }
}
