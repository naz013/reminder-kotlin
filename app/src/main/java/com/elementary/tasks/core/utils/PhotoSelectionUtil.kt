package com.elementary.tasks.core.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore

import com.elementary.tasks.R

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import timber.log.Timber

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class PhotoSelectionUtil(private val activity: Activity, private val mCallback: UriCallback?) {

    private var imageUri: Uri? = null

    fun selectImage() {
        val items = arrayOf<CharSequence>(activity.getString(R.string.gallery), activity.getString(R.string.take_a_shot))
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.image)
        builder.setItems(items) { dialog, item ->
            dialog.dismiss()
            if (item == 0) {
                pickFromGallery()
            } else {
                takePhoto()
            }
        }
        builder.show()
    }

    private fun pickFromGallery() {
        if (!checkSdPermission(REQUEST_SD_CARD)) {
            return
        }
        var intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        val chooser = Intent.createChooser(intent, activity.getString(R.string.gallery))
        try {
            activity.startActivityForResult(chooser, PICK_FROM_GALLERY)
        } catch (e: ActivityNotFoundException) {
            checkSdPermission(REQUEST_SD_CARD)
        }
    }

    private fun checkSdPermission(code: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return if (!Permissions.checkPermission(activity, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
                Permissions.requestPermission(activity, code, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
                false
            } else {
                true
            }
        }
        return true
    }

    private fun checkCameraPermission(code: Int): Boolean {
        if (Module.isNougat) {
            return if (!Permissions.checkPermission(activity, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL, Permissions.CAMERA)) {
                Permissions.requestPermission(activity, code, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL, Permissions.CAMERA)
                false
            } else {
                true
            }
        }
        return true
    }

    private fun showPhoto(imageUri: Uri) {
        Timber.d("showPhoto: %s", imageUri)
        mCallback?.onImageSelected(imageUri, null)
    }

    private fun takePhoto() {
        if (!checkCameraPermission(REQUEST_CAMERA)) {
            return
        }
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(activity.packageManager) == null) {
            return
        }
        if (Module.isNougat) {
            if (pictureIntent.resolveActivity(activity.packageManager) != null) {
                val photoFile = createImageFile()
                imageUri = UriUtil.getUri(activity, photoFile)
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                try {
                    activity.startActivityForResult(pictureIntent, PICK_FROM_CAMERA)
                } catch (e: ActivityNotFoundException) {
                    checkCameraPermission(REQUEST_CAMERA)
                }
            }
        } else {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
            imageUri = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            try {
                activity.startActivityForResult(pictureIntent, PICK_FROM_CAMERA)
            } catch (e: ActivityNotFoundException) {
                checkCameraPermission(REQUEST_CAMERA)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, "$imageFileName.jpg")
    }

    private fun getExternalFilesDir(directoryPictures: String): File {
        val sd = Environment.getExternalStorageDirectory()
        return File(sd, File(directoryPictures, "Reminder").toString())
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) {
            return
        }
        when (requestCode) {
            REQUEST_SD_CARD -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFromGallery()
            }
            REQUEST_CAMERA -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto()
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult: %d, %d, %s", requestCode, resultCode, data)
        if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            if (imageUri != null) showPhoto(imageUri!!)
        } else if (requestCode == PICK_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            if (imageUri != null) {
                showPhoto(imageUri!!)
            } else if (data?.clipData != null) {
                mCallback?.onImageSelected(null, data.clipData)
            }
        }
    }

    interface UriCallback {
        fun onImageSelected(uri: Uri?, clipData: ClipData?)
    }

    companion object {

        private const val PICK_FROM_GALLERY = 25
        private const val PICK_FROM_CAMERA = 26
        private const val REQUEST_SD_CARD = 1112
        private const val REQUEST_CAMERA = 1113
    }
}
