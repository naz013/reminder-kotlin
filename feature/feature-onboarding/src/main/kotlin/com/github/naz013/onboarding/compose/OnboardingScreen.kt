package com.github.naz013.onboarding.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.onboarding.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.AnimatedGradientBackground

private const val ONBOARDING_PAGE_COUNT = 3
private const val WELCOME_PAGE = 0
private const val CAPABILITIES_PAGE = 1
private const val GET_STARTED_PAGE = 2

@Composable
fun OnboardingScreen(
  onSkip: () -> Unit,
  onCreateFirstReminder: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })

  AnimatedGradientBackground(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier =
          Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (pagerState.currentPage != GET_STARTED_PAGE) {
          TextButton(onClick = onSkip) {
            Text(stringResource(R.string.onboarding_skip))
          }
        }
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.weight(1f).fillMaxWidth(),
      ) { page ->
        when (page) {
          WELCOME_PAGE -> OnboardingWelcomePage()
          CAPABILITIES_PAGE -> OnboardingCapabilitiesPage()
          else -> OnboardingGetStartedPage(
            onCreateFirstReminder = onCreateFirstReminder,
            onMaybeLater = onSkip,
          )
        }
      }

      OnboardingPageIndicator(
        pageCount = ONBOARDING_PAGE_COUNT,
        currentPage = pagerState.currentPage,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
      )

      Spacer(modifier = Modifier.navigationBarsPadding())
    }
  }
}

@Composable
private fun OnboardingWelcomePage(modifier: Modifier = Modifier) {
  OnboardingPageContent(
    icon = AppIcons.Fluent.ClockAlarm,
    title = stringResource(R.string.onboarding_welcome_title),
    subtitle = stringResource(R.string.onboarding_welcome_subtitle),
    modifier = modifier,
  )
}

@Composable
private fun OnboardingCapabilitiesPage(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(R.string.onboarding_capabilities_title),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = stringResource(R.string.onboarding_capabilities_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(32.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
      OnboardingCapabilityCaption(
        icon = AppIcons.Fluent.Note,
        caption = stringResource(R.string.onboarding_notes_caption),
      )
      OnboardingCapabilityCaption(
        icon = AppIcons.Fluent.FoodCake,
        caption = stringResource(R.string.onboarding_birthdays_caption),
      )
      OnboardingCapabilityCaption(
        icon = AppIcons.Fluent.Calendar,
        caption = stringResource(R.string.onboarding_calendar_caption),
      )
    }
  }
}

@Composable
private fun OnboardingCapabilityCaption(
  icon: Painter,
  caption: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.width(88.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Surface(
      shape = CircleShape,
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    ) {
      Icon(
        painter = icon,
        contentDescription = null,
        modifier = Modifier.padding(14.dp).size(24.dp),
        tint = MaterialTheme.colorScheme.primary,
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = caption,
      style = MaterialTheme.typography.labelMedium,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun OnboardingGetStartedPage(
  onCreateFirstReminder: () -> Unit,
  onMaybeLater: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = AppIcons.Fluent.CheckmarkCircle,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.onboarding_get_started_title),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = stringResource(R.string.onboarding_get_started_subtitle),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(32.dp))
    Button(
      onClick = onCreateFirstReminder,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(stringResource(R.string.onboarding_create_first_reminder))
    }
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onMaybeLater) {
      Text(stringResource(R.string.onboarding_maybe_later))
    }
  }
}

@Composable
private fun OnboardingPageContent(
  icon: Painter,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = icon,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun OnboardingPageIndicator(
  pageCount: Int,
  currentPage: Int,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.Center,
  ) {
    repeat(pageCount) { page ->
      val isSelected = page == currentPage
      val color =
        if (isSelected) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        }
      Box(
        modifier =
          Modifier
            .padding(horizontal = 4.dp)
            .size(if (isSelected) 10.dp else 8.dp)
            .clip(CircleShape)
            .background(color),
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
  AppTheme {
    OnboardingScreen(
      onSkip = {},
      onCreateFirstReminder = {},
    )
  }
}
