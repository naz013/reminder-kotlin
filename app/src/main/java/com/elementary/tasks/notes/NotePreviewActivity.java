package com.elementary.tasks.notes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.cloud.FileConfig;
import com.elementary.tasks.core.utils.AssetsUtil;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Notifier;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.QuickReturnUtils;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.databinding.ActivityNotePreviewBinding;
import com.elementary.tasks.navigation.settings.images.GridMarginDecoration;
import com.elementary.tasks.reminder.models.Reminder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

    private static final int REQUEST_SD_CARD = 1122;
    private NoteItem mItem;
    private Bitmap img;
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
        binding.editReminder.setOnClickListener(v -> {

        });
        binding.deleteReminder.setOnClickListener(v -> showReminderDeleteDialog());
    }

    private void initImagesList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 6);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int size = mAdapter.getItemCount();
                switch (size % 3) {
                    case 1:
                        if (position == 0) {
                            return 6;
                        } else {
                            return 2;
                        }
                    case 2:
                        if (position < 2) {
                            return 3;
                        } else {
                            return 2;
                        }
                    default:
                        return 2;
                }
            }
        });
        binding.imagesList.setLayoutManager(gridLayoutManager);
        binding.imagesList.addItemDecoration(new GridMarginDecoration(getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        binding.imagesList.setHasFixedSize(true);
        binding.imagesList.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ImagesGridAdapter(this);
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
                int minDist = 0, maxDist = QuickReturnUtils.dp2px(NotePreviewActivity.this, 200);
                if (scrollY > maxDist) {
                    return 255;
                } else if (scrollY<minDist) {
                    return 0;
                } else {
                    return (int)  ((255.0 / maxDist) * scrollY);
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

    private void openImage() {
        if (!mItem.getImages().isEmpty()) {
            File path = MemoryUtil.getImageCacheDir();
            boolean isDirectory = true;
            if (!path.exists()) {
                isDirectory = path.mkdirs();
            }
            if (isDirectory) {
                String fileName = UUID.randomUUID().toString() + FileConfig.FILE_NAME_IMAGE;
                File f = new File(path + File.separator + fileName);
                try {
                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    try {
                        fo.write(mItem.getImages().get(0).getImage());
                        fo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(NotePreviewActivity.this, ImagePreviewActivity.class)
                            .putExtra(Constants.FILE_PICKED, f.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void moveToStatus() {
        if (mItem != null){
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

    private void loadData(){
        img = null;
        mItem = RealmDb.getInstance().getNote(mId);
        if (mItem != null){
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
            getWindow().setStatusBarColor(themeUtil.getNoteDarkColor(color));
        }
        binding.scrollContent.setBackgroundColor(themeUtil.getNoteLightColor(color));
    }

    private void showReminder() {
        // TODO: 12.12.2016 Add reminder loading for note
        Reminder reminder = RealmDb.getInstance().getReminderByNote(mItem.getKey());
        if (reminder != null){
            String dateTime = TimeUtil.getDateTimeFromGmt(reminder.getEventTime(), Prefs.getInstance(this).is24HourFormatEnabled());
            binding.reminderTime.setText(dateTime);
            binding.reminderContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showImage() {
        List<NoteImage> list = mItem.getImages();
        if (!list.isEmpty()){
            mAdapter.addImages(list);
            binding.appBar.setBackgroundColor(themeUtil.getNoteColor(mItem.getColor()));
            binding.appBar.getBackground().setAlpha(0);
        } else {
            binding.appBar.setBackgroundColor(themeUtil.getNoteColor(mItem.getColor()));
            binding.appBar.getBackground().setAlpha(255);
        }
    }

    private void shareNote(){
        // TODO: 12.12.2016 Add note sharing.
//        if (!NoteHelper.getInstance(this).shareNote(mItem.getId())) {
//            Messages.toast(this, getString(R.string.error_sending));
//            closeWindow();
//        }
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
            // TODO: 12.12.2016 Add reminder deleting from note
//            Reminder.delete(mItem.getLinkId(), NotePreviewActivity.this);
            binding.reminderContainer.setVisibility(View.GONE);
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteNote() {
        RealmDb.getInstance().deleteNote(mItem);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SD_CARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImage();
                }
                break;
        }
    }
}
