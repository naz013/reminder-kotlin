package com.elementary.tasks.reminder.create

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import androidx.core.view.get
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.os.datapicker.MelodyPicker
import com.elementary.tasks.core.os.datapicker.UriPicker
import com.elementary.tasks.core.os.datapicker.VoiceRecognitionLauncher
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.databinding.ActivityCreateReminderBinding
import com.elementary.tasks.databinding.ListItemNavigationBinding
import com.elementary.tasks.reminder.create.fragments.ApplicationFragment
import com.elementary.tasks.reminder.create.fragments.DateFragment
import com.elementary.tasks.reminder.create.fragments.EmailFragment
import com.elementary.tasks.reminder.create.fragments.LocationFragment
import com.elementary.tasks.reminder.create.fragments.MonthFragment
import com.elementary.tasks.reminder.create.fragments.PlacesTypeFragment
import com.elementary.tasks.reminder.create.fragments.ReminderInterface
import com.elementary.tasks.reminder.create.fragments.ShopFragment
import com.elementary.tasks.reminder.create.fragments.TimerFragment
import com.elementary.tasks.reminder.create.fragments.TypeFragment
import com.elementary.tasks.reminder.create.fragments.WeekFragment
import com.elementary.tasks.reminder.create.fragments.YearFragment
import com.elementary.tasks.voice.ConversationViewModel
import com.google.android.material.snackbar.Snackbar
import org.apache.commons.lang3.StringUtils
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate
import timber.log.Timber
import java.io.File
import java.util.*

class CreateReminderActivity : BindingActivity<ActivityCreateReminderBinding>(), ReminderInterface {

  private val dateTimeManager by inject<DateTimeManager>()

  private val viewModel by viewModel<EditReminderViewModel> { parametersOf(getId()) }
  private val conversationViewModel by viewModel<ConversationViewModel>()
  private val stateViewModel by viewModel<ReminderStateViewModel>()

  private val permissionFlow = PermissionFlow(this, dialogues)
  private val melodyPicker = MelodyPicker(this) {
    fragment?.onMelodySelect(it)
    showCurrentMelody()
  }
  private val voiceRecognitionLauncher = VoiceRecognitionLauncher(this) {
    processVoiceResult(it)
  }
  private val uriPicker = UriPicker(this)

  private var fragment: TypeFragment<*>? = null
  private var isEditing: Boolean = false
  private var isTablet = false
  private var hasLocation = false
  override val state: ReminderStateViewModel
    get() = stateViewModel
  override val defGroup: ReminderGroup?
    get() = stateViewModel.group
  override var canExportToTasks: Boolean = false
  override var canExportToCalendar: Boolean = false

