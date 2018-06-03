package com.elementary.tasks.birthdays.create_edit;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.work.CheckBirthdaysAsync;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.data.models.Birthday;
import com.elementary.tasks.core.services.PermanentBirthdayReceiver;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Contacts;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.view_models.birthdays.BirthdayViewModel;
import com.elementary.tasks.databinding.ActivityAddBirthdayBinding;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

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
    private static final int CONTACT_PERM = 102;

    private ActivityAddBirthdayBinding binding;
    private BirthdayViewModel viewModel;

    private int myYear = 0;
    private int myMonth = 0;
    private int myDay = 0;
    private String number;
    @Nullable
    private Birthday mBirthday;
    private long date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_birthday);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.container.setVisibility(View.GONE);
        binding.contactCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) binding.container.setVisibility(View.VISIBLE);
            else binding.container.setVisibility(View.GONE);
        });
        binding.birthDate.setOnClickListener(view -> dateDialog());
        binding.pickContact.setOnClickListener(view -> pickContact());

        loadBirthday();
    }

    private void showBirthday(@Nullable Birthday birthday) {
        this.mBirthday = birthday;

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        binding.toolbar.setTitle(R.string.add_birthday);
        if (birthday != null) {
            binding.birthName.setText(birthday.getName());
            try {
                Date dt = CheckBirthdaysAsync.DATE_FORMAT.parse(birthday.getDate());
                if (dt != null) calendar.setTime(dt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (!TextUtils.isEmpty(birthday.getNumber())) {
                binding.phone.setText(birthday.getNumber());
                binding.contactCheck.setChecked(true);
            }
            binding.toolbar.setTitle(R.string.edit_birthday);
            this.number = birthday.getNumber();
        } else if (date != 0) {
            calendar.setTimeInMillis(date);
        }
        myYear = calendar.get(Calendar.YEAR);
        myMonth = calendar.get(Calendar.MONTH);
        myDay = calendar.get(Calendar.DAY_OF_MONTH);
        binding.birthDate.setText(CheckBirthdaysAsync.DATE_FORMAT.format(calendar.getTime()));
    }

    private void loadBirthday() {
        date = getIntent().getLongExtra(Constants.INTENT_DATE, 0);
        int id = getIntent().getIntExtra(Constants.INTENT_ID, 0);
        initViewModel(id);
        if (getIntent().getData() != null) {
            try {
                Uri name = getIntent().getData();
                String scheme = name.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    ContentResolver cr = getContentResolver();
                    mBirthday = BackupTool.getInstance().getBirthday(cr, name);
                } else {
                    mBirthday = BackupTool.getInstance().getBirthday(name.getPath(), null);
                }
                showBirthday(mBirthday);
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void initViewModel(int id) {
        viewModel = ViewModelProviders.of(this, new BirthdayViewModel.Factory(getApplication(), id)).get(BirthdayViewModel.class);
        viewModel.birthday.observe(this, this::showBirthday);
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case SAVED:
                    case DELETED:
                        closeScreen();
                        break;
                }
            }
        });
    }

    private boolean checkContactPermission(int code) {
        if (!Permissions.checkPermission(this, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
            Permissions.requestPermission(this, code, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
            return false;
        }
        return true;
    }

    private void pickContact() {
        if (!checkContactPermission(101)) {
            return;
        }
        SuperUtil.selectContact(this, Constants.REQUEST_CODE_CONTACTS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_group_edit, menu);
        if (mBirthday != null) {
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

    @Override
    protected void onStop() {
        super.onStop();
        if (mBirthday != null && getPrefs().isAutoSaveEnabled()) {
            saveBirthday();
        }
    }

    private void saveBirthday() {
        String contact = binding.birthName.getText().toString();
        if (contact.matches("")) {
            binding.birthName.setError(getString(R.string.must_be_not_empty));
            return;
        }
        int contactId = 0;
        if (binding.contactCheck.isChecked()) {
            number = binding.phone.getText().toString().trim();
            if (TextUtils.isEmpty(number)) {
                binding.phone.setError(getString(R.string.you_dont_insert_number));
                return;
            }
            if (!checkContactPermission(CONTACT_PERM)) {
                return;
            }
            contactId = Contacts.getIdFromNumber(number, this);
        }
        if (mBirthday != null) {
            mBirthday.setName(contact);
            mBirthday.setContactId(contactId);
            mBirthday.setDate(binding.birthDate.getText().toString());
            mBirthday.setNumber(number);
            mBirthday.setDay(myDay);
            mBirthday.setMonth(myMonth);
        } else {
            mBirthday = new Birthday(contact, binding.birthDate.getText().toString().trim(), number, 0, contactId, myDay, myMonth);
        }
        viewModel.saveBirthday(mBirthday);
    }

    private void closeScreen() {
        setResult(RESULT_OK);
        finish();
        sendBroadcast(new Intent(this, PermanentBirthdayReceiver.class)
                .setAction(PermanentBirthdayReceiver.ACTION_SHOW));
    }

    private void deleteItem() {
        if (mBirthday != null) {
            viewModel.deleteBirthday(mBirthday);
        }
    }

    private void dateDialog() {
        TimeUtil.showDatePicker(this, myDateCallBack, myYear, myMonth, myDay);
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
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SuperUtil.selectContact(AddBirthdayActivity.this, Constants.REQUEST_CODE_CONTACTS);
                }
                break;
            case CONTACT_PERM:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveBirthday();
                }
                break;
        }
    }
}
