package com.elementary.tasks.calendar.dayview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.elementary.tasks.calendar.BaseCalendarFragment
import com.elementary.tasks.calendar.dayview.weekheader.WeekAdapter
import com.elementary.tasks.databinding.FragmentDayViewBinding
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate

class WeekViewFragment : BaseCalendarFragment<FragmentDayViewBinding>() {

  private lateinit var pagerAdapter: InfiniteDayViewPagerAdapter
  private var isUpdatingProgrammatically = false

  private val viewModel by viewModel<WeekViewModel> { parametersOf(getDate()) }

  private val weekAdapter = WeekAdapter { viewModel.selectDate(it.localDate) }

  private fun getDate(): LocalDate {
    return arguments?.let {
      dateTimeManager.fromMillis(WeekViewFragmentArgs.fromBundle(it).date).toLocalDate()
    } ?: LocalDate.now()
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentDayViewBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Logger.d(TAG, "On view created")
    binding.weekGridView.adapter = weekAdapter
    binding.fab.setOnClickListener { tryToShowActionDialog() }

    initPager()
    initViewModel()
  }

  private fun tryToShowActionDialog() {
    showActionDialog(viewModel.lastSelectedDate)
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.state.nonNullObserve(viewLifecycleOwner) { onStateChanged(it) }
    viewModel.moveToDate.observeEvent(viewLifecycleOwner) { updateDate(it, true) }
  }

  private fun onStateChanged(state: DayViewState) {
    weekAdapter.submitList(state.days)
    setTitle(state.title)
  }

  private fun initPager() {
    Logger.i(TAG, "Initializing pager, date: ${viewModel.initDate}, position: ${viewModel.lastPosition}")

    pagerAdapter = InfiniteDayViewPagerAdapter(this, viewModel.initDate)
    binding.pager.adapter = pagerAdapter
    binding.pager.offscreenPageLimit = 1

    // Reduce sensitivity to make swiping smoother
    try {
      val recyclerView = binding.pager.getChildAt(0) as? RecyclerView
      recyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to configure pager", e)
    }

    // Set initial position to center
    binding.pager.setCurrentItem(viewModel.lastPosition, false)

    // Listen for page changes
    binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        super.onPageSelected(position)

        // Skip if we're updating programmatically
        if (isUpdatingProgrammatically) {
          Logger.d(TAG, "Skipping page selected - programmatic update")
          return
        }

        // Calculate the date for this position
        val newDate = pagerAdapter.getDateForPosition(position)
        Logger.d(TAG, "Page selected: $position, date: $newDate")

        viewModel.onDateSelected(newDate)
        viewModel.updateLastPosition(position)
      }
    })
  }

  override fun getTitle(): String = viewModel.state.value?.title ?: ""

  private fun updateDate(targetDate: LocalDate, smooth: Boolean) {
    Logger.d(TAG, "Update date: $targetDate, smooth: $smooth")

    // Calculate the position for this date
    val targetPosition = pagerAdapter.getPositionForDate(targetDate)
    Logger.d(TAG, "Target position: $targetPosition")

    // Update pager position
    isUpdatingProgrammatically = true
    binding.pager.post {
      binding.pager.setCurrentItem(targetPosition, smooth)
      viewModel.updateLastPosition(targetPosition)
      // Reset flag after a short delay to ensure the transition completes
      binding.pager.postDelayed({
        isUpdatingProgrammatically = false
      }, 100)
    }
  }

  companion object {
    private const val TAG = "WeekViewFragment"
  }
}
