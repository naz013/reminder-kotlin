package com.elementary.tasks.settings.other

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhatsNewFragment : BaseComposeToolbarFragment() {
  private val viewModel by viewModel<WhatsNewViewModel>()

  @Composable
  override fun Content() {
    WhatsNewScreen(
      versionAndDate =
        stringResource(
          R.string.whats_new_version_and_date,
          viewModel.versionName,
          viewModel.buildDate,
        ),
      whatsNewText = stringResource(R.string.whats_new_text),
    )
  }

  override fun getTitle(): String = getString(R.string.whats_new)
}

@Composable
private fun WhatsNewScreen(
  versionAndDate: String,
  whatsNewText: String,
) {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
  ) {
    Spacer(
      modifier =
        Modifier.height(
          dimensionResource(com.github.naz013.ui.common.R.dimen.collapse_toolbar_margin_top),
        ),
    )
    Text(
      text = versionAndDate,
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.tertiary,
    )
    Text(
      text = whatsNewText,
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(top = 16.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun WhatsNewScreenPreview() {
  WhatsNewScreen(versionAndDate = "1.0.0, Jan 1", whatsNewText = "Sample changelog text")
}
