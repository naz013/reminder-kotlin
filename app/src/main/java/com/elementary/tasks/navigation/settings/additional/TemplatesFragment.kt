package com.elementary.tasks.navigation.settings.additional

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
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.filter.SearchModifier
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Dialogues
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ViewUtils
import com.elementary.tasks.core.view_models.sms_templates.SmsTemplatesViewModel
import com.elementary.tasks.databinding.FragmentSettingsTemplatesListBinding
import com.elementary.tasks.navigation.settings.BaseSettingsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class TemplatesFragment : BaseSettingsFragment<FragmentSettingsTemplatesListBinding>() {

  private val adapter = TemplatesAdapter()
  private val viewModel by viewModel<SmsTemplatesViewModel>()

  private var mSearchView: SearchView? = null
  private var mSearchMenu: MenuItem? = null

  private val searchModifier = object : SearchModifier<SmsTemplate>(null, {
    adapter.data = it
    binding.templatesList.smoothScrollToPosition(0)
    refreshView()
  }) {
    override fun filter(v: SmsTemplate): Boolean {
      return searchValue.isEmpty() || v.title.toLowerCase().contains(searchValue.toLowerCase())
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
    viewModel.smsTemplates.observe(viewLifecycleOwner, { smsTemplates ->
      if (smsTemplates != null) {
        showTemplates(smsTemplates)
      }
    })
  }

  private fun openCreateScreen() {
    startActivity(Intent(context, TemplateActivity::class.java))
  }

  private fun initTemplateList() {
    binding.templatesList.layoutManager = LinearLayoutManager(context)
    adapter.actionsListener = object : ActionsListener<SmsTemplate> {
      override fun onAction(view: View, position: Int, t: SmsTemplate?, actions: ListActions) {
        when (actions) {
          ListActions.MORE -> if (t != null) {
            showMenu(view, t)
          }
          ListActions.OPEN -> if (t != null) {
            openTemplate(t)
          }
          else -> {
          }
        }
      }
    }
    binding.templatesList.adapter = adapter
    ViewUtils.listenScrollableView(binding.templatesList, { setToolbarAlpha(toAlpha(it.toFloat())) }) {
      if (it) binding.fab.show()
      else binding.fab.hide()
    }
    refreshView()
  }

  private fun showMenu(view: View, smsTemplate: SmsTemplate) {
    val items = arrayOf(getString(R.string.edit), getString(R.string.delete))
    Dialogues.showPopup(view, {
      when (it) {
        0 -> openTemplate(smsTemplate)
        1 -> deleteTemplate(smsTemplate)
      }
    }, *items)
  }

  private fun openTemplate(smsTemplate: SmsTemplate) {
    startActivity(Intent(context, TemplateActivity::class.java)
      .putExtra(Constants.INTENT_ID, smsTemplate.key))
  }

  private fun deleteTemplate(smsTemplate: SmsTemplate) {
    viewModel.deleteSmsTemplate(smsTemplate)
  }

  override fun getTitle(): String = getString(R.string.messages)

  private fun showTemplates(smsTemplates: List<SmsTemplate>) {
    searchModifier.original = smsTemplates
  }

  private fun refreshView() {
    if (adapter.itemCount == 0) {
      binding.emptyItem.visibility = View.VISIBLE
      binding.templatesList.visibility = View.GONE
    } else {
      binding.emptyItem.visibility = View.GONE
      binding.templatesList.visibility = View.VISIBLE
    }
  }
}
