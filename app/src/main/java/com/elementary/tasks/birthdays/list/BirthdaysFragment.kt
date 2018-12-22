package com.elementary.tasks.birthdays.list

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.createEdit.AddBirthdayActivity
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.viewModels.birthdays.BirthdaysViewModel
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.reminder.lists.filters.FilterCallback
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
class BirthdaysFragment : BaseNavigationFragment(), FilterCallback<Birthday> {

    private lateinit var viewModel: BirthdaysViewModel
    private val birthdayResolver = BirthdayResolver(deleteAction = { birthday -> viewModel.deleteBirthday(birthday) })

    private val mAdapter = BirthdaysRecyclerAdapter()
    private var mSearchView: SearchView? = null
    private var mSearchMenu: MenuItem? = null

    private val filterController = BirthdayFilterController(this)

    private val queryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            filterController.setSearchValue(query)
            if (mSearchMenu != null) {
                mSearchMenu?.collapseActionView()
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
        inflater?.inflate(R.menu.fragment_trash, menu)
        menu?.findItem(R.id.action_delete_all)?.isVisible = false

        val searchIcon = ContextCompat.getDrawable(context!!, R.drawable.ic_twotone_search_24px)
        if (isDark) {
            val white = ContextCompat.getColor(context!!, R.color.whitePrimary)
            DrawableCompat.setTint(searchIcon!!, white)
        } else {
            val black = ContextCompat.getColor(context!!, R.color.pureBlack)
            DrawableCompat.setTint(searchIcon!!, black)
        }
        menu?.getItem(0)?.icon = searchIcon

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

    override fun layoutRes(): Int = R.layout.fragment_birthdays

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab.setOnClickListener { addPlace() }
        initList()
        initViewModel()
    }

    private fun addPlace() {
        startActivity(Intent(context, AddBirthdayActivity::class.java))
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(BirthdaysViewModel::class.java)
        viewModel.birthdays.observe(this, Observer { list ->
            if (list != null) {
                filterController.original = list
            }
        })
    }

    override fun getTitle(): String = getString(R.string.birthdays)

    private fun initList() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter.actionsListener = object : ActionsListener<Birthday> {
            override fun onAction(view: View, position: Int, t: Birthday?, actions: ListActions) {
                if (t != null) {
                    birthdayResolver.resolveAction(view, t, actions)
                }
            }
        }
        recyclerView.adapter = mAdapter
        ViewUtils.listenScrollableView(recyclerView) {
            callback?.onScrollUpdate(it)
        }
        refreshView()
    }

    private fun refreshView() {
        if (mAdapter.itemCount == 0) {
            emptyItem.visibility = View.VISIBLE
        } else {
            emptyItem.visibility = View.GONE
        }
    }

    override fun onChanged(result: List<Birthday>) {
        mAdapter.submitList(result)
        recyclerView.smoothScrollToPosition(0)
        refreshView()
    }

    companion object {

        fun newInstance(): BirthdaysFragment {
            return BirthdaysFragment()
        }
    }
}
