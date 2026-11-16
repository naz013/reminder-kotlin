package com.github.naz013.appwidgets.routine

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
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
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.github.naz013.analytics.Widget
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.GlanceAppWidgetIdExtractor
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetId
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.roundedBackground
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File

internal class RoutineGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val dataKey = ActionParameters.Key<WidgetIntentProtocol>(
    AppWidgetActionActivity.DATA
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )

  override val stateDefinition: GlanceStateDefinition<RoutineAppWidgetState>
    get() = object : GlanceStateDefinition<RoutineAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<RoutineAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        return RoutineAppWidgetStateDataStore(
          routineAppWidgetViewModel = get<RoutineAppWidgetViewModel> {
            parametersOf(RoutineWidgetPrefsProvider(context, widgetId))
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val texts = RoutineWidgetTexts.from(context)
    val viewIntent = viewIntent(context)
    provideContent {
      GlanceAppWidgetTheme {
        RoutineContent(state = currentState(), viewIntent = viewIntent, texts = texts)
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val texts = RoutineWidgetTexts.from(context)
    val viewIntent = viewIntent(context)
    val previewState = get<RoutineAppWidgetViewModel> {
      parametersOf(RoutineWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        RoutineContent(state = previewState, viewIntent = viewIntent, texts = texts)
      }
    }
  }

  private fun viewIntent(context: Context): Intent {
    return Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
  }

  private fun homeAction(viewIntent: Intent): Action {
    return actionStartActivity(
      intent = viewIntent,
      parameters = actionParametersOf(
        directionKey to Direction.HOME,
        widgetTypeKey to Widget.ROUTINE
      )
    )
  }

  @Composable
  private fun RoutineContent(
    modifier: GlanceModifier = GlanceModifier,
    state: RoutineAppWidgetState,
    viewIntent: Intent,
    texts: RoutineWidgetTexts
  ) {
    val item = state.item
    val background = item?.let { Color(it.backgroundColor) } ?: Color(FALLBACK_BACKGROUND)
    val foreground = item?.let { Color(it.foregroundColor) } ?: Color.White
    Box(
      modifier = modifier
        .fillMaxSize()
        .roundedBackground(background)
        .systemWidgetShape()
        .padding(12.dp),
      contentAlignment = Alignment.Center
    ) {
      when {
        !state.isUnlocked -> MessageText(texts.unavailable, foreground, homeAction(viewIntent))
        item == null -> MessageText(texts.empty, foreground, homeAction(viewIntent))
        else -> RoutineItem(item = item, viewIntent = viewIntent, texts = texts)
      }
    }
  }

  @Composable
  private fun MessageText(text: String, color: Color, action: Action) {
    Text(
      text = text,
      modifier = GlanceModifier.clickable(onClick = action),
      style = TextStyle(color = ColorProvider(color), textAlign = TextAlign.Center)
    )
  }

  @Composable
  private fun RoutineItem(
    item: UiRoutineWidgetItem,
    viewIntent: Intent,
    texts: RoutineWidgetTexts
  ) {
    val textColor = ColorProvider(Color(item.foregroundColor))
    Column(modifier = GlanceModifier.fillMaxSize()) {
      Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically
      ) {
        if (item.iconRes != null) {
          Image(
            modifier = GlanceModifier.size(28.dp),
            provider = ImageProvider(item.iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(textColor)
          )
          Spacer(modifier = GlanceModifier.width(8.dp))
        }
        Text(
          text = item.name,
          modifier = GlanceModifier.defaultWeight(),
          style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor),
          maxLines = 1
        )
      }
      Text(
        text = "${item.stepCountLabel} · ${item.durationLabel}",
        style = TextStyle(fontSize = 12.sp, color = textColor),
        maxLines = 1
      )
      Text(
        text = item.statusLabel,
        style = TextStyle(fontSize = 12.sp, color = textColor),
        maxLines = 1
      )
      Spacer(modifier = GlanceModifier.defaultWeight())
      Box(
        modifier = GlanceModifier
          .fillMaxWidth()
          .cornerRadius(16.dp)
          .background(textColor)
          .padding(vertical = 8.dp)
          .clickable(onClick = startAction(item, viewIntent)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (item.isRunning) texts.resume else texts.start,
          style = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ColorProvider(Color(item.backgroundColor))
          )
        )
      }
    }
  }

  private fun startAction(item: UiRoutineWidgetItem, viewIntent: Intent): Action {
    return actionStartActivity(
      intent = viewIntent,
      parameters = actionParametersOf(
        directionKey to Direction.ROUTINE_START,
        dataKey to WidgetIntentProtocol(
          mapOf<String, Any?>(Pair(IntentKeys.INTENT_ID, item.id))
        ),
        widgetTypeKey to Widget.ROUTINE
      )
    )
  }

  private class RoutineWidgetTexts(
    val empty: String,
    val unavailable: String,
    val start: String,
    val resume: String
  ) {
    companion object {
      fun from(context: Context) = RoutineWidgetTexts(
        empty = context.getString(R.string.no_routines),
        unavailable = context.getString(R.string.widget_routine_unavailable),
        start = context.getString(R.string.start_routine),
        resume = context.getString(R.string.widget_routine_resume)
      )
    }
  }

  companion object {
    private const val TAG = "RoutineGlanceAppWidget"
    private const val FALLBACK_BACKGROUND = 0xFF455A64
  }
}
