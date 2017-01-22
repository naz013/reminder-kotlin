package com.elementary.tasks.birthdays;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.services.PermanentBirthdayService;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.databinding.ActivityAddBirthdayBinding;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

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

public class AddBirthdayActivity extends ThemedActivity {

    private static final int MENU_ITEM_DELETE = 12;

    private ActivityAddBirthdayBinding binding;

    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 0;
    private String number;
    private BirthdayItem mItem;
    private long date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadBirthday();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_birthday);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        if (themeUtil.isDark()){
            binding.dateIcon.setImageResource(R.drawable.ic_calendar_white);
            binding.pickContact.setImageResource(R.drawable.ic_perm_identity_white_24dp);
        } else {
            binding.dateIcon.setImageResource(R.drawable.ic_calendar);
            binding.pickContact.setImageResource(R.drawable.ic_perm_identity_black_24dp);
        }
        binding.container.setVisibility(View.GONE);
        binding.contactCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) binding.container.setVisibility(View.VISIBLE);
            else binding.container.setVisibility(View.GONE);
        });
        binding.birthDate.setOnClickListener(view -> dateDialog());
        binding.pickContact.setOnClickListener(view -> pickContact());
        showBirthday();
    }

    private void showBirthday() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        if (mItem != null) {
            binding.birthName.setText(mItem.getName());
            try {
                Date dt = CheckBirthdaysAsync.dateFormat.parse(mItem.getDate());
                if (dt != null) calendar.setTime(dt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            binding.birthName.setText(mItem.getName());
            if (!TextUtils.isEmpty(mItem.getNumber())) {
                binding.phone.setText(mItem.getNumber());
                binding.contactCheck.setChecked(true);
            }
            binding.toolbar.setTitle(R.string.edit_birthday);
            this.number = mItem.getNumber();
        } else if (date != 0) {
            calendar.setTimeInMillis(date);
        }
        myYear = calendar.get(Calendar.YEAR);
        myMonth = calendar.get(Calendar.MONTH);
        myDay = calendar.get(Calendar.DAY_OF_MONTH);
        binding.birthDate.setText(CheckBirthdaysAsync.dateFormat.format(calendar.getTime()));
    }

    private void loadBirthday() {
        Intent intent = getIntent();
        date = intent.getLongExtra(Constants.INTENT_DATE, 0);
        String id = intent.getStringExtra(Constants.INTENT_ID);
        if (id != null) {
            mItem = RealmDb.getInstance().getBirthday(id);
        } else {
            try {
                Uri name = intent.getData();
                String scheme = name.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    ContentResolver cr = getContentResolver();
                    mItem = BackupTool.getInstance().getBirthday(cr, name);
                } else {
                    mItem = BackupTool.getInstance().getBirthday(name.getPath(), null);
                }
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void pickContact() {
        if (Permissions.checkPermission(AddBirthdayActivity.this, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            SuperUtil.selectContact(AddBirthdayActivity.this, Constants.REQUEST_CODE_CONTACTS);
        } else {
            Permissions.requestPermission(AddBirthdayActivity.this, 101, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_group_edit, menu);
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveBirthday();
                return true;
            case android.R.id.home:
                finish();
                return true;
            case MENU_ITEM_DELETE:
                deleteItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveBirthday() {
        String contact = binding.birthName.getText().toString();
        if (contact.matches("")) {
            binding.birthName.setError(getString(R.string.must_be_not_empty));
            return;
        }
        int contactId = Contacts.getIdFromNumber(number, this);
        if (mItem != null) {
            mItem.setName(contact);
            mItem.setContactId(contactId);
            mItem.setDate(binding.birthDate.getText().toString());
            mItem.setNumber(number);
            mItem.setDay(myDay);
            mItem.setMonth(myMonth);
        } else {
            mItem = new BirthdayItem(contact, binding.birthDate.getText().toString().trim(), number, 0, contactId, myDay, myMonth);
        }
        RealmDb.getInstance().saveObject(mItem);
        closeScreen();
    }

    private void closeScreen() {
        setResult(RESULT_OK);
        finish();
        startService(new Intent(this, PermanentBirthdayService.class).setAction(PermanentBirthdayService.ACTION_SHOW));
    }

    private void deleteItem() {
        if (mItem != null) {
            RealmDb.getInstance().deleteBirthday(mItem);
        }
        closeScreen();
    }

    private void dateDialog() {
        new DatePickerDialog(this, myDateCallBack, myYear, myMonth, myDay).show();
    }

    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            String monthStr;
            if (myMonth < 9) {
                monthStr = "0" + (myMonth + 1);
            } else monthStr = String.valueOf((myMonth + 1));
            String dayStr;
            if (myDay < 10) {
                dayStr = "0" + myDay;
            } else dayStr = String.valueOf(myDay);
            binding.birthDate.setText(SuperUtil.appendString(String.valueOf(myYear), "-", monthStr, "-", dayStr));
        }
    };

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra(Constants.SELECTED_CONTACT_NAME);
                number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
                if (binding.birthName.getText().toString().matches("")) {
                    binding.birthName.setText(name);
                }
                binding.phone.setText(number);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SuperUtil.selectContact(AddBirthdayActivity.this, Constants.REQUEST_CODE_CONTACTS);
                }
                break;
        }
    }
}
