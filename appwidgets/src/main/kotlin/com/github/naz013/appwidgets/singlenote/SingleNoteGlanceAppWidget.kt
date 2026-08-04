package com.github.naz013.appwidgets.singlenote

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
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
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import com.github.naz013.analytics.Widget
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.GlanceAppWidgetIdExtractor
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetId
import com.github.naz013.appwidgets.WidgetIntentProtocol
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File

internal class SingleNoteGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val dataKey = ActionParameters.Key<WidgetIntentProtocol>(
    AppWidgetActionActivity.DATA
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )

  override val stateDefinition: GlanceStateDefinition<SingleNoteAppWidgetState>
    get() = object : GlanceStateDefinition<SingleNoteAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<SingleNoteAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        return SingleNoteAppWidgetStateDataStore(
          singleNoteAppWidgetViewModel = get<SingleNoteAppWidgetViewModel> {
            parametersOf(context, SingleNoteWidgetPrefsProvider(context, widgetId))
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        Logger.d(TAG, "Get location $fileKey")
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val viewIntent = Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val emptyStateText = context.getString(R.string.widget_note_note_not_selected)
    provideContent {
      GlanceAppWidgetTheme {
        SingleNoteContent(
          state = currentState(),
          viewIntent = viewIntent,
          emptyStateText = emptyStateText
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val viewIntent = Intent(context, AppWidgetActionActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val emptyStateText = context.getString(R.string.widget_note_note_not_selected)
    val previewState = get<SingleNoteAppWidgetViewModel> {
      parametersOf(context, SingleNoteWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        SingleNoteContent(
          state = previewState,
          viewIntent = viewIntent,
          emptyStateText = emptyStateText
        )
      }
    }
  }

  @Composable
  private fun SingleNoteContent(
    modifier: GlanceModifier = GlanceModifier,
    state: SingleNoteAppWidgetState,
    viewIntent: Intent,
    emptyStateText: String
  ) {
    Box(
      modifier = modifier.fillMaxSize().systemWidgetShape().padding(8.dp),
      contentAlignment = Alignment.Center
    ) {
      val bitmap = state.bitmap
      val noteId = state.noteId
      if (bitmap != null && noteId != null) {
        Image(
          modifier = GlanceModifier.fillMaxSize()
            .clickable(
              onClick = actionStartActivity(
                intent = viewIntent,
                parameters = actionParametersOf(
                  directionKey to Direction.NOTE_PREVIEW,
                  dataKey to WidgetIntentProtocol(
                    mapOf<String, Any?>(Pair(IntentKeys.INTENT_ID, noteId))
                  ),
                  widgetTypeKey to Widget.SINGLE_NOTE
                )
              )
            ),
          provider = ImageProvider(bitmap),
          contentDescription = null,
          contentScale = ContentScale.Fit
        )
      } else {
        Text(text = emptyStateText)
      }
    }
  }

  companion object {
    private const val TAG = "SingleNoteGlanceAppWidget"
  }
}
