package com.elementary.tasks.core.utils.ui

import android.app.Activity
import android.app.SearchManager
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView

class SearchMenuHandler(
  private val searchManager: SearchManager?,
  @StringRes private val hintRes: Int?,
  filterController: (String) -> Unit
) {

  private var mSearchView: SearchView? = null
  private var mSearchMenu: MenuItem? = null
  private val searchCloseListener = {
    filterController.invoke("")
    false
  }
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
    mSearchMenu?.also { searchMenu ->
      mSearchView = searchMenu.actionView as SearchView?
      mSearchView?.also { searchView ->
        hintRes?.also { searchView.queryHint = searchView.context.getString(it) }
        searchManager?.also { searchManager ->
          runCatching {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
          }
        }
        searchView.setOnQueryTextListener(queryTextListener)
        searchView.setOnCloseListener(searchCloseListener)
      }
    }
  }
}
