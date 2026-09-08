package com.github.naz013.feature.settings.other.whatsnew

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.foundation.component.GradientHeroCard
import com.github.naz013.ui.common.compose.foundation.component.GradientScreenHeader

@Composable
internal fun WhatsNewScreen(
  versionAndDate: String,
  whatsNewText: String,
  onBackClick: () -> Unit,
) {
  AnimatedGradientBackground {
    Column(modifier = Modifier.fillMaxSize()) {
      GradientScreenHeader(
        onBackClick = onBackClick,
        contentDescription = stringResource(R.string.cd_back),
      )
      Column(
        modifier = Modifier
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
        GradientHeroCard {
          Text(
            text = whatsNewText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
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
    onBackClick = {},
  )
}
