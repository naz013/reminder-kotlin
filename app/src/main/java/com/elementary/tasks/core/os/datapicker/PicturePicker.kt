package com.elementary.tasks.core.os.datapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.CacheUtil
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PicturePicker private constructor(
  launcherCreator: LauncherCreator<Intent, ActivityResult>,
  private val resultCallback: (String) -> Unit
) : IntentPicker<Intent, ActivityResult>(
  ActivityResultContracts.StartActivityForResult(),
  launcherCreator
), KoinComponent {

  private val cacheUtil by inject<CacheUtil>()
  private val dispatcherProvider by inject<DispatcherProvider>()
  private val scope = CoroutineScope(Job())

  constructor(
    activity: ComponentActivity,
    resultCallback: (pathToCache: String) -> Unit
  ) : this(ActivityLauncherCreator(activity), resultCallback)

  constructor(
    fragment: Fragment,
    resultCallback: (pathToCache: String) -> Unit
  ) : this(FragmentLauncherCreator(fragment), resultCallback)

  fun pickPicture() {
    launch(getIntent())
  }

  override fun dispatchResult(result: ActivityResult) {
    if (result.resultCode == Activity.RESULT_OK) {
      result.data?.data?.also { cacheFile(it) }
    }
  }

  private fun cacheFile(uri: Uri) {
    scope.launch(dispatcherProvider.io()) {
      val path = cacheUtil.cacheFile(uri)
      if (path != null) {
        withUIContext { resultCallback.invoke(path) }
      }
    }
  }

  private fun getIntent(): Intent {
    val intent = Intent()
    intent.type = "image/*"
    intent.action = Intent.ACTION_GET_CONTENT
    return Intent.createChooser(intent, getActivity().getString(R.string.select_image))
  }
}
