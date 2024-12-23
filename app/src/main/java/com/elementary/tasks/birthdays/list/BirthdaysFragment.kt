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
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.github.naz013.ui.common.view.ViewUtils
import com.elementary.tasks.databinding.FragmentBirthdaysBinding
import com.elementary.tasks.home.eventsview.BaseSubEventsFragment
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.login.LoginApi
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.visibleGone
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class BirthdaysFragment : BaseSubEventsFragment<FragmentBirthdaysBinding>() {

  private val viewModel by viewModel<BirthdaysViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> viewModel.deleteBirthday(birthday.uuId) }
  )
  private val mAdapter = BirthdaysRecyclerAdapter()
  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search
  ) { viewModel.onSearchUpdate(it) }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentBirthdaysBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()
    addMenu(R.menu.fragment_birthdays, { false }) {
      searchMenuHandler.initSearchMenu(requireActivity(), it, R.id.action_search)
    }
    binding.fab.setOnClickListener { addNew() }
    initList()
    initViewModel()

    analyticsEventSender.send(ScreenUsedEvent(Screen.BIRTHDAYS))
  }

  private fun addNew() {
    withContext { LoginApi.openLogged(it, AddBirthdayActivity::class.java) }
  }

  private fun initViewModel() {
    viewModel.birthdays.nonNullObserve(viewLifecycleOwner) {
      mAdapter.submitList(it)
      binding.recyclerView.smoothScrollToPosition(0)
      binding.emptyItem.visibleGone(it.isEmpty())
    }
  }

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
    ViewUtils.listenScrollableView(binding.recyclerView) {
      if (it) {
        binding.fab.show()
      } else {
        binding.fab.hide()
      }
    }
  }
}
