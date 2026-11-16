package com.github.naz013.appwidgets.pomodoro

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
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
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.roundedBackground
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File

internal class PomodoroGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )

  override val stateDefinition: GlanceStateDefinition<PomodoroAppWidgetState>
    get() = object : GlanceStateDefinition<PomodoroAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<PomodoroAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        return PomodoroAppWidgetStateDataStore(get<PomodoroAppWidgetViewModel>())
      }

      override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val texts = PomodoroWidgetTexts.from(context)
    val viewIntent = viewIntent(context)
    provideContent {
      GlanceAppWidgetTheme {
        PomodoroContent(state = currentState(), viewIntent = viewIntent, texts = texts)
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val texts = PomodoroWidgetTexts.from(context)
    val viewIntent = viewIntent(context)
    val previewState = get<PomodoroAppWidgetViewModel>().getState()
    provideContent {
      GlanceAppWidgetTheme {
        PomodoroContent(state = previewState, viewIntent = viewIntent, texts = texts)
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
        widgetTypeKey to Widget.POMODORO
      )
    )
  }

  private fun engineAction(action: PomodoroWidgetAction): Action {
    return actionRunCallback<PomodoroWidgetActionCallback>(
      actionParametersOf(PomodoroWidgetActionCallback.ACTION_KEY to action.name)
    )
  }

  @Composable
  private fun PomodoroContent(
    modifier: GlanceModifier = GlanceModifier,
    state: PomodoroAppWidgetState,
    viewIntent: Intent,
    texts: PomodoroWidgetTexts
  ) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .roundedBackground(BACKGROUND)
        .systemWidgetShape()
        .padding(12.dp),
      contentAlignment = Alignment.Center
    ) {
      when {
        !state.isUnlocked -> Text(
          text = texts.unavailable,
          modifier = GlanceModifier.clickable(onClick = homeAction(viewIntent)),
          style = TextStyle(color = ColorProvider(FOREGROUND), textAlign = TextAlign.Center)
        )

        state.isIdle -> IdleContent(state = state, texts = texts)
        else -> RunningContent(state = state, texts = texts)
      }
    }
  }

  @Composable
  private fun IdleContent(state: PomodoroAppWidgetState, texts: PomodoroWidgetTexts) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
      Text(
        text = texts.title,
        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorProvider(FOREGROUND)),
        maxLines = 1
      )
      Text(
        text = state.focusTodayLabel,
        style = TextStyle(fontSize = 12.sp, color = ColorProvider(FOREGROUND)),
        maxLines = 2
      )
      Spacer(modifier = GlanceModifier.defaultWeight())
      WidgetButton(
        modifier = GlanceModifier.fillMaxWidth(),
        text = texts.startFocus,
        action = engineAction(PomodoroWidgetAction.START)
      )
    }
  }

  @Composable
  private fun RunningContent(state: PomodoroAppWidgetState, texts: PomodoroWidgetTexts) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
      Text(
        text = state.phaseLabel,
        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorProvider(FOREGROUND)),
        maxLines = 1
      )
      Text(
        text = state.timeLabel,
        style = TextStyle(fontSize = 14.sp, color = ColorProvider(FOREGROUND)),
        maxLines = 1
      )
      Text(
        text = state.focusTodayLabel,
        style = TextStyle(fontSize = 12.sp, color = ColorProvider(FOREGROUND)),
        maxLines = 1
      )
      Spacer(modifier = GlanceModifier.defaultWeight())
      Row(modifier = GlanceModifier.fillMaxWidth()) {
        val toggle = if (state.isPaused) PomodoroWidgetAction.RESUME else PomodoroWidgetAction.PAUSE
        WidgetButton(
          modifier = GlanceModifier.defaultWeight(),
          text = if (state.isPaused) texts.resume else texts.pause,
          action = engineAction(toggle)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        WidgetButton(
          modifier = GlanceModifier.defaultWeight(),
          text = texts.stop,
          action = engineAction(PomodoroWidgetAction.STOP)
        )
      }
    }
  }

  @Composable
  private fun WidgetButton(modifier: GlanceModifier, text: String, action: Action) {
    Box(
      modifier = modifier
        .cornerRadius(16.dp)
        .background(ColorProvider(FOREGROUND))
        .padding(vertical = 8.dp)
        .clickable(onClick = action),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = text,
        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorProvider(BACKGROUND)),
        maxLines = 1
      )
    }
  }

  private class PomodoroWidgetTexts(
    val title: String,
    val unavailable: String,
    val startFocus: String,
    val pause: String,
    val resume: String,
    val stop: String
  ) {
    companion object {
      fun from(context: Context) = PomodoroWidgetTexts(
        title = context.getString(R.string.pomodoro_focus_timer),
        unavailable = context.getString(R.string.widget_pomodoro_unavailable),
        startFocus = context.getString(R.string.widget_pomodoro_start_focus),
        pause = context.getString(R.string.pomodoro_pause),
        resume = context.getString(R.string.pomodoro_resume),
        stop = context.getString(R.string.pomodoro_stop)
      )
    }
  }

  companion object {
    private const val TAG = "PomodoroGlanceAppWidget"
    private val BACKGROUND = Color(0xFF455A64)
    private val FOREGROUND = Color.White
  }
}
