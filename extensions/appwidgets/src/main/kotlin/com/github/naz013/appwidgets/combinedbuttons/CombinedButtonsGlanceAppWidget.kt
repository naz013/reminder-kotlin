package com.github.naz013.appwidgets.combinedbuttons

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import com.github.naz013.analytics.Widget
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.compose.ComposeResourceProvider
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.roundedBackground
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.logging.Logger

internal class CombinedButtonsGlanceAppWidget : GlanceAppWidget() {

  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )
  private val composeResourceProvider: (Context) -> ComposeResourceProvider = {
    ComposeResourceProvider(it)
  }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
    Logger.d(TAG, "Refreshing the widget with the ID = $widgetId")
    val backgroundColorCode = CombinedWidgetPrefsProvider(context, widgetId).getWidgetBackground()

    provideContent {
      GlanceAppWidgetTheme {
        CombinedButtonsContent(
          context = context,
          colorGroup = composeResourceProvider(context).getColors(backgroundColorCode),
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    provideContent {
      GlanceAppWidgetTheme {
        CombinedButtonsContent(
          context = null,
          colorGroup = composeResourceProvider(context).getColors(3),
        )
      }
    }
  }

  @Composable
  private fun CombinedButtonsContent(
    context: Context?,
    colorGroup: ComposeResourceProvider.ColorGroup,
  ) {
    Row(
      modifier = GlanceModifier.fillMaxSize()
        .roundedBackground(colorGroup.background)
        .systemWidgetShape(),
      horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      ActionButton(
        context = context,
        iconRes = R.drawable.ic_fluent_clock_alarm,
        contrastColor = colorGroup.foreground,
        direction = Direction.ADD_REMINDER
      )
      ActionButton(
        context = context,
        iconRes = R.drawable.ic_fluent_note,
        contrastColor = colorGroup.foreground,
        direction = Direction.ADD_NOTE
      )
      ActionButton(
        context = context,
        iconRes = R.drawable.ic_fluent_food_cake,
        contrastColor = colorGroup.foreground,
        direction = Direction.ADD_BIRTHDAY
      )
    }
  }

  @Composable
  private fun RowScope.ActionButton(
    context: Context?,
    iconRes: Int,
    contrastColor: ColorProvider,
    direction: Direction
  ) {
    val onClick: Action = if (context != null) {
      val buttonIntent = Intent(context, AppWidgetActionActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      }
      actionStartActivity(
        intent = buttonIntent,
        parameters = actionParametersOf(
          directionKey to direction,
          widgetTypeKey to Widget.COMBINED
        )
      )
    } else {
      object : Action {}
    }
    Image(
      modifier = GlanceModifier
        .defaultWeight()
        .padding(12.dp)
        .clickable(onClick = onClick),
      provider = ImageProvider(iconRes),
      contentDescription = null,
      colorFilter = ColorFilter.tint(contrastColor)
    )
  }

  companion object {
    private const val TAG = "CombinedButtonsGlanceAppWidget"
  }
}
