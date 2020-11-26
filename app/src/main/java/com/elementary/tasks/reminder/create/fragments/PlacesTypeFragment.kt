package com.elementary.tasks.reminder.create.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.fragments.PlacesMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.fadeInAnimation
import com.elementary.tasks.core.utils.fadeOutAnimation
import com.elementary.tasks.core.utils.isVisible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.views.ActionView
import com.elementary.tasks.databinding.FragmentReminderPlaceBinding
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

class PlacesTypeFragment : RadiusTypeFragment<FragmentReminderPlaceBinding>() {

  private var mPlacesMap: PlacesMapFragment? = null
  private val mListener = object : MapListener {
    override fun placeChanged(place: LatLng, address: String) {
    }

    override fun onZoomClick(isFull: Boolean) {
      iFace.setFullScreenMode(isFull)
    }

    override fun onBackClick() {
      if (!isTablet()) {
        val map = mPlacesMap ?: return
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

  override fun recreateMarker() {
    mPlacesMap?.recreateMarker()
  }

  override fun prepare(): Reminder? {
    if (!Permissions.ensureForeground(requireActivity(), REQ_FOREGROUND)) {
      return null
    }
    if (!Permissions.ensureBackgroundLocation(requireActivity(), REQ_BG_LOCATION)) {
      return null
    }
    val reminder = super.prepare() ?: return null
    val map = mPlacesMap ?: return null
    var type = Reminder.BY_PLACES
    val places = map.places
    if (places.isEmpty()) {
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
        Reminder.BY_PLACES_CALL
      } else {
        Reminder.BY_PLACES_SMS
      }
    }

    reminder.places = places
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
  ) = FragmentReminderPlaceBinding.inflate(inflater, container, false)

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
    binding.mapContainer.visibleGone(isTablet())
    binding.mapButton.visibleGone(!isTablet())

    val placesMap = PlacesMapFragment()
    placesMap.setListener(mListener)
    placesMap.setCallback(object : MapCallback {
      override fun onMapReady() {
        mPlacesMap?.selectMarkers(iFace.reminderState.reminder.places)
      }
    })
    placesMap.markerRadius = prefs.radius
    placesMap.setMarkerStyle(prefs.markerStyle)
    fragmentManager?.beginTransaction()
      ?.replace(binding.mapFrame.id, placesMap)
      ?.addToBackStack(null)
      ?.commit()
    this.mPlacesMap = placesMap

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
    binding.mapButton.setOnClickListener { toggleMap() }
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
        binding.mapContainer.fadeOutAnimation()
        binding.scrollView.fadeInAnimation()
      } else {
        binding.scrollView.fadeOutAnimation()
        binding.mapContainer.fadeInAnimation()
      }
    }
  }

  override fun onBackPressed(): Boolean {
    return mPlacesMap == null || mPlacesMap!!.onBackPressed()
  }

  private fun editReminder() {
    val reminder = iFace.reminderState.reminder
    Timber.d("editReminder: %s", reminder)
    if (reminder.eventTime != "" && reminder.hasReminder) {
      binding.dateView.setDateTime(reminder.eventTime)
      binding.attackDelay.isChecked = true
    }
  }

  companion object {
    const val REQ_FOREGROUND = 2121
    const val REQ_BG_LOCATION = 2122
  }
}
