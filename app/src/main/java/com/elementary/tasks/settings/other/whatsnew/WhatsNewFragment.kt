package com.elementary.tasks.settings.other.whatsnew

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.ComposeFragment
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.withAlpha
import org.koin.androidx.viewmodel.ext.android.viewModel

class WhatsNewFragment : ComposeFragment() {
  private val viewModel by viewModel<WhatsNewViewModel>()

  @Composable
  override fun FragmentContent() {
    val state by viewModel.state.collectAsState(WhatsNewState())
    WhatsNewScreen(
      versionAndDate = state.versionName + "\n" + state.lastUpdated,
      whatsNewText = state.whatsNewText,
      onBackClick = { moveBack() },
    )
  }

  private fun moveBack() {
    activity?.onBackPressedDispatcher?.onBackPressed()
  }
}

@Composable
private fun WhatsNewScreen(
  versionAndDate: String,
  whatsNewText: String,
  onBackClick: () -> Unit,
) {
  AnimatedGradientBackground {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier = Modifier
          .statusBarsPadding()
          .fillMaxWidth()
          .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background.withAlpha(0.25f)),
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_builder_arrow_left),
            contentDescription = stringResource(R.string.cd_back),
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
      ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = versionAndDate,
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = whatsNewText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(20.dp),
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun WhatsNewScreenPreview() {
  WhatsNewScreen(
    versionAndDate = "1.0.0, Jan 1",
    whatsNewText = "Sample changelog text",
    onBackClick = {})
}
