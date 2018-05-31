package com.elementary.tasks.notes.preview;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.data.models.Reminder;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.BackupTool;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.TelephonyUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.view_models.notes.NoteViewModel;
import com.elementary.tasks.databinding.ActivityNotePreviewBinding;
import com.elementary.tasks.navigation.settings.images.GridMarginDecoration;
import com.elementary.tasks.notes.create.CreateNoteActivity;
import com.elementary.tasks.notes.create.NoteImage;
import com.elementary.tasks.notes.list.ImagesGridAdapter;
import com.elementary.tasks.notes.list.KeepLayoutManager;
import com.elementary.tasks.reminder.create_edit.CreateReminderActivity;

import java.io.File;
import java.util.List;

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

public class NotePreviewActivity extends ThemedActivity {

    public static final String PREVIEW_IMAGES = "preview_image_key";

    @Nullable
    private Note mNote;
    @Nullable
    private Reminder mReminder;
    private String mId;

    private final ImagesGridAdapter mAdapter = new ImagesGridAdapter();
    private ActivityNotePreviewBinding binding;
    private NoteViewModel viewModel;

    private ProgressDialog mProgress;

    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getIntent().getStringExtra(Constants.INTENT_ID);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note_preview);
        initActionBar();
        initImagesList();
        initScrollView();
        initReminderCard();

        initViewModel();
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this, new NoteViewModel.Factory(getApplication(), mId)).get(NoteViewModel.class);
        viewModel.note.observe(this, note -> {
            if (note != null) {
                showNote(note);
            }
        });
        viewModel.reminder.observe(this, reminder -> {
            if (reminder != null) {
                showReminder(reminder);
            }
        });
        viewModel.result.observe(this, commands -> {
            if (commands != null) {
                switch (commands) {
                    case DELETED:
                        closeWindow();
                        break;
                }
            }
        });
    }

    private void initReminderCard() {
        binding.reminderContainer.setVisibility(View.GONE);
        binding.editReminder.setOnClickListener(v -> editReminder());
        binding.deleteReminder.setOnClickListener(v -> showReminderDeleteDialog());
    }

    private void editReminder() {
        if (mReminder != null) {
            startActivity(new Intent(this, CreateReminderActivity.class)
                    .putExtra(Constants.INTENT_ID, mReminder.getUuId()));
        }
    }

    private void initImagesList() {
        mAdapter.setActionsListener((view, position, noteImage, actions) -> {
            switch (actions) {
                case OPEN:
                    openImagePreview(position);
                    break;
            }
        });
        binding.imagesList.setLayoutManager(new KeepLayoutManager(this, 6, mAdapter));
        binding.imagesList.addItemDecoration(new GridMarginDecoration(getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        binding.imagesList.setAdapter(mAdapter);
    }

    private void openImagePreview(int position) {
        Note note = new Note();
        note.setKey(PREVIEW_IMAGES);
        note.setImages(mAdapter.getData());
        viewModel.saveNote(note);
        startActivity(new Intent(this, ImagePreviewActivity.class)
                .putExtra(Constants.INTENT_ID, note.getKey())
                .putExtra(Constants.INTENT_POSITION, position));
    }

    private void initScrollView() {
        binding.scrollContent.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = binding.scrollContent.getScrollY();
                if (!mNote.getImages().isEmpty()) {
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
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.toolbar.setTitle("");
    }

    private void editNote() {
        startActivity(new Intent(NotePreviewActivity.this, CreateNoteActivity.class)
                .putExtra(Constants.INTENT_ID, mNote.getKey()));
    }

    private void moveToStatus() {
        if (mNote != null) {
            new Notifier(this).showNoteNotification(mNote);
        }
    }

    @Override
    public void onBackPressed() {
        closeWindow();
    }

    private void showNote(Note note) {
        this.mNote = note;
        if (note != null) {
            binding.noteText.setText(note.getSummary());
            binding.noteText.setTypeface(AssetsUtil.getTypeface(this, note.getStyle()));
            if (Module.isLollipop()) {
                getWindow().setStatusBarColor(getThemeUtil().getNoteDarkColor(note.getColor()));
            }
            binding.scrollContent.setBackgroundColor(getThemeUtil().getNoteLightColor(note.getColor()));
            showImages(note.getImages());
        }
    }

    private void showReminder(Reminder reminder) {
        mReminder = reminder;
        if (reminder != null) {
            String dateTime = TimeUtil.getDateTimeFromGmt(reminder.getEventTime(), getPrefs().is24HourFormatEnabled());
            binding.reminderTime.setText(dateTime);
            binding.reminderContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showImages(List<NoteImage> images) {
        if (!images.isEmpty()) {
            mAdapter.setImages(images);
            binding.appBar.setBackgroundColor(getThemeUtil().getNoteColor(mNote.getColor()));
            binding.appBar.getBackground().setAlpha(0);
        } else {
            binding.appBar.setBackgroundColor(getThemeUtil().getNoteColor(mNote.getColor()));
            binding.appBar.getBackground().setAlpha(255);
        }
    }

    private void hideProgress() {
        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
        }
    }

    private void showProgress() {
        mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false);
    }

    private void shareNote() {
        showProgress();
        BackupTool.CreateCallback callback = this::sendNote;
        new Thread(() -> BackupTool.getInstance().createNote(mNote, callback)).start();
    }

    private void sendNote(File file) {
        hideProgress();
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show();
            return;
        }
        TelephonyUtil.sendNote(file, this, mNote.getSummary());
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
            mUiHandler.post(this::finishAfterTransition);
        } else {
            finish();
        }
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setMessage(getString(R.string.delete_this_note));
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            if (mNote != null) viewModel.deleteNote(mNote);
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showReminderDeleteDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setMessage(R.string.delete_this_reminder);
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
            dialog.dismiss();
            deleteReminder();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteReminder() {
        if (mReminder != null) {
            viewModel.deleteReminder(mReminder);
            binding.reminderContainer.setVisibility(View.GONE);
        }
    }
}