  private val typeSelectListener = object : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
      prefs.lastUsedReminder = position
      openScreen(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }
  }
  private val reminderObserver: Observer<in Reminder> = Observer { reminder ->
    if (reminder != null) {
      editReminder(reminder)
    }
  }

  override fun inflateBinding() = ActivityCreateReminderBinding.inflate(layoutInflater)

  override fun requireLogin() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    hasLocation = Module.hasLocation(this)
    isTablet = resources.getBoolean(R.bool.is_tablet)
    canExportToCalendar = prefs.isCalendarEnabled || prefs.isStockCalendarEnabled
    canExportToTasks = stateViewModel.isLoggedToGoogleTasks()
    initActionBar()
    initNavigation()

    if (savedInstanceState == null) {
      stateViewModel.reminder.priority = prefs.defaultPriority
    }

    loadReminder()
  }

  private fun openScreen(position: Int) {
    when (position) {
      DATE -> replaceFragment(DateFragment())
      TIMER -> replaceFragment(TimerFragment())
      WEEK -> replaceFragment(WeekFragment())
      EMAIL -> replaceFragment(EmailFragment())
      APP -> replaceFragment(ApplicationFragment())
      MONTH -> replaceFragment(MonthFragment())
      SHOP -> replaceFragment(ShopFragment())
      YEAR -> replaceFragment(YearFragment())
      GPS -> replaceFragment(LocationFragment())
      GPS_PLACE -> replaceFragment(PlacesTypeFragment())
    }
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.reminder.observe(this, reminderObserver)
    viewModel.result.observe(this) { commands ->
      if (commands != null) {
        when (commands) {
          Commands.DELETED, Commands.SAVED -> {
            setResult(Activity.RESULT_OK)
            finish()
          }

          else -> {
          }
        }
      }
    }
    viewModel.allGroups.observe(this) {
      if (it != null && it.isNotEmpty()) {
        stateViewModel.group = it[0]
        showGroup(it[0])
      }
    }
  }

  private fun getId(): String = intentString(Constants.INTENT_ID)

  private fun loadReminder() {
    val id = getId()
    val date = intentSerializable(Constants.INTENT_DATE, LocalDate::class.java)
    initViewModel()
    when {
      intent?.action == Intent.ACTION_SEND -> {
        if ("text/plain" == intent.type) {
          handleSendText(intent)
        }
      }

      id != "" -> {
        isEditing = true
      }

      date != null -> {
        stateViewModel.reminder.type = Reminder.BY_DATE
        stateViewModel.reminder.eventTime = dateTimeManager.getGmtFromDateTime(date)
        editReminder(stateViewModel.reminder, false)
      }

      intent.data != null -> {
        readFromIntent()
      }

      intent.hasExtra(Constants.INTENT_ITEM) -> {
        runCatching {
          val reminder = intentParcelable(Constants.INTENT_ITEM, Reminder::class.java) ?: Reminder()
          editReminder(reminder, false, fromFile = true)
        }
      }

      else -> {
        var lastPos = prefs.lastUsedReminder
        if (lastPos >= binding.navSpinner.adapter.count) lastPos = 0
        binding.navSpinner.setSelection(lastPos)
      }
    }
  }

  private fun readFromIntent() {
    permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
      intent.data?.let {
        try {
          var fromFile = false
          val reminder = if (ContentResolver.SCHEME_CONTENT != it.scheme) {
            val any = MemoryUtil.readFromUri(this, it, FileConfig.FILE_NAME_REMINDER)
            if (any != null && any is Reminder) {
              fromFile = true
              any
            } else {
              Reminder()
            }
          } else Reminder()
          editReminder(reminder, false, fromFile)
        } catch (e: Throwable) {
          Timber.d("loadReminder: ${e.message}")
        }
      }
    }
  }

  private fun editReminder(reminder: Reminder, stop: Boolean = true, fromFile: Boolean = false) {
    Timber.d("editReminder: $stop, $reminder")
    stateViewModel.reminder = reminder
    stateViewModel.isFromFile = fromFile
    if (fromFile) {
      viewModel.findSame(reminder.uuId)
    }
    if (stop) {
      viewModel.pauseReminder(reminder)
      stateViewModel.original = reminder
      stateViewModel.isPaused = true
    } else {
      val group = defGroup
      if (reminder.groupUuId.isBlank() && group != null) {
        stateViewModel.reminder.groupUuId = group.groupUuId
        stateViewModel.reminder.groupColor = group.groupColor
        stateViewModel.reminder.groupTitle = group.groupTitle
      }
    }
    val current = binding.navSpinner.selectedItemPosition
    var toSelect = 0
    when (reminder.type) {
      Reminder.BY_DATE, Reminder.BY_DATE_CALL, Reminder.BY_DATE_SMS -> toSelect = DATE
      Reminder.BY_TIME, Reminder.BY_TIME_CALL, Reminder.BY_TIME_SMS -> toSelect = TIMER
      Reminder.BY_WEEK, Reminder.BY_WEEK_CALL, Reminder.BY_WEEK_SMS -> toSelect = WEEK
      Reminder.BY_DATE_EMAIL -> toSelect = EMAIL
      Reminder.BY_DATE_APP, Reminder.BY_DATE_LINK -> toSelect = APP
      Reminder.BY_MONTH, Reminder.BY_MONTH_CALL, Reminder.BY_MONTH_SMS -> toSelect = MONTH
      Reminder.BY_DATE_SHOP -> toSelect = SHOP
      Reminder.BY_DAY_OF_YEAR, Reminder.BY_DAY_OF_YEAR_CALL, Reminder.BY_DAY_OF_YEAR_SMS ->
        toSelect = YEAR
      else -> {
        if (hasLocation) {
          when (reminder.type) {
            Reminder.BY_LOCATION, Reminder.BY_LOCATION_CALL, Reminder.BY_LOCATION_SMS,
            Reminder.BY_OUT_SMS, Reminder.BY_OUT_CALL, Reminder.BY_OUT -> toSelect = GPS

            else -> if (Module.isPro) {
              toSelect = when (reminder.type) {
                Reminder.BY_PLACES, Reminder.BY_PLACES_SMS, Reminder.BY_PLACES_CALL -> GPS_PLACE
                else -> DATE
              }
            }
          }
        } else {
          toSelect = DATE
        }
      }
    }
    if (current == toSelect) {
      openScreen(toSelect)
    } else {
      binding.navSpinner.setSelection(toSelect)
    }
  }

  private fun initNavigation() {
    val list = mutableListOf<UiSelectorReminder>()
    list.add(UiSelectorReminder(getString(R.string.by_date)))
    list.add(UiSelectorReminder(getString(R.string.timer)))
    list.add(UiSelectorReminder(getString(R.string.alarm)))
    list.add(UiSelectorReminder(getString(R.string.e_mail)))
    if (Module.is12) {
      list.add(UiSelectorReminder(getString(R.string.open_link)))
    } else {
      list.add(UiSelectorReminder(getString(R.string.launch_application)))
    }
    list.add(UiSelectorReminder(getString(R.string.day_of_month)))
    list.add(UiSelectorReminder(getString(R.string.yearly)))
    list.add(UiSelectorReminder(getString(R.string.shopping_list)))
    if (hasLocation) {
      list.add(UiSelectorReminder(getString(R.string.location)))
      if (Module.isPro) {
        list.add(UiSelectorReminder(getString(R.string.places)))
      }
    }
    val adapter = TitleNavigationAdapter(list)
    binding.navSpinner.adapter = adapter
    binding.navSpinner.onItemSelectedListener = typeSelectListener
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
  }

  private fun changeGroup() {
    val groups = viewModel.groups
    val names = groups.map { it.groupTitle }
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.choose_group)
    builder.setSingleChoiceItems(
      ArrayAdapter(
        this,
        android.R.layout.simple_list_item_single_choice, names
      ), names.indexOf(stateViewModel.reminder.groupTitle)
    ) { dialog, which ->
      dialog.dismiss()
      showGroup(groups[which])
    }
    builder.create().show()
  }

  private fun showGroup(item: ReminderGroup?) {
    if (item == null) return
    val frag = fragment ?: return
    frag.onGroupUpdate(item)
  }

  private fun openRecognizer() {
    voiceRecognitionLauncher.recognize(true)
  }

  private fun replaceFragment(fragment: TypeFragment<*>) {
    runCatching {
      supportFragmentManager.beginTransaction()
        .replace(R.id.main_container, fragment, null)
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        .commitAllowingStateLoss()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_add -> {
        askNotificationPermissionIfNeeded()
        return true
      }

      R.id.action_voice -> {
        openRecognizer()
        return true
      }

      R.id.action_delete -> {
        deleteReminder()
        return true
      }

      R.id.action_configure -> {
        startActivity(Intent(this, ConfigureActivity::class.java))
        return true
      }

      android.R.id.home -> {
        closeScreen()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun selectMelody() {
    permissionFlow.askPermission(Permissions.READ_EXTERNAL) { melodyPicker.pickMelody() }
  }

  override fun attachFile() {
    permissionFlow.askPermission(Permissions.READ_EXTERNAL) { selectAnyFile() }
  }

  private fun closeScreen() {
    if (isEditing) {
      if (!stateViewModel.reminder.isActive) {
        viewModel.resumeReminder(stateViewModel.reminder)
      }
      setResult(Activity.RESULT_OK)
      finish()
    } else {
      setResult(Activity.RESULT_OK)
      finish()
    }
  }

  private fun deleteReminder() {
    if (stateViewModel.reminder.isRemoved) {
      dialogues.askConfirmation(this, getString(R.string.delete)) {
        if (it) viewModel.deleteReminder(stateViewModel.reminder, true)
      }
    } else {
      dialogues.askConfirmation(this, getString(R.string.move_to_trash)) {
        if (it) viewModel.moveToTrash(stateViewModel.reminder)
      }
    }
  }

  private fun askNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissionFlow.askPermission(Permissions.POST_NOTIFICATION) { askCopySaving() }
    } else {
      askCopySaving()
    }
  }

  private fun askCopySaving() {
    if (stateViewModel.isFromFile && viewModel.hasSameInDb) {
      dialogues.getMaterialDialog(this)
        .setMessage(R.string.same_reminder_message)
        .setPositiveButton(R.string.keep) { dialogInterface, _ ->
          dialogInterface.dismiss()
          save(true)
        }
        .setNegativeButton(R.string.replace) { dialogInterface, _ ->
          dialogInterface.dismiss()
          save()
        }
        .setNeutralButton(R.string.cancel) { dialogInterface, _ ->
          dialogInterface.dismiss()
        }
        .create()
        .show()
    } else {
      save()
    }
  }

  private fun save(newId: Boolean = false) {
    fragment?.let {
      it.prepare()?.let { item ->
        Timber.d("save: $item")
        viewModel.reminder.removeObserver(reminderObserver)
        stateViewModel.isSaving = true
        if (newId) {
          item.uuId = UUID.randomUUID().toString()
        }
        viewModel.saveAndStartReminder(item, isEditing)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.activity_create_reminder, menu)
    menu[0].isVisible = Module.hasMicrophone(this)
    menu[2].isVisible = isEditing && !stateViewModel.isFromFile
    return true
  }

  private fun processVoiceResult(matches: List<String>) {
    if (matches.isNotEmpty()) {
      val model = conversationViewModel.findResults(matches)
      if (model != null) {
        editReminder(model, false)
      } else {
        val text = matches[0]
        fragment?.onVoiceAction(StringUtils.capitalize(text))
      }
    }
  }

  private fun handleSendText(intent: Intent) {
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
      stateViewModel.reminder.summary = it
      editReminder(stateViewModel.reminder, false)
    }
  }

  private fun showCurrentMelody() {
    val musicFile = File(stateViewModel.reminder.melodyPath)
    showSnackbar(
      String.format(getString(R.string.melody_x), musicFile.name),
      getString(R.string.delete)
    ) { removeMelody() }
  }

  private fun removeMelody() {
    fragment?.onMelodySelect("")
  }

  private fun selectAnyFile() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "*/*"
    try {
      uriPicker.launchIntent(intent) { uri ->
        uri?.also { fragment?.onAttachmentSelect(it) }
      }
    } catch (e: Exception) {
      toast(R.string.app_not_found)
    }
  }

  override fun selectGroup() {
    changeGroup()
  }

  override fun showSnackbar(title: String, actionName: String, listener: View.OnClickListener) {
    Snackbar.make(binding.coordinator, title, Snackbar.LENGTH_SHORT).setAction(actionName, listener)
      .show()
  }

  override fun showSnackbar(title: String) {
    Snackbar.make(binding.coordinator, title, Snackbar.LENGTH_SHORT).show()
  }

  override fun setFullScreenMode(fullScreenEnabled: Boolean) {
    if (!isTablet) {
      binding.appBar.visibleGone(!fullScreenEnabled)
    }
  }

  override fun updateScroll(y: Int) {
    if (!isTablet) binding.appBar.isSelected = y > 0
  }

  override fun isTablet(): Boolean {
    return isTablet
  }

  override fun setFragment(typeFragment: TypeFragment<*>?) {
    this.fragment = typeFragment
  }

  override fun onDestroy() {
    super.onDestroy()
    if (stateViewModel.isPaused && !stateViewModel.isSaving) {
      stateViewModel.original?.let { viewModel.resumeReminder(it) }
    }
    updatesHelper.updateWidgets()
    updatesHelper.updateCalendarWidget()
  }

  override fun handleBackPress(): Boolean {
    if (fragment != null && fragment?.onBackPressed() == true) {
      closeScreen()
    }
    return true
  }

  private class UiSelectorReminder(val title: String)

  private inner class TitleNavigationAdapter(private val items: List<UiSelectorReminder>) : BaseAdapter() {

    override fun getCount(): Int {
      return items.size
    }

    override fun getItem(index: Int): Any {
      return items[index]
    }

    override fun getItemId(position: Int): Long {
      return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
      val cView = ListItemNavigationBinding.inflate(layoutInflater)
      cView.txtTitle.text = items[position].title
      return cView.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
      val cView = ListItemNavigationBinding.inflate(layoutInflater)
      cView.txtTitle.text = items[position].title
      return cView.root
    }
  }

  companion object {

    private const val DATE = 0
    private const val TIMER = 1
    private const val WEEK = 2
    private const val EMAIL = 3
    private const val APP = 4
    private const val MONTH = 5
    private const val YEAR = 6
    private const val SHOP = 7
    private const val GPS = 8
    private const val GPS_PLACE = 9
  }
}
