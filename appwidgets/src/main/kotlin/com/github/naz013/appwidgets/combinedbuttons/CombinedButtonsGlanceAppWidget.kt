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
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.github.naz013.analytics.Widget
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.roundedBackground

internal class CombinedButtonsGlanceAppWidget : GlanceAppWidget() {

  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
    val backgroundColorCode = CombinedWidgetPrefsProvider(context, widgetId).getWidgetBackground()
    val contrastColor = ColorProvider(
      day = WidgetUtils.getContrastColor(backgroundColorCode),
      night = WidgetUtils.getContrastColor(backgroundColorCode)
    )
    val viewIntent = Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    provideContent {
      GlanceAppWidgetTheme {
        CombinedButtonsContent(
          backgroundColorCode = backgroundColorCode,
          contrastColor = contrastColor,
          viewIntent = viewIntent
        )
      }
    }
  }

  @Composable
  private fun CombinedButtonsContent(
    backgroundColorCode: Int,
    contrastColor: ColorProvider,
    viewIntent: Intent
  ) {
    Row(
      modifier = GlanceModifier.fillMaxSize()
        .roundedBackground(backgroundColorCode),
      horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      ActionButton(
        iconRes = R.drawable.ic_fluent_clock_alarm,
        contrastColor = contrastColor,
        direction = Direction.ADD_REMINDER,
        viewIntent = viewIntent
      )
      ActionButton(
        iconRes = R.drawable.ic_fluent_note,
        contrastColor = contrastColor,
        direction = Direction.ADD_NOTE,
        viewIntent = viewIntent
      )
      ActionButton(
        iconRes = R.drawable.ic_fluent_food_cake,
        contrastColor = contrastColor,
        direction = Direction.ADD_BIRTHDAY,
        viewIntent = viewIntent
      )
    }
  }

  @Composable
  private fun RowScope.ActionButton(
    iconRes: Int,
    contrastColor: ColorProvider,
    direction: Direction,
    viewIntent: Intent
  ) {
    Image(
      modifier = GlanceModifier
        .defaultWeight()
        .padding(12.dp)
        .clickable(
          onClick = actionStartActivity(
            intent = viewIntent,
            parameters = actionParametersOf(
              directionKey to direction,
              widgetTypeKey to Widget.COMBINED
            )
          )
        ),
      provider = ImageProvider(iconRes),
      contentDescription = null,
      colorFilter = ColorFilter.tint(contrastColor)
    )
  }
}
