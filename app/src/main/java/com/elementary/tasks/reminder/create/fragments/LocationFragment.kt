package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.isVisible
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.FragmentReminderLocationBinding
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

class LocationFragment : RadiusTypeFragment<FragmentReminderLocationBinding>() {

  private var mAdvancedMapFragment: AdvancedMapFragment? = null
  private var lastPos: LatLng? = null

  private val mListener = object : MapListener {
    override fun placeChanged(place: LatLng, address: String) {
      lastPos = place
    }

    override fun onZoomClick(isFull: Boolean) {
      iFace.setFullScreenMode(isFull)
    }

    override fun onBackClick() {
      if (!isTablet()) {
        val map = mAdvancedMapFragment ?: return
        if (map.isFullscreen) {
          map.isFullscreen = false
          iFace.setFullScreenMode(false)
        }
        if (binding.mapContainer.visibility == View.VISIBLE) {
          ViewUtils.fadeOutAnimation(binding.mapContainer)
          ViewUtils.fadeInAnimation(binding.scrollView)
        }
      }
    }
  }

  private fun showPlaceOnMap() {
    val reminder = iFace.reminderState.reminder
    if (!Reminder.isGpsType(reminder.type)) return
    val text = reminder.summary
    if (reminder.places.isNotEmpty()) {
      val jPlace = reminder.places[0]
      val latitude = jPlace.latitude
      val longitude = jPlace.longitude
      if (mAdvancedMapFragment != null) {
        mAdvancedMapFragment?.markerRadius = jPlace.radius
        lastPos = LatLng(latitude, longitude)
        mAdvancedMapFragment?.addMarker(lastPos, text, true, animate = true)
        toggleMap()
      }
    }
  }

  override fun recreateMarker() {
    mAdvancedMapFragment?.recreateMarker()
  }

