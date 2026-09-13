package com.github.naz013.feature.settings.other.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground
import com.github.naz013.ui.common.compose.foundation.component.GradientHeroCard
import com.github.naz013.ui.common.compose.foundation.component.GradientScreenHeader

@Composable
internal fun AboutScreen(
  state: AboutState,
  onBackClick: () -> Unit,
  onPrivacyPolicyClick: () -> Unit,
  onTermsClick: () -> Unit,
  onOssClick: () -> Unit,
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
        Spacer(modifier = Modifier.height(8.dp))
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = state.appName,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = state.versionInfo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
        GradientHeroCard {
          Text(
            text = state.description,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Spacer(modifier = Modifier.height(32.dp))
        SectionTitle(text = stringResource(R.string.about_legal_section))
        Spacer(modifier = Modifier.height(16.dp))
        GradientHeroCard {
          LinkRow(
            icon = AppIcons.Fluent.TabTrackingPrevention,
            title = stringResource(R.string.privacy_policy),
            onClick = onPrivacyPolicyClick,
          )
          HorizontalDivider()
          LinkRow(
            icon = AppIcons.Fluent.DocumentOnePage,
            title = stringResource(R.string.terms_and_conditions),
            onClick = onTermsClick,
          )
          HorizontalDivider()
          LinkRow(
            icon = AppIcons.Fluent.Code,
            title = stringResource(R.string.open_source_licenses),
            onClick = onOssClick,
          )
        }
        if (state.translators.isNotEmpty()) {
          Spacer(modifier = Modifier.height(32.dp))
          SectionTitle(text = stringResource(R.string.about_credits_section))
          Spacer(modifier = Modifier.height(16.dp))
          GradientHeroCard {
            Text(
              text = state.translators,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
          text = state.copyright,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.tertiary,
  )
}

@Composable
private fun LinkRow(icon: Painter, title: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Icon(
      painter = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
    )
    Icon(
      painter = AppIcons.Fluent.ChevronRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
  AppTheme(darkTheme = false) {
    AboutScreen(
      state = AboutState(
        appName = "Reminder",
        versionInfo = "Version 5.0.0 (500)",
        description = "A comprehensive task manager that keeps reminders, notes, birthdays, and " +
          "Google Tasks organized, with automatic cloud backup.",
        translators = "Chinese: Waye Wrong",
        copyright = "© 2026 Reminder. All rights reserved.",
      ),
      onBackClick = {},
      onPrivacyPolicyClick = {},
      onTermsClick = {},
      onOssClick = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview_DarkTheme() {
  AppTheme(darkTheme = true) {
    AboutScreen(
      state = AboutState(
        appName = "Reminder PRO",
        versionInfo = "Version 5.0.0 (500)",
        description = "A comprehensive task manager that keeps reminders, notes, birthdays, and " +
          "Google Tasks organized, with automatic cloud backup.",
        translators = "Chinese: Waye Wrong",
        copyright = "© 2026 Reminder PRO. All rights reserved.",
      ),
      onBackClick = {},
      onPrivacyPolicyClick = {},
      onTermsClick = {},
      onOssClick = {},
    )
  }
}
