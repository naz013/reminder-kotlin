package com.github.naz013.appwidgets.habit

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
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
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
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
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
import com.github.naz013.ui.common.icon.DrawableCatalog
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File

internal class HabitGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val dataKey = ActionParameters.Key<WidgetIntentProtocol>(
    AppWidgetActionActivity.DATA
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )

  override val stateDefinition: GlanceStateDefinition<HabitAppWidgetState>
    get() = object : GlanceStateDefinition<HabitAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<HabitAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        return HabitAppWidgetStateDataStore(
          habitAppWidgetViewModel = get<HabitAppWidgetViewModel> {
            parametersOf(HabitWidgetPrefsProvider(context, widgetId))
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val viewIntent = viewIntent(context)
    val emptyText = context.getString(R.string.no_habits)
    val unlockText = context.getString(R.string.widget_habits_unlock)
    val checkInDescription = context.getString(R.string.habit_check_in)
    provideContent {
      GlanceAppWidgetTheme {
        HabitContent(
          state = currentState(),
          viewIntent = viewIntent,
          emptyText = emptyText,
          unlockText = unlockText,
          checkInDescription = checkInDescription
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val viewIntent = viewIntent(context)
    val emptyText = context.getString(R.string.no_habits)
    val unlockText = context.getString(R.string.widget_habits_unlock)
    val checkInDescription = context.getString(R.string.habit_check_in)
    val previewState = get<HabitAppWidgetViewModel> {
      parametersOf(HabitWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        HabitContent(
          state = previewState,
          viewIntent = viewIntent,
          emptyText = emptyText,
          unlockText = unlockText,
          checkInDescription = checkInDescription
        )
      }
    }
  }

  private fun viewIntent(context: Context): Intent {
    return Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
  }

  @Composable
  private fun HabitContent(
    modifier: GlanceModifier = GlanceModifier,
    state: HabitAppWidgetState,
    viewIntent: Intent,
    emptyText: String,
    unlockText: String,
    checkInDescription: String
  ) {
    val widgetColors = ComposeResourceProvider(LocalContext.current).getColors(state.backgroundColor)
    Box(
      modifier = modifier
        .fillMaxSize()
        .roundedBackground(widgetColors.background)
        .systemWidgetShape()
        .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      when {
        !state.isUnlocked -> Text(
          text = unlockText,
          modifier = GlanceModifier.clickable(
            onClick = actionStartActivity(
              intent = viewIntent,
              parameters = actionParametersOf(
                directionKey to Direction.HOME,
                widgetTypeKey to Widget.HABITS
              )
            )
          ),
          style = TextStyle(color = widgetColors.foreground)
        )

        state.items.isEmpty() -> Text(
          text = emptyText,
          style = TextStyle(color = widgetColors.foreground)
        )

        else -> LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
          items(state.items) { item ->
            HabitItem(
              item = item,
              viewIntent = viewIntent,
              checkInDescription = checkInDescription,
              textColor = widgetColors.foreground
            )
          }
        }
      }
    }
  }

  @Composable
  private fun HabitItem(
    modifier: GlanceModifier = GlanceModifier,
    item: UiHabitWidgetItem,
    viewIntent: Intent,
    checkInDescription: String,
    textColor: ColorProvider
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)
        .clickable(
          onClick = actionStartActivity(
            intent = viewIntent,
            parameters = actionParametersOf(
              directionKey to Direction.HABIT_PREVIEW,
              dataKey to WidgetIntentProtocol(
                mapOf<String, Any?>(Pair(IntentKeys.INTENT_ID, item.id))
              ),
              widgetTypeKey to Widget.HABITS
            )
          )
        ),
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      Image(
        modifier = GlanceModifier
          .size(32.dp)
          .padding(4.dp)
          .clickable(
            onClick = actionRunCallback<HabitCheckInActionCallback>(
              actionParametersOf(HabitCheckInActionCallback.HABIT_ID_KEY to item.id)
            )
          ),
        provider = ImageProvider(
          if (item.isDone) DrawableCatalog.Fluent.CheckboxChecked else DrawableCatalog.Fluent.CheckboxUnchecked
        ),
        contentDescription = checkInDescription,
        colorFilter = ColorFilter.tint(textColor)
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Column(modifier = GlanceModifier.defaultWeight()) {
        Text(
          text = item.name,
          style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor),
          maxLines = 1
        )
      }
      Text(
        text = item.progressLabel,
        style = TextStyle(fontSize = 12.sp, color = textColor),
        maxLines = 1
      )
    }
  }

  companion object {
    private const val TAG = "HabitGlanceAppWidget"
  }
}
