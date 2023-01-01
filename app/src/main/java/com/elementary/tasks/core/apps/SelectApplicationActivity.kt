package com.elementary.tasks.core.apps

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.InputMethodManagerWrapper
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.databinding.ActivityApplicationListBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectApplicationActivity : BindingActivity<ActivityApplicationListBinding>() {

  private val viewModel by viewModel<SelectApplicationViewModel>()
  private val inputMethodManagerWrapper by inject<InputMethodManagerWrapper>()

  private var appsRecyclerAdapter = AppsRecyclerAdapter()
  private val searchModifier = object : SearchModifier<UiApplicationList>(
    null, {
      appsRecyclerAdapter.submitList(it)
      binding.contactsList.smoothScrollToPosition(0)
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
    binding.loaderView.gone()
    initActionBar()
    initSearchView()
    initRecyclerView()
  }

  override fun onStart() {
    super.onStart()
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
    binding.loaderView.gone()
  }

  private fun showProgress() {
    binding.loaderView.visible()
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
    binding.contactsList.layoutManager = LinearLayoutManager(this)
    binding.contactsList.adapter = appsRecyclerAdapter
    binding.contactsList.isNestedScrollingEnabled = false
    ViewUtils.listenScrollableView(binding.scroller) {
      binding.toolbarView.isSelected = it > 0
    }
  }

  private fun initSearchView() {
    binding.searchField.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            searchModifier.setSearchValue(s.toString())
        }

        override fun afterTextChanged(s: Editable) {
        }
    })
  }

  private fun initActionBar() {
    binding.backButton.setOnClickListener { handleBackPress() }
  }

  override fun onPause() {
    super.onPause()
    inputMethodManagerWrapper.hideKeyboard(binding.searchField)
  }

  private fun refreshView(count: Int) {
    if (count > 0) {
      binding.emptyItem.gone()
      binding.scroller.visible()
    } else {
      binding.scroller.gone()
      binding.emptyItem.visible()
    }
  }

  override fun handleBackPress(): Boolean {
    val intent = Intent()
    setResult(RESULT_CANCELED, intent)
    finish()
    return true
  }
}
