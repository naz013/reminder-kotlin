package com.elementary.tasks.core.apps

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.apps.filter.SearchModifier
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.dp2px
import com.elementary.tasks.core.utils.ui.listenScrollableView
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.views.SpaceItemDecoration
import com.elementary.tasks.databinding.ActivityApplicationListBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectApplicationActivity : BindingActivity<ActivityApplicationListBinding>() {

  private val viewModel by viewModel<SelectApplicationViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()

  private var appsRecyclerAdapter = AppsRecyclerAdapter()
  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search
  ) { searchModifier.setSearchValue(it) }
  private val searchModifier = object : SearchModifier<UiApplicationList>(
    null, {
      appsRecyclerAdapter.submitList(it)
      binding.listView.smoothScrollToPosition(0)
      refreshView(it.size)
  }) {
    override fun filter(v: UiApplicationList): Boolean {
      return searchValue.isEmpty() || (v.name
        ?: "").lowercase().contains(searchValue.lowercase())
    }
  }

  override fun inflateBinding() = ActivityApplicationListBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding.progressView.gone()
    initActionBar()
    initRecyclerView()
  }

  override fun onStart() {
    super.onStart()
    lifecycle.addObserver(viewModel)
    viewModel.applications.nonNullObserve(this) { searchModifier.original = it }
    viewModel.isInProgress.nonNullObserve(this) {
      if (it) {
        showProgress()
      } else {
        hideProgress()
      }
    }
  }

  private fun hideProgress() {
    binding.progressView.gone()
  }

  private fun showProgress() {
    binding.progressView.visible()
  }

  private fun initRecyclerView() {
    appsRecyclerAdapter.actionsListener = object : ActionsListener<UiApplicationList> {
      override fun onAction(view: View, position: Int, t: UiApplicationList?, actions: ListActions) {
        if (t != null) {
          val intent = Intent()
          intent.putExtra(Constants.SELECTED_APPLICATION, t.packageName)
          setResult(RESULT_OK, intent)
          finish()
        }
      }
    }
    val layoutManager = LinearLayoutManager(this)
    binding.listView.addItemDecoration(SpaceItemDecoration(dp2px(16)))
    binding.listView.layoutManager = layoutManager
    binding.listView.adapter = appsRecyclerAdapter

    binding.listView.listenScrollableView {
      binding.toolbarView.isSelected = it > 0
    }
  }

  private fun initActionBar() {
    binding.toolbar.setNavigationOnClickListener { handleBackPress() }
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also {
      searchMenuHandler.initSearchMenu(this, it, R.id.action_search)
    }
  }

  private fun refreshView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
    binding.listView.visibleGone(count != 0)
  }

  override fun handleBackPress(): Boolean {
    val intent = Intent()
    setResult(RESULT_CANCELED, intent)
    finish()
    return true
  }
}
