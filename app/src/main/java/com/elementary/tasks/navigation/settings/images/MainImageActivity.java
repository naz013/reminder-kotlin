package com.elementary.tasks.navigation.settings.images;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.data.models.MainImage;
import com.elementary.tasks.core.network.Api;
import com.elementary.tasks.core.network.RetrofitBuilder;
import com.elementary.tasks.core.utils.Dialogues;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.UriUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.view_models.main_image.MainImagesViewModel;
import com.elementary.tasks.core.views.roboto.RoboRadioButton;
import com.elementary.tasks.databinding.ActivityMainImageBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
public class MainImageActivity extends ThemedActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String DEFAULT_PHOTO = "https://unsplash.it/1280/768?image=33";
    private static final String NONE_PHOTO = "";
    private static final String TAG = "MainImageActivity";
    private static final int START_SIZE = 50;
    private static final int REQUEST_DOWNLOAD = 112;
    private static final int REQUEST_REMINDER = 113;

    private ActivityMainImageBinding binding;
    private RecyclerView imagesList;
    private ImagesRecyclerAdapter mAdapter;

    private List<MainImage> mPhotoList = new ArrayList<>();
    private int mPointer;

    private int position = -1;
    private int mWidth, mHeight;
    @Nullable
    private MainImage mSelectedItem;
    private MainImagesViewModel viewModel;

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            int visiblePosition = layoutManager.findLastVisibleItemPosition();
            int count = mAdapter.getItemCount();
            if (visiblePosition >= count - 10 && mPointer < mPhotoList.size() - 1 && mPointer < count + (START_SIZE / 2) - 1) {
                int endPoint = mPointer + (START_SIZE / 2);
                boolean last = endPoint >= mPhotoList.size();
                if (last) endPoint = mPhotoList.size() - 1;
                List<MainImage> nextChunk = mPhotoList.subList(mPointer, endPoint);
                mPointer += (START_SIZE / 2);
                if (last) mPointer = mPhotoList.size() - 1;
                mAdapter.addItems(nextChunk);
            }
        }
    };
    @Nullable
    private Call<List<MainImage>> mCall;
    private Callback<List<MainImage>> mPhotoCallback = new Callback<List<MainImage>>() {
        @Override
        public void onResponse(@NonNull Call<List<MainImage>> call, Response<List<MainImage>> response) {
            LogUtil.d(TAG, "onResponse: " + response.code() + ", " + response.message());
            if (response.code() == Api.OK) {
                if (mPhotoList.isEmpty() && response.body() != null) {
                    mPhotoList = new ArrayList<>(response.body());
                    saveToDb(response.body());
                    loadDataToList();
                } else {
                    saveToDb(response.body());
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<MainImage>> call, Throwable t) {
            LogUtil.d(TAG, "onFailure: " + t.getLocalizedMessage());
        }
    };

    private void saveToDb(List<MainImage> body) {
        viewModel.saveImages(body);
    }

    private SelectListener mListener = new SelectListener() {
        @Override
        public void onImageSelected(boolean b) {
            if (b) {
                binding.selectGroup.clearCheck();
            } else {
                ((RoboRadioButton) findViewById(R.id.defaultCheck)).setChecked(true);
            }
        }

        @Override
        public void deselectOverItem(int position) {

        }

        @Override
        public void onItemLongClicked(int position, View view) {
            showImage(position);
        }
    };
    private ViewUtils.AnimationCallback mAnimationCallback = (code) -> {
        if (code == 0) {
            binding.fullImageView.setImageBitmap(null);
            mSelectedItem = null;
        }
    };

    private void loadDataToList() {
        mPointer = START_SIZE - 1;
        mAdapter = new ImagesRecyclerAdapter(this, mPhotoList.subList(0, mPointer), mListener);
        mAdapter.setPrevSelected(position);
        imagesList.setAdapter(mAdapter);
        binding.emptyLayout.emptyItem.setVisibility(View.GONE);
        imagesList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_image);
        initActionBar();
        initRadios();
        binding.emptyLayout.emptyItem.setVisibility(View.VISIBLE);
        binding.emptyLayout.emptyText.setText(R.string.no_images);
        initRecyclerView();
        initImageContainer();

        initViewModel();
    }

    private void initViewModel() {
        viewModel = ViewModelProviders.of(this).get(MainImagesViewModel.class);
        viewModel.images.observe(this, mainImages -> {
            if (mainImages != null) {
                mPhotoList = mainImages;
            }
            initData();
        });
    }

    private void initData() {
        if (!mPhotoList.isEmpty()) {
            loadDataToList();
        }
        mCall = RetrofitBuilder.getUnsplashApi().getAllImages();
        mCall.enqueue(mPhotoCallback);
    }

    private void initRadios() {
        binding.defaultCheck.setOnCheckedChangeListener(this);
        binding.noneCheck.setOnCheckedChangeListener(this);
        position = getPrefs().getImageId();
        String path = getPrefs().getImagePath();
        if (path.matches(NONE_PHOTO)) {
            binding.noneCheck.setChecked(true);
        } else if (position == -1 || path.matches(DEFAULT_PHOTO)) {
            binding.defaultCheck.setChecked(true);
        }
    }

    private void initActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        binding.toolbar.setTitle(getString(R.string.main_image));
    }

    private void initImageContainer() {
        binding.imageContainer.setVisibility(View.GONE);
        binding.fullContainer.setVisibility(View.GONE);
        binding.fullContainer.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                hideImage();
            }
            return true;
        });
        binding.fullImageView.setOnTouchListener((view, motionEvent) -> true);
        binding.downloadButton.setOnClickListener(view -> showDownloadDialog());
        binding.setToMonthButton.setOnClickListener(view -> showMonthDialog());
        binding.setReminderButton.setOnClickListener(v -> setToReminder());
        if (!getPrefs().isCalendarImagesEnabled()) {
            binding.setToMonthButton.setVisibility(View.GONE);
        }
    }

    private void setToReminder() {
        if (!Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL)) {
            Permissions.requestPermission(this, REQUEST_REMINDER, Permissions.WRITE_EXTERNAL, Permissions.READ_EXTERNAL);
            return;
        }
        if (mSelectedItem != null && MemoryUtil.isSdPresent()) {
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File imageFile = new File(folder, mSelectedItem.getFilename());
            Uri destination = UriUtil.getUri(this, imageFile);
            getPrefs().setReminderImage(destination.toString());
            new DownloadAsync(this, mSelectedItem.getFilename(), imageFile.toString(), 1080, 1920, mSelectedItem.getId()).execute();
        } else {
            Toast.makeText(this, R.string.no_sd_card, Toast.LENGTH_SHORT).show();
            return;
        }
        hideImage();
    }

    private void showMonthDialog() {
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        builder.setItems(R.array.month_list, (dialogInterface, i) -> setImageForMonth(i));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDownloadDialog() {
        if (!Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, REQUEST_DOWNLOAD, Permissions.WRITE_EXTERNAL);
            return;
        }
        if (mSelectedItem == null) return;
        AlertDialog.Builder builder = Dialogues.getDialog(this);
        CharSequence maxSize = mSelectedItem.getHeight() + "x" + mSelectedItem.getWidth();
        builder.setItems(new CharSequence[]{maxSize, "1080x1920", "768x1280", "480x800"}, (dialogInterface, i) -> {
            int width = mSelectedItem.getWidth();
            int height = mSelectedItem.getHeight();
            switch (i) {
                case 1:
                    width = 1080;
                    height = 1920;
                    break;
                case 2:
                    width = 768;
                    height = 1280;
                    break;
                case 3:
                    width = 480;
                    height = 800;
                    break;
            }
            downloadImage(width, height);
            dialogInterface.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showImage(int position) {
        mSelectedItem = mPhotoList.get(position);
        if (mSelectedItem != null) {
            binding.photoInfoView.setText(getString(R.string.number) + mSelectedItem.getId() + " " + mSelectedItem.getAuthor());
            Glide.with(binding.fullImageView)
                    .load(RetrofitBuilder.getImageLink(mSelectedItem.getId()))
                    .into(binding.fullImageView);
            ViewUtils.showReveal(binding.fullContainer);
            ViewUtils.show(this, binding.imageContainer, mAnimationCallback);
        }
    }

    private void hideImage() {
        ViewUtils.hide(this, binding.imageContainer, mAnimationCallback);
        ViewUtils.hideReveal(binding.fullContainer);
    }

    private void downloadImage(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        if (!Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, 112, Permissions.WRITE_EXTERNAL);
            return;
        }
        if (mSelectedItem != null && MemoryUtil.isSdPresent()) {
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File imageFile = new File(folder, mSelectedItem.getFilename());
            new DownloadAsync(this, mSelectedItem.getFilename(), imageFile.toString(), width, height, mSelectedItem.getId()).execute();
        } else {
            Toast.makeText(this, R.string.no_sd_card, Toast.LENGTH_SHORT).show();
            return;
        }
        hideImage();
    }

    private void setImageForMonth(int month) {
        if (mSelectedItem == null) return;
        MonthImage monthImage = getPrefs().getCalendarImages();
        monthImage.setPhoto(month, mSelectedItem.getId());
        getPrefs().setCalendarImages(monthImage);
        hideImage();
    }

    private void initRecyclerView() {
        imagesList = binding.imagesList;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (position % 6) {
                    case 5:
                        return 3;
                    case 3:
                        return 2;
                    default:
                        return 1;
                }
            }
        });
        imagesList.setLayoutManager(gridLayoutManager);
        imagesList.addItemDecoration(new GridMarginDecoration(getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        imagesList.setHasFixedSize(true);
        imagesList.setItemAnimator(new DefaultItemAnimator());
        imagesList.addOnScrollListener(mOnScrollListener);
        imagesList.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCall != null && !mCall.isExecuted()) {
            mCall.cancel();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.defaultCheck:
                if (b) setImageUrl(DEFAULT_PHOTO, -1);
                break;
            case R.id.noneCheck:
                if (b) setImageUrl(NONE_PHOTO, -1);
                break;
        }
    }

    private void setImageUrl(String imageUrl, int id) {
        getPrefs().setImagePath(imageUrl);
        getPrefs().setImageId(id);
        if (mAdapter != null) mAdapter.deselectLast();
    }

    @Override
    public void onBackPressed() {
        if (binding.fullContainer.getVisibility() == View.VISIBLE) {
            hideImage();
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return;
        switch (requestCode) {
            case REQUEST_DOWNLOAD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadImage(mWidth, mHeight);
                }
                break;
            case REQUEST_REMINDER:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setToReminder();
                }
                break;
        }
    }
}
