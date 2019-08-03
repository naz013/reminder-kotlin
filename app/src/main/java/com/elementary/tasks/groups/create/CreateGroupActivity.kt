package com.elementary.tasks.groups.create

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.groups.GroupViewModel
import com.elementary.tasks.databinding.ActivityCreateGroupBinding

class CreateGroupActivity : BindingActivity<ActivityCreateGroupBinding>(R.layout.activity_create_group) {

    private lateinit var viewModel: GroupViewModel
    private var mItem: ReminderGroup? = null
    private var mUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()

        binding.colorSlider.setColors(ThemeUtil.colorsForSliderThemed(this))
        binding.colorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)

        if (savedInstanceState != null) {
            binding.colorSlider.setSelection(savedInstanceState.getInt(ARG_COLOR, 0))
        }

        loadGroup()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_COLOR, binding.colorSlider.selectedItem)
        super.onSaveInstanceState(outState)
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
        binding.toolbar.setTitle(R.string.create_group)
    }

    private fun showGroup(reminderGroup: ReminderGroup) {
        this.mItem = reminderGroup
        if (!viewModel.isEdited) {
            binding.nameInput.setText(reminderGroup.groupTitle)
            binding.colorSlider.setSelection(reminderGroup.groupColor)
            binding.defaultCheck.isEnabled = !reminderGroup.isDefaultGroup
            binding.defaultCheck.isChecked = reminderGroup.isDefaultGroup
            viewModel.isEdited = true
        }
        binding.toolbar.setTitle(R.string.change_group)
        invalidateOptionsMenu()
    }

    private fun loadGroup() {
        val id = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        initViewModel(id)
        if (intent.data != null) {
            mUri = intent.data
            readUri()
        } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
            try {
                (intent.getParcelableExtra(Constants.INTENT_ITEM) as ReminderGroup?)?.let {
                    showGroup(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readUri() {
        if (!Permissions.checkPermission(this, SD_REQ, Permissions.READ_EXTERNAL)) {
            return
        }
        mUri?.let {
            try {
                (if (ContentResolver.SCHEME_CONTENT != it.scheme) {
                    val any = MemoryUtil.decryptToJson(this, it)
                    if (any != null && any is ReminderGroup) {
                        any
                    } else {
                        null
                    }
                } else null)?.let { item -> showGroup(item) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initViewModel(id: String) {
        viewModel = ViewModelProviders.of(this, GroupViewModel.Factory(id)).get(GroupViewModel::class.java)
        viewModel.reminderGroup.observe(this, Observer { group ->
            group?.let { showGroup(it) }
        })
        viewModel.result.observe(this, Observer { commands ->
            commands?.let {
                when (it) {
                    Commands.SAVED, Commands.DELETED -> finish()
                    else -> {
                    }
                }
            }
        })
        viewModel.allGroups.observe(this, Observer { groups ->
            groups?.let { invalidateOptionsMenu() }
        })
    }

    private fun saveGroup() {
        val text = binding.nameInput.text.toString().trim()
        if (text.isEmpty()) {
            binding.nameLayout.error = getString(R.string.must_be_not_empty)
            binding.nameLayout.isErrorEnabled = true
            return
        }
        val wasDefault = mItem?.isDefaultGroup ?: false
        val item = (mItem ?: ReminderGroup()).apply {
            this.groupColor = binding.colorSlider.selectedItem
            this.groupDateTime = TimeUtil.gmtDateTime
            this.groupTitle = text
            this.isDefaultGroup = binding.defaultCheck.isChecked
        }
        viewModel.saveGroup(item, wasDefault)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_simple_save_action, menu)
        mItem?.let {
            if (!it.isDefaultGroup) {
                viewModel.allGroups.value?.let { groups ->
                    if (groups.size > 1) {
                        menu.add(Menu.NONE, MENU_ITEM_DELETE, 100, getString(R.string.delete))
                    }
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                saveGroup()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            MENU_ITEM_DELETE -> {
                dialogues.askConfirmation(this, getString(R.string.delete)) {
                    if (it) deleteItem()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteItem() {
        mItem?.let {
            viewModel.deleteGroup(it)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SD_REQ && Permissions.checkPermission(grantResults)) {
            readUri()
        }
    }

    companion object {
        private const val MENU_ITEM_DELETE = 12
        private const val SD_REQ = 555
        private const val ARG_COLOR = "arg_color"
    }
}
