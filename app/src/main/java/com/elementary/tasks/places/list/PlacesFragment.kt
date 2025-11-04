package com.elementary.tasks.places.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.ShareFile
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.databinding.FragmentPlacesBinding
import com.elementary.tasks.navigation.NavigationAnimations
import com.elementary.tasks.navigation.fragments.BaseSettingsFragment
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.fragment.toast
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.applyBottomInsetsMargin
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlacesFragment : BaseSettingsFragment<FragmentPlacesBinding>() {

  private val viewModel by viewModel<PlacesViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()

  private val adapter = PlacesRecyclerAdapter(object : ActionsListener<UiPlaceList> {
    override fun onAction(view: View, position: Int, t: UiPlaceList?, actions: ListActions) {
      if (t == null) return
      when (actions) {
        ListActions.OPEN -> openPlace(t)
        ListActions.MORE -> showMore(view, t)
        else -> {
        }
      }
    }
  })

  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search_place
  ) { viewModel.onSearchUpdate(it) }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentPlacesBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()
    binding.fab.applyBottomInsetsMargin()

    addMenu(R.menu.fragment_reminders_archive, { false }) {
      it.findItem(R.id.action_delete_all)?.isVisible = false
      ViewUtils.tintMenuIcon(requireContext(), it, 0, R.drawable.ic_fluent_search, isDark)
      searchMenuHandler.initSearchMenu(requireActivity(), it, R.id.action_search)
    }
    binding.fab.setOnClickListener { addPlace() }
    initList()
    initViewModel()
  }

  private fun addPlace() {
    navigate {
      navigate(R.id.editPlaceFragment, null, NavigationAnimations.inDepthNavOptions())
    }
  }

  private fun initViewModel() {
    viewModel.places.nonNullObserve(viewLifecycleOwner) { places ->
      adapter.submitList(places)
      binding.recyclerView.smoothScrollToPosition(0)
      refreshView(places.size)
    }
    viewModel.resultEvent.observeEvent(viewLifecycleOwner) {
      when (it) {
        Commands.DELETED -> {
          toast(R.string.deleted)
        }

        else -> {
        }
      }
    }
    viewModel.shareFile.nonNullObserve(viewLifecycleOwner) {
      sendPlace(it)
    }
  }

  override fun getTitle(): String = getString(R.string.places)

  private fun initList() {
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(
        resources.getInteger(R.integer.num_of_cols),
        StaggeredGridLayoutManager.VERTICAL
      )
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
    binding.recyclerView.adapter = adapter
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) {
        binding.fab.show()
      } else {
        binding.fab.hide()
      }
    }
    refreshView(0)
  }

  private fun showMore(view: View, place: UiPlaceList) {
    Dialogues.showPopup(view, { i ->
      when (i) {
        0 -> openPlace(place)
        1 -> viewModel.sharePlace(place.id)
        2 -> withContext {
          dialogues.askConfirmation(it, getString(R.string.delete)) { b ->
            if (b) viewModel.deletePlace(place.id)
          }
        }
      }
    }, getString(R.string.edit), getString(R.string.share), getString(R.string.delete))
  }

  private fun sendPlace(shareFile: ShareFile<UiPlaceList>) {
    if (shareFile.file == null || !shareFile.file.exists() || !shareFile.file.canRead()) {
      showErrorSending()
      return
    }
    TelephonyUtil.sendFile(shareFile.file, requireContext(), shareFile.item.name)
  }

  private fun showErrorSending() {
    toast(R.string.error_sending)
  }

  private fun openPlace(place: UiPlaceList) {
    navigate {
      navigate(
        R.id.editPlaceFragment,
        Bundle().apply {
          putString(IntentKeys.INTENT_ID, place.id)
        },
        NavigationAnimations.inDepthNavOptions()
      )
    }
  }

  private fun refreshView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
  }

  companion object {

    fun newInstance(): PlacesFragment {
      return PlacesFragment()
    }
  }
}
