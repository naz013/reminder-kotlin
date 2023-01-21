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
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.isVisible
import com.elementary.tasks.core.utils.ui.fadeInAnimation
import com.elementary.tasks.core.utils.ui.fadeOutAnimation
import com.elementary.tasks.core.utils.visibleGone
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
          binding.mapContainer.fadeOutAnimation()
          binding.scrollView.fadeInAnimation()
        }
      }
    }
  }

  private fun showPlaceOnMap() {
    val reminder = iFace.state.reminder
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
    if (!Permissions.checkForeground(requireActivity())) {
      permissionFlow.askPermission(Permissions.FOREGROUND_SERVICE) {}
      return null
    }
    if (!Permissions.isBgLocationAllowed(requireActivity())) {
      showBgLocationPopup()
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
      type = if (binding.actionView.actionState == ActionView.ActionState.CALL) {
        if (binding.enterCheck.isChecked) Reminder.BY_LOCATION_CALL else Reminder.BY_OUT_CALL
      } else {
        if (binding.enterCheck.isChecked) Reminder.BY_LOCATION_SMS else Reminder.BY_OUT_SMS
      }
    }
    val radius = mAdvancedMapFragment?.markerRadius ?: prefs.radius
    reminder.places = listOf(
      Place(
        radius = radius,
        marker = map.markerStyle,
        latitude = pos.latitude,
        longitude = pos.longitude,
        name = reminder.summary,
        dateTime = dateTimeManager.getNowGmtDateTime()
      )
    )
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
      val startTime = binding.dateView.selectedDateTime
      reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
      reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
      Timber.d("EVENT_TIME %s", dateTimeManager.logDateTime(startTime))
    } else {
      reminder.eventTime = ""
      reminder.startTime = ""
    }
    return reminder
  }

  private fun showBgLocationPopup() {
    dialogues.getMaterialDialog(requireContext())
      .setMessage(R.string.bg_location_message)
      .setPositiveButton(R.string.allow) { dialog, _ ->
        dialog.dismiss()
        permissionFlow.askPermission(Permissions.BACKGROUND_LOCATION) {}
      }
      .setNegativeButton(R.string.do_not_allow) { dialog, _ ->
        dialog.dismiss()
      }
      .create()
      .show()
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
      iFace.state.isDelayAdded = isChecked
      binding.delayLayout.visibleGone(isChecked)
    }
    binding.attackDelay.isChecked = iFace.state.isDelayAdded

    binding.leaveCheck.setOnCheckedChangeListener { _, isChecked ->
      iFace.state.isLeave = isChecked
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
      if (binding.actionView.actionState == ActionView.ActionState.SMS) {
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
        binding.mapContainer.fadeOutAnimation()
        binding.scrollView.fadeInAnimation()
      } else {
        binding.scrollView.fadeOutAnimation()
        binding.mapContainer.fadeInAnimation()
      }
    }
  }

  override fun onBackPressed(): Boolean {
    return mAdvancedMapFragment == null || mAdvancedMapFragment?.onBackPressed() == true
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    Timber.d("editReminder: %s", reminder)
    if (reminder.eventTime != "" && reminder.hasReminder) {
      binding.dateView.setDateTime(reminder.eventTime)
      binding.attackDelay.isChecked = true
    }
    if (iFace.state.isLeave && Reminder.isBase(reminder.type, Reminder.BY_OUT)) {
      binding.leaveCheck.isChecked = true
    } else {
      binding.enterCheck.isChecked = true
    }
  }
}
