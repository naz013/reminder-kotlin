package com.elementary.tasks.groups;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.view_models.groups.GroupViewModel;
import com.elementary.tasks.core.views.ColorPickerView;
import com.elementary.tasks.databinding.ActivityCreateGroupBinding;

import java.io.IOException;
import java.util.UUID;

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
public class CreateGroupActivity extends ThemedActivity implements ColorPickerView.OnColorListener {

    private static final int MENU_ITEM_DELETE = 12;

    private ActivityCreateGroupBinding binding;
    private GroupViewModel viewModel;

    private int color = 0;
    @Nullable
    private Group mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_group);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.pickerView.setListener(this);
        binding.pickerView.setSelectedColor(color);
        setColor(color);

        loadGroup();
    }

    private void showGroup(@NonNull Group group) {
        this.mItem = group;
        binding.editField.setText(group.getTitle());
        color = group.getColor();
        binding.pickerView.setSelectedColor(color);
        setColor(color);
        invalidateOptionsMenu();
    }

    private void loadGroup() {
        Intent intent = getIntent();
        String id = intent.getStringExtra(Constants.INTENT_ID);
        if (id != null) {
            initViewModel(id);
        } else if (intent.getData() != null) {
            try {
                Uri name = intent.getData();
                String scheme = name.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    ContentResolver cr = getContentResolver();
                    mItem = BackupTool.getInstance().getGroup(cr, name);
                } else {
                    mItem = BackupTool.getInstance().getGroup(name.getPath(), null);
                }
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void initViewModel(String id) {
        viewModel = ViewModelProviders.of(this, new GroupViewModel.Factory(getApplication(), id)).get(GroupViewModel.class);
        viewModel.group.observe(this, group -> {
            if (group != null) {
                showGroup(group);
            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case SAVED:
                    case DELETED:
                        finish();
                        break;
                }
            }
        });
        viewModel.allGroups.observe(this, groups -> {
            if (groups != null) {
                invalidateOptionsMenu();
            }
        });
    }

    private void saveGroup() {
        String text = binding.editField.getText().toString().trim();
        if (text.length() == 0) {
            binding.editField.setError(getString(R.string.must_be_not_empty));
            return;
        }
        if (mItem == null) {
            mItem = new Group(text, UUID.randomUUID().toString(), color, TimeUtil.getGmtDateTime());
        } else {
            mItem.setColor(color);
            mItem.setDateTime(TimeUtil.getGmtDateTime());
            mItem.setTitle(text);
        }
        viewModel.saveGroup(mItem);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mItem != null && getPrefs().isAutoSaveEnabled()) {
            saveGroup();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_group_edit, menu);
        if (mItem != null && viewModel.allGroups.getValue() != null && viewModel.allGroups.getValue().size() > 1) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveGroup();
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

    private void deleteItem() {
        if (mItem != null) {
            viewModel.deleteGroup(mItem);
        }
        finish();
    }

    private void setColor(int i) {
        color = i;
        ThemeUtil cs = ThemeUtil.getInstance(this);
        binding.appBar.setBackgroundColor(cs.getColor(cs.getCategoryColor(i)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.getNoteDarkColor(i));
        }
    }

    @Override
    public void onColorSelect(int code) {
        setColor(code);
    }
}
