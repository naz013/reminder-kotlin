package com.elementary.tasks.places.list

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.view_models.places.PlacesViewModel
import com.elementary.tasks.databinding.FragmentPlacesBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.places.create.CreatePlaceActivity
import com.elementary.tasks.reminder.lists.filters.FilterCallback
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

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
class PlacesFragment : BaseNavigationFragment(), FilterCallback<Place> {

    private var binding: FragmentPlacesBinding? = null
    private var viewModel: PlacesViewModel? = null

    private val mAdapter = PlacesRecyclerAdapter()
    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    private val filterController = PlaceFilterController(this)

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            filterController.setSearchValue(query)
            if (mSearchMenu != null) {
                mSearchMenu!!.collapseActionView()
            }
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            filterController.setSearchValue(newText)
            return false
        }
    }
    private val mSearchCloseListener = { false }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.archive_menu, menu)
        menu!!.findItem(R.id.action_delete_all).isVisible = false
        mSearchMenu = menu.findItem(R.id.action_search)
        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (mSearchMenu != null) {
            mSearchView = mSearchMenu!!.actionView as SearchView
        }
        if (mSearchView != null) {
            if (searchManager != null) {
                mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            }
            mSearchView!!.setOnQueryTextListener(queryTextListener)
            mSearchView!!.setOnCloseListener(mSearchCloseListener)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPlacesBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)
        viewModel!!.places.observe(this, { places ->
            if (places != null) {
                filterController.original = places
            }
        })
        viewModel!!.result.observe(this, { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> {
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (callback != null) {
            callback!!.onTitleChange(getString(R.string.places))
            callback!!.onFragmentSelect(this)
            callback!!.setClick { view -> startActivity(Intent(context, CreatePlaceActivity::class.java)) }
            callback!!.onScrollChanged(binding!!.recyclerView)
        }
    }

    private fun initList() {
        binding!!.recyclerView.setHasFixedSize(false)
        binding!!.recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.actionsListener = { view, position, place, actions ->
            when (actions) {
                ListActions.OPEN -> if (place != null) openPlace(place!!)
                ListActions.MORE -> showMore(place)
            }
        }
        binding!!.recyclerView.adapter = mAdapter
        refreshView()
    }

    private fun showMore(place: Place?) {
        val items = arrayOf(getString(R.string.edit), getString(R.string.delete))
        Dialogues.showLCAM(context!!, { item ->
            if (item == 0) {
                openPlace(place!!)
            } else if (item == 1) {
                viewModel!!.deletePlace(place!!)
            }
        }, *items)
    }

    private fun openPlace(place: Place) {
        startActivity(Intent(context, CreatePlaceActivity::class.java)
                .putExtra(Constants.INTENT_ID, place.id))
    }

    private fun refreshView() {
        if (mAdapter.itemCount == 0) {
            binding!!.emptyItem.visibility = View.VISIBLE
            binding!!.recyclerView.visibility = View.GONE
        } else {
            binding!!.emptyItem.visibility = View.GONE
            binding!!.recyclerView.visibility = View.VISIBLE
        }
    }

    override fun onChanged(result: List<Place>) {
        mAdapter.data = result
        binding!!.recyclerView.smoothScrollToPosition(0)
        refreshView()
    }
}
