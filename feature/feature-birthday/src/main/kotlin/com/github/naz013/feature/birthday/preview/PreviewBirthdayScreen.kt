package com.github.naz013.feature.birthday.preview

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.naz013.feature.birthday.R as FeatureBirthdayR
import com.github.naz013.ui.common.R
import com.github.naz013.ui.birthday.UiBirthdayPreview
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.tag.TagChipRow
import com.github.naz013.ui.tag.TagChipState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/** Matches the stagger pattern already used for list rows in `ChronologicalHomeScreen`. */
private const val DETAIL_ROW_ANIMATION_DURATION_MS = 250
private const val DETAIL_ROW_STAGGER_DELAY_MS = 30L
private const val DETAIL_ROW_MAX_STAGGER_DELAY_MS = 180L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewBirthdayScreen(
  modifier: Modifier = Modifier,
  state: PreviewBirthdayState,
  // True when shown as a two-pane detail pane rather than pushed full-screen - only changes the
  // leading icon (close vs. back), onBackClick pops the entry either way.
  renderAsDetailPane: Boolean = false,
  onBackClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismiss: () -> Unit,
  onCallClick: () -> Unit,
  onSmsClick: () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.details)) },
        navigationIcon = {
          MenuIconButton(
            icon = if (renderAsDetailPane) AppIcons.Fluent.Dismiss else AppIcons.Builder.ArrowLeft,
            contentDescription = if (renderAsDetailPane) stringResource(R.string.acc_close) else null,
            onClick = onBackClick,
          )
        },
        actions = {
          MenuIconButton(
            icon = painterResource(R.drawable.ic_fluent_edit),
            contentDescription = stringResource(R.string.edit),
            onClick = onEditClick,
          )
          MenuIconButton(
            icon = painterResource(R.drawable.ic_fluent_delete),
            contentDescription = stringResource(R.string.delete),
            onClick = onDeleteClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    val birthday = state.birthday
    if (birthday == null) {
      Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
      return@Scaffold
    }

    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(padding),
    ) {
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
      ) {
        BirthdayDetails(birthday = birthday, tags = state.tags)

        if (birthday.hasBirthdayToday && birthday.number != null) {
          Row(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedButton(onClick = onCallClick, modifier = Modifier.weight(1f)) {
              Text(stringResource(R.string.make_call))
            }
            OutlinedButton(onClick = onSmsClick, modifier = Modifier.weight(1f)) {
              Text(stringResource(R.string.send_sms))
            }
          }
        }

        adsContent()
      }

      if (birthday.hasBirthdayToday && state.playConfetti) {
        ConfettiOverlay(modifier = Modifier.align(Alignment.Center))
      }
    }
  }

  if (state.showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = onDeleteDismiss,
      text = { Text(stringResource(R.string.are_you_sure)) },
      confirmButton = {
        TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) }
      },
      dismissButton = {
        TextButton(onClick = onDeleteDismiss) { Text(stringResource(R.string.no)) }
      },
    )
  }
}

@Composable
private fun BirthdayDetails(
  birthday: UiBirthdayPreview,
  tags: List<TagChipState>,
) {
  val photo = birthday.photo
  if (photo != null) {
    AnimatedAvatar(photo = photo)
  }
  AnimatedDetailRow(index = 0) {
    DetailRow(
      icon = R.drawable.ic_fluent_person,
      text = birthday.name,
      iconTint = MaterialTheme.colorScheme.primary,
      textStyle = MaterialTheme.typography.headlineSmall,
      textColor = MaterialTheme.colorScheme.primary,
      topPadding = if (photo != null) 16.dp else 32.dp,
    )
  }
  val number = birthday.number
  if (number != null) {
    val displayName =
      if (birthday.contactName != null) {
        "${birthday.contactName} ($number)"
      } else {
        number
      }
    AnimatedDetailRow(index = 1) {
      DetailRow(icon = R.drawable.ic_fluent_phone, text = displayName)
    }
  }
  birthday.ageFormatted?.let {
    AnimatedDetailRow(index = 2) { DetailRow(icon = R.drawable.ic_fluent_emoji_laugh, text = it) }
  }
  birthday.dateOfBirth?.let {
    AnimatedDetailRow(index = 3) { DetailRow(icon = R.drawable.ic_fluent_food_cake, text = it) }
  }
  birthday.nextBirthdayDate?.let {
    AnimatedDetailRow(index = 4) {
      DetailRow(icon = R.drawable.ic_fluent_alert, text = it, contentDescription = stringResource(R.string.estimated_next_reminder))
    }
  }
  if (tags.isNotEmpty()) {
    AnimatedDetailRow(index = 5) { TagsRow(tags = tags) }
  }
}

