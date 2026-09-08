package com.github.naz013.feature.settings.proversion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.foundation.component.GradientHeroCard
import com.github.naz013.ui.common.compose.foundation.component.GradientScreenHeader

@Composable
internal fun ProVersionScreen(
  advantages: List<String>,
  onBackClick: () -> Unit,
  onBuyClick: () -> Unit,
  renderAsDetailPane: Boolean = false,
) {
  AnimatedGradientBackground {
    Column(modifier = Modifier.fillMaxSize()) {
      GradientScreenHeader(
        onBackClick = onBackClick,
        contentDescription = stringResource(if (renderAsDetailPane) R.string.acc_close else R.string.cd_back),
        icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Builder.ArrowLeft,
      )
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp),
      ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          text = stringResource(R.string.pro_advantages),
          style = MaterialTheme.typography.headlineSmallEmphasized,
          color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        GradientHeroCard(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          advantages.forEach { advantage ->
            Text(
              text = advantage,
              style = MaterialTheme.typography.titleMediumEmphasized,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = onBuyClick,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(text = stringResource(R.string.pro_buy))
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ProVersionScreenPreview() {
  AppTheme(darkTheme = false) {
    ProVersionScreen(
      advantages = listOf("- No Advertisement", "- LED notification"),
      onBackClick = {},
      onBuyClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun ProVersionScreenPreview_DarkTheme() {
  AppTheme(darkTheme = true) {
    ProVersionScreen(
      advantages = listOf("- No Advertisement", "- LED notification"),
      onBackClick = {},
      onBuyClick = {},
    )
  }
}
