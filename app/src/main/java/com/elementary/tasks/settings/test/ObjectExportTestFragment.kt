package com.elementary.tasks.settings.test

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.elementary.tasks.core.os.datapicker.UriPicker
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.elementary.tasks.notes.ObserveEvent
import com.github.naz013.ui.common.fragment.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class ObjectExportTestFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<ObjectExportViewModel>()
  private val uriPicker = UriPicker(this)

  @Composable
  override fun Content() {
    val state by viewModel.state.collectAsState()
    viewModel.navigationEvent.ObserveEvent { handleEvent(it) }

    ObjectExportScreen(
      state = state,
      onObjectTypeSelected = viewModel::onObjectTypeSelected,
      onItemClick = viewModel::onItemClick,
    )
  }

  private fun handleEvent(event: ObjectExportEvent) {
    when (event) {
      is ObjectExportEvent.RequestSaveLocation -> {
        val intent =
          Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, event.fileName)
          }
        uriPicker.launchIntent(intent) { uri ->
          if (uri != null) {
            viewModel.onSaveLocationPicked(event.itemId, uri)
          }
        }
      }

      ObjectExportEvent.ObjectSaved -> toast("Object is saved")
    }
  }

  override fun getTitle(): String = "Save object to File"
}
