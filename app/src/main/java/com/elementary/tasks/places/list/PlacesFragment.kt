package com.elementary.tasks.places.list

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.core.viewModels.places.PlacesViewModel
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import com.elementary.tasks.places.create.CreatePlaceActivity
import kotlinx.android.synthetic.main.fragment_places.*

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
class PlacesFragment : BaseSettingsFragment() {

    private lateinit var viewModel: PlacesViewModel

    private val mAdapter = PlacesRecyclerAdapter()
    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    private val searchModifier = object : SearchModifier<Place>(null, {
        mAdapter.submitList(it)
        recyclerView.smoothScrollToPosition(0)
        refreshView(it.size)
    }) {
        override fun filter(v: Place): Boolean {
            return searchValue.isEmpty() || v.name.toLowerCase().contains(searchValue.toLowerCase())
        }
    }

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            searchModifier.setSearchValue(query)
            if (mSearchMenu != null) {
                mSearchMenu?.collapseActionView()
            }
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            searchModifier.setSearchValue(newText)
            return false
        }
    }
    private val mSearchCloseListener = { false }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_trash, menu)

        menu?.findItem(R.id.action_delete_all)?.isVisible = false
        ViewUtils.tintMenuIcon(context!!, menu, 0, R.drawable.ic_twotone_search_24px, isDark)

        mSearchMenu = menu?.findItem(R.id.action_search)
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
        if (mSearchMenu != null) {
            mSearchView = mSearchMenu?.actionView as SearchView?
        }
        if (mSearchView != null) {
            if (searchManager != null) {
                mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
            }
            mSearchView?.setOnQueryTextListener(queryTextListener)
            mSearchView?.setOnCloseListener(mSearchCloseListener)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun layoutRes(): Int = R.layout.fragment_places

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener { addPlace() }
        initList()
        initViewModel()
    }

    private fun addPlace() {
        startActivity(Intent(context, CreatePlaceActivity::class.java))
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(PlacesViewModel::class.java)
        viewModel.places.observe(this, Observer { places ->
            if (places != null) {
                searchModifier.original = places
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> {
                    }
                    else -> {
                    }
                }
            }
        })
    }

    override fun getTitle(): String = getString(R.string.places)

    private fun initList() {
        if (prefs.isTwoColsEnabled && ViewUtils.isHorizontal(context!!)) {
            recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(context)
        }
        mAdapter.actionsListener = object : ActionsListener<Place> {
            override fun onAction(view: View, position: Int, t: Place?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN -> if (t != null) openPlace(t)
                    ListActions.MORE -> if (t != null) showMore(view, t)
                    else -> {
                    }
                }
            }
        }
        recyclerView.adapter = mAdapter
        ViewUtils.listenScrollableView(recyclerView) {
            setScroll(it)
        }
        refreshView(0)
    }

    private fun showMore(view: View, place: Place?) {
        Dialogues.showPopup(view, {
            if (it == 0) {
                openPlace(place!!)
            } else if (it == 1) {
                viewModel.deletePlace(place!!)
            }
        }, getString(R.string.edit), getString(R.string.delete))
    }

    private fun openPlace(place: Place) {
        startActivity(Intent(context, CreatePlaceActivity::class.java)
                .putExtra(Constants.INTENT_ID, place.id))
    }

    private fun refreshView(count: Int) {
        if (count == 0) {
            emptyItem.visibility = View.VISIBLE
        } else {
            emptyItem.visibility = View.GONE
        }
    }

    companion object {

        fun newInstance(): PlacesFragment {
            return PlacesFragment()
        }
    }
}
