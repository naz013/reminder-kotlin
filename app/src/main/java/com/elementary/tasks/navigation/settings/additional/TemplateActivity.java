package com.elementary.tasks.navigation.settings.additional;

import android.content.ContentResolver;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.ActivityTemplateLayoutBinding;

import java.io.IOException;

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

public class TemplateActivity extends ThemedActivity {

    private static final int MENU_ITEM_DELETE = 12;

    private ActivityTemplateLayoutBinding binding;
    private TemplateItem mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadTemplate();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_template_layout);
        initActionBar();
        initMessageField();
        showTemplate();
    }

    private void loadTemplate() {
        Intent intent = getIntent();
        String id = intent.getStringExtra(Constants.INTENT_ID);
        if (id != null) {
            mItem = RealmDb.getInstance().getTemplate(id);
        } else if (intent.getData() != null) {
            try {
                Uri name = intent.getData();
                String scheme = name.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    ContentResolver cr = getContentResolver();
                    mItem = BackupTool.getInstance().getTemplate(cr, name);
                } else {
                    mItem = BackupTool.getInstance().getTemplate(name.getPath(), null);
                }
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void initMessageField() {
        binding.messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLeftView(s.length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void updateLeftView(int count) {
        binding.leftCharacters.setText(String.format(getString(R.string.left_characters_x), (120 - count) + ""));
    }

    private void showTemplate() {
        if (mItem != null) {
            binding.messageInput.setText(mItem.getTitle());
            String title = mItem.getTitle();
            if (title != null) {
                updateLeftView(title.length());
            }
        }
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveTemplate();
                return true;
            case android.R.id.home:
                finish();
                return true;
            case MENU_ITEM_DELETE:
                deleteItem();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteItem() {
        if (mItem != null) {
            RealmDb.getInstance().deleteTemplates(mItem);
            new DeleteTemplateFilesAsync(this).execute(mItem.getKey());
        }
        finish();
    }

    private void saveTemplate() {
        String text = binding.messageInput.getText().toString().trim();
        if (text.length() == 0) {
            binding.messageInput.setError(getString(R.string.must_be_not_empty));
            return;
        }
        String date = TimeUtil.getGmtDateTime();
        if (mItem != null){
            mItem.setDate(date);
            mItem.setTitle(text);
        } else {
            mItem = new TemplateItem(text, date);
        }
        RealmDb.getInstance().saveObject(mItem);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create_template, menu);
        if (mItem != null) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }
}