@Composable
private fun TagsRow(tags: List<TagChipState>) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 12.dp),
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_builder_group),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onBackground,
      modifier = Modifier.size(32.dp),
    )
    TagChipRow(tags = tags, modifier = Modifier.padding(start = 16.dp))
  }
}

/** Pop-in for the contact photo - this screen's one hero element. */
@Composable
private fun AnimatedAvatar(photo: Bitmap) {
  val visibleState = remember { MutableTransitionState(false) }
  LaunchedEffect(Unit) { visibleState.targetState = true }
  Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    AnimatedVisibility(
      visibleState = visibleState,
      enter =
        fadeIn() +
          scaleIn(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            initialScale = 0.6f,
          ),
    ) {
      Image(
        bitmap = photo.asImageBitmap(),
        contentDescription = stringResource(R.string.acc_contact_photo),
        contentScale = ContentScale.Crop,
        modifier =
          Modifier
            .padding(top = 32.dp)
            .size(72.dp)
            .clip(CircleShape),
      )
    }
  }
}

/** Staggers each detail row in on entry, matching the list-row pattern in `ChronologicalHomeScreen`. */
@Composable
private fun AnimatedDetailRow(
  index: Int,
  content: @Composable () -> Unit,
) {
  val visibleState = remember { MutableTransitionState(false) }
  LaunchedEffect(Unit) {
    delay((index * DETAIL_ROW_STAGGER_DELAY_MS).coerceAtMost(DETAIL_ROW_MAX_STAGGER_DELAY_MS).milliseconds)
    visibleState.targetState = true
  }
  AnimatedVisibility(
    visibleState = visibleState,
    enter =
      fadeIn(animationSpec = tween(DETAIL_ROW_ANIMATION_DURATION_MS)) +
        slideInVertically(animationSpec = tween(DETAIL_ROW_ANIMATION_DURATION_MS)) { fullHeight -> fullHeight / 6 },
  ) {
    content()
  }
}

@Composable
private fun DetailRow(
  icon: Int,
  text: String,
  modifier: Modifier = Modifier,
  contentDescription: String? = null,
  iconTint: Color = MaterialTheme.colorScheme.onBackground,
  textColor: Color = MaterialTheme.colorScheme.onBackground,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  topPadding: Dp = 12.dp,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = topPadding),
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = contentDescription,
      tint = iconTint,
      modifier = Modifier.size(32.dp),
    )
    Text(
      text = text,
      style = textStyle,
      color = textColor,
      modifier =
        Modifier
          .weight(1f)
          .padding(start = 16.dp),
    )
  }
}

/** Confetti auto-plays once (1s delay, ~3.5s total), matching the legacy Lottie timing. */
@Composable
private fun ConfettiOverlay(modifier: Modifier = Modifier) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(FeatureBirthdayR.raw.birthday_confetti))
  var isPlaying by remember { mutableStateOf(false) }
  var isVisible by remember { mutableStateOf(true) }
  LaunchedEffect(Unit) {
    delay(1000L)
    isPlaying = true
    delay(2500L)
    isVisible = false
  }
  if (isVisible) {
    LottieAnimation(
      composition = composition,
      isPlaying = isPlaying,
      speed = 0.75f,
      iterations = 1,
      modifier = modifier.size(200.dp),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBirthdayScreenPreview() {
  AppTheme {
    PreviewBirthdayScreen(
      state =
        PreviewBirthdayState(
          birthday =
            UiBirthdayPreview(
              uuId = "1",
              name = "Test User",
              number = "1234567890",
              photo = null,
              contactName = "Test User",
              ageFormatted = "25 years",
              dateOfBirth = "25 May, 2000",
              nextBirthdayDate = "25 May, 2026",
              hasBirthdayToday = false,
            ),
        ),
      onBackClick = {},
      onEditClick = {},
      onDeleteClick = {},
      onDeleteConfirmed = {},
      onDeleteDismiss = {},
      onCallClick = {},
      onSmsClick = {},
      adsContent = {},
    )
  }
}
