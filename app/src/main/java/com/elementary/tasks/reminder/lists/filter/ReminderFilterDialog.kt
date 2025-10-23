package com.elementary.tasks.reminder.lists.filter

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.ComposeBottomSheetDialogFragment
import com.github.naz013.ui.common.compose.foundation.PrimaryIconButton
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

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
      onDateRangeChanged = { groupId, startDate, endDate ->
        viewModel.dateRangeChanged(groupId, startDate, endDate)
      },
      onClearAll = { viewModel.clearAllFilters() },
      onApply = { viewModel.onApplyFilters() },
      onDismiss = { dismiss() }
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Logger.i(TAG, "On view created.")

    val filters = arguments?.readParcelable(ARG_FILTERS, Filters::class.java)

    if (savedInstanceState == null && filters != null) {
      viewModel.setFilters(filters)
    }

    initViewModel()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    Logger.i(TAG, "On attach.")
    setupBottomSheet()
  }

  private fun initViewModel() {
    viewModel.applyFilters.observeEvent(viewLifecycleOwner) { appliedFilters ->
      Logger.i(TAG, "Filters applied: ${appliedFilters.selectedFilters.size}")
      val result = Bundle().apply {
        putParcelable(APPLIED_FILTERS_KEY, appliedFilters)
      }
      parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
      dismiss()
    }
  }

  private fun setupBottomSheet() {
    // Open the bottom sheet expanded having half screen height
    dialog?.let { dlg ->
      dlg.setOnShowListener { dialogInterface ->
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let { sheet ->
          val behavior = BottomSheetBehavior.from(sheet)
          behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
          behavior.peekHeight = resources.displayMetrics.heightPixels / 2
        }
      }
    }
  }

  companion object {
    private const val TAG = "ReminderFilterDialog"
    private const val ARG_FILTERS = "filters"
    private const val ARG_TITLE = "dialog_title"
    const val REQUEST_KEY = "filter_dialog_request"
    private const val APPLIED_FILTERS_KEY = "applied_filters"

    fun getAppliedFiltersFromResult(result: Bundle): AppliedFilters? {
      return result.readParcelable(APPLIED_FILTERS_KEY, AppliedFilters::class.java)
    }

    fun newInstance(
      filters: Filters,
      title: String
    ): ReminderFilterDialog {
      return ReminderFilterDialog().apply {
        arguments = Bundle().apply {
          putParcelable(ARG_FILTERS, filters)
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
  filterGroups: List<UiFilterGroup>,
  onFilterToggle: (groupId: String, filterId: String) -> Unit,
  onDateRangeChanged: (groupId: String, startDate: LocalDate?, endDate: LocalDate?) -> Unit,
  onClearAll: () -> Unit,
  onApply: () -> Unit,
  onDismiss: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Header with action buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      PrimaryIconButton(
        icon = AppIcons.Clear,
        contentDescription = stringResource(R.string.cancel),
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
        icon = AppIcons.Ok,
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
          },
          onDateRangeChanged = { startDate, endDate ->
            onDateRangeChanged(group.id, startDate, endDate)
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterGroupSection(
  group: UiFilterGroup,
  onFilterToggle: (String) -> Unit,
  onDateRangeChanged: (startDate: LocalDate?, endDate: LocalDate?) -> Unit
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

    when (val filter = group.filter) {
      is UiReminderGroupFilter -> {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          filter.chips.forEach { chip ->
            FilterChip(
              selected = chip.isSelected,
              onClick = { onFilterToggle(chip.id) },
              label = {
                Text(
                  text = chip.label,
                  style = MaterialTheme.typography.labelLarge
                )
              }
            )
          }
        }
      }
      is UiDateRangeFilter -> {
        DateRangeFilterSection(
          filter = filter,
          onDateRangeChanged = onDateRangeChanged
        )
      }
    }
  }
}

@Composable
fun DateRangeFilterSection(
  filter: UiDateRangeFilter,
  onDateRangeChanged: (startDate: LocalDate?, endDate: LocalDate?) -> Unit
) {
  val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

  // Convert LocalDate to days since minDate for slider
  val minDays = 0f
  val maxDays = remember(filter.minDate, filter.maxDate) {
    filter.minDate.until(filter.maxDate).days.toFloat()
  }

  val initialStartDays = remember(filter.startDate, filter.minDate) {
    filter.startDate?.let { filter.minDate.until(it).days.toFloat() } ?: minDays
  }

  val initialEndDays = remember(filter.endDate, filter.minDate) {
    filter.endDate?.let { filter.minDate.until(it).days.toFloat() } ?: maxDays
  }

  var sliderRange by remember(initialStartDays, initialEndDays) {
    mutableStateOf(initialStartDays..initialEndDays)
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Display selected range
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = filter.minDate.plusDays(sliderRange.start.toLong()).format(dateFormatter),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = filter.minDate.plusDays(sliderRange.endInclusive.toLong()).format(dateFormatter),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    // Range Slider
    RangeSlider(
      value = sliderRange,
      onValueChange = { newRange ->
        sliderRange = newRange
      },
      onValueChangeFinished = {
        val startDate = if (sliderRange.start >= minDays) {
          filter.minDate.plusDays(sliderRange.start.toLong())
        } else {
          null
        }
        val endDate = if (sliderRange.endInclusive <= maxDays) {
          filter.minDate.plusDays(sliderRange.endInclusive.toLong())
        } else {
          null
        }
        onDateRangeChanged(startDate, endDate)
      },
      valueRange = minDays..maxDays,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

