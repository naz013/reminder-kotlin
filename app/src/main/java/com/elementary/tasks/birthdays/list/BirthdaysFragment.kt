package com.elementary.tasks.birthdays.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.birthdays.list.filters.SearchModifier
import com.elementary.tasks.birthdays.list.filters.SortModifier
import com.elementary.tasks.core.analytics.Screen
import com.elementary.tasks.core.analytics.ScreenUsedEvent
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentBirthdaysBinding
import com.elementary.tasks.navigation.fragments.BaseNavigationFragment
import com.elementary.tasks.pin.PinLoginActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class BirthdaysFragment : BaseNavigationFragment<FragmentBirthdaysBinding>(),
    (List<UiBirthdayList>) -> Unit {

  private val viewModel by viewModel<BirthdaysViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> viewModel.deleteBirthday(birthday.uuId) }
  )
  private val mAdapter = BirthdaysRecyclerAdapter()
  private val filterController = SearchModifier(SortModifier(), this)
  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search
  ) {
    filterController.setSearchValue(it)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentBirthdaysBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    addMenu(R.menu.fragment_active_menu, { false }) {
      searchMenuHandler.initSearchMenu(requireActivity(), it, R.id.action_search)
    }
    binding.fab.setOnClickListener { addPlace() }
    initList()
    initViewModel()

    analyticsEventSender.send(ScreenUsedEvent(Screen.BIRTHDAYS))
  }

  private fun addPlace() {
    withContext { PinLoginActivity.openLogged(it, AddBirthdayActivity::class.java) }
  }

  private fun initViewModel() {
    viewModel.birthdays.observe(viewLifecycleOwner) { filterController.original = it }
  }

  override fun getTitle(): String = getString(R.string.birthdays)

  private fun initList() {
    if (resources.getBoolean(R.bool.is_tablet)) {
      binding.recyclerView.layoutManager = StaggeredGridLayoutManager(
        resources.getInteger(R.integer.num_of_cols),
        StaggeredGridLayoutManager.VERTICAL
      )
    } else {
      binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    mAdapter.actionsListener = object : ActionsListener<UiBirthdayList> {
      override fun onAction(view: View, position: Int, t: UiBirthdayList?, actions: ListActions) {
        if (t != null) {
          birthdayResolver.resolveAction(view, t, actions)
        }
      }
    }
    binding.recyclerView.adapter = mAdapter
    ViewUtils.listenScrollableView(
      binding.recyclerView,
      { setToolbarAlpha(toAlpha(it.toFloat())) }) {
      if (it) binding.fab.show()
      else binding.fab.hide()
    }
  }

  override fun invoke(result: List<UiBirthdayList>) {
    mAdapter.submitList(result)
    binding.recyclerView.smoothScrollToPosition(0)
    binding.emptyItem.visibleGone(result.isEmpty())
  }
}
