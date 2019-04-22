package com.elementary.tasks.places.create

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.places.PlaceViewModel
import com.elementary.tasks.databinding.ActivityCreatePlaceBinding
import com.google.android.gms.maps.model.LatLng
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaceActivity : ThemedActivity<ActivityCreatePlaceBinding>(), MapListener, MapCallback {

    private lateinit var viewModel: PlaceViewModel
    private val stateViewModel: CreatePlaceViewModel by viewModel()

    private var mGoogleMap: AdvancedMapFragment? = null

    private var mItem: Place? = null
    private var mUri: Uri? = null

    private val backupTool: BackupTool by inject()

    override fun layoutRes(): Int = R.layout.activity_create_place

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateViewModel.isPlaceEdited = savedInstanceState != null

        initActionBar()

        mGoogleMap = AdvancedMapFragment.newInstance(false, true, false, false,
                prefs.markerStyle, themeUtil.isDark, false)
        mGoogleMap?.setListener(this)
        mGoogleMap?.setCallback(this)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mGoogleMap!!)
                .addToBackStack(null)
                .commit()
        loadPlace()
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.backButton.setOnClickListener { finish() }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, PlaceViewModel.Factory(id)).get(PlaceViewModel::class.java)
        viewModel.place.observe(this, Observer { place ->
            place?.let { showPlace(it) }
        })
        viewModel.result.observe(this, Observer { commands ->
            commands?.let {
                when (it) {
                    Commands.SAVED, Commands.DELETED -> finish()
                    else -> {
                    }
                }
            }
        })
    }

    private fun loadPlace() {
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        initViewModel(id)
        if (intent.data != null) {
            mUri = intent.data
            readUri()
        } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
            try {
                mItem = intent.getSerializableExtra(Constants.INTENT_ITEM) as Place?
                showPlace(mItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readUri() {
        if (!Permissions.ensurePermissions(this, SD_REQ, Permissions.READ_EXTERNAL)) {
            return
        }
        mUri?.let {
            try {
                val scheme = it.scheme
                mItem = if (ContentResolver.SCHEME_CONTENT != scheme) {
                    backupTool.getPlace(it.path, null)
                } else null
                showPlace(mItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showPlace(place: Place?) {
        this.mItem = place
        place?.let {
            binding.titleView.text = getString(R.string.edit_place)
            if (!stateViewModel.isPlaceEdited) {
                binding.placeName.setText(place.name)
                stateViewModel.place = place
                stateViewModel.isPlaceEdited = true
                showPlaceOnMap()
            }
        }
    }

    private fun addPlace() {
        if (stateViewModel.place.hasLatLng()) {
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
            val marker = mGoogleMap?.markerStyle ?: prefs.markerStyle
            val item = (mItem ?: Place()).apply {
                this.name = name
                this.latitude = latitude
                this.longitude = longitude
                this.marker = marker
                this.radius = prefs.radius
            }
            viewModel.savePlace(item)
        } else {
            Toast.makeText(this, getString(R.string.you_dont_select_place), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                addPlace()
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

    private fun deleteItem() {
        mItem?.let { viewModel.deletePlace(it) }
    }

    private fun showPlaceOnMap() {
        val map = mGoogleMap ?: return
        if (stateViewModel.place.hasLatLng()) {
            map.setStyle(stateViewModel.place.marker)
            mGoogleMap?.addMarker(stateViewModel.place.latLng(), stateViewModel.place.name, true, true, -1)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_palce_edit, menu)
        if (mItem != null) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SD_REQ && Permissions.isAllGranted(grantResults)) {
            readUri()
        }
    }

    companion object {
        private const val SD_REQ = 555
        private const val MENU_ITEM_DELETE = 12
    }
}
