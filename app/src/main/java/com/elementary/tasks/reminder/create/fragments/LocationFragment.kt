package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.params.ReminderExplanationVisibility
import com.elementary.tasks.core.utils.ui.fadeInAnimation
import com.elementary.tasks.core.utils.ui.fadeOutAnimation
import com.elementary.tasks.core.utils.ui.gone
import com.elementary.tasks.core.utils.ui.isVisible
import com.elementary.tasks.core.utils.ui.visible
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.core.views.ClosableLegacyBuilderWarningView
import com.elementary.tasks.databinding.FragmentReminderLocationBinding
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.logging.Logger
import com.google.android.gms.maps.model.LatLng

class LocationFragment : RadiusTypeFragment<FragmentReminderLocationBinding>() {

  private var simpleMapFragment: SimpleMapFragment? = null
  private var lastPos: LatLng? = null
  private var mapState = MapState.WINDOWED

  private fun showPlaceOnMap() {
    val reminder = iFace.state.reminder
    if (!Reminder.isGpsType(reminder.type)) return
    val text = reminder.summary
    if (reminder.places.isNotEmpty()) {
      val place = reminder.places[0]
      val latitude = place.latitude
      val longitude = place.longitude
      iFace.state.radius = place.radius
      lastPos = LatLng(latitude, longitude)
      simpleMapFragment?.run {
        changeRadius(place.radius)
        addMarker(
          latLng = LatLng(latitude, longitude),
          title = text,
          clear = true,
          animate = true
        )
        toggleMap()
      }
    }
  }

  override fun getExplanationVisibilityType(): ReminderExplanationVisibility.Type {
    return ReminderExplanationVisibility.Type.BY_LOCATION
  }

  override fun getExplanationView(): View {
    return binding.explanationView
  }

  override fun setCloseListenerToExplanationView(listener: View.OnClickListener) {
    binding.explanationView.setOnClickListener(listener)
  }

  override fun prepare(): Reminder? {
    if (!Permissions.checkForeground(requireActivity())) {
      permissionFlow.askPermission(Permissions.FOREGROUND_SERVICE) {}
      return null
    }
    if (Module.is15 && !Permissions.checkPermission(
        requireContext(),
        Permissions.FOREGROUND_SERVICE_LOCATION
      )
    ) {
      permissionFlow.askPermission(Permissions.FOREGROUND_SERVICE_LOCATION) {}
      return null
    }
    if (!Permissions.isBgLocationAllowed(requireActivity())) {
      showBgLocationPopup()
      return null
    }
    val reminder = super.prepare() ?: return null

    var type = if (!isLeaving()) {
      Reminder.BY_LOCATION
    } else {
      Reminder.BY_OUT
    }

    val pos = lastPos
    if (pos == null) {
      iFace.showSnackbar(getString(R.string.you_dont_select_place))
      return null
    }
    if (TextUtils.isEmpty(reminder.summary)) {
      binding.taskLayout.error = getString(R.string.task_summary_is_empty)
      binding.taskLayout.isErrorEnabled = true
      if (mapState == MapState.FULLSCREEN) {
        showWindowedState()
      }
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
        if (!isLeaving()) {
          Reminder.BY_LOCATION_CALL
        } else {
          Reminder.BY_OUT_CALL
        }
      } else {
        if (!isLeaving()) {
          Reminder.BY_LOCATION_SMS
        } else {
          Reminder.BY_OUT_SMS
        }
      }
    }

