package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.BuilderItem
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.Place

/**
 * Full-screen host for [MapValueEditor], used instead of [com.elementary.tasks.reminder.build.valuedialog.ValueEditorSheet]'s
 * `AppModalBottomSheet` for the Arriving/Leaving coordinates editors.
 *
 * `AppModalBottomSheet` renders its content in a separate Compose `Popup`/`Dialog` window, which is
 * not part of the fragment view tree `parentFragment.childFragmentManager` searches when resolving
 * a `FragmentContainerView` by id - embedding `SimpleMapFragment` there throws
 * `IllegalArgumentException: No view found for id ...`. Rendering this screen directly in
 * `BuildReminderFragment`'s own composition (same window) keeps the container reachable.
 */
@Composable
fun MapEditorScreen(
  builderItem: BuilderItem<Place>,
  parentFragment: Fragment,
  dateTimeManager: DateTimeManager,
  onDismissRequest: () -> Unit,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 24.dp, end = 16.dp, top = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = builderItem.title,
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismissRequest) {
          Icon(
            painter = painterResource(R.drawable.ic_builder_chevron_down),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      MapValueEditor(
        builderItem = builderItem,
        parentFragment = parentFragment,
        dateTimeManager = dateTimeManager,
        onValueChange = onValueChange,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      )
    }
  }
}
