package com.elementary.tasks.core.utils

import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Patterns
import android.view.LayoutInflater
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import coil.request.ImageRequest
import com.elementary.tasks.R
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.datapicker.CameraPhotoPicker
import com.elementary.tasks.core.os.datapicker.MultiPicturePicker
import com.elementary.tasks.databinding.ViewUrlFieldBinding
import com.github.naz013.common.Module
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.fragment.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PhotoSelectionUtil(
  private val fragment: Fragment,
  private val mCallback: UriCallback?,
) : DefaultLifecycleObserver,
  KoinComponent {
  private val dialogues by inject<Dialogues>()
  private val imageLoader by inject<ImageLoader>()

  private lateinit var permissionFlow: PermissionFlow
  private lateinit var multiPicturePicker: MultiPicturePicker
  private lateinit var cameraPhotoPicker: CameraPhotoPicker

  private val coroutineScope = CoroutineScope(Job())

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    permissionFlow = PermissionFlow(fragment, dialogues)
    multiPicturePicker = MultiPicturePicker(fragment) { mCallback?.onImageSelected(it) }
    cameraPhotoPicker = CameraPhotoPicker(fragment) { mCallback?.onImageSelected(listOf(it)) }
  }

  fun onDestroy() {
    coroutineScope.cancel()
  }

  interface UriCallback {
    fun onImageSelected(uris: List<Uri>)

    fun onBitmapReady(bitmap: Bitmap)
  }

  companion object {
    private const val TAG = "PhotoSelectionUtil"
  }
}
