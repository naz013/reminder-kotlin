package com.elementary.tasks.reminder.lists.filter

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.ui.common.compose.ComposeBottomSheetDialogFragment
import com.github.naz013.ui.common.compose.foundation.PrimaryIconButton
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderFilterDialog : ComposeBottomSheetDialogFragment() {

  private val viewModel by viewModel<ReminderFilterDialogViewModel>()

  @Composable
  override fun FragmentContent() {
    val filterGroups by viewModel.filterGroups.observeAsState(emptyList())

    val title = arguments?.getString(ARG_TITLE) ?: ""

    FilterDialogContent(
      title = title,
      filterGroups = filterGroups,
      onFilterToggle = { groupId, filterId ->
        viewModel.toggleFilter(groupId, filterId)
      },
      onClearAll = { viewModel.clearAllFilters() },
      onApply = {
        val result = Bundle().apply {
          val selectedFilters = viewModel.getSelectedFiltersMap()
          val appliedFilters = AppliedFilters(selectedFilters)
          putParcelable(APPLIED_FILTERS_KEY, appliedFilters)
        }
        parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
        dismiss()
      },
      onDismiss = { dismiss() }
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val filterGroups = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      arguments?.getParcelableArrayList(ARG_FILTER_GROUPS, FilterGroup::class.java)?.toList()
    } else {
      @Suppress("DEPRECATION")
      arguments?.getParcelableArrayList<FilterGroup>(ARG_FILTER_GROUPS)?.toList()
    } ?: emptyList()

    if (savedInstanceState == null) {
      viewModel.setFilterGroups(filterGroups)
    }
  }

  companion object {
    private const val ARG_FILTER_GROUPS = "filter_groups"
    private const val ARG_TITLE = "dialog_title"
    const val REQUEST_KEY = "filter_dialog_request"
    private const val APPLIED_FILTERS_KEY = "applied_filters"

    fun getAppliedFiltersFromResult(result: Bundle): AppliedFilters? {
      return result.readParcelable(APPLIED_FILTERS_KEY, AppliedFilters::class.java)
    }

    fun newInstance(
      filterGroups: List<FilterGroup>,
      title: String
    ): ReminderFilterDialog {
      return ReminderFilterDialog().apply {
        arguments = Bundle().apply {
          putParcelableArrayList(ARG_FILTER_GROUPS, ArrayList(filterGroups))
          putString(ARG_TITLE, title)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterDialogContent(
  title: String,
  filterGroups: List<FilterGroup>,
  onFilterToggle: (groupId: String, filterId: String) -> Unit,
  onClearAll: () -> Unit,
  onApply: () -> Unit,
  onDismiss: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    // Header with action buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      PrimaryIconButton(
        icon = Icons.Filled.Close,
        contentDescription = stringResource(android.R.string.cancel),
        onClick = onDismiss,
        color = MaterialTheme.colorScheme.errorContainer,
        iconColor = MaterialTheme.colorScheme.onErrorContainer
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center,
        maxLines = 2,
      )
      Spacer(modifier = Modifier.width(8.dp))
      PrimaryIconButton(
        icon = Icons.Filled.Check,
        contentDescription = stringResource(R.string.filters_apply),
        onClick = onApply,
        color = MaterialTheme.colorScheme.primaryContainer,
        iconColor = MaterialTheme.colorScheme.onPrimaryContainer
      )
    }

    HorizontalDivider(
      modifier = Modifier.padding(vertical = 8.dp),
      color = MaterialTheme.colorScheme.outlineVariant
    )

    // Filter groups
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
    ) {
      filterGroups.forEach { group ->
        FilterGroupSection(
          group = group,
          onFilterToggle = { filterId ->
            onFilterToggle(group.id, filterId)
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterGroupSection(
  group: FilterGroup,
  onFilterToggle: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
  ) {
    Text(
      text = group.title,
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(bottom = 8.dp)
    )
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      group.filters.forEach { filter ->
        FilterChip(
          selected = filter.isSelected,
          onClick = { onFilterToggle(filter.id) },
          label = {
            Text(
              text = filter.label,
              style = MaterialTheme.typography.labelLarge
            )
          }
        )
      }
    }
  }
}

// Preview functions
@Preview(showBackground = true, name = "Filter Dialog - Empty")
@Composable
private fun FilterDialogContentPreview_Empty() {
  MaterialTheme {
    FilterDialogContent(
      title = "Empty Filters",
      filterGroups = emptyList(),
      onFilterToggle = { _, _ -> },
      onClearAll = { },
      onApply = { },
      onDismiss = { }
    )
  }
}

@Preview(showBackground = true, name = "Filter Dialog - Long title")
@Composable
private fun FilterDialogContentPreview_LongTitle() {
  MaterialTheme {
    FilterDialogContent(
      title = "Very Long Filter Dialog Title to Test Text Wrapping",
      filterGroups = emptyList(),
      onFilterToggle = { _, _ -> },
      onClearAll = { },
      onApply = { },
      onDismiss = { }
    )
  }
}

@Preview(showBackground = true, name = "Filter Dialog - With Data")
@Composable
private fun FilterDialogContentPreview_WithData() {
  MaterialTheme {
    FilterDialogContent(
      title = "Filter Reminders",
      filterGroups = listOf(
        FilterGroup(
          id = "status",
          title = "Status",
          filters = listOf(
            Filter(id = "active", label = "Active", isSelected = true),
            Filter(id = "completed", label = "Completed", isSelected = false),
            Filter(id = "snoozed", label = "Snoozed", isSelected = true),
            Filter(id = "paused", label = "Paused", isSelected = false)
          )
        ),
        FilterGroup(
          id = "priority",
          title = "Priority",
          filters = listOf(
            Filter(id = "high", label = "High", isSelected = false),
            Filter(id = "medium", label = "Medium", isSelected = true),
            Filter(id = "low", label = "Low", isSelected = false)
          )
        ),
        FilterGroup(
          id = "type",
          title = "Reminder Type",
          filters = listOf(
            Filter(id = "by_date", label = "By Date", isSelected = false),
            Filter(id = "by_time", label = "By Time", isSelected = false),
            Filter(id = "recurring", label = "Recurring", isSelected = false),
            Filter(id = "location", label = "Location-based", isSelected = false),
            Filter(id = "timer", label = "Timer", isSelected = false),
            Filter(id = "weekday", label = "Weekday", isSelected = false)
          )
        )
      ),
      onFilterToggle = { _, _ -> },
      onClearAll = { },
      onApply = { },
      onDismiss = { }
    )
  }
}

@Preview(showBackground = true, name = "Filter Dialog - All Selected")
@Composable
private fun FilterDialogContentPreview_AllSelected() {
  MaterialTheme {
    FilterDialogContent(
      title = "All Filters Selected",
      filterGroups = listOf(
        FilterGroup(
          id = "status",
          title = "Status",
          filters = listOf(
            Filter(id = "active", label = "Active", isSelected = true),
            Filter(id = "completed", label = "Completed", isSelected = true),
            Filter(id = "snoozed", label = "Snoozed", isSelected = true)
          )
        ),
        FilterGroup(
          id = "priority",
          title = "Priority",
          filters = listOf(
            Filter(id = "high", label = "High", isSelected = true),
            Filter(id = "medium", label = "Medium", isSelected = true),
            Filter(id = "low", label = "Low", isSelected = true)
          )
        )
      ),
      onFilterToggle = { _, _ -> },
      onClearAll = { },
      onApply = { },
      onDismiss = { }
    )
  }
}

@Preview(showBackground = true, name = "Filter Group Section")
@Composable
private fun FilterGroupSectionPreview() {
  MaterialTheme {
    FilterGroupSection(
      group = FilterGroup(
        id = "type",
        title = "Reminder Type",
        filters = listOf(
          Filter(id = "by_date", label = "By Date", isSelected = true),
          Filter(id = "by_time", label = "By Time", isSelected = false),
          Filter(id = "recurring", label = "Recurring", isSelected = true),
          Filter(id = "location", label = "Location-based", isSelected = false),
          Filter(id = "timer", label = "Timer", isSelected = false)
        )
      ),
      onFilterToggle = { }
    )
  }
}
