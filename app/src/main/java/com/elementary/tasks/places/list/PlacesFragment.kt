package com.elementary.tasks.places.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.data.models.ShareFile
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.places.PlacesViewModel
import com.elementary.tasks.databinding.FragmentPlacesBinding
import com.elementary.tasks.settings.BaseSettingsFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlacesFragment : BaseSettingsFragment<FragmentPlacesBinding>() {

  private val viewModel by viewModel<PlacesViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()

  private val adapter = PlacesRecyclerAdapter(currentStateHolder, object : ActionsListener<Place> {
    override fun onAction(view: View, position: Int, t: Place?, actions: ListActions) {
      if (t == null) return
      when (actions) {
        ListActions.OPEN -> openPlace(t)
        ListActions.MORE -> showMore(view, t)
        else -> {
        }
      }
    }
  })

  private val searchModifier = object : SearchModifier<Place>(null, {
    adapter.submitList(it)
    binding.recyclerView.smoothScrollToPosition(0)
    refreshView(it.size)
  }) {
    override fun filter(v: Place): Boolean {
      return searchValue.isEmpty() || v.name.lowercase().contains(searchValue.lowercase())
    }
  }
  private val searchMenuHandler = SearchMenuHandler(systemServiceProvider.provideSearchManager()) {
    searchModifier.setSearchValue(it)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.fragment_trash, menu)

    menu.findItem(R.id.action_delete_all)?.isVisible = false
    ViewUtils.tintMenuIcon(requireContext(), menu, 0, R.drawable.ic_twotone_search_24px, isDark)

    searchMenuHandler.initSearchMenu(requireActivity(), menu, R.id.action_search)
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentPlacesBinding.inflate(inflater, container, false)

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
    viewModel.places.observe(viewLifecycleOwner) { places ->
      if (places != null) {
        searchModifier.original = places
      }
    }
    viewModel.result.observe(viewLifecycleOwner) {
      when (it) {
        Commands.DELETED -> {
        }
        else -> {
        }
      }
    }
    viewModel.shareFile.observe(viewLifecycleOwner) {
      sendPlace(it)
    }
  }

  override fun getTitle(): String = getString(R.string.places)

  private fun initList() {
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(resources.getInteger(R.integer.num_of_cols), StaggeredGridLayoutManager.VERTICAL)
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    binding.recyclerView.adapter = adapter
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
        1 -> viewModel.sharePlace(place)
        2 -> withContext {
          dialogues.askConfirmation(it, getString(R.string.delete)) { b ->
            if (b) viewModel.deletePlace(place)
          }
        }
      }
    }, getString(R.string.edit), getString(R.string.share), getString(R.string.delete))
  }

  private fun sendPlace(shareFile: ShareFile<Place>) {
    if (shareFile.file == null || !shareFile.file.exists() || !shareFile.file.canRead()) {
      showErrorSending()
      return
    }
    TelephonyUtil.sendFile(shareFile.file, requireContext(), shareFile.item.name)
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
