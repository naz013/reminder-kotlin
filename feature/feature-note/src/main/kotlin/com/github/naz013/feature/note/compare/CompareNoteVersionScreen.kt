package com.github.naz013.feature.note.compare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.note.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.deviceScreenConfiguration

private const val MARKER_ADDED = "+"
private const val MARKER_REMOVED = "-"
private const val MARKER_SAME = " "

/** Shows the selected history version next to the note's current text; layout follows the window size class. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CompareNoteVersionScreen(
  modifier: Modifier = Modifier,
  state: CompareNoteVersionState,
  onBackClick: () -> Unit,
  onRestoreClick: () -> Unit,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.surface)
      .navigationBarsPadding(),
  ) {
    TopAppBar(
      title = { Text(text = stringResource(R.string.note_version_compare_title)) },
      navigationIcon = {
        MenuIconButton(
          icon = AppIcons.Builder.ArrowLeft,
          contentDescription = stringResource(R.string.cd_back),
          onClick = onBackClick,
        )
      },
      actions = {
        if (state.status == CompareStatus.Loaded) {
          TextButton(onClick = onRestoreClick) {
            Text(text = stringResource(R.string.note_version_restore))
          }
        }
      },
      modifier = Modifier.statusBarsPadding(),
    )
    when (state.status) {
      CompareStatus.Loading -> CenteredContent {
        CircularProgressIndicator()
      }

      CompareStatus.Unavailable -> CenteredContent {
        Text(
          text = stringResource(R.string.note_version_compare_unavailable),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(16.dp),
        )
      }

      CompareStatus.Loaded -> ComparePanes(state = state)
    }
  }
}

@Composable
private fun CenteredContent(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    content()
  }
}

@Composable
private fun ComparePanes(
  modifier: Modifier = Modifier,
  state: CompareNoteVersionState,
) {
  Column(modifier = modifier.fillMaxSize()) {
    if (state.isIdentical) {
      Text(
        text = stringResource(R.string.note_version_identical),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.secondaryContainer)
          .padding(horizontal = 16.dp, vertical = 8.dp),
      )
    }
    val historyPane: @Composable (Modifier) -> Unit = { paneModifier ->
      ComparePane(
        modifier = paneModifier,
        label = state.historyLabel,
        lines = state.historyLines,
      )
    }
    val currentPane: @Composable (Modifier) -> Unit = { paneModifier ->
      ComparePane(
        modifier = paneModifier,
        label = stringResource(R.string.note_version_current),
        lines = state.currentLines,
      )
    }
    when (compareLayoutFor(deviceScreenConfiguration())) {
      CompareLayout.Stacked -> Column(modifier = Modifier.fillMaxSize()) {
        historyPane(Modifier.weight(1f).fillMaxWidth())
        currentPane(Modifier.weight(1f).fillMaxWidth())
      }

      CompareLayout.SideBySide -> Row(modifier = Modifier.fillMaxSize()) {
        historyPane(Modifier.weight(1f).fillMaxSize())
        currentPane(Modifier.weight(1f).fillMaxSize())
      }
    }
  }
}

@Composable
private fun ComparePane(
  modifier: Modifier = Modifier,
  label: String,
  lines: List<DiffLine>,
) {
  Surface(
    modifier = modifier.padding(4.dp),
    tonalElevation = 1.dp,
    shape = MaterialTheme.shapes.medium,
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
      )
      LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(lines) { _, line -> DiffLineRow(line = line) }
      }
    }
  }
}

@Composable
private fun DiffLineRow(
  modifier: Modifier = Modifier,
  line: DiffLine,
) {
  val (background, content) = when (line.change) {
    LineChange.Same -> Color.Transparent to MaterialTheme.colorScheme.onSurface
    LineChange.Removed -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    LineChange.Added -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
  }
  val marker = when (line.change) {
    LineChange.Same -> MARKER_SAME
    LineChange.Removed -> MARKER_REMOVED
    LineChange.Added -> MARKER_ADDED
  }
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .fillMaxWidth()
      .background(background)
      .padding(horizontal = 12.dp, vertical = 2.dp),
  ) {
    Text(
      text = marker,
      style = MaterialTheme.typography.bodyMedium,
      fontFamily = FontFamily.Monospace,
      color = content,
      modifier = Modifier.width(12.dp),
    )
    Text(
      text = line.text,
      style = MaterialTheme.typography.bodyMedium,
      color = content,
      modifier = Modifier.weight(1f),
    )
  }
}
