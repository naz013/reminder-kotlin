package com.elementary.tasks.places.create

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.MemoryUtil
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.places.PlaceViewModel
import com.elementary.tasks.databinding.ActivityCreatePlaceBinding
import com.elementary.tasks.pin.PinLoginActivity
import com.google.android.gms.maps.model.LatLng
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class CreatePlaceActivity : BindingActivity<ActivityCreatePlaceBinding>(), MapListener, MapCallback {

  private val viewModel by viewModel<PlaceViewModel> { parametersOf(getId()) }
  private val stateViewModel by viewModel<CreatePlaceViewModel>()
  private var googleMap: AdvancedMapFragment? = null

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
      stateViewModel.isLogged = intent.getBooleanExtra(ARG_LOGGED, false)
    }
  }

  override fun onStart() {
    super.onStart()
    if (prefs.hasPinCode && !stateViewModel.isLogged) {
      PinLoginActivity.verify(this)
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

  private fun getId(): String = intent.getStringExtra(Constants.INTENT_ID) ?: ""

  private fun loadPlace() {
    initViewModel()
    if (intent.data != null) {
      readUri()
    } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
      try {
        val place = intent.getParcelableExtra(Constants.INTENT_ITEM) as Place?
        showPlace(place, true)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun readUri() {
    if (!Permissions.checkPermission(this, SD_REQ, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
      return
    }
    intent.data?.let {
      try {
        val place = if (ContentResolver.SCHEME_CONTENT != it.scheme) {
          val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_PLACE)
          if (any != null && any is Place) {
            any
          } else null
        } else null
        showPlace(place, true)
      } catch (e: Exception) {
        e.printStackTrace()
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
    var name: String = binding.placeName.text.toString().trim()
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
      Toast.makeText(this, getString(R.string.you_dont_select_place), Toast.LENGTH_SHORT).show()
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

  override fun onBackPressed() {
    finish()
  }

  override fun placeChanged(place: LatLng, address: String) {
    stateViewModel.place.apply {
      this.latitude = place.latitude
      this.longitude = place.longitude
      this.name = address
    }
    if (binding.placeName.text.toString().trim() == "") {
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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == PinLoginActivity.REQ_CODE) {
      if (resultCode != Activity.RESULT_OK) {
        finish()
      } else {
        stateViewModel.isLogged = true
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == SD_REQ && Permissions.checkPermission(grantResults)) {
      readUri()
    }
  }

  companion object {
    private const val SD_REQ = 555
    private const val MENU_ITEM_DELETE = 12
    private const val ARG_LOGGED = "arg_logged"

    fun openLogged(context: Context, intent: Intent? = null) {
      if (intent == null) {
        context.startActivity(Intent(context, CreatePlaceActivity::class.java)
          .putExtra(ARG_LOGGED, true))
      } else {
        intent.putExtra(ARG_LOGGED, true)
        context.startActivity(intent)
      }
    }
  }
}
