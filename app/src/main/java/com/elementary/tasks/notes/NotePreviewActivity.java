package com.elementary.tasks.notes;

import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.controller.EventControl;
import com.elementary.tasks.core.controller.EventControlFactory;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.creators.CreateReminderActivity;
import com.elementary.tasks.databinding.ActivityNotePreviewBinding;
import com.elementary.tasks.navigation.settings.images.GridMarginDecoration;
import com.elementary.tasks.reminder.models.Reminder;

import java.io.File;
import java.util.List;

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

public class NotePreviewActivity extends ThemedActivity {

    private NoteItem mItem;
    private Reminder mReminder;
    private String mId;

    private ImagesGridAdapter mAdapter;
    private ActivityNotePreviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getIntent().getStringExtra(Constants.INTENT_ID);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note_preview);
        initActionBar();
        initImagesList();
        initScrollView();
        initReminderCard();
    }

    private void initReminderCard() {
        binding.reminderContainer.setVisibility(View.GONE);
        binding.editReminder.setOnClickListener(v ->
                startActivity(new Intent(this, CreateReminderActivity.class).putExtra(Constants.INTENT_ID, mReminder.getUuId())));
        binding.deleteReminder.setOnClickListener(v -> showReminderDeleteDialog());
    }

    private void initImagesList() {
        mAdapter = new ImagesGridAdapter(this);
        binding.imagesList.setLayoutManager(new KeepLayoutManager(this, 6, mAdapter));
        binding.imagesList.addItemDecoration(new GridMarginDecoration(getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        binding.imagesList.setHasFixedSize(true);
        binding.imagesList.setItemAnimator(new DefaultItemAnimator());
        binding.imagesList.setAdapter(mAdapter);
    }

    private void initScrollView() {
        binding.scrollContent.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = binding.scrollContent.getScrollY();
                if (!mItem.getImages().isEmpty()) {
                    binding.appBar.getBackground().setAlpha(getAlphaForActionBar(scrollY));
                } else {
                    binding.appBar.getBackground().setAlpha(255);
                }
            }

            private int getAlphaForActionBar(int scrollY) {
                int minDist = 0, maxDist = MeasureUtils.dp2px(NotePreviewActivity.this, 200);
                if (scrollY > maxDist) {
                    return 255;
                } else if (scrollY < minDist) {
                    return 0;
                } else {
                    return (int) ((255.0 / maxDist) * scrollY);
                }
            }
        });
    }

    private void initActionBar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle("");
    }

    private void editNote() {
        startActivity(new Intent(NotePreviewActivity.this, ActivityCreateNote.class)
                .putExtra(Constants.INTENT_ID, mItem.getKey()));
    }

    private void moveToStatus() {
        if (mItem != null) {
            new Notifier(this).showNoteNotification(mItem);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onBackPressed() {
        closeWindow();
    }

    private void loadData() {
        mItem = RealmDb.getInstance().getNote(mId);
        if (mItem != null) {
            showNote();
            showImage();
            showReminder();
        } else {
            finish();
        }
    }

    private void showNote() {
        String note = mItem.getSummary();
        binding.noteText.setText(note);
        int color = mItem.getColor();
        int style = mItem.getStyle();
        binding.noteText.setTypeface(AssetsUtil.getTypeface(this, style));
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(getThemeUtil().getNoteDarkColor(color));
        }
        binding.scrollContent.setBackgroundColor(getThemeUtil().getNoteLightColor(color));
    }

    private void showReminder() {
        mReminder = RealmDb.getInstance().getReminderByNote(mItem.getKey());
        if (mReminder != null) {
            String dateTime = TimeUtil.getDateTimeFromGmt(mReminder.getEventTime(), getPrefs().is24HourFormatEnabled());
            binding.reminderTime.setText(dateTime);
            binding.reminderContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showImage() {
        List<NoteImage> list = mItem.getImages();
        if (!list.isEmpty()) {
            mAdapter.setImages(list);
            binding.appBar.setBackgroundColor(getThemeUtil().getNoteColor(mItem.getColor()));
            binding.appBar.getBackground().setAlpha(0);
        } else {
            binding.appBar.setBackgroundColor(getThemeUtil().getNoteColor(mItem.getColor()));
            binding.appBar.getBackground().setAlpha(255);
        }
    }

    private void shareNote() {
        File file = BackupTool.getInstance().createNote(mItem);
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show();
            return;
        }
        TelephonyUtil.sendNote(file, this, mItem.getSummary());
        closeWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preview_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                closeWindow();
                return true;
            case R.id.action_share:
                shareNote();
                return true;
            case R.id.action_delete:
                showDeleteDialog();
                return true;
            case R.id.action_status:
                moveToStatus();
                return true;
            case R.id.action_edit:
                editNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void closeWindow() {
        if (Module.isLollipop()) {
            finishAfterTransition();
        } else {
            finish();
        }
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_this_note));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteNote();
            closeWindow();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showReminderDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_this_reminder);
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            EventControl control = EventControlFactory.getController(this, mReminder);
            control.stop();
            RealmDb.getInstance().deleteReminder(mReminder.getUuId());
            binding.reminderContainer.setVisibility(View.GONE);
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteNote() {
        RealmDb.getInstance().deleteNote(mItem);
    }
}
