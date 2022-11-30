package com.elementary.tasks.core.apps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.databinding.ActivityApplicationListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectApplicationActivity : BindingActivity<ActivityApplicationListBinding>() {

  private val viewModel by viewModel<SelectApplicationViewModel>()
  private var adapter: AppsRecyclerAdapter = AppsRecyclerAdapter()
  private val searchModifier = object : SearchModifier<ApplicationItem>(
    null, {
      adapter.submitList(it)
      binding.contactsList.smoothScrollToPosition(0)
      refreshView(it.size)
  }) {
    override fun filter(v: ApplicationItem): Boolean {
      return searchValue.isEmpty() || (v.name
        ?: "").lowercase().contains(searchValue.lowercase())
    }
  }

  override fun inflateBinding() = ActivityApplicationListBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.packageManager = packageManager
    viewModel.loadApps()

    binding.loaderView.visibility = View.GONE
    initActionBar()
    initSearchView()
    initRecyclerView()
  }

  override fun onStart() {
    super.onStart()
    viewModel.applications.observe(this) { applications ->
      applications?.let { searchModifier.original = it }
    }
    viewModel.isLoading.observe(this) { isLoading ->
      isLoading?.let {
        if (it) {
          showProgress()
        } else {
          hideProgress()
        }
      }
    }
  }

  private fun hideProgress() {
    binding.loaderView.visibility = View.GONE
  }

  private fun showProgress() {
    binding.loaderView.visibility = View.VISIBLE
  }

  private fun initRecyclerView() {
    adapter.actionsListener = object : ActionsListener<ApplicationItem> {
      override fun onAction(view: View, position: Int, t: ApplicationItem?, actions: ListActions) {
        if (t != null) {
          val intent = Intent()
          intent.putExtra(Constants.SELECTED_APPLICATION, t.packageName)
          setResult(RESULT_OK, intent)
          finish()
        }
      }
    }
    binding.contactsList.layoutManager = LinearLayoutManager(this)
    binding.contactsList.adapter = adapter
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
    binding.backButton.setOnClickListener { onBackPressed() }
  }

  override fun onPause() {
    super.onPause()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.hideSoftInputFromWindow(binding.searchField.windowToken, 0)
  }

  private fun refreshView(count: Int) {
    if (count > 0) {
      binding.emptyItem.visibility = View.GONE
      binding.scroller.visibility = View.VISIBLE
    } else {
      binding.scroller.visibility = View.GONE
      binding.emptyItem.visibility = View.VISIBLE
    }
  }

  override fun onBackPressed() {
    val intent = Intent()
    setResult(RESULT_CANCELED, intent)
    finish()
  }
}
