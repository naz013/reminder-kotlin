package com.github.naz013.feature.settings.proversion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.withAlpha

@Composable
internal fun ProVersionScreen(
  advantages: List<String>,
  onBackClick: () -> Unit,
  onBuyClick: () -> Unit,
) {
  AnimatedGradientBackground {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier =
          Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(modifier = Modifier.width(16.dp))
        TooltipIconButton(contentDescription = stringResource(R.string.cd_back)) {
          IconButton(
            onClick = onBackClick,
            modifier =
              Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background.withAlpha(0.25f)),
          ) {
            Icon(
              painter = AppIcons.Builder.ArrowLeft,
              contentDescription = stringResource(R.string.cd_back),
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
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
          text = stringResource(R.string.pro_advantages),
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            advantages.forEach { advantage ->
              Text(
                text = advantage,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
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
