package com.elementary.tasks.reminder.build.valuedialog.editor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.AttachmentFile
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.AttachmentType
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.UriToAttachmentFileAdapter

private val GRID_MAX_HEIGHT = 320.dp

/**
 * Attachment file grid: a "pick files" button + a 4-column grid of the picked files (image
 * thumbnail for images, an icon + name otherwise), each removable. Replaces
 * `AttachmentsController`. Picking is delegated to [onPickFiles], which owns the
 * Fragment-registered `MultipleUriPicker` launcher.
 */
@Composable
fun AttachmentsValueEditor(
  builderItem: BuilderItem<List<String>>,
  attachmentFileAdapter: UriToAttachmentFileAdapter,
  onPickFiles: (onResult: (List<Uri>) -> Unit) -> Unit,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var files by remember(builderItem) {
    mutableStateOf(builderItem.modifier.getValue()?.map { Uri.parse(it) } ?: emptyList())
  }

  fun commit(newFiles: List<Uri>) {
    files = newFiles
    builderItem.modifier.update(newFiles.map { it.toString() }.ifEmpty { null })
    onValueChange(builderItem)
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    OutlinedButton(
      onClick = { onPickFiles { picked -> commit(files + picked) } },
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(stringResource(R.string.builder_pick_files))
    }

    if (files.isNotEmpty()) {
      LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = GRID_MAX_HEIGHT)
          .padding(top = 8.dp),
      ) {
        itemsIndexed(files, key = { _, uri -> uri.toString() }) { index, uri ->
          val attachmentFile = remember(uri) { attachmentFileAdapter(uri) }
          AttachmentCell(
            attachmentFile = attachmentFile,
            onRemove = { commit(files.toMutableList().apply { removeAt(index) }) },
          )
        }
      }
    }
  }
}

@Composable
private fun AttachmentCell(attachmentFile: AttachmentFile, onRemove: () -> Unit) {
  Box(
    modifier = Modifier
      .padding(4.dp)
      .aspectRatio(1f),
  ) {
    if (attachmentFile.type == AttachmentType.IMAGE) {
      AsyncImage(
        model = attachmentFile.uri,
        contentDescription = attachmentFile.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(8.dp)),
      )
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
          .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Icon(
          painter = painterResource(attachmentFile.icon),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(32.dp),
        )
        Text(
          text = attachmentFile.name,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
    }
    IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(24.dp)) {
      Icon(
        imageVector = Icons.Filled.Close,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}
