package com.elementary.tasks.places.create

import android.content.ContentResolver
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.datapicker.LoginLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.places.PlaceViewModel
import com.elementary.tasks.databinding.ActivityCreatePlaceBinding
import com.elementary.tasks.pin.PinLoginActivity
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

class CreatePlaceActivity : BindingActivity<ActivityCreatePlaceBinding>(), MapListener, MapCallback {

  private val viewModel by viewModel<PlaceViewModel> { parametersOf(getId()) }
  private val stateViewModel by viewModel<CreatePlaceViewModel>()
  private val permissionFlow = PermissionFlow(this, dialogues)
  private var googleMap: AdvancedMapFragment? = null
  private val loginLauncher = LoginLauncher(this) {
    if (!it) {
      finish()
    } else {
      stateViewModel.isLogged = true
    }
  }

  override fun inflateBinding() = ActivityCreatePlaceBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    stateViewModel.isPlaceEdited = savedInstanceState != null

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
      stateViewModel.isLogged = intentBoolean(PinLoginActivity.ARG_LOGGED)
    }
  }

  override fun onStart() {
    super.onStart()
    if (prefs.hasPinCode && !stateViewModel.isLogged) {
      loginLauncher.askLogin()
    }
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    binding.backButton.setOnClickListener { finish() }
  }

  private fun initViewModel() {
    lifecycle.addObserver(stateViewModel)
    lifecycle.addObserver(viewModel)
    viewModel.place.observe(this) { place ->
      place?.let { showPlace(it) }
    }
    viewModel.result.observe(this) {
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
        val place = intentParcelable(Constants.INTENT_ITEM, Place::class.java)
        showPlace(place, true)
      }
    }
  }

  private fun readUri() {
    intent.data?.let {
      runCatching {
        val place = if (ContentResolver.SCHEME_CONTENT != it.scheme) {
          val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_PLACE)
          if (any != null && any is Place) {
            any
          } else null
        } else null
        showPlace(place, true)
      }
    }
  }

  private fun showPlace(place: Place?, fromFile: Boolean = false) {
    place?.also {
      binding.titleView.text = getString(R.string.edit_place)
      if (!stateViewModel.isPlaceEdited) {
        binding.placeName.setText(place.name)
        stateViewModel.place = place
        stateViewModel.isPlaceEdited = true
        stateViewModel.isFromFile = fromFile
        if (fromFile) {
          viewModel.findSame(it.id)
        }
        showPlaceOnMap()
      }
    }
  }

  private fun savePlace(newId: Boolean = false) {
    var name: String = binding.placeName.trimmedText()
    if (name == "") {
      name = stateViewModel.place.name
    }
    if (name == "") {
      binding.placeLayout.error = getString(R.string.must_be_not_empty)
      binding.placeLayout.isErrorEnabled = true
      return
    }
    val latitude = stateViewModel.place.latitude
    val longitude = stateViewModel.place.longitude
    val marker = googleMap?.markerStyle ?: prefs.markerStyle
    val item = stateViewModel.place.apply {
      this.name = name
      this.latitude = latitude
      this.longitude = longitude
      this.marker = marker
      this.radius = prefs.radius
    }
    if (newId) {
      item.id = UUID.randomUUID().toString()
    }
    viewModel.savePlace(item)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_add -> {
        askCopySaving()
        true
      }
      MENU_ITEM_DELETE -> {
        dialogues.askConfirmation(this, getString(R.string.delete)) {
          if (it) deleteItem()
        }
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun askCopySaving() {
    if (stateViewModel.place.hasLatLng()) {
      if (stateViewModel.isFromFile && viewModel.hasSameInDb) {
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

  private fun deleteItem() {
    viewModel.deletePlace(stateViewModel.place)
  }

  private fun showPlaceOnMap() {
    googleMap?.takeIf {
      stateViewModel.place.hasLatLng()
    }?.also {
      it.setStyle(stateViewModel.place.marker)
      it.addMarker(stateViewModel.place.latLng(), stateViewModel.place.name,
        clear = true, animate = true, radius = -1)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_place_edit, menu)
    if (stateViewModel.isPlaceEdited && !stateViewModel.isFromFile) {
      menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
    }
    return true
  }

  override fun placeChanged(place: LatLng, address: String) {
    stateViewModel.place.apply {
      this.latitude = place.latitude
      this.longitude = place.longitude
      this.name = address
    }
    if (binding.placeName.trimmedText() == "") {
      binding.placeName.setText(address)
    }
  }

  override fun onBackClick() {
  }

  override fun onZoomClick(isFull: Boolean) {
  }

  override fun onMapReady() {
    if (stateViewModel.isPlaceEdited) {
      showPlaceOnMap()
    }
  }

  companion object {
    private const val MENU_ITEM_DELETE = 12
  }
}
