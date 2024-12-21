package com.elementary.tasks.places.create

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.github.naz013.domain.Place
import com.elementary.tasks.core.data.ui.place.UiPlaceEdit
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.LoginLauncher
import com.elementary.tasks.core.os.toast
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.applyTopInsets
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.databinding.ActivityCreatePlaceBinding
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.simplemap.SimpleMapFragment
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreatePlaceActivity : BindingActivity<ActivityCreatePlaceBinding>() {

  private val viewModel by viewModel<PlaceViewModel> { parametersOf(getId()) }
  private var googleMap: SimpleMapFragment? = null
  private val loginLauncher = LoginLauncher(this) {
    if (!it) {
      finish()
    } else {
      viewModel.isLogged = true
    }
  }

  override fun inflateBinding() = ActivityCreatePlaceBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    initActionBar()
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

    supportFragmentManager.beginTransaction()
      .replace(R.id.fragment_container, googleMap!!)
      .addToBackStack(null)
      .commit()

    loadPlace()

    if (savedInstanceState == null) {
      viewModel.isLogged = intentBoolean(PinLoginActivity.ARG_LOGGED)
    }
  }

  override fun onStart() {
    super.onStart()
    if (prefs.hasPinCode && !viewModel.isLogged) {
      loginLauncher.askLogin()
    }
  }

  private fun initActionBar() {
    binding.appBar.applyTopInsets()
    binding.toolbar.setNavigationOnClickListener { finish() }
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.action_add -> {
          askCopySaving()
          true
        }

        R.id.action_delete -> {
          dialogues.askConfirmation(this, getString(R.string.delete)) {
            if (it) {
              viewModel.deletePlace()
            }
          }
          true
        }

        else -> false
      }
    }
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also {
      it.getItem(1).isVisible = viewModel.canDelete
    }
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.place.nonNullObserve(this) { showPlace(it) }
    viewModel.result.nonNullObserve(this) {
      when (it) {
        Commands.SAVED, Commands.DELETED -> finish()
        else -> {
        }
      }
    }
  }

  private fun getId(): String = intentString(Constants.INTENT_ID)

  private fun loadPlace() {
    initViewModel()
    if (intent.data != null) {
      permissionFlow.askPermission(Permissions.READ_EXTERNAL) { readUri() }
    } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
      runCatching {
        viewModel.loadFromIntent(intentSerializable(Constants.INTENT_ITEM, Place::class.java))
      }
    }
  }

  private fun readUri() {
    intent.data?.let { viewModel.loadFromUri(it) }
  }

  private fun showPlace(place: UiPlaceEdit) {
    binding.toolbar.title = getString(R.string.edit_place)
    binding.placeName.setText(place.name)
    showPlaceOnMap(place)
    updateMenu()
  }

  private fun savePlace(newId: Boolean = false) {
    val name: String = binding.placeName.trimmedText()
    if (name.isEmpty()) {
      binding.placeLayout.error = getString(R.string.must_be_not_empty)
      binding.placeLayout.isErrorEnabled = true
      return
    }
    viewModel.savePlace(
      PlaceViewModel.SavePlaceData(
        name = name,
        newId = newId
      )
    )
  }

  private fun askCopySaving() {
    if (viewModel.hasLatLng()) {
      if (viewModel.isFromFile && viewModel.hasSameInDb) {
        dialogues.getMaterialDialog(this)
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
}
