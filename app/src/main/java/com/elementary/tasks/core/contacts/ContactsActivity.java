package com.elementary.tasks.core.contacts;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.databinding.ActivityContactsListBinding;

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

public class ContactsActivity extends ThemedActivity implements NumberCallback {

    private ActivityContactsListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts_list);
        initActionBar();
        initTabNavigation();
    }

    private void initTabNavigation() {
        ViewPagerAdapter mSectionsPagerAdapter = new ViewPagerAdapter(this, getFragmentManager());
        binding.viewPager.setAdapter(mSectionsPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        binding.toolbar.setTitle("");
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
        return true;
    }

    @Override
    public void onContactSelected(String number, String name) {
        Intent intent = new Intent();
        if (number != null) intent.putExtra(Constants.SELECTED_CONTACT_NUMBER, number);
        intent.putExtra(Constants.SELECTED_CONTACT_NAME, name);
        setResult(RESULT_OK, intent);
        finish();
    }
}
