package com.elementary.tasks.settings.test

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.fragment.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeveloperFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<DeveloperViewModel>()

  @Composable
  override fun Content() {
    DeveloperScreen(onResetBannersClick = { viewModel.onResetBannersClick() })
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.bannersReset.observeEvent(viewLifecycleOwner) {
      toast("Home Screen banners have been reset")
    }
  }

  override fun getTitle(): String = "Developer"
}

@Composable
private fun DeveloperScreen(onResetBannersClick: () -> Unit) {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
  ) {
    DeveloperOption(
      title = "Reset banners state on Home Screen",
      subtitle = "Shows the privacy, login and what's new banners again",
      onClick = onResetBannersClick,
    )
    HorizontalDivider()
  }
}

@Composable
private fun DeveloperOption(
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  ListItem(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
    headlineContent = { Text(text = title) },
    supportingContent = { Text(text = subtitle) },
  )
}

@Preview(showBackground = true)
@Composable
private fun DeveloperScreenPreview() {
  DeveloperScreen(onResetBannersClick = {})
}
