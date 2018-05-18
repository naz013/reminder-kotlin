package com.elementary.tasks.core.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elementary.tasks.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Copyright 2018 Nazar Suhovich
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
public class PhotoSelectionUtil {

    private static final int PICK_FROM_GALLERY = 25;
    private static final int PICK_FROM_CAMERA = 26;
    private static final int REQUEST_SD_CARD = 1112;
    private static final int REQUEST_CAMERA = 1113;

    @Nullable
    private Uri imageUri;
    @NonNull
    private Activity activity;
    @Nullable
    private UriCallback mCallback;

    public PhotoSelectionUtil(@NonNull Activity activity, @Nullable UriCallback callback) {
        this.activity = activity;
        this.mCallback = callback;
    }

    public void selectImage() {
        CharSequence[] items = {activity.getString(R.string.gallery), activity.getString(R.string.take_a_shot)};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.image);
        builder.setItems(items, (dialog, item) -> {
            dialog.dismiss();
            if (item == 0) {
                pickFromGallery();
            } else {
                takePhoto();
            }
        });
        builder.show();
    }

    private void pickFromGallery() {
        if (!checkSdPermission(REQUEST_SD_CARD)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        Intent chooser = Intent.createChooser(intent, activity.getString(R.string.gallery));
        activity.startActivityForResult(chooser, PICK_FROM_GALLERY);
    }

    private boolean checkSdPermission(int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Permissions.checkPermission(activity, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
                Permissions.requestPermission(activity, code, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    private boolean checkCameraPermission(int code) {
        if (Module.isNougat()) {
            if (!Permissions.checkPermission(activity, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL, Permissions.CAMERA)) {
                Permissions.requestPermission(activity, code, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL, Permissions.CAMERA);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    private void showPhoto(@NonNull Uri imageUri) {
        Timber.d("showPhoto: %s", imageUri);
        if (mCallback != null) {
            mCallback.onImageSelected(imageUri, null);
        }
    }

    private void takePhoto() {
        if (!checkCameraPermission(REQUEST_CAMERA)) {
            return;
        }
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(activity.getPackageManager()) == null) {
            return;
        }
        if (Module.isNougat()) {
            if (pictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                File photoFile = createImageFile();
                imageUri = UriUtil.getUri(activity, photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                activity.startActivityForResult(pictureIntent, PICK_FROM_CAMERA);
            }
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            activity.startActivityForResult(pictureIntent, PICK_FROM_CAMERA);

        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) storageDir.mkdirs();
        return new File(storageDir, imageFileName + ".jpg");
    }

    private File getExternalFilesDir(String directoryPictures) {
        File sd = Environment.getExternalStorageDirectory();
        return new File(sd, new File(directoryPictures, "Reminder").toString());
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        }
        switch (requestCode) {
            case REQUEST_SD_CARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                }
                break;
            case REQUEST_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                }
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("onActivityResult: %d, %d, %s", requestCode, resultCode, data);
        if (requestCode == PICK_FROM_CAMERA && (resultCode == Activity.RESULT_OK)) {
            if (imageUri != null) showPhoto(imageUri);
        } else if (requestCode == PICK_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            imageUri = data.getData();
            if (imageUri != null) {
                showPhoto(imageUri);
            } else if (data.getClipData() != null) {
                if (mCallback != null) mCallback.onImageSelected(null, data.getClipData());
            }
        }
    }

    public interface UriCallback {
        void onImageSelected(@Nullable Uri uri, @Nullable ClipData clipData);
    }
}
