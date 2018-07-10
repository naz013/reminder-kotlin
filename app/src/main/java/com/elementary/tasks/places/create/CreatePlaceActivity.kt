package com.elementary.tasks.places.create

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.ThemedActivity
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.interfaces.MapListener
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.places.PlaceViewModel
import com.elementary.tasks.databinding.ActivityCreatePlaceBinding
import com.google.android.gms.maps.model.LatLng

import java.io.IOException
import java.util.ArrayList
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

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

    private var binding: ActivityCreatePlaceBinding? = null
    private var viewModel: PlaceViewModel? = null
    private var mGoogleMap: AdvancedMapFragment? = null
    private var mItem: Place? = null
    private var place: LatLng? = null
    private var placeTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_place)
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)

        mGoogleMap = AdvancedMapFragment.newInstance(false, false, false, false,
                prefs!!.markerStyle, themeUtil!!.isDark)
        mGoogleMap!!.setListener(this)
        mGoogleMap!!.setCallback(this)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, mGoogleMap!!)
                .addToBackStack(null)
                .commit()
        loadPlace()
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, PlaceViewModel.Factory(application, id)).get(PlaceViewModel::class.java)
        viewModel!!.place.observe(this, { place ->
            if (place != null) {
                showPlace(place)
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.SAVED, Commands.DELETED -> finish()
                }
            }
        })
    }

    private fun loadPlace() {
        val intent = intent
        val id = intent.getStringExtra(Constants.INTENT_ID)
        initViewModel(id)
        if (intent.data != null) {
            try {
                val name = intent.data
                val scheme = name!!.scheme
                if (ContentResolver.SCHEME_CONTENT == scheme) {
                    val cr = contentResolver
                    mItem = BackupTool.getInstance().getPlace(cr, name)
                } else {
                    mItem = BackupTool.getInstance().getPlace(name.path, null)
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
            mGoogleMap!!.addMarker(LatLng(place.latitude, place.longitude), place.name, true, true, -1)
            binding!!.placeName.setText(place.name)
        }
    }

    private fun addPlace() {
        if (place != null) {
            var name: String? = binding!!.placeName.text!!.toString().trim { it <= ' ' }
            if (name!!.matches("".toRegex())) {
                name = placeTitle
            }
            if (name == null || name.matches("".toRegex())) {
                binding!!.placeName.error = getString(R.string.must_be_not_empty)
                return
            }
            val latitude = place!!.latitude
            val longitude = place!!.longitude
            if (mItem != null) {
                mItem!!.name = name
                mItem!!.latitude = latitude
                mItem!!.longitude = longitude
            } else {
                mItem = Place(prefs!!.radius, 0, latitude, longitude, name, "", ArrayList())
            }
            viewModel!!.savePlace(mItem!!)
        } else {
            Toast.makeText(this, getString(R.string.you_dont_select_place), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_add -> {
                addPlace()
                return true
            }
            MENU_ITEM_DELETE -> {
                deleteItem()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem() {
        if (mItem != null) {
            viewModel!!.deletePlace(mItem!!)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mItem != null && prefs!!.isAutoSaveEnabled) {
            addPlace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_palce_edit, menu)
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

        private val MENU_ITEM_DELETE = 12
    }
}
