package com.elementary.tasks.places.create

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.ui.place.UiPlaceEdit
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.LoginLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.databinding.ActivityCreatePlaceBinding
import com.elementary.tasks.pin.PinLoginActivity
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CreatePlaceActivity : BindingActivity<ActivityCreatePlaceBinding>(), MapListener,
  MapCallback {

  private val viewModel by viewModel<PlaceViewModel> { parametersOf(getId()) }
  private val permissionFlow = PermissionFlow(this, dialogues)
  private var googleMap: AdvancedMapFragment? = null
  private val loginLauncher = LoginLauncher(this) {
    if (!it) {
      finish()
    } else {
      viewModel.isLogged = true
    }
  }

  override fun inflateBinding() = ActivityCreatePlaceBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()
    googleMap = AdvancedMapFragment.newInstance(
      isPlaces = false,
      isStyles = true,
      isBack = false,
      isZoom = false,
      markerStyle = prefs.markerStyle,
      isDark = isDarkMode,
      isRadius = false
    )
    googleMap?.setListener(this)
    googleMap?.setCallback(this)

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
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    binding.backButton.setOnClickListener { finish() }
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
        viewModel.loadFromIntent(intentParcelable(Constants.INTENT_ITEM, Place::class.java))
      }
    }
  }

  private fun readUri() {
    intent.data?.let { viewModel.loadFromUri(it) }
  }

  private fun showPlace(place: UiPlaceEdit) {
    binding.titleView.text = getString(R.string.edit_place)
    binding.placeName.setText(place.name)
    showPlaceOnMap(place)
  }

  private fun savePlace(newId: Boolean = false) {
    val name: String = binding.placeName.trimmedText()
    if (name.isEmpty()) {
      binding.placeLayout.error = getString(R.string.must_be_not_empty)
      binding.placeLayout.isErrorEnabled = true
      return
    }
    val marker = googleMap?.markerStyle ?: prefs.markerStyle
    viewModel.savePlace(
      PlaceViewModel.SavePlaceData(
        name = name,
        newId = newId,
        radius = prefs.radius,
        marker = marker
      )
    )
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_add -> {
        askCopySaving()
        true
      }

      MENU_ITEM_DELETE -> {
        dialogues.askConfirmation(this, getString(R.string.delete)) {
          if (it) {
            viewModel.deletePlace()
          }
        }
        true
      }

      else -> super.onOptionsItemSelected(item)
    }
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
    googleMap?.also {
      it.setStyle(place.marker)
      it.addMarker(
        LatLng(place.lat, place.lng),
        place.name,
        clear = true,
        animate = true,
        radius = -1
      )
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_place_edit, menu)
    if (viewModel.canDelete) {
      menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
    }
    return true
  }

  override fun placeChanged(place: LatLng, address: String) {
    viewModel.lat = place.latitude
    viewModel.lng = place.longitude
    viewModel.address = address
    if (binding.placeName.trimmedText().isEmpty()) {
      binding.placeName.setText(address)
    }
  }

  override fun onBackClick() {
  }

  override fun onZoomClick(isFull: Boolean) {
  }

  override fun onMapReady() {
    if (viewModel.canDelete) {
      viewModel.getPlace()?.also {
        showPlaceOnMap(it)
      }
    }
  }

  companion object {
    private const val MENU_ITEM_DELETE = 12
  }
}
