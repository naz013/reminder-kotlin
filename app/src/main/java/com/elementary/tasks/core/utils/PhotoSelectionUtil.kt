package com.elementary.tasks.core.utils

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Patterns
import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.request.ImageRequest
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.datapicker.CameraPhotoPicker
import com.elementary.tasks.core.os.datapicker.MultiPicturePicker
import com.github.naz013.ui.common.Dialogues
import com.elementary.tasks.databinding.ViewUrlFieldBinding
import com.github.naz013.common.Module
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.activity.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PhotoSelectionUtil(
  private val activity: ComponentActivity,
  private val mCallback: UriCallback?
) : DefaultLifecycleObserver, KoinComponent {

  private val dialogues by inject<Dialogues>()
  private val imageLoader by inject<ImageLoader>()

  private lateinit var permissionFlow: PermissionFlow
  private lateinit var multiPicturePicker: MultiPicturePicker
  private lateinit var cameraPhotoPicker: CameraPhotoPicker

  private val coroutineScope = CoroutineScope(Job())

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    permissionFlow = PermissionFlow(activity, dialogues)
    multiPicturePicker = MultiPicturePicker(activity) { mCallback?.onImageSelected(it) }
    cameraPhotoPicker = CameraPhotoPicker(activity) {
      mCallback?.onImageSelected(listOf(it))
    }
  }

  fun selectImage() {
    val hasCamera = Module.hasCamera(activity)
    val items = if (hasCamera) {
      arrayOf(
        getString(R.string.gallery),
        getString(R.string.take_a_shot),
        getString(R.string.from_url)
      )
    } else {
      arrayOf(
        getString(R.string.gallery),
        getString(R.string.from_url)
      )
    }
    val builder = dialogues.getMaterialDialog(activity)
    builder.setTitle(R.string.image)
    builder.setItems(items) { dialog, item ->
      dialog.dismiss()
      if (hasCamera) {
        when (item) {
          0 -> tryToPickFromGallery()
          1 -> tryToTakePhoto()
          2 -> checkClipboard()
        }
      } else {
        when (item) {
          0 -> tryToPickFromGallery()
          1 -> checkClipboard()
        }
      }
    }
    builder.create().show()
  }

  fun onDestroy() {
    coroutineScope.cancel()
  }

  private fun tryToPickFromGallery() {
    permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
      multiPicturePicker.pickPictures()
    }
  }

  private fun tryToTakePhoto() {
    permissionFlow.askPermissions(
      listOf(
        Permissions.CAMERA,
        Permissions.WRITE_EXTERNAL,
        Permissions.READ_EXTERNAL
      )
    ) {
      cameraPhotoPicker.takePhoto()
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
    val view = ViewUrlFieldBinding.inflate(LayoutInflater.from(activity))
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
      coroutineScope.launch(Dispatchers.Default) {
        try {
          val request = ImageRequest.Builder(activity)
            .data(url)
            .build()
          val bitmap = imageLoader.execute(request).drawable?.toBitmap()
          if (bitmap != null) {
            withUIContext {
              mCallback?.onBitmapReady(bitmap)
            }
          } else {
            withUIContext {
              activity.toast(R.string.failed_to_download)
            }
          }
        } catch (e: Exception) {
          Logger.d("downloadUrl: $e")
          withUIContext {
            activity.toast(R.string.failed_to_download)
          }
        }
      }
    } else {
      activity.toast(R.string.wrong_url)
    }
  }

  private fun getString(@StringRes res: Int) = activity.getString(res)

  interface UriCallback {
    fun onImageSelected(uris: List<Uri>)

    fun onBitmapReady(bitmap: Bitmap)
  }
}
