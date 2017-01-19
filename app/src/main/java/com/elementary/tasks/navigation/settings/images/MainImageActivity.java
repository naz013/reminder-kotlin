package com.elementary.tasks.navigation.settings.images;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.ThemedActivity;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Permissions;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.core.views.roboto.RoboRadioButton;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private LinearLayout emptyItem;
    private RadioGroup selectGroup;
    private RecyclerView imagesList;
    private ImagesRecyclerAdapter mAdapter;
    private RelativeLayout fullContainer;
    private ImageView fullImageView;
    private ImageButton downloadButton;
    private ImageButton setToMonthButton;
    private RoboTextView photoInfoView;
    private CardView imageContainer;

    private List<ImageItem> mPhotoList = new ArrayList<>();
    private int mPointer;

    private int position = -1;
    private ImageItem mSelectedItem;

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            int visiblePosition = layoutManager.findLastVisibleItemPosition();
            int count = mAdapter.getItemCount();
            if (visiblePosition >= count - 10 && mPointer < mPhotoList.size() -1 && mPointer < count + (START_SIZE / 2) - 1) {
                int endPoint = mPointer + (START_SIZE / 2);
                boolean last = endPoint >= mPhotoList.size();
                if (last) endPoint = mPhotoList.size() - 1;
                List<ImageItem> nextChunk = mPhotoList.subList(mPointer, endPoint);
                mPointer += (START_SIZE / 2);
                if (last) mPointer = mPhotoList.size() - 1;
                mAdapter.addItems(nextChunk);
            }
        }
    };
    private Call<List<ImageItem>> mCall;
    private Callback<List<ImageItem>> mPhotoCallback = new Callback<List<ImageItem>>() {
        @Override
        public void onResponse(Call<List<ImageItem>> call, Response<List<ImageItem>> response) {
            if (response.code() == Api.OK) {
                mPhotoList = new ArrayList<>(response.body());
                if (position != -1) mPhotoList.get(position).setSelected(true);
                loadDataToList();
            }
        }

        @Override
        public void onFailure(Call<List<ImageItem>> call, Throwable t) {

        }
    };
    private SelectListener mListener = new SelectListener() {
        @Override
        public void onImageSelected(boolean b) {
            if (b) {
                selectGroup.clearCheck();
            } else {
                ((RoboRadioButton) findViewById(R.id.defaultCheck)).setChecked(true);
            }
        }

        @Override
        public void deselectOverItem(int position) {
            mPhotoList.get(position).setSelected(false);
        }

        @Override
        public void onItemLongClicked(int position, View view) {
            showImage(position);
        }
    };
    private ViewUtils.AnimationCallback mAnimationCallback = (code) -> {
        if (code == 0) {
            fullImageView.setImageBitmap(null);
            mSelectedItem = null;
        }
    };

    private void loadDataToList() {
        mPointer = START_SIZE - 1;
        mAdapter = new ImagesRecyclerAdapter(this, mPhotoList.subList(0, mPointer), mListener);
        mAdapter.setPrevSelected(position);
        imagesList.setAdapter(mAdapter);
        emptyItem.setVisibility(View.GONE);
        imagesList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_image_layout);
        initActionBar();

        selectGroup = (RadioGroup) findViewById(R.id.selectGroup);
        RoboRadioButton defaultCheck = (RoboRadioButton) findViewById(R.id.defaultCheck);
        RoboRadioButton noneCheck = (RoboRadioButton) findViewById(R.id.noneCheck);
        defaultCheck.setOnCheckedChangeListener(this);
        noneCheck.setOnCheckedChangeListener(this);
        position = Prefs.getInstance(this).getImageId();
        String path = Prefs.getInstance(this).getImagePath();
        if (path.matches(NONE_PHOTO)) {
            noneCheck.setChecked(true);
        } else if (position == -1 || path.matches(DEFAULT_PHOTO)) {
            defaultCheck.setChecked(true);
        }
        emptyItem = (LinearLayout) findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) findViewById(R.id.emptyText);
        emptyText.setText(R.string.no_images);
        ImageView emptyImage = (ImageView) findViewById(R.id.emptyImage);
        if (themeUtil.isDark()) {
            emptyImage.setImageResource(R.drawable.ic_broken_image_white_24dp);
        } else {
            emptyImage.setImageResource(R.drawable.ic_broken_image_black_24dp);
        }
        initRecyclerView();
        initImageContainer();
        mCall = RetrofitBuilder.getApi().getAllImages();
        mCall.enqueue(mPhotoCallback);
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setTitle(getString(R.string.main_image));
    }

    private void initImageContainer() {
        imageContainer = (CardView) findViewById(R.id.imageContainer);
        imageContainer.setVisibility(View.GONE);
        fullContainer = (RelativeLayout) findViewById(R.id.fullContainer);
        fullContainer.setVisibility(View.GONE);
        fullContainer.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                hideImage();
            }
            return true;
        });
        photoInfoView = (RoboTextView) findViewById(R.id.photoInfoView);
        fullImageView = (ImageView) findViewById(R.id.fullImageView);
        fullImageView.setOnTouchListener((view, motionEvent) -> true);
        downloadButton = (ImageButton) findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(view -> showDownloadDialog());
        setToMonthButton = (ImageButton) findViewById(R.id.setToMonthButton);
        setToMonthButton.setOnClickListener(view -> showMonthDialog());
        if (!Prefs.getInstance(this).isCalendarImagesEnabled()) {
            setToMonthButton.setVisibility(View.GONE);
        }
        if (themeUtil.isDark()) {
            downloadButton.setImageResource(R.drawable.ic_get_app_white_24dp);
            setToMonthButton.setImageResource(R.drawable.ic_calendar_white);
        } else {
            downloadButton.setImageResource(R.drawable.ic_get_app_black_24dp);
            setToMonthButton.setImageResource(R.drawable.ic_calendar);
        }
    }

    private void showMonthDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.month_list, (dialogInterface, i) -> setImageFotMonth(i));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDownloadDialog() {
        if (!Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, 112, Permissions.WRITE_EXTERNAL);
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence maxSize = mSelectedItem.getHeight() + "x" + mSelectedItem.getWidth();
        builder.setItems(new CharSequence[]{maxSize, "1920x1080", "1280x768", "800x480"}, (dialogInterface, i) -> {
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
            photoInfoView.setText(getString(R.string.number) + mSelectedItem.getId() + " " + mSelectedItem.getAuthor());
            Picasso.with(this)
                    .load(RetrofitBuilder.getImageLink(mSelectedItem.getId()))
                    .error(themeUtil.isDark() ? R.drawable.ic_broken_image_white_24dp : R.drawable.ic_broken_image_black_24dp)
                    .into(fullImageView);
            ViewUtils.showReveal(fullContainer);
            ViewUtils.show(this, imageContainer, mAnimationCallback);
        }
    }

    private void hideImage() {
        ViewUtils.hide(this, imageContainer, mAnimationCallback);
        ViewUtils.hideReveal(fullContainer);
    }

    private void downloadImage(int width, int height) {
        if (!Permissions.checkPermission(this, Permissions.WRITE_EXTERNAL)) {
            Permissions.requestPermission(this, 112, Permissions.WRITE_EXTERNAL);
            return;
        }
        if (MemoryUtil.isSdPresent()) {
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

    private void setImageFotMonth(int month) {
        MonthImage monthImage = Prefs.getInstance(this).getCalendarImages();
        monthImage.setPhoto(month, mSelectedItem.getId());
        Prefs.getInstance(this).setCalendarImages(monthImage);
        hideImage();
    }

    private void initRecyclerView() {
        imagesList = (RecyclerView) findViewById(R.id.imagesList);
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
        imagesList.setOnScrollListener(mOnScrollListener);
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
        Prefs.getInstance(this).setImagePath(imageUrl);
        Prefs.getInstance(this).setImageId(id);
        if (mAdapter != null) mAdapter.deselectLast();
    }

    @Override
    public void onBackPressed() {
        if (fullContainer.getVisibility() == View.VISIBLE) {
            hideImage();
        } else {
            finish();
        }
    }
}
