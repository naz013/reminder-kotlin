package com.elementary.tasks.groups.create

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.groups.GroupViewModel
import com.elementary.tasks.databinding.ActivityCreateGroupBinding
import com.elementary.tasks.navigation.settings.security.PinLoginActivity
import java.util.*

class CreateGroupActivity : BindingActivity<ActivityCreateGroupBinding>(R.layout.activity_create_group) {

    private val viewModel: GroupViewModel by lazy {
        ViewModelProviders.of(this, GroupViewModel.Factory(getId())).get(GroupViewModel::class.java)
    }
    private var mItem: ReminderGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initActionBar()

        binding.colorSlider.setColors(ThemeUtil.colorsForSliderThemed(this))
        binding.colorSlider.setSelectorColorResource(if (isDarkMode) R.color.pureWhite else R.color.pureBlack)

        if (savedInstanceState != null) {
            binding.colorSlider.setSelection(savedInstanceState.getInt(ARG_COLOR, 0))
        } else {
            viewModel.isLogged = intent.getBooleanExtra(ARG_LOGGED, false)
        }

        loadGroup()
    }

    override fun onStart() {
        super.onStart()
        if (prefs.hasPinCode && !viewModel.isLogged) {
            PinLoginActivity.verify(this)
        }
    }

    private fun getId(): String = intent.getStringExtra(Constants.INTENT_ID) ?: ""

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

    private fun showGroup(reminderGroup: ReminderGroup, fromFile: Boolean = false) {
        this.mItem = reminderGroup
        if (!viewModel.isEdited) {
            binding.nameInput.setText(reminderGroup.groupTitle)
            binding.colorSlider.setSelection(reminderGroup.groupColor)
            binding.defaultCheck.isEnabled = !reminderGroup.isDefaultGroup
            binding.defaultCheck.isChecked = reminderGroup.isDefaultGroup
            viewModel.isEdited = true
            viewModel.isFromFile = fromFile
            if (fromFile) {
                viewModel.findSame(reminderGroup.groupUuId)
            }
        }
        binding.toolbar.setTitle(R.string.change_group)
        invalidateOptionsMenu()
    }

    private fun loadGroup() {
        initViewModel()
        if (intent.data != null) {
            readUri()
        } else if (intent.hasExtra(Constants.INTENT_ITEM)) {
            try {
                (intent.getParcelableExtra(Constants.INTENT_ITEM) as ReminderGroup?)?.let {
                    showGroup(it, true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun readUri() {
        if (!Permissions.checkPermission(this, SD_REQ, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            return
        }
        intent.data?.let {
            try {
                (if (ContentResolver.SCHEME_CONTENT != it.scheme) {
                    val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_GROUP)
                    if (any != null && any is ReminderGroup) {
                        any
                    } else {
                        null
                    }
                } else null)?.let { item -> showGroup(item, true) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initViewModel() {
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

    private fun saveGroup(newId: Boolean = false) {
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
        if (newId) {
            item.groupUuId = UUID.randomUUID().toString()
        }
        viewModel.saveGroup(item, wasDefault)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_simple_save_action, menu)
        mItem?.let {
            if (!it.isDefaultGroup && !viewModel.isFromFile) {
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
                askCopySaving()
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

    private fun askCopySaving() {
        if (viewModel.isFromFile && viewModel.hasSameInDb) {
            dialogues.getMaterialDialog(this)
                    .setMessage(R.string.same_group_message)
                    .setPositiveButton(R.string.keep) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        saveGroup(true)
                    }
                    .setNegativeButton(R.string.replace) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        saveGroup()
                    }
                    .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()
        } else {
            saveGroup()
        }
    }

    private fun deleteItem() {
        mItem?.let {
            viewModel.deleteGroup(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PinLoginActivity.REQ_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                finish()
            } else {
                viewModel.isLogged = true
            }
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
        private const val ARG_LOGGED = "arg_logged"

        fun openLogged(context: Context, intent: Intent? = null) {
            if (intent == null) {
                context.startActivity(Intent(context, CreateGroupActivity::class.java)
                        .putExtra(ARG_LOGGED, true))
            } else {
                intent.putExtra(ARG_LOGGED, true)
                context.startActivity(intent)
            }
        }
    }
}
