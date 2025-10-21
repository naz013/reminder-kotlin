package com.elementary.tasks.birthdays.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.BirthdayResolver
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.databinding.FragmentBirthdaysBinding
import com.elementary.tasks.home.eventsview.BaseSubEventsFragment
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.Module
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.livedata.nonNullObserve
import com.github.naz013.ui.common.view.ViewUtils
import com.github.naz013.ui.common.view.applyBottomInsets
import com.github.naz013.ui.common.view.visibleGone
import org.koin.androidx.viewmodel.ext.android.viewModel

class BirthdaysFragment : BaseSubEventsFragment<FragmentBirthdaysBinding>() {

  private val viewModel by viewModel<BirthdaysViewModel>()
  private val birthdayResolver = BirthdayResolver(
    dialogAction = { dialogues },
    deleteAction = { birthday -> viewModel.deleteBirthday(birthday.uuId) },
    birthdayEditAction = {
      navigate {
        navigate(
          R.id.editBirthdayFragment,
          Bundle().apply {
            putString(IntentKeys.INTENT_ID, it.uuId)
          }
        )
      }
    },
    birthdayOpenAction = {
      navigate {
        navigate(
          R.id.previewBirthdayFragment,
          Bundle().apply {
            putString(IntentKeys.INTENT_ID, it.uuId)
          }
        )
      }
    }
  )
  private val mAdapter = BirthdaysRecyclerAdapter()

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentBirthdaysBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerView.applyBottomInsets()
    binding.fab.setOnClickListener { addNew() }

    fragmentMenuController?.removeMenu()

    binding.birthdaySearchBar.doAfterTextChanged {
      viewModel.onSearchUpdate(it?.toString().orEmpty())
    }

    initList()
    initViewModel()

    analyticsEventSender.send(ScreenUsedEvent(Screen.BIRTHDAYS))
  }

  private fun addNew() {
    navigate { navigate(R.id.editBirthdayFragment) }
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.birthdays.nonNullObserve(viewLifecycleOwner) {
      mAdapter.submitList(it)
      binding.recyclerView.smoothScrollToPosition(0)
      binding.emptyItem.visibleGone(it.isEmpty())
    }
    viewModel.canSearch.observe(viewLifecycleOwner) {
      binding.birthdaySearchBar.visibleGone(it == true)
    }
  }

  private fun initList() {
    if (Module.isTablet(requireContext())) {
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
