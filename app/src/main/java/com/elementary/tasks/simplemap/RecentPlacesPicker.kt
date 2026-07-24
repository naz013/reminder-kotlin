package com.elementary.tasks.simplemap

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.place.UiPlaceList

@Composable
internal fun RecentPlacesCard(
  places: List<UiPlaceList>,
  onPlaceSelected: (UiPlaceList) -> Unit,
  modifier: Modifier = Modifier,
) {
  PickerCard(modifier = modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(
        text = stringResource(R.string.recent_places),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
      )
      LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
        items(places, key = { it.id }) { place ->
          RecentPlaceRow(place = place, onClick = { onPlaceSelected(place) })
        }
      }
    }
  }
}

@Composable
private fun RecentPlaceRow(
  place: UiPlaceList,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp),
  ) {
    val markerBitmap = remember(place.marker) { place.marker.toBitmap().asImageBitmap() }
    Image(
      bitmap = markerBitmap,
      contentDescription = null,
      modifier = Modifier.size(32.dp),
    )
    Column(modifier = Modifier.padding(start = 12.dp)) {
      Text(text = place.name, style = MaterialTheme.typography.bodyLarge)
      place.formattedDate?.let {
        Text(text = it, style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}
