package com.elementary.tasks.core.file_explorer;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Sound;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.roboto.RoboEditText;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.ActivityFileExplorerBinding;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class FileExplorerActivity extends ThemedActivity {

    private static final String TAG = "FileExplorerActivity";
    private static final int SD_CARD = 444;
    public static final String TYPE_MUSIC = "music";
    public static final String TYPE_PHOTO = "photo";

    private ArrayList<String> str = new ArrayList<>();
    private Boolean firstLvl = true;
    private boolean isDark = false;
    private boolean mFilter;

    private ArrayList<FileDataItem> mDataList;
    private File path = new File(Environment.getExternalStorageDirectory() + "");
    private String mFileName;
    private String mFilePath;

    private String filType;

    private FileRecyclerAdapter mAdapter;
    private Sound mSound;

    private ActivityFileExplorerBinding binding;
    private RecyclerView mFilesList;
    private LinearLayout mPlayerLayout;
    private RoboTextView mMelodyTitle;
    private RoboEditText mSearchView;

    private RecyclerClickListener recyclerClick = this::selectFile;

    private FilterCallback mFilterCallback = new FilterCallback() {
        @Override
        public void filter(int size) {
            mFilesList.scrollToPosition(0);
        }
    };

    private void selectFile(int position) {
        FileDataItem item = mAdapter.getItem(position);
        mFileName = item.getFileName();
        mFilePath = item.getFilePath();
        File sel = new File(path + "/" + mFileName);
        if (sel.isDirectory()) {
            firstLvl = false;
            str.add(mFileName);
            mDataList = null;
            path = new File(sel + "");
            loadFileList();
            loadList();
        } else if (mFileName.equalsIgnoreCase(getString(R.string.up)) && !sel.exists()) {
            moveUp();
        } else {
            if (filType.matches("any")) {
                sendFile();
            } else if (filType.equals(TYPE_PHOTO)) {
                if (isImage(mFileName)) {
                    showFullImage();
                } else {
                    Toast.makeText(this, "Not a image file", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (isMelody(mFileName)) {
                    play();
                } else {
                    Toast.makeText(this, getString(R.string.not_music_file), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showFullImage() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setTitle(mFileName);
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, MeasureUtils.dp2px(this, 256));
        imageView.setLayoutParams(layoutParams);
        Picasso.with(this)
                .load(new File(mFilePath))
                .into(imageView);
        builder.setView(imageView);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            sendFile();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void moveUp() {
        String s = str.remove(str.size() - 1);
        path = new File(path.toString().substring(0,
                path.toString().lastIndexOf(s)));
        mDataList = null;
        if (str.isEmpty()) {
            firstLvl = true;
        }
        loadFileList();
        loadList();
    }

    private void sendFile() {
        Intent intent = new Intent();
        intent.putExtra(Constants.FILE_PICKED, mFilePath);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSound = new Sound(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_explorer);
        filType = getIntent().getStringExtra(Constants.FILE_TYPE);
        if (filType == null) filType = TYPE_MUSIC;
        isDark = getThemeUtil().isDark();
        initActionBar();
        initRecyclerView();
        initPlayer();
        initSearch();
        initButtons();
        if (Permissions.checkPermission(this, Permissions.READ_EXTERNAL)) {
            loadFileList();
            loadList();
        } else {
            Permissions.requestPermission(this, SD_CARD, Permissions.READ_EXTERNAL);
        }
    }

    private void initPlayer() {
        mPlayerLayout = binding.playerLayout;
        mPlayerLayout.setVisibility(View.GONE);
        mMelodyTitle = binding.currentMelody;
    }

    private void initRecyclerView() {
        mFilesList = binding.mDataList;
        mFilesList.setHasFixedSize(true);
        mFilesList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initActionBar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

    private void initSearch() {
        mSearchView = binding.searchField;
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mFilter && mAdapter != null) mAdapter.filter(s.toString(), mDataList);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initButtons() {
        ImageButton clearButton = binding.clearButton;
        binding.fab.setOnClickListener(mListener);
        binding.pauseButton.setOnClickListener(mListener);
        binding.stopButton.setOnClickListener(mListener);
        binding.playButton.setOnClickListener(mListener);
        clearButton.setOnClickListener(mListener);
    }

    private void loadList() {
        if (mDataList == null) {
            Toast.makeText(this, getString(R.string.no_files), Toast.LENGTH_SHORT).show();
            finish();
        }
        mFilesList.setAdapter(mAdapter);
    }

    private void play() {
        if (!mSound.isPlaying()) {
            if (mPlayerLayout.getVisibility() == View.GONE) {
                ViewUtils.expand(mPlayerLayout);
            }
            if (mSound.isPaused() && mSound.isSameFile(mFilePath)) {
                mSound.resume();
            } else {
                mSound.play(mFilePath);
                mMelodyTitle.setText(mFileName);
            }
        } else {
            if (mSound.isSameFile(mFilePath)) {
                return;
            }
            mSound.play(mFilePath);
            mMelodyTitle.setText(mFileName);
        }
    }

    private void pause() {
        if (mSound.isPlaying()) {
            mSound.pause();
        }
    }

    private void stop() {
        if (mSound.isPlaying()) {
            mSound.stop(true);
        }
        ViewUtils.collapse(mPlayerLayout);
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        mFilter = false;
        mSearchView.setText("");
        mFilter = true;
        if (path.exists()) {
            createFilteredFileList();
        }
        mAdapter = new FileRecyclerAdapter(this, mDataList, recyclerClick, mFilterCallback);
    }

    private void createFilteredFileList() {
        FilenameFilter filter = (dir, filename) -> {
            File sel = new File(dir, filename);
            return (sel.isFile() || sel.isDirectory())
                    && !sel.isHidden();
        };

        List<String> list;
        try {
            list = Arrays.asList(path.list(filter));
        } catch (NullPointerException e) {
            list = new ArrayList<>();
        }
        Collections.sort(list);
        mDataList = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            String fileName = list.get(i);
            File sel = new File(path, fileName);
            mDataList.add(i, new FileDataItem(fileName, 0, sel.toString()));

            if (sel.isDirectory()) {
                mDataList.get(i).setIcon(getDirectoryIcon());
            }
        }

        if (!firstLvl) {
            addUpItem();
        }
    }

    private void addUpItem() {
        ArrayList<FileDataItem> temp = new ArrayList<>(mDataList.size() + 1);
        temp.add(0, new FileDataItem(getString(R.string.up), getUndoIcon(), ""));
        temp.addAll(mDataList);
        mDataList = temp;
    }

    private boolean isMelody(String file) {
        return file != null && (file.endsWith(".mp3") || file.endsWith(".ogg")
                || file.endsWith(".m4a") || file.endsWith(".flac"));
    }

    private boolean isImage(String file) {
        return file != null && (file.endsWith(".jpg") || file.endsWith(".jpeg")
                || file.endsWith(".png") || file.endsWith(".tiff"));
    }

    private int getDirectoryIcon() {
        return isDark ? R.drawable.ic_folder_white_24dp : R.drawable.ic_folder_black_24dp;
    }

    private int getUndoIcon() {
        return isDark ? R.drawable.ic_undo_white_24dp : R.drawable.ic_undo_black_24dp;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!firstLvl) {
            moveUp();
        } else {
            exit();
        }
    }

    private void exit() {
        if (isMelody(mFileName)) {
            stop();
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab:
                    saveChoice();
                    break;
                case R.id.playButton:
                    play();
                    break;
                case R.id.stopButton:
                    stop();
                    break;
                case R.id.pauseButton:
                    pause();
                    break;
                case R.id.clearButton:
                    mSearchView.setText("");
                    break;
            }
        }
    };

    private void saveChoice() {
        if (isMelody(mFileName)) {
            stop();
            sendFile();
        } else {
            Toast.makeText(this, getString(R.string.not_music_file), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SD_CARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadFileList();
                    loadList();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
        }
    }
}
