package com.elementary.tasks.core.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import com.elementary.tasks.R
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhotoSelectionUtil(private val activity: Activity, private val dialogues: Dialogues,
                         private val urlSupported: Boolean = true, private val mCallback: UriCallback?) {

  private var imageUri: Uri? = null

  fun selectImage() {
    val hasCamera = Module.hasCamera(activity)
    val items = if (urlSupported) {
      if (hasCamera) {
        arrayOf(
          activity.getString(R.string.gallery),
          activity.getString(R.string.take_a_shot),
          activity.getString(R.string.from_url)
        )
      } else {
        arrayOf(activity.getString(R.string.gallery), activity.getString(R.string.from_url))
      }
    } else {
      if (hasCamera) {
        arrayOf(activity.getString(R.string.gallery), activity.getString(R.string.take_a_shot))
      } else {
        arrayOf(activity.getString(R.string.gallery))
      }
    }
    val builder = dialogues.getMaterialDialog(activity)
    builder.setTitle(R.string.image)
    builder.setItems(items) { dialog, item ->
      dialog.dismiss()
      if (hasCamera) {
        when (item) {
          0 -> pickFromGallery()
          1 -> takePhoto()
          2 -> checkClipboard()
        }
      } else {
        when (item) {
          0 -> pickFromGallery()
          1 -> checkClipboard()
        }
      }
    }
    builder.create().show()
  }

  private fun pickFromGallery() {
    if (!checkSdPermission(REQUEST_SD_CARD)) {
      return
    }
    var intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "image/*"
    if (urlSupported) {
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
    return Permissions.checkPermission(activity, code, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)
  }

  private fun checkCameraPermission(code: Int): Boolean {
    return Permissions.checkPermission(activity, code, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL, Permissions.CAMERA)
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
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "IMG_" + timeStamp + "_"
    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    if (!storageDir.exists()) storageDir.mkdirs()
    return File(storageDir, "$imageFileName.jpg")
  }

  private fun getExternalFilesDir(directoryPictures: String): File {
    val sd = Environment.getExternalStorageDirectory()
    return File(sd, File(directoryPictures, "Reminder").toString())
  }

  @Suppress("UNUSED_PARAMETER")
  fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    if (Permissions.checkPermission(grantResults)) {
      when (requestCode) {
        REQUEST_SD_CARD -> pickFromGallery()
        REQUEST_CAMERA -> takePhoto()
      }
    }
  }

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    Timber.d("onActivityResult: %d, %d, %s", requestCode, resultCode, data)
    if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
      val uri = imageUri ?: return
      showPhoto(uri)
    } else if (requestCode == PICK_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
      imageUri = data?.data
      val clipData = data?.clipData
      val uri = imageUri
      if (uri != null) {
        showPhoto(uri)
      } else if (clipData != null) {
        mCallback?.onImageSelected(null, clipData)
      }
    }
  }

  private fun checkClipboard() {
    val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
      ?: return
    if (clipboard.hasPrimaryClip()) {
      val text = clipboard.primaryClip?.getItemAt(0)?.text
      if (text != null && Patterns.WEB_URL.matcher(text).matches()) {
        showClipboardDialog(text.toString())
      } else {
        showUrlDialog()
      }
    } else {
      showUrlDialog()
    }
  }

  private fun showUrlDialog() {
    val builder = dialogues.getMaterialDialog(activity)
    val view = com.elementary.tasks.databinding.ViewUrlFieldBinding.inflate(LayoutInflater.from(activity))
    builder.setView(view.root)
    builder.setPositiveButton(R.string.download) { dialog, _ ->
      dialog.dismiss()
      downloadUrl(view.urlField.text.toString().trim())
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
    }
    builder.create().show()
  }

  private fun showClipboardDialog(text: String) {
    val builder = dialogues.getMaterialDialog(activity)
    builder.setMessage(text)
    builder.setPositiveButton(R.string.download) { dialog, _ ->
      dialog.dismiss()
      downloadUrl(text)
    }
    builder.setNegativeButton(R.string.cancel) { dialog, _ ->
      dialog.dismiss()
      showUrlDialog()
    }
    builder.create().show()
  }

  private fun downloadUrl(url: String) {
    if (Patterns.WEB_URL.matcher(url).matches()) {
      launchDefault {
        try {
          val bitmap = Picasso.get()
            .load(url)
            .get()
          if (bitmap != null) {
            withUIContext {
              mCallback?.onBitmapReady(bitmap)
            }
          } else {
            withUIContext {
              Toast.makeText(activity, R.string.failed_to_download, Toast.LENGTH_SHORT).show()
            }
          }
        } catch (e: Exception) {
          Timber.d("downloadUrl: $e")
          withUIContext {
            Toast.makeText(activity, R.string.failed_to_download, Toast.LENGTH_SHORT).show()
          }
        }
      }
    } else {
      Toast.makeText(activity, R.string.wrong_url, Toast.LENGTH_SHORT).show()
    }
  }

  interface UriCallback {
    fun onImageSelected(uri: Uri?, clipData: ClipData?)

    fun onBitmapReady(bitmap: Bitmap)
  }

  companion object {

    private const val PICK_FROM_GALLERY = 25
    private const val PICK_FROM_CAMERA = 26
    private const val REQUEST_SD_CARD = 1112
    private const val REQUEST_CAMERA = 1113
  }
}
