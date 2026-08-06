package com.github.naz013.tags.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.tags.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagsScreen(
  state: TagsScreenState,
  onBackClick: () -> Unit,
  onAddClick: () -> Unit,
  onTagClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.tags)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick
          )
        },
        actions = {
          MenuIconButton(
            icon = AppIcons.Fluent.Add,
            contentDescription = stringResource(R.string.new_tag),
            onClick = onAddClick,
            iconColor = MaterialTheme.colorScheme.primary,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    },
  ) { padding ->
    when (val listState = state.listState) {
      is TagsListState.Loading -> {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is TagsListState.Empty -> {
        TagsEmptyState(modifier = Modifier.fillMaxSize().padding(padding))
      }

      is TagsListState.Ready -> {
        FlowRow(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
              start = 16.dp,
              end = 16.dp,
              top = padding.calculateTopPadding() + 8.dp,
              bottom = padding.calculateBottomPadding() + 88.dp
            ),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listState.tags.forEach { tag ->
            TagListItem(tag = tag, onClick = { onTagClick(tag.id) })
          }
        }
      }
    }
  }
}

@Composable
private fun TagListItem(
  tag: TagState,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    onClick = onClick,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Canvas(modifier = Modifier.size(20.dp)) {
        drawCircle(color = tag.color)
      }
      Text(text = tag.name, style = MaterialTheme.typography.bodyLarge)
    }
  }
}

@Composable
private fun TagsEmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      painter = AppIcons.Builder.Tag,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    )
    Text(
      text = stringResource(R.string.no_tags),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp)
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun TagsScreenPreview() {
  AppTheme {
    TagsScreen(
      state = TagsScreenState(
        listState = TagsListState.Ready(
          tags = listOf(
            TagState(id = "1", name = "Work", color = Color.Red),
            TagState(id = "2", name = "Home", color = Color.Cyan)
          )
        )
      ),
      onBackClick = {},
      onAddClick = {},
      onTagClick = {}
    )
  }
}