    reminder.places = listOf(
      Place(
        radius = iFace.state.radius,
        marker = iFace.state.markerStyle,
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
    reminder.hasReminder = binding.enableDelayCheck.isChecked
    reminder.after = 0L
    reminder.delay = 0
    reminder.eventCount = 0
    reminder.repeatInterval = 0
    reminder.recurDataObject = null

    if (binding.enableDelayCheck.isChecked) {
      val startTime = binding.dateView.selectedDateTime
      reminder.startTime = dateTimeManager.getGmtFromDateTime(startTime)
      reminder.eventTime = dateTimeManager.getGmtFromDateTime(startTime)
      Logger.d("EVENT_TIME %s", dateTimeManager.logDateTime(startTime))
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

  override fun getDynamicViews(): List<View> {
    return listOfNotNull(
      binding.ledView,
      binding.tuneExtraView,
      binding.attachmentView,
      binding.groupView,
      binding.taskSummary,
      binding.dateView,
      binding.priorityView,
      binding.actionView
    )
  }

  override fun getLegacyMessageView(): ClosableLegacyBuilderWarningView {
    return binding.legacyBuilderWarningView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (!isTablet()) {
      binding.mapContainer.gone()
      binding.mapButton.visible()
      binding.searchBlock.visible()
    } else {
      binding.mapContainer.visible()
      binding.mapButton.gone()
      binding.searchBlock.gone()
    }

    val simpleMapFragment = SimpleMapFragment.newInstance(
      SimpleMapFragment.MapParams(
        isRadius = true,
        isStyles = true,
        isPlaces = true,
        rememberMarkerStyle = false,
        rememberMarkerRadius = false,
        rememberMapStyle = true,
        customButtons = listOf(
          SimpleMapFragment.MapCustomButton(
            icon = R.drawable.ic_fluent_chevron_left,
            id = MAP_EXIT_BUTTON
          ),
          SimpleMapFragment.MapCustomButton(
            icon = R.drawable.ic_builder_map_full_screen,
            id = MAP_FULLSCREEN_BUTTON
          )
        ),
        radiusParams = SimpleMapFragment.RadiusParams(
          radius = iFace.state.radius
        ),
        markerStyle = iFace.state.markerStyle
      )
    )

    simpleMapFragment.mapCallback = object : SimpleMapFragment.MapCallback {
      override fun onMapReady() {
        showPlaceOnMap()
      }

      override fun onLocationSelected(markerState: SimpleMapFragment.MarkerState) {
        lastPos = markerState.latLng
        iFace.state.radius = markerState.radius
        binding.radiusView.radiusInM = markerState.radius
      }
    }
    simpleMapFragment.radiusChangeListener = object : SimpleMapFragment.RadiusChangeListener {
      override fun onRadiusChanged(radius: Int) {
        iFace.state.radius = radius
        binding.radiusView.radiusInM = radius
      }
    }
    simpleMapFragment.customButtonCallback = object : SimpleMapFragment.CustomButtonCallback {
      override fun onButtonClicked(buttonId: Int) {
        if (buttonId == MAP_FULLSCREEN_BUTTON) {
          if (mapState == MapState.WINDOWED) {
            showFullScreenState()
          } else {
            showWindowedState()
          }
        } else {
          toggleMap()
        }
      }
    }

    fragmentManager?.beginTransaction()
      ?.replace(binding.mapFrame.id, simpleMapFragment)
      ?.addToBackStack(null)
      ?.commit()

    this.simpleMapFragment = simpleMapFragment

    binding.tuneExtraView.hasAutoExtra = false

    binding.delayLayout.gone()
    binding.enableDelayCheck.setOnCheckedChangeListener { _, isChecked ->
      iFace.state.isDelayAdded = isChecked
      binding.delayLayout.visibleGone(isChecked)
    }
    binding.enableDelayCheck.isChecked = iFace.state.isDelayAdded

    binding.triggerOptionGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
      iFace.state.isLeave = isChecked && checkedId == R.id.leaveCheck
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
      simpleMapFragment.addMarker(
        latLng = pos,
        title = title,
        clear = true,
        animate = true
      )
    }

    binding.radiusView.onRadiusChangeListener = {
      iFace.state.radius = it
      simpleMapFragment.changeRadius(it)
    }
    binding.radiusView.radiusInM = iFace.state.radius
    binding.radiusView.useMetric = prefs.useMetric

    editReminder()
  }

  private fun isLeaving(): Boolean {
    return binding.triggerOptionGroup.checkedButtonId == R.id.leaveCheck
  }

  private fun checkLeaving() {
    binding.triggerOptionGroup.check(R.id.leaveCheck)
  }

  private fun checkArriving() {
    binding.triggerOptionGroup.check(R.id.enterCheck)
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

  private fun showWindowedState() {
    if (!isTablet()) {
      val map = simpleMapFragment ?: return
      mapState = MapState.WINDOWED
      iFace.setFullScreenMode(false)
      map.changeCustomButton(
        SimpleMapFragment.MapCustomButton(
          icon = R.drawable.ic_builder_map_full_screen,
          id = MAP_FULLSCREEN_BUTTON
        )
      )
    }
  }

  private fun showFullScreenState() {
    if (!isTablet()) {
      val map = simpleMapFragment ?: return
      mapState = MapState.FULLSCREEN
      iFace.setFullScreenMode(true)
      map.changeCustomButton(
        SimpleMapFragment.MapCustomButton(
          icon = R.drawable.ic_fluent_chevron_left,
          id = MAP_FULLSCREEN_BUTTON
        )
      )
    }
  }

  override fun onBackPressed(): Boolean {
    return simpleMapFragment == null || simpleMapFragment?.onBackPressed() == true
  }

  private fun editReminder() {
    val reminder = iFace.state.reminder
    Logger.d("editReminder: $reminder")
    if (reminder.eventTime != "" && reminder.hasReminder) {
      binding.dateView.setDateTime(reminder.eventTime)
      binding.enableDelayCheck.isChecked = true
    }
    iFace.state.isLeave = Reminder.isBase(reminder.type, Reminder.BY_OUT)
    if (Reminder.isBase(reminder.type, Reminder.BY_OUT)) {
      checkLeaving()
    } else {
      checkArriving()
    }
  }

  private enum class MapState {
    WINDOWED, FULLSCREEN
  }

  companion object {
    private const val MAP_EXIT_BUTTON = 0
    private const val MAP_FULLSCREEN_BUTTON = 1
  }
}
