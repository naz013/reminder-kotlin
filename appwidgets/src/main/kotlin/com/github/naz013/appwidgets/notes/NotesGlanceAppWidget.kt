package com.github.naz013.appwidgets.notes

import android.appwidget.AppWidgetManager
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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
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
import com.github.naz013.appwidgets.compose.GlanceAppWidgetTheme
import com.github.naz013.appwidgets.compose.paletteContrastColor
import com.github.naz013.appwidgets.compose.roundedBackground
import com.github.naz013.appwidgets.compose.systemWidgetShape
import com.github.naz013.appwidgets.notes.data.NotesAppWidgetState
import com.github.naz013.appwidgets.notes.data.UiNoteWidgetItem
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.logging.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.File

internal class NotesGlanceAppWidget : GlanceAppWidget(), KoinComponent {

  private val widgetIdKey = ActionParameters.Key<Int>(
    AppWidgetManager.EXTRA_APPWIDGET_ID
  )
  private val directionKey = ActionParameters.Key<Direction>(
    AppWidgetActionActivity.DIRECTION
  )
  private val dataKey = ActionParameters.Key<WidgetIntentProtocol>(
    AppWidgetActionActivity.DATA
  )
  private val widgetTypeKey = ActionParameters.Key<Widget>(
    AppWidgetActionActivity.WIDGET_TYPE
  )

  override val stateDefinition: GlanceStateDefinition<NotesAppWidgetState>
    get() = object : GlanceStateDefinition<NotesAppWidgetState> {
      override suspend fun getDataStore(
        context: Context,
        fileKey: String
      ): DataStore<NotesAppWidgetState> {
        Logger.d(TAG, "Get data store $fileKey")
        val widgetId = GlanceAppWidgetIdExtractor.extract(fileKey)
        return NotesAppWidgetStateDataStore(
          notesAppWidgetViewModel = get<NotesAppWidgetViewModel> {
            parametersOf(NotesWidgetPrefsProvider(context, widgetId))
          }
        )
      }

      override fun getLocation(context: Context, fileKey: String): File {
        Logger.d(TAG, "Get location $fileKey")
        return context.dataStoreFile(fileKey)
      }
    }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val configIntent = Intent(context, NotesWidgetConfigActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val titleText = context.getString(R.string.notes)
    val emptyStateText = context.getString(R.string.no_notes)
    provideContent {
      GlanceAppWidgetTheme {
        NotesContent(
          context = context,
          state = currentState(),
          configIntent = configIntent,
          titleText = titleText,
          emptyStateText = emptyStateText
        )
      }
    }
  }

  override suspend fun providePreview(context: Context, widgetCategory: Int) {
    val configIntent = Intent(context, NotesWidgetConfigActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    val titleText = context.getString(R.string.notes)
    val emptyStateText = context.getString(R.string.no_notes)
    val previewState = get<NotesAppWidgetViewModel> {
      parametersOf(NotesWidgetPrefsProvider(context, WidgetId.PREVIEW_ID))
    }.getState()
    provideContent {
      GlanceAppWidgetTheme {
        NotesContent(
          context = context,
          state = previewState,
          configIntent = configIntent,
          titleText = titleText,
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
  private fun NotesContent(
    modifier: GlanceModifier = GlanceModifier,
    context: Context,
    state: NotesAppWidgetState,
    configIntent: Intent,
    titleText: String,
    emptyStateText: String
  ) {
    val headerContrastColorProvider = paletteContrastColor(
      state.headerBackgroundColor,
      state.headerContrastColor
    )
    Column(
      modifier = modifier.fillMaxSize().systemWidgetShape()
    ) {
      Row(
        modifier = GlanceModifier.fillMaxWidth()
          .height(56.dp)
          .roundedBackground(state.headerBackgroundColor),
        verticalAlignment = Alignment.Vertical.CenterVertically
      ) {
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
          text = titleText,
          modifier = GlanceModifier.fillMaxWidth()
            .defaultWeight(),
          style = TextStyle(
            fontSize = 18.sp,
            color = headerContrastColorProvider
          ),
          maxLines = 1
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Image(
          modifier = GlanceModifier
            .size(44.dp)
            .padding(8.dp)
            .cornerRadius(16.dp)
            .clickable(
              onClick = actionStartActivity(
                intent = configIntent,
                parameters = actionParametersOf(widgetIdKey to state.widgetId)
              )
            ),
          provider = ImageProvider(R.drawable.ic_fluent_settings),
          contentDescription = null,
          colorFilter = ColorFilter.tint(headerContrastColorProvider)
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Image(
          modifier = GlanceModifier
            .size(44.dp)
            .padding(8.dp)
            .cornerRadius(16.dp)
            .clickable(
              onClick = actionStartActivity(
                intent = viewIntent(context),
                parameters = actionParametersOf(
                  directionKey to Direction.ADD_NOTE,
                  widgetTypeKey to Widget.NOTES
                )
              )
            ),
          provider = ImageProvider(R.drawable.ic_fluent_add),
          contentDescription = null,
          colorFilter = ColorFilter.tint(headerContrastColorProvider)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
      }
      Spacer(modifier = GlanceModifier.height(4.dp))
      if (state.items.isEmpty()) {
        Text(
          text = emptyStateText,
          modifier = GlanceModifier.fillMaxWidth().padding(16.dp)
        )
      } else {
        LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
          items(state.items.size) { index: Int ->
            NoteItem(
              context = context,
              data = state.items[index]
            )
          }
        }
      }
    }
  }

  @Composable
  private fun NoteItem(
    context: Context,
    data: UiNoteWidgetItem
  ) {
    val contentColorProvider = ColorProvider(
      day = data.contentColor,
      night = data.contentColor
    )
    Column(
      modifier = GlanceModifier.fillMaxWidth()
        .padding(8.dp)
        .cornerRadius(8.dp)
        .background(data.backgroundColor)
        .clickable(
          onClick = actionStartActivity(
            intent = viewIntent(context),
            parameters = actionParametersOf(
              directionKey to Direction.NOTE_PREVIEW,
              dataKey to WidgetIntentProtocol(
                mapOf<String, Any?>(Pair(IntentKeys.INTENT_ID, data.uuId))
              ),
              widgetTypeKey to Widget.NOTES
            )
          )
        )
    ) {
      Text(
        text = data.text,
        modifier = GlanceModifier.fillMaxWidth(),
        style = TextStyle(
          fontSize = data.textSize,
          color = contentColorProvider
        ),
        maxLines = 5
      )
      if (data.image != null) {
        Spacer(modifier = GlanceModifier.height(4.dp))
        Image(
          modifier = GlanceModifier.fillMaxWidth().height(180.dp),
          provider = ImageProvider(data.image),
          contentDescription = null,
          contentScale = ContentScale.Fit
        )
      }
    }
  }

  companion object {
    private const val TAG = "NotesGlanceAppWidget"
  }
}
