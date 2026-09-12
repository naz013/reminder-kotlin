package com.github.naz013.appwidgets.nextreminder

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.github.naz013.analytics.Widget
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.GlanceAppWidgetIdExtractor
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetId
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.compose.ComposeResourceProvider
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.roundedBackground
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File

internal class NextReminderGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val dataKey = ActionParameters.Key<WidgetIntentProtocol>(
    AppWidgetActionActivity.DATA
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )
  private val composeResourceProvider: (Context) -> ComposeResourceProvider = {
    ComposeResourceProvider(it)
  }

  override val stateDefinition: GlanceStateDefinition<NextReminderAppWidgetState>
    get() = object : GlanceStateDefinition<NextReminderAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<NextReminderAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        return NextReminderAppWidgetStateDataStore(
          nextReminderAppWidgetViewModel = get<NextReminderAppWidgetViewModel> {
            parametersOf(NextReminderWidgetPrefsProvider(context, widgetId))
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        Logger.d(TAG, "Get location $fileKey")
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    provideContent {
      GlanceAppWidgetTheme {
        NextReminderContent(
          context = context,
          state = currentState()
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val previewState = get<NextReminderAppWidgetViewModel> {
      parametersOf(NextReminderWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        NextReminderContent(
          context = context,
          state = previewState
        )
      }
    }
  }

  @Composable
  private fun NextReminderContent(
    modifier: GlanceModifier = GlanceModifier,
    context: Context,
    state: NextReminderAppWidgetState
  ) {
    val emptyStateText = context.getString(R.string.widget_next_reminder_empty_state)
    val widgetColors = composeResourceProvider(context).getColors(state.backgroundColor)
    val viewIntent = Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val clickAction = if (state.uuId != null) {
      actionStartActivity(
        intent = viewIntent,
        parameters = actionParametersOf(
          directionKey to Direction.REMINDER_PREVIEW,
          dataKey to WidgetIntentProtocol(mapOf<String, Any?>(Pair(IntentKeys.INTENT_ID, state.uuId))),
          widgetTypeKey to Widget.NEXT_REMINDER
        )
      )
    } else {
      actionStartActivity(
        intent = viewIntent,
        parameters = actionParametersOf(
          directionKey to Direction.ADD_REMINDER,
          widgetTypeKey to Widget.NEXT_REMINDER
        )
      )
    }
    Row(
      modifier = modifier
        .fillMaxSize()
        .roundedBackground(widgetColors.background)
        .systemWidgetShape()
        .padding(8.dp)
        .clickable(onClick = clickAction),
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      Image(
        modifier = GlanceModifier.size(28.dp),
        provider = ImageProvider(R.drawable.ic_fluent_clock_alarm),
        contentDescription = null,
        colorFilter = ColorFilter.tint(widgetColors.foreground)
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      if (state.title != null) {
        Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
          Text(
            text = state.title,
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(fontSize = 16.sp, color = widgetColors.foreground),
            maxLines = 1
          )
          if (state.dateTime != null) {
            Text(
              text = state.dateTime,
              modifier = GlanceModifier.fillMaxWidth(),
              style = TextStyle(fontSize = 13.sp, color = widgetColors.foreground),
              maxLines = 1
            )
          }
        }
      } else {
        Text(
          text = emptyStateText,
          modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
          style = TextStyle(fontSize = 16.sp, color = widgetColors.foreground),
          maxLines = 2
        )
      }
    }
  }

  companion object {
    private const val TAG = "NextReminderGlanceAppWidget"
  }
}
