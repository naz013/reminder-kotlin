package com.elementary.tasks.settings.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.viewbinding.ViewBinding
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.ui.common.compose.composeView
import com.github.naz013.ui.common.fragment.toast

class DeveloperFragment : BaseSettingsFragment<DeveloperFragment.ComposeBinding>() {
  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): ComposeBinding = ComposeBinding(composeView { DeveloperScreen(onResetBannersClick = ::resetBannerStates) })

  private fun resetBannerStates() {
    prefs.isPrivacyPolicyShowed = false
    prefs.isUserLogged = false
    prefs.lastVersionCode = 0
    toast("Home Screen banners have been reset")
  }

  override fun getTitle(): String = "Developer"

  class ComposeBinding(
    private val view: ComposeView,
  ) : ViewBinding {
    override fun getRoot(): View = view
  }
}

@Composable
private fun DeveloperScreen(onResetBannersClick: () -> Unit) {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
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
