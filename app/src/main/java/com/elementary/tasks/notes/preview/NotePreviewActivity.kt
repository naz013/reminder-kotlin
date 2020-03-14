package com.elementary.tasks.notes.preview

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.notes.NoteViewModel
import com.elementary.tasks.core.views.GridMarginDecoration
import com.elementary.tasks.databinding.ActivityNotePreviewBinding
import com.elementary.tasks.notes.create.CreateNoteActivity
import com.elementary.tasks.notes.list.ImagesGridAdapter
import com.elementary.tasks.notes.list.KeepLayoutManager
import com.elementary.tasks.reminder.create.CreateReminderActivity
import org.koin.android.ext.android.inject
import java.io.File

class NotePreviewActivity : BindingActivity<ActivityNotePreviewBinding>(R.layout.activity_note_preview) {

    private var mNote: NoteWithImages? = null
    private var mReminder: Reminder? = null
    private var mId: String = ""
    private var isBgDark = false

    private val mAdapter = ImagesGridAdapter()
    private lateinit var viewModel: NoteViewModel

    private val mUiHandler = Handler(Looper.getMainLooper())

    private val themeUtil: ThemeUtil by inject()
    private val backupTool: BackupTool by inject()
    private val imagesSingleton: ImagesSingleton by inject()
    private val adsProvider = AdsProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isBgDark = isDarkMode
        mId = intent.getStringExtra(Constants.INTENT_ID) ?: ""
        initActionBar()
        updateTextColors()
        initImagesList()
        initReminderCard()
        initViewModel()
        if (!Module.isPro) {
            binding.adsCard.show()
            adsProvider.showBanner(
                    binding.adsHolder,
                    AdsProvider.NOTE_BANNER_ID,
                    R.layout.list_item_ads_hor
            ) {
                binding.adsCard.show()
            }
        } else {
            binding.adsCard.show()
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, NoteViewModel.Factory(mId)).get(NoteViewModel::class.java)
        viewModel.note.observe(this, Observer { note ->
            if (note != null) {
                showNote(note)
            }
        })
        viewModel.reminder.observe(this, Observer { reminder ->
            if (reminder != null) {
                showReminder(reminder)
            } else {
                this.mReminder = null
                binding.reminderContainer.visibility = View.GONE
            }
        })
        viewModel.result.observe(this, Observer { commands ->
            if (commands != null) {
                when (commands) {
                    Commands.DELETED -> closeWindow()
                    else -> {
                    }
                }
            }
        })
    }

    private fun initReminderCard() {
        binding.reminderContainer.visibility = View.GONE
        binding.editReminder.setOnClickListener { editReminder() }
        binding.deleteReminder.setOnClickListener { showReminderDeleteDialog() }
    }

    private fun editReminder() {
        if (mReminder != null) {
            CreateReminderActivity.openLogged(this, Intent(this, CreateReminderActivity::class.java)
                    .putExtra(Constants.INTENT_ID, mReminder?.uuId))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter.actionsListener = null
    }

    private fun initImagesList() {
        mAdapter.actionsListener = object : ActionsListener<ImageFile> {
            override fun onAction(view: View, position: Int, t: ImageFile?, actions: ListActions) {
                when (actions) {
                    ListActions.OPEN -> openImagePreview(position)
                    else -> {
                    }
                }
            }
        }
        binding.imagesList.layoutManager = KeepLayoutManager(this, 6, mAdapter)
        binding.imagesList.addItemDecoration(GridMarginDecoration(resources.getDimensionPixelSize(R.dimen.grid_item_spacing)))
        binding.imagesList.adapter = mAdapter
    }

    private fun openImagePreview(position: Int) {
        imagesSingleton.setCurrent(mAdapter.data)
        startActivity(Intent(this, ImagePreviewActivity::class.java)
                .putExtra(Constants.INTENT_POSITION, position))
    }

    private fun initActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.title = ""
        binding.toolbar.inflateMenu(R.menu.activity_preview_note)
        updateIcons()
    }

    private fun updateIcons() {
        binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isBgDark)
        ViewUtils.tintOverflowButton(binding.toolbar, isBgDark)
        invalidateOptionsMenu()
    }

    private fun editNote() {
        val noteWithImages = mNote
        if (noteWithImages != null) {
            CreateNoteActivity.openLogged(this, Intent(this, CreateNoteActivity::class.java)
                    .putExtra(Constants.INTENT_ID, noteWithImages.note?.key))
        }
    }

    private fun moveToStatus() {
        val noteWithImages = mNote
        if (noteWithImages != null) {
            Notifier.showNoteNotification(this, prefs, noteWithImages)
        }
    }

    override fun onBackPressed() {
        closeWindow()
    }

    private fun showNote(noteWithImages: NoteWithImages?) {
        this.mNote = noteWithImages
        if (noteWithImages != null) {
            val noteColor = themeUtil.getNoteLightColor(noteWithImages.getColor(), noteWithImages.getOpacity(), noteWithImages.getPalette())
            showImages(noteWithImages.images)
            binding.noteText.text = noteWithImages.getSummary()
            binding.noteText.typeface = AssetsUtil.getTypeface(this, noteWithImages.getStyle())
            window.statusBarColor = noteColor
            window.navigationBarColor = noteColor
            binding.windowBackground.setBackgroundColor(noteColor)
            isBgDark = if (ThemeUtil.isAlmostTransparent(noteWithImages.getOpacity())) {
                isDarkMode
            } else {
                ThemeUtil.isColorDark(noteColor)
            }
            updateTextColors()
            updateIcons()
        }
    }

    private fun updateTextColors() {
        val textColor = if (isBgDark) {
            ContextCompat.getColor(this, R.color.pureWhite)
        } else {
            ContextCompat.getColor(this, R.color.pureBlack)
        }
        binding.noteText.setTextColor(textColor)
    }

    private fun showReminder(reminder: Reminder?) {
        mReminder = reminder
        if (reminder != null) {
            val dateTime = TimeUtil.getDateTimeFromGmt(reminder.eventTime, prefs.is24HourFormat,
                    prefs.appLanguage)
            binding.reminderTime.text = dateTime
            binding.reminderContainer.visibility = View.VISIBLE
        } else {
            binding.reminderContainer.visibility = View.GONE
        }
    }

    private fun showImages(images: List<ImageFile>) {
        mAdapter.submitList(images)
    }

    private fun hideProgress() {

    }

    private fun showProgress() {

    }

    private fun shareNote() {
        if (!Permissions.checkPermission(this, SEND_CODE, Permissions.READ_EXTERNAL, Permissions.WRITE_EXTERNAL)) {
            return
        }
        showProgress()
        launchDefault {
            val file = backupTool.noteToFile(this@NotePreviewActivity, mNote)
            withUIContext {
                hideProgress()
                if (file != null) {
                    sendNote(file)
                } else {
                    showErrorSending()
                }
            }
        }
    }

    private fun sendNote(file: File) {
        if (isFinishing) return
        if (!file.exists() || !file.canRead()) {
            showErrorSending()
            return
        }
        val noteWithImages = mNote
        if (noteWithImages != null) {
            TelephonyUtil.sendNote(file, this, noteWithImages.note?.summary)
        }
    }

    private fun showErrorSending() {
        Toast.makeText(this, getString(R.string.error_sending), Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_preview_note, menu)
        ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_edit_24px, isBgDark)
        ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_twotone_favorite_24px, isBgDark)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                closeWindow()
                return true
            }
            R.id.action_share -> {
                shareNote()
                return true
            }
            R.id.action_delete -> {
                showDeleteDialog()
                return true
            }
            R.id.action_status -> {
                moveToStatus()
                return true
            }
            R.id.action_edit -> {
                editNote()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun closeWindow() {
        mUiHandler.post { finishAfterTransition() }
    }

    private fun showDeleteDialog() {
        val builder = dialogues.getMaterialDialog(this)
        builder.setMessage(getString(R.string.delete_this_note))
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            if (mNote != null) viewModel.deleteNote(mNote!!)
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showReminderDeleteDialog() {
        val builder = dialogues.getMaterialDialog(this)
        builder.setMessage(R.string.delete_this_reminder)
        builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            dialog.dismiss()
            deleteReminder()
        }
        builder.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun deleteReminder() {
        val reminder = mReminder ?: return
        viewModel.deleteReminder(reminder)
        binding.reminderContainer.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SEND_CODE -> if (Permissions.checkPermission(grantResults)) {
                shareNote()
            }
        }
    }

    companion object {

        private const val SEND_CODE = 25501
    }
}
