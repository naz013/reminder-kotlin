package com.elementary.tasks.core.utils

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.widget.SearchView

class SearchMenuHandler(filterController: (String) -> Unit) {

  private var mSearchView: SearchView? = null
  private var mSearchMenu: MenuItem? = null
  private val searchCloseListener = { false }
  private val queryTextListener = object : SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String): Boolean {
      filterController.invoke(query)
      if (mSearchMenu != null) {
        mSearchMenu?.collapseActionView()
      }
      return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
      filterController.invoke(newText)
      return false
    }
  }

  fun initSearchMenu(activity: Activity, menu: Menu, @IdRes searchItemId: Int) {
    mSearchMenu = menu.findItem(searchItemId)
    val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
    mSearchMenu?.let { searchMenu ->
      mSearchView = searchMenu.actionView as SearchView?
      mSearchView?.let { searchView ->
        if (searchManager != null) {
          searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        }
        searchView.setOnQueryTextListener(queryTextListener)
        searchView.setOnCloseListener(searchCloseListener)
      }
    }
  }
}