package com.elementary.tasks.birthdays.list

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.birthdays.list.filters.SearchModifier
import com.elementary.tasks.birthdays.list.filters.SortModifier
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.birthdays.BirthdaysViewModel
import com.elementary.tasks.databinding.FragmentBirthdaysBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class BirthdaysFragment : BaseNavigationFragment<FragmentBirthdaysBinding>(), (List<Birthday>) -> Unit {

  private val viewModel by viewModel<BirthdaysViewModel>()
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> viewModel.deleteBirthday(birthday) }
  )

  private val mAdapter = BirthdaysRecyclerAdapter(prefs) {
    filterController.original = viewModel.birthdays.value ?: listOf()
  }
  private var mSearchView: SearchView? = null
  private var mSearchMenu: MenuItem? = null

  private val sortModifier = SortModifier(null, null, prefs)
  private val filterController = SearchModifier(sortModifier, this)

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

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.fragment_active_menu, menu)

    mSearchMenu = menu.findItem(R.id.action_search)
    val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
    mSearchMenu?.let { searchMenu ->
      mSearchView = searchMenu.actionView as SearchView?
      val activity = activity
      mSearchView?.let { searchView ->
        if (searchManager != null && activity != null) {
          searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        }
        searchView.setOnQueryTextListener(queryTextListener)
        searchView.setOnCloseListener(mSearchCloseListener)
      }
    }
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun layoutRes(): Int = R.layout.fragment_birthdays

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fab.setOnClickListener { addPlace() }
    initList()
    initViewModel()
  }

  private fun addPlace() {
    withContext { AddBirthdayActivity.openLogged(it) }
  }

  private fun initViewModel() {
    viewModel.birthdays.observe(viewLifecycleOwner, { filterController.original = it })
  }

  override fun getTitle(): String = getString(R.string.birthdays)

  private fun initList() {
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(resources.getInteger(R.integer.num_of_cols),
        StaggeredGridLayoutManager.VERTICAL)
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    mAdapter.actionsListener = object : ActionsListener<Birthday> {
      override fun onAction(view: View, position: Int, t: Birthday?, actions: ListActions) {
        if (t != null) {
          birthdayResolver.resolveAction(view, t, actions)
        }
      }
    }
    binding.recyclerView.adapter = mAdapter
    ViewUtils.listenScrollableView(binding.recyclerView, { setToolbarAlpha(toAlpha(it.toFloat())) }) {
      if (it) binding.fab.show()
      else binding.fab.hide()
    }
    refreshView(0)
  }

  private fun refreshView(count: Int) {
    if (count == 0) {
      binding.emptyItem.visibility = View.VISIBLE
    } else {
      binding.emptyItem.visibility = View.GONE
    }
  }

  override fun invoke(result: List<Birthday>) {
    val newList = BirthdayAdsHolder.updateList(result)
    mAdapter.submitList(newList)
    binding.recyclerView.smoothScrollToPosition(0)
    refreshView(newList.size)
  }
}
