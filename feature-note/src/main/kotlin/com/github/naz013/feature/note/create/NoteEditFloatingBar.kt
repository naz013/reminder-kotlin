package com.github.naz013.feature.note.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.foundation.component.CloudBubble

private const val FLOATING_BAR_ANIMATION_DURATION_MS = 300
private const val BAR_PRESSED_SCALE = 1.05f

/**
 * One entry in the floating editing bar. Deliberately generic — new tools can be added to the
 * bar (per REM-1027 Phase 2) without touching [NoteEditFloatingBar] itself: just append another
 * item to the list built by the caller.
 *
 * @param bubbleContent when non-null, tapping this item opens a [CloudBubble] anchored
 *   to it; when null, [onClick] is a direct one-shot action (e.g. opening the image picker).
 */
data class NoteEditBarItem(
  val id: String,
  val contentDescription: String,
  val selected: Boolean = false,
  val showBadge: Boolean = false,
  val onClick: () -> Unit,
  val icon: @Composable () -> Unit,
  val bubbleContent: (@Composable () -> Unit)? = null,
  val bubbleWidth: Dp = 272.dp,
)

private val BAR_ITEM_SIZE = 52.dp

@Composable
fun NoteEditFloatingBar(
  items: List<NoteEditBarItem>,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  val visibleState = remember { MutableTransitionState(false) }
  LaunchedEffect(Unit) {
    visibleState.targetState = true
  }
  var pressedCount by remember { mutableStateOf(0) }
  val barScale by animateFloatAsState(
    targetValue = if (pressedCount > 0) BAR_PRESSED_SCALE else 1f,
    animationSpec =
      spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
      ),
    label = "bar_scale",
  )
  AnimatedVisibility(
    modifier = modifier,
    visibleState = visibleState,
    enter =
      scaleIn(
        animationSpec =
          spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
          ),
        initialScale = 0f,
      ) + fadeIn(animationSpec = tween(FLOATING_BAR_ANIMATION_DURATION_MS / 2)),
  ) {
    Surface(
      modifier = Modifier.scale(barScale),
      shape = RoundedCornerShape(percent = 50),
      color = containerColor,
      shadowElevation = 4.dp,
      tonalElevation = 4.dp,
    ) {
      Row(
        modifier =
          Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        items.forEach { item ->
          NoteEditBarIconSlot(
            item = item,
            containerColor = containerColor,
            contentColor = contentColor,
            onPressedChange = { pressed -> pressedCount += if (pressed) 1 else -1 },
          )
        }
      }
    }
  }
}

@Composable
private fun NoteEditBarIconSlot(
  item: NoteEditBarItem,
  containerColor: Color,
  contentColor: Color,
  onPressedChange: (Boolean) -> Unit,
) {
  val interactionSource = remember { MutableInteractionSource() }
  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      when (interaction) {
        is PressInteraction.Press -> onPressedChange(true)
        is PressInteraction.Release, is PressInteraction.Cancel -> onPressedChange(false)
        else -> Unit
      }
    }
  }

  Box(
    modifier = Modifier.size(BAR_ITEM_SIZE),
    contentAlignment = Alignment.Center,
  ) {
    IconButton(
      onClick = item.onClick,
      interactionSource = interactionSource,
      modifier = Modifier.fillMaxWidth().height(BAR_ITEM_SIZE),
    ) {
      item.icon()
    }
    if (item.showBadge) {
      Box(
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .padding(top = 8.dp, end = 8.dp)
            .size(6.dp)
            .clip(CircleShape)
            .background(contentColor),
      )
    }
    if (item.selected) {
      item.bubbleContent?.let { bubble ->
        CloudBubble(
          onDismissRequest = item.onClick,
          containerColor = containerColor,
          contentColor = contentColor,
          modifier = Modifier.width(item.bubbleWidth),
          content = bubble,
        )
      }
    }
  }
}
