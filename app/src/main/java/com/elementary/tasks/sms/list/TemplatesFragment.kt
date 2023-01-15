package com.elementary.tasks.sms.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.R
import com.elementary.tasks.core.data.ui.sms.UiSmsList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.Dialogues
import com.elementary.tasks.core.utils.ui.SearchMenuHandler
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.FragmentSettingsTemplatesListBinding
import com.elementary.tasks.settings.BaseSettingsFragment
import com.elementary.tasks.sms.create.TemplateActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class TemplatesFragment : BaseSettingsFragment<FragmentSettingsTemplatesListBinding>() {

  private val templatesAdapter = TemplatesAdapter()
  private val viewModel by viewModel<SmsTemplatesViewModel>()
  private val systemServiceProvider by inject<SystemServiceProvider>()

  private val searchMenuHandler = SearchMenuHandler(
    systemServiceProvider.provideSearchManager(),
    R.string.search
  ) { viewModel.onSearchUpdate(it) }

  override fun inflate(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ) = FragmentSettingsTemplatesListBinding.inflate(inflater, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    addMenu(R.menu.templates_menu, onMenuItemListener = { false }) { menu ->
      searchMenuHandler.initSearchMenu(requireActivity(), menu, R.id.action_search)
    }
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
    refreshView(0)
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
    templatesAdapter.submitList(smsTemplates)
    binding.templatesList.smoothScrollToPosition(0)
    refreshView(smsTemplates.size)
  }

  private fun refreshView(count: Int) {
    binding.emptyItem.visibleGone(count == 0)
    binding.templatesList.visibleGone(count != 0)
  }
}
