package com.elementary.tasks.places.create

import android.content.ContentResolver
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.places.PlaceViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_create_place.*
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class CreatePlaceActivity : ThemedActivity(), MapListener, MapCallback {

    private lateinit var viewModel: PlaceViewModel
    private var mGoogleMap: AdvancedMapFragment? = null
    private var mItem: Place? = null
    private var place: LatLng? = null
    private var placeTitle: String = ""

    @Inject
    lateinit var backupTool: BackupTool

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_place)
        initActionBar()

        mGoogleMap = AdvancedMapFragment.newInstance(false, true, false, false,
                prefs.markerStyle, themeUtil.isDark)
        mGoogleMap?.setListener(this)
        mGoogleMap?.setCallback(this)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mGoogleMap!!)
                .addToBackStack(null)
                .commit()
        loadPlace()
    }

    private fun initActionBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        backButton.setOnClickListener { finish() }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, PlaceViewModel.Factory(application, id)).get(PlaceViewModel::class.java)
        viewModel.place.observe(this, Observer{ place ->
            if (place != null) {
                showPlace(place)
            }
        })
        viewModel.result.observe(this, Observer{ commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> finish()
                    else -> {
                    }
                }
            }
        })
    }

    private fun loadPlace() {
        val intent = intent
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        initViewModel(id)
        if (intent.data != null) {
            try {
                val name = intent.data ?: return
                val scheme = name.scheme
                mItem = if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    backupTool.getPlace(cr, name)
                } else {
                    backupTool.getPlace(name.path, null)
                }
                showPlace(mItem)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private fun showPlace(place: Place?) {
        this.mItem = place
        if (place != null) {
            titleView.text = getString(R.string.edit_place)
            mGoogleMap?.setStyle(place.marker)
            mGoogleMap?.addMarker(LatLng(place.latitude, place.longitude), place.name, true, true, -1)
            placeName.setText(place.name)
        }
    }

    private fun addPlace() {
        val pl = place
        if (pl != null) {
            var name: String = placeName.text.toString().trim()
            if (name.matches("".toRegex())) {
                name = placeTitle
            }
            if (name == "" || name.matches("".toRegex())) {
                placeLayout.error = getString(R.string.must_be_not_empty)
                placeLayout.isErrorEnabled = true
                return
            }
            val latitude = pl.latitude
            val longitude = pl.longitude
            var item = mItem
            if (item != null) {
                item.name = name
                item.latitude = latitude
                item.longitude = longitude
                item.marker = mGoogleMap?.markerStyle ?: prefs.markerStyle
            } else {
                item = Place(prefs.radius, mGoogleMap?.markerStyle ?: prefs.markerStyle, latitude, longitude, name, "", ArrayList())
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
                deleteItem()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem() {
        mItem.let {
            if (it != null) {
                viewModel.deletePlace(it)
            }
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
        this.place = place
        placeTitle = address
    }

    override fun onBackClick() {

    }

    override fun onZoomClick(isFull: Boolean) {

    }

    override fun onMapReady() {
        if (mItem != null) showPlace(mItem)
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
    }
}