  override fun prepare(): Reminder? {
    if (!Permissions.ensureForeground(requireActivity(), REQ_FOREGROUND)) {
      return null
    }
    if (!Permissions.ensureBackgroundLocation(requireActivity(), REQ_BG_LOCATION)) {
      return null
    }
    val reminder = super.prepare() ?: return null
    val map = mAdvancedMapFragment ?: return null
    var type = if (binding.enterCheck.isChecked) Reminder.BY_LOCATION else Reminder.BY_OUT
    val pos = lastPos
    if (pos == null) {
      iFace.showSnackbar(getString(R.string.you_dont_select_place))
      return null
    }
    if (TextUtils.isEmpty(reminder.summary)) {
      binding.taskLayout.error = getString(R.string.task_summary_is_empty)
      binding.taskLayout.isErrorEnabled = true
      map.invokeBack()
      return null
    }
    var number = ""
    if (binding.actionView.hasAction()) {
      number = binding.actionView.number
      if (TextUtils.isEmpty(number)) {
        iFace.showSnackbar(getString(R.string.you_dont_insert_number))
        return null
      }
      type = if (binding.actionView.type == ActionView.TYPE_CALL) {
        if (binding.enterCheck.isChecked) Reminder.BY_LOCATION_CALL else Reminder.BY_OUT_CALL
      } else {
        if (binding.enterCheck.isChecked) Reminder.BY_LOCATION_SMS else Reminder.BY_OUT_SMS
      }
    }
    val radius = mAdvancedMapFragment?.markerRadius ?: prefs.radius
    reminder.places = listOf(Place(
      radius = radius,
      marker = map.markerStyle,
      latitude = pos.latitude,
      longitude = pos.longitude,
      name = reminder.summary))
    reminder.target = number
    reminder.type = type
    reminder.exportToCalendar = false
    reminder.exportToTasks = false
    reminder.hasReminder = binding.attackDelay.isChecked
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.repeatInterval = 0
    if (binding.attackDelay.isChecked) {
      val startTime = binding.dateView.dateTime
      reminder.startTime = TimeUtil.getGmtFromDateTime(startTime)
      reminder.eventTime = TimeUtil.getGmtFromDateTime(startTime)
      Timber.d("EVENT_TIME %s", TimeUtil.getFullDateTime(startTime, true))
    } else {
      reminder.eventTime = ""
      reminder.startTime = ""
    }
    return reminder
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentReminderLocationBinding.inflate(inflater, container, false)

  override fun provideViews() {
    setViews(
      scrollView = binding.scrollView,
      expansionLayout = binding.moreLayout,
      ledPickerView = binding.ledView,
      extraView = binding.tuneExtraView,
      melodyView = binding.melodyView,
      attachmentView = binding.attachmentView,
      groupView = binding.groupView,
      summaryView = binding.taskSummary,
      dateTimeView = binding.dateView,
      loudnessPickerView = binding.loudnessView,
      priorityPickerView = binding.priorityView,
      windowTypeView = binding.windowTypeView,
      actionView = binding.actionView
    )
  }

  override fun onNewHeader(newHeader: String) {
    binding.cardSummary.text = newHeader
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (!isTablet()) {
      binding.mapContainer.visibility = View.GONE
      binding.mapButton.visibility = View.VISIBLE
      binding.searchBlock.visibility = View.VISIBLE
    } else {
      binding.mapContainer.visibility = View.VISIBLE
      binding.mapButton.visibility = View.GONE
      binding.searchBlock.visibility = View.GONE
    }

    val advancedMapFragment = AdvancedMapFragment.newInstance(true, true, true, true,
      prefs.markerStyle, themeUtil.isDark)
    advancedMapFragment.setListener(mListener)
    advancedMapFragment.setCallback(object : MapCallback {
      override fun onMapReady() {
        showPlaceOnMap()
      }
    })
    fragmentManager?.beginTransaction()
      ?.replace(binding.mapFrame.id, advancedMapFragment)
      ?.addToBackStack(null)
      ?.commit()
    this.mAdvancedMapFragment = advancedMapFragment

    binding.tuneExtraView.hasAutoExtra = false

    binding.delayLayout.visibility = View.GONE
    binding.attackDelay.setOnCheckedChangeListener { _, isChecked ->
      iFace.reminderState.isDelayAdded = isChecked
      if (isChecked) {
        binding.delayLayout.visibility = View.VISIBLE
      } else {
        binding.delayLayout.visibility = View.GONE
      }
    }
    binding.attackDelay.isChecked = iFace.reminderState.isDelayAdded

    binding.leaveCheck.setOnCheckedChangeListener { _, isChecked ->
      iFace.reminderState.isLeave = isChecked
    }

    binding.clearButton.setOnClickListener { binding.addressField.setText("") }
    binding.mapButton.setOnClickListener { toggleMap() }
    binding.addressField.setOnItemClickListener { _, _, position, _ ->
      val sel = binding.addressField.getAddress(position) ?: return@setOnItemClickListener
      val lat = sel.latitude
      val lon = sel.longitude
      val pos = LatLng(lat, lon)
      var title: String? = binding.taskSummary.text.toString().trim()
      if (title != null && title.matches("".toRegex())) title = pos.toString()
      mAdvancedMapFragment?.addMarker(pos, title, true, animate = true)
    }

    editReminder()
  }

  override fun updateActions() {
    if (binding.actionView.hasAction()) {
      if (binding.actionView.type == ActionView.TYPE_MESSAGE) {
        binding.tuneExtraView.hasAutoExtra = false
      } else {
        binding.tuneExtraView.hasAutoExtra = true
        binding.tuneExtraView.hint = getString(R.string.enable_making_phone_calls_automatically)
      }
    } else {
      binding.tuneExtraView.hasAutoExtra = false
    }
  }

  private fun toggleMap() {
    if (!isTablet()) {
      if (binding.mapContainer.isVisible()) {
        ViewUtils.fadeOutAnimation(binding.mapContainer)
        ViewUtils.fadeInAnimation(binding.scrollView)
      } else {
        ViewUtils.fadeOutAnimation(binding.scrollView)
        ViewUtils.fadeInAnimation(binding.mapContainer)
      }
    }
  }

  override fun onBackPressed(): Boolean {
    return mAdvancedMapFragment == null || mAdvancedMapFragment?.onBackPressed() == true
  }

  private fun editReminder() {
    val reminder = iFace.reminderState.reminder
    Timber.d("editReminder: %s", reminder)
    if (reminder.eventTime != "" && reminder.hasReminder) {
      binding.dateView.setDateTime(reminder.eventTime)
      binding.attackDelay.isChecked = true
    }
    if (iFace.reminderState.isLeave && Reminder.isBase(reminder.type, Reminder.BY_OUT)) {
      binding.leaveCheck.isChecked = true
    } else {
      binding.enterCheck.isChecked = true
    }
  }

  companion object {
    const val REQ_FOREGROUND = 2121
    const val REQ_BG_LOCATION = 2122
  }
}
