package com.elementary.tasks.sms.create

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.SmsTemplate
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.ui.trimmedText
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.databinding.ActivityTemplateBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class TemplateActivity : BindingActivity<ActivityTemplateBinding>() {

  private val viewModel by viewModel<CreateSmsTemplateViewModel> { parametersOf(getId()) }
  private val permissionFlow = PermissionFlow(this, dialogues)

  override fun inflateBinding() = ActivityTemplateBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initActionBar()
    initViewModel()
    loadTemplate()
  }

  private fun getId(): String = intentString(Constants.INTENT_ID)

  private fun loadTemplate() {
    if (intent.data != null) {
      permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
        readUri()
      }
    } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
      runCatching {
        viewModel.loadFromIntent(intentParcelable(Constants.INTENT_ITEM, SmsTemplate::class.java))
      }
    }
  }

  private fun readUri() {
    intent.data?.also { viewModel.loadFromFile(it) }
  }

  private fun initViewModel() {
    viewModel.smsTemplate.nonNullObserve(this) { showTemplate(it) }
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.SAVED, Commands.DELETED -> finish()
        else -> {
        }
      }
    }
    lifecycle.addObserver(viewModel)
  }

  private fun showTemplate(smsTemplate: SmsTemplate) {
    binding.toolbar.title = getString(R.string.edit_template)
    binding.messageInput.setText(smsTemplate.title)
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_add -> {
        askCopySaving()
        return true
      }
      android.R.id.home -> {
        finish()
        return true
      }
      MENU_ITEM_DELETE -> {
        viewModel.deleteSmsTemplate()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  private fun askCopySaving() {
    if (viewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(this)
        .setMessage(R.string.same_template_message)
        .setPositiveButton(R.string.keep) { dialogInterface, _ ->
          dialogInterface.dismiss()
          saveTemplate(true)
        }
        .setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          saveTemplate()
        }
        .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }
        .create()
        .show()
    } else {
      saveTemplate()
    }
  }

  private fun saveTemplate(newId: Boolean = false) {
    val text = binding.messageInput.trimmedText()
    if (text.isEmpty()) {
      binding.messageLayout.error = getString(R.string.must_be_not_empty)
      binding.messageLayout.isErrorEnabled = true
      return
    }
    viewModel.saveTemplate(text, newId)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_create_template, menu)
    if (viewModel.canDelete()) {
      menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
    }
    return true
  }

  companion object {
    private const val MENU_ITEM_DELETE = 12
  }
}
