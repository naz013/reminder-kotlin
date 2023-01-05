package com.elementary.tasks.sms.list

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.sms.UiSmsList
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.databinding.FragmentSettingsTemplatesListBinding
import com.elementary.tasks.settings.BaseSettingsFragment
import com.elementary.tasks.sms.create.TemplateActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TemplatesFragment : BaseSettingsFragment<FragmentSettingsTemplatesListBinding>() {

  private val templatesAdapter = TemplatesAdapter()
  private val viewModel by viewModel<SmsTemplatesViewModel>()

  private var mSearchView: SearchView? = null
  private var mSearchMenu: MenuItem? = null

  private val searchModifier = object : SearchModifier<UiSmsList>(null, {
    templatesAdapter.submitList(it)
    binding.templatesList.smoothScrollToPosition(0)
    refreshView()
  }) {
    override fun filter(v: UiSmsList): Boolean {
      return searchValue.isEmpty() || v.text.lowercase().contains(searchValue.lowercase())
    }
  }

  private val queryTextListener = object : SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String): Boolean {
      searchModifier.setSearchValue(query)
      mSearchMenu?.collapseActionView()
      return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
      searchModifier.setSearchValue(newText)
      return false
    }
  }

  private val mCloseListener = {
    searchModifier.setSearchValue("")
    true
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.templates_menu, menu)
    mSearchMenu = menu.findItem(R.id.action_search)
    val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager?
    if (mSearchMenu != null) {
      mSearchView = mSearchMenu?.actionView as SearchView?
    }
    if (mSearchView != null) {
      val act = activity
      if (searchManager != null && act != null) {
        mSearchView?.setSearchableInfo(searchManager.getSearchableInfo(act.componentName))
      }
      mSearchView?.setOnQueryTextListener(queryTextListener)
      mSearchView?.setOnCloseListener(mCloseListener)
    }
    super.onCreateOptionsMenu(menu, inflater)
  }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsTemplatesListBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.fab.setOnClickListener { openCreateScreen() }
    initTemplateList()
    initViewModel()
  }

  private fun initViewModel() {
    viewModel.smsTemplates.nonNullObserve(viewLifecycleOwner) { showTemplates(it) }
    lifecycle.addObserver(viewModel)
  }

  private fun openCreateScreen() {
    startActivity(Intent(context, TemplateActivity::class.java))
  }

  private fun initTemplateList() {
    binding.templatesList.layoutManager = LinearLayoutManager(context)
    templatesAdapter.actionsListener = object : ActionsListener<UiSmsList> {
      override fun onAction(view: View, position: Int, t: UiSmsList?, actions: ListActions) {
        when (actions) {
          ListActions.MORE -> if (t != null) {
            showMenu(view, t)
          }
          ListActions.OPEN -> if (t != null) {
            openTemplate(t.id)
          }
          else -> {
          }
        }
      }
    }
    binding.templatesList.adapter = templatesAdapter
    ViewUtils.listenScrollableView(binding.templatesList, { setToolbarAlpha(toAlpha(it.toFloat())) }) {
      if (it) binding.fab.show()
      else binding.fab.hide()
    }
    refreshView()
  }

  private fun showMenu(view: View, uiSmsList: UiSmsList) {
    val items = arrayOf(getString(R.string.edit), getString(R.string.delete))
    Dialogues.showPopup(view, {
      when (it) {
        0 -> openTemplate(uiSmsList.id)
        1 -> viewModel.deleteSmsTemplate(uiSmsList.id)
      }
    }, *items)
  }

  private fun openTemplate(id: String) {
    startActivity(Intent(context, TemplateActivity::class.java)
      .putExtra(Constants.INTENT_ID, id))
  }

  override fun getTitle(): String = getString(R.string.messages)

  private fun showTemplates(smsTemplates: List<UiSmsList>) {
    Timber.d("showTemplates: $smsTemplates")
    searchModifier.original = smsTemplates
  }

  private fun refreshView() {
    if (templatesAdapter.itemCount == 0) {
      binding.emptyItem.visibility = View.VISIBLE
      binding.templatesList.visibility = View.GONE
    } else {
      binding.emptyItem.visibility = View.GONE
      binding.templatesList.visibility = View.VISIBLE
    }
  }
}
