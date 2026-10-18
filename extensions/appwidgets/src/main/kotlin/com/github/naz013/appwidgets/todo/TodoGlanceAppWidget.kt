package com.github.naz013.appwidgets.todo

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
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
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
import androidx.glance.text.TextStyle
import com.github.naz013.analytics.Widget
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.GlanceAppWidgetIdExtractor
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetId
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.appwidgets.events.data.UiShopListWidget
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File

internal class TodoGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val dataKey = ActionParameters.Key<WidgetIntentProtocol>(
    AppWidgetActionActivity.DATA
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )

  override val stateDefinition: GlanceStateDefinition<TodoAppWidgetState>
    get() = object : GlanceStateDefinition<TodoAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<TodoAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        return TodoAppWidgetStateDataStore(
          todoAppWidgetViewModel = get<TodoAppWidgetViewModel> {
            parametersOf(TodoWidgetPrefsProvider(context, widgetId))
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val viewIntent = viewIntent(context)
    val emptyStateText = context.getString(R.string.widget_todo_not_available)
    provideContent {
      GlanceAppWidgetTheme {
        TodoContent(
          state = currentState(),
          viewIntent = viewIntent,
          emptyStateText = emptyStateText
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val viewIntent = viewIntent(context)
    val emptyStateText = context.getString(R.string.widget_todo_not_available)
    val previewState = get<TodoAppWidgetViewModel> {
      parametersOf(TodoWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        TodoContent(
          state = previewState,
          viewIntent = viewIntent,
          emptyStateText = emptyStateText
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
  private fun TodoContent(
    modifier: GlanceModifier = GlanceModifier,
    state: TodoAppWidgetState,
    viewIntent: Intent,
    emptyStateText: String
  ) {
    val reminderId = state.reminderId
    Box(
      modifier = modifier
        .fillMaxSize()
        .systemWidgetShape()
        .background(GlanceTheme.colors.widgetBackground)
        .padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      if (reminderId == null) {
        Text(
          text = emptyStateText,
          style = TextStyle(color = GlanceTheme.colors.onSurface)
        )
      } else {
        Column(modifier = GlanceModifier.fillMaxSize()) {
          Text(
            text = state.title,
            modifier = GlanceModifier
              .fillMaxWidth()
              .padding(bottom = 8.dp)
              .clickable(
                onClick = actionStartActivity(
                  intent = viewIntent,
                  parameters = actionParametersOf(
                    directionKey to Direction.REMINDER_PREVIEW,
                    dataKey to WidgetIntentProtocol(
                      mapOf<String, Any?>(Pair(IntentKeys.INTENT_ID, reminderId))
                    ),
                    widgetTypeKey to Widget.TODO
                  )
                )
              ),
            style = TextStyle(
              fontSize = 18.sp,
              fontWeight = FontWeight.Medium,
              color = GlanceTheme.colors.onSurface
            ),
            maxLines = 2
          )
          LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            items(state.items) { item -> TodoItem(item = item) }
          }
        }
      }
    }
  }

  @Composable
  private fun TodoItem(modifier: GlanceModifier = GlanceModifier, item: UiShopListWidget) {
    Row(
      modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
      verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
      Image(
        modifier = GlanceModifier.size(24.dp),
        provider = ImageProvider(item.iconRes),
        contentDescription = null,
        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
      )
      Spacer(modifier = GlanceModifier.width(8.dp))
      Text(
        text = item.text,
        modifier = GlanceModifier.fillMaxWidth(),
        style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSurface),
        maxLines = 2
      )
    }
  }

  companion object {
    private const val TAG = "TodoGlanceAppWidget"
  }
}
