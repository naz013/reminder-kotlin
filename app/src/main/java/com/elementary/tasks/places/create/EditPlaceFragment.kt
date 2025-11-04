package com.elementary.tasks.places.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.ui.place.UiPlaceEdit
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.databinding.FragmentEditPlaceBinding
import com.elementary.tasks.navigation.toolbarfragment.BaseToolbarFragment
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.fragment.toast
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditPlaceFragment : BaseToolbarFragment<FragmentEditPlaceBinding>() {

  private val viewModel by viewModel<EditPlaceViewModel> { parametersOf(idFromIntent()) }
  private var googleMap: SimpleMapFragment? = null
  private var forceExit: Boolean = false

  private fun idFromIntent(): String = arguments?.getString(IntentKeys.INTENT_ID) ?: ""

  override fun getTitle(): String {
    return if (viewModel.hasId()) {
      getString(R.string.edit_place)
    } else {
      getString(R.string.new_place)
    }
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): FragmentEditPlaceBinding {
    return FragmentEditPlaceBinding.inflate(inflater, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Logger.i(TAG, "Opening the place screen for id: ${idFromIntent()}")
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    googleMap = SimpleMapFragment.newInstance(
      SimpleMapFragment.MapParams(
        isPlaces = false,
        isStyles = true,
        isRadius = true,
        rememberMarkerRadius = false,
        rememberMarkerStyle = false
      )
    )

    googleMap?.mapCallback = object : SimpleMapFragment.MapCallback {
      override fun onMapReady() {
        if (viewModel.canDelete) {
          viewModel.getPlace()?.also {
            showPlaceOnMap(it)
          }
        }
      }

      override fun onLocationSelected(markerState: SimpleMapFragment.MarkerState) {
        viewModel.lat = markerState.latLng.latitude
        viewModel.lng = markerState.latLng.longitude
        viewModel.address = markerState.address
        viewModel.markerStyle = markerState.style
        viewModel.markerRadius = markerState.radius

        if (binding.placeName.trimmedText().isEmpty()) {
          binding.placeName.setText(viewModel.address)
        }
      }
    }

    childFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, googleMap!!)
      .commit()

    addMenu(
      menuRes = R.menu.fragment_edit_place,
      onMenuItemListener = { menuItem ->
        return@addMenu when (menuItem.itemId) {
          R.id.action_add -> {
            askCopySaving()
            true
          }

          R.id.action_delete -> {
            dialogues.askConfirmation(requireContext(), getString(R.string.delete)) {
              if (it) {
                viewModel.deletePlace()
              }
            }
            true
          }

          else -> false
        }
      },
      menuModifier = { menu ->
        menu.getItem(1).isVisible = viewModel.canDelete
      }
    )

    initViewModel()
    loadPlace()
  }

  private fun loadPlace() {
    if (arguments?.getBoolean(IntentKeys.INTENT_ITEM, false) == true) {
      viewModel.loadFromIntent()
    }
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.place.nonNullObserve(viewLifecycleOwner) { showPlace(it) }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) {
      when (it) {
        Commands.SAVED, Commands.DELETED -> {
          forceExit = true
          moveBack()
        }
        else -> {
        }
      }
    }
  }

  private fun showPlace(place: UiPlaceEdit) {
    binding.placeName.setText(place.name)
    showPlaceOnMap(place)
    invalidateOptionsMenu()
  }

  private fun savePlace(newId: Boolean = false) {
    val name: String = binding.placeName.trimmedText()
    if (name.isEmpty()) {
      binding.placeLayout.error = getString(R.string.must_be_not_empty)
      binding.placeLayout.isErrorEnabled = true
      return
    }
    viewModel.savePlace(
      EditPlaceViewModel.SavePlaceData(
        name = name,
        newId = newId
      )
    )
  }

  private fun askCopySaving() {
    if (viewModel.hasLatLng()) {
      if (viewModel.isFromFile && viewModel.hasSameInDb) {
        dialogues.getMaterialDialog(requireContext())
          .setMessage(R.string.same_place_message)
          .setPositiveButton(R.string.keep) { dialogInterface, _ ->
            dialogInterface.dismiss()
            savePlace(true)
          }
          .setNegativeButton(R.string.replace) { dialogInterface, _ ->
            dialogInterface.dismiss()
            savePlace()
          }
          .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
            dialogInterface.dismiss()
          }
          .create()
          .show()
      } else {
        savePlace()
      }
    } else {
      toast(R.string.you_dont_select_place)
    }
  }

  private fun showPlaceOnMap(place: UiPlaceEdit) {
    googleMap?.run {
      addMarker(
        latLng = LatLng(place.lat, place.lng),
        title = place.name,
        markerStyle = place.marker,
        radius = place.radius,
        clear = true,
        animate = true
      )
    }
  }

  override fun canGoBack(): Boolean {
    val canCloseMap = googleMap?.onBackPressed()
    Logger.i(TAG, "Map can be closed: $canCloseMap")
    return canCloseMap == true || forceExit
  }

  companion object {
    private const val TAG = "EditGroupFragment"
  }
}
