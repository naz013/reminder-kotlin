package com.elementary.tasks.notes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.QuickReturnUtils;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.databinding.ActivityNotePreviewBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private ActivityNotePreviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getIntent().getStringExtra(Constants.INTENT_ID);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note_preview);
        initActionBar();
        initScrollView();
        initImageView();
        initReminderCard();
    }

    private void initReminderCard() {
        binding.reminderContainer.setVisibility(View.GONE);
        binding.editReminder.setOnClickListener(v -> {
            // TODO: 14.11.2016 Add reminder edit call
        });
        binding.deleteReminder.setOnClickListener(v -> showReminderDeleteDialog());
    }

    private void initImageView() {
        if (Module.isLollipop()) binding.imageView.setTransitionName("image");
        binding.imageView.setOnClickListener(v -> {
            if (Permissions.checkPermission(NotePreviewActivity.this, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
                openImage();
            } else {
                Permissions.requestPermission(NotePreviewActivity.this, REQUEST_SD_CARD, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
            }
        });
    }

    private void initScrollView() {
        binding.scrollContent.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = binding.scrollContent.getScrollY();
                if (mItem.getImage() != null) {
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
        if (mItem.getImage() != null) {
            File path = MemoryUtil.getImageCacheDir();
            boolean isDirectory = true;
            if (!path.exists()) {
                isDirectory = path.mkdirs();
            }
            if (isDirectory) {
                String fileName = UUID.randomUUID().toString() + FileConfig.FILE_NAME_IMAGE;
                File f = new File(path + File.separator + fileName);
                boolean isFile = false;
                try {
                    isFile = f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isFile) {
                    FileOutputStream fo = null;
                    try {
                        fo = new FileOutputStream(f);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (fo != null) {
                        try {
                            fo.write(mItem.getImage());
                            fo.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(NotePreviewActivity.this, ImagePreviewActivity.class)
                                .putExtra(Constants.FILE_PICKED, f.toString()));
                    }
                }
            }
        }
    }

    private void moveToStatus() {
        if (mItem != null){
//            new Notifier(this).showNoteNotification(mItem);
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
//        if (mItem.getLinkId() != 0){
//            ReminderItem reminderItem = ReminderHelper.getInstance(this).getReminder(mItem.getLinkId());
//            if (reminderItem != null){
//                long feature = reminderItem.getDateTime();
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTimeInMillis(System.currentTimeMillis());
//                if (feature != 0) {
//                    calendar.setTimeInMillis(feature);
//                }
//                reminderTime.setText(TimeUtil.getDateTime(calendar.getTime(),
//                        SharedPrefs.getInstance(this).getBoolean(Prefs.IS_24_TIME_FORMAT)));
//                reminderContainer.setVisibility(View.VISIBLE);
//            }
//        }
    }

    private void showImage() {
        byte[] imageByte = mItem.getImage();
        if (imageByte != null){
            img = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
            binding.imageView.setImageBitmap(img);
            binding.imageView.setVisibility(View.VISIBLE);
            binding.appBar.setBackgroundColor(themeUtil.getNoteColor(mItem.getColor()));
            binding.appBar.getBackground().setAlpha(0);
        } else {
            binding.imageView.setVisibility(View.GONE);
            binding.appBar.setBackgroundColor(themeUtil.getNoteColor(mItem.getColor()));
            binding.appBar.getBackground().setAlpha(255);
        }
    }

    private void shareNote(){
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
//            Reminder.delete(mItem.getLinkId(), NotePreviewActivity.this);
//            NoteHelper.getInstance(this).linkReminder(mItem.getId(), 0);
            binding.reminderContainer.setVisibility(View.GONE);
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, which) -> {
            dialog.dismiss();
        });
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
