package com.elementary.tasks.places.list

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.places.PlacesViewModel
import com.elementary.tasks.databinding.FragmentPlacesBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import org.koin.android.ext.android.inject
import java.io.File

class PlacesFragment : BaseSettingsFragment<FragmentPlacesBinding>() {

  private val backupTool: BackupTool by inject()
  private val viewModel: PlacesViewModel by lazy {
    ViewModelProvider(this).get(PlacesViewModel::class.java)
  }

  private val mAdapter = PlacesRecyclerAdapter()
  private var mSearchView: SearchView? = null
  private var mSearchMenu: MenuItem? = null

  private val searchModifier = object : SearchModifier<Place>(null, {
    mAdapter.submitList(it)
    binding.recyclerView.smoothScrollToPosition(0)
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

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.fragment_trash, menu)

    menu.findItem(R.id.action_delete_all)?.isVisible = false
    ViewUtils.tintMenuIcon(requireContext(), menu, 0, R.drawable.ic_twotone_search_24px, isDark)

    mSearchMenu = menu.findItem(R.id.action_search)
    val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
    if (mSearchMenu != null) {
      mSearchView = mSearchMenu?.actionView as SearchView?
    }
    if (mSearchView != null) {
      if (searchManager != null) {
        mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
      }
      mSearchView?.setOnQueryTextListener(queryTextListener)
      mSearchView?.setOnCloseListener(mSearchCloseListener)
    }
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun layoutRes(): Int = R.layout.fragment_places

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fab.setOnClickListener { addPlace() }
    initList()
    initViewModel()
  }

  private fun addPlace() {
    safeNavigation(PlacesFragmentDirections.actionPlacesFragmentToCreatePlaceActivity("", true))
  }

  private fun initViewModel() {
    viewModel.places.observe(viewLifecycleOwner, { places ->
      if (places != null) {
        searchModifier.original = places
      }
    })
    viewModel.result.observe(viewLifecycleOwner, { commands ->
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
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(resources.getInteger(R.integer.num_of_cols), StaggeredGridLayoutManager.VERTICAL)
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
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
    binding.recyclerView.adapter = mAdapter
    ViewUtils.listenScrollableView(binding.recyclerView, { setToolbarAlpha(toAlpha(it.toFloat())) }) {
      if (it) binding.fab.show()
      else binding.fab.hide()
    }
    refreshView(0)
  }

  private fun showMore(view: View, place: Place) {
    Dialogues.showPopup(view, { i ->
      when (i) {
        0 -> openPlace(place)
        1 -> sharePlace(place)
        2 -> withContext {
          dialogues.askConfirmation(it, getString(R.string.delete)) { b ->
            if (b) viewModel.deletePlace(place)
          }
        }
      }
    }, getString(R.string.edit), getString(R.string.share), getString(R.string.delete))
  }

  private fun sharePlace(place: Place) {
    launchDefault {
      val file = backupTool.placeToFile(requireContext(), place)
      withUIContext {
        if (file != null) {
          sendPlace(place, file)
        } else {
          showErrorSending()
        }
      }
    }
  }

  private fun sendPlace(place: Place, file: File) {
    if (!file.exists() || !file.canRead()) {
      showErrorSending()
      return
    }
    TelephonyUtil.sendFile(file, requireContext(), place.name)
  }

  private fun showErrorSending() {
    Toast.makeText(context, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
  }

  private fun openPlace(place: Place) {
    safeNavigation(PlacesFragmentDirections.actionPlacesFragmentToCreatePlaceActivity(place.id, true))
  }

  private fun refreshView(count: Int) {
    if (count == 0) {
      binding.emptyItem.visibility = View.VISIBLE
    } else {
      binding.emptyItem.visibility = View.GONE
    }
  }

  companion object {

    fun newInstance(): PlacesFragment {
      return PlacesFragment()
    }
  }
}
