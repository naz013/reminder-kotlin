package com.elementary.tasks.monthview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.DialogBottomSheetDayBinding
import com.elementary.tasks.dayview.day.EventModel
import com.maxkeppeler.sheets.core.Sheet
import timber.log.Timber

typealias LoadCallback = (
  listView: RecyclerView,
  loadingView: View,
  emptyView: View,
  list: List<EventModel>
) -> Unit

class DayBottomSheetDialog(
  context: Context,
  private val label: String,
  private val list: List<EventModel>,
  private val addReminderCallback: () -> Unit,
  private val addBirthdayCallback: () -> Unit,
  private val loadCallback: LoadCallback
) : Sheet() {

  private lateinit var binding: DialogBottomSheetDayBinding

  init {
    windowContext = context
  }

  override fun onCreateLayoutView(): View {
    Timber.d("onCreateLayoutView: $windowContext, ${requireContext()}")
    binding = DialogBottomSheetDayBinding.inflate(LayoutInflater.from(activity))

    title(label)
    displayCloseButton(true)
    displayPositiveButton(true)
    displayNegativeButton(true)

    positiveListener = addReminderCallback
    positiveText = getString(R.string.add_reminder_menu)

    onNegative(getString(R.string.add_birthday)) {
      Timber.d("onNegative")
      addBirthdayCallback.invoke()
    }

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.loadingView.visibleGone(list.isNotEmpty())
    binding.eventsList.visibleGone(list.isNotEmpty())
    binding.emptyItem.visibleGone(list.isEmpty())
    if (list.isNotEmpty()) {
      binding.eventsList.layoutManager = LinearLayoutManager(requireContext())
      loadCallback.invoke(binding.eventsList, binding.loadingView, binding.emptyItem, list)
    }
  }
}
