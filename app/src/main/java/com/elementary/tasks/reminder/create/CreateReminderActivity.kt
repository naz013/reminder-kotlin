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
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.deeplink.ReminderDatetimeTypeDeepLinkData
import com.elementary.tasks.core.os.PermissionFlowDelegateImpl
import com.elementary.tasks.core.os.datapicker.UriPicker
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.databinding.ActivityCreateReminderBinding
import com.elementary.tasks.databinding.ListItemNavigationBinding
import com.elementary.tasks.reminder.create.fragments.ApplicationFragment
import com.elementary.tasks.reminder.create.fragments.DateFragment
import com.elementary.tasks.reminder.create.fragments.EmailFragment
import com.elementary.tasks.reminder.create.fragments.LocationFragment
import com.elementary.tasks.reminder.create.fragments.MonthFragment
import com.elementary.tasks.reminder.create.fragments.ReminderInterface
import com.elementary.tasks.reminder.create.fragments.ShopFragment
import com.elementary.tasks.reminder.create.fragments.TimerFragment
import com.elementary.tasks.reminder.create.fragments.TypeFragment
import com.elementary.tasks.reminder.create.fragments.WeekFragment
import com.elementary.tasks.reminder.create.fragments.YearFragment
import com.elementary.tasks.reminder.create.fragments.recur.RecurFragment
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.FileConfig
import com.github.naz013.common.Module
import com.github.naz013.common.Permissions
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.ui.common.Dialogues
import com.github.naz013.ui.common.activity.BindingActivity
import com.github.naz013.ui.common.activity.toast
import com.github.naz013.ui.common.context.startActivity
import com.github.naz013.ui.common.view.visibleGone
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

@Deprecated("Replaced by new Builder")
class CreateReminderActivity : BindingActivity<ActivityCreateReminderBinding>(), ReminderInterface {

  private val prefs by inject<Prefs>()
  private val dateTimeManager by inject<DateTimeManager>()
  private val dialogues by inject<Dialogues>()
  private val appWidgetUpdater by inject<AppWidgetUpdater>()
  private val intentDataReader by inject<IntentDataReader>()

  private val viewModel by viewModel<EditReminderViewModel> { parametersOf(getId()) }
  private val stateViewModel by viewModel<ReminderStateViewModel>()

  private val permissionFlowDelegate = PermissionFlowDelegateImpl(this)
  private val uriPicker = UriPicker(this)

  private var fragment: TypeFragment<*>? = null
  private var isEditing: Boolean = false
  private var isTablet = false
  private var hasLocation = false
  private var selectorList = emptyList<UiSelectorReminder>()

  override val state: ReminderStateViewModel
    get() = stateViewModel
  override val defGroup: ReminderGroup?
    get() = stateViewModel.group
  override var canExportToTasks: Boolean = false
  override var canExportToCalendar: Boolean = false

  private val reminderObserver: Observer<in Reminder?> = Observer { reminder ->
    if (reminder != null) {
      editReminder(reminder)
    }
  }

  override fun inflateBinding() = ActivityCreateReminderBinding.inflate(layoutInflater)

  override fun requireLogin() = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setHomeButtonEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)

    hasLocation = Module.hasLocation(this)
    isTablet = resources.getBoolean(R.bool.is_tablet)
    canExportToCalendar = prefs.isCalendarEnabled || prefs.isStockCalendarEnabled
    canExportToTasks = stateViewModel.isLoggedToGoogleTasks()

    initNavigation()

    if (savedInstanceState == null) {
      stateViewModel.reminder.priority = prefs.defaultPriority
      stateViewModel.radius = prefs.radius
      stateViewModel.markerStyle = prefs.markerStyle
    }

    loadReminder()
  }

  private fun openScreen(uiSelectorType: UiSelectorType) {
    when (uiSelectorType) {
      UiSelectorType.DATE -> replaceFragment(DateFragment())
      UiSelectorType.TIMER -> replaceFragment(TimerFragment())
      UiSelectorType.WEEK -> replaceFragment(WeekFragment())
      UiSelectorType.EMAIL -> replaceFragment(EmailFragment())
      UiSelectorType.APP -> replaceFragment(ApplicationFragment())
      UiSelectorType.MONTH -> replaceFragment(MonthFragment())
      UiSelectorType.SHOP -> replaceFragment(ShopFragment())
      UiSelectorType.YEAR -> replaceFragment(YearFragment())
      UiSelectorType.GPS -> replaceFragment(LocationFragment())
      UiSelectorType.RECUR -> replaceFragment(RecurFragment())
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

  private fun getId(): String = intentString(IntentKeys.INTENT_ID)

  private fun loadReminder() {
    val id = getId()
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

      intent.data != null -> {
        readFromIntent()
      }

      intent.getBooleanExtra(IntentKeys.INTENT_ITEM, false) -> {
        intentDataReader.get(IntentKeys.INTENT_ITEM, Reminder::class.java)?.run {
          Logger.logEvent("Reminder loaded from intent")
          editReminder(this, false, fromFile = true)
        }
      }

      intent.getBooleanExtra(IntentKeys.INTENT_DEEP_LINK, false) -> {
        runCatching {
          val parser = DeepLinkDataParser()
          when (val deepLinkData = parser.readDeepLinkData(intent)) {
            is ReminderDatetimeTypeDeepLinkData -> {
              stateViewModel.reminder.type = deepLinkData.type
              stateViewModel.reminder.eventTime = dateTimeManager.getGmtFromDateTime(
                dateTime = deepLinkData.dateTime
              )
              editReminder(stateViewModel.reminder, false)
            }

            else -> {
            }
          }
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
    permissionFlowDelegate.permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
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
          } else {
            Reminder()
          }
          editReminder(reminder, false, fromFile)
        } catch (e: Throwable) {
          Logger.d("loadReminder: ${e.message}")
        }
      }
    }
  }

  private fun editReminder(reminder: Reminder, stop: Boolean = true, fromFile: Boolean = false) {
    Logger.d("editReminder: $stop, $reminder")
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
    val current = binding.navSpinner.selectedItemPosition.let { selectorList.getOrNull(it) }?.type
    var toSelect: UiSelectorType = UiSelectorType.DATE
    when (reminder.type) {
      Reminder.BY_DATE, Reminder.BY_DATE_CALL, Reminder.BY_DATE_SMS -> {
        toSelect = UiSelectorType.DATE
      }

      Reminder.BY_TIME, Reminder.BY_TIME_CALL, Reminder.BY_TIME_SMS -> {
        toSelect = UiSelectorType.TIMER
      }

      Reminder.BY_WEEK, Reminder.BY_WEEK_CALL, Reminder.BY_WEEK_SMS -> {
        toSelect = UiSelectorType.WEEK
      }

      Reminder.BY_DATE_EMAIL -> {
        toSelect = UiSelectorType.EMAIL
      }

      Reminder.BY_DATE_APP, Reminder.BY_DATE_LINK -> {
        toSelect = UiSelectorType.APP
      }

      Reminder.BY_MONTH, Reminder.BY_MONTH_CALL, Reminder.BY_MONTH_SMS -> {
        toSelect = UiSelectorType.MONTH
      }

      Reminder.BY_DATE_SHOP -> {
        toSelect = UiSelectorType.SHOP
      }

      Reminder.BY_DAY_OF_YEAR, Reminder.BY_DAY_OF_YEAR_CALL, Reminder.BY_DAY_OF_YEAR_SMS -> {
        toSelect = UiSelectorType.YEAR
      }

      Reminder.BY_RECUR, Reminder.BY_RECUR_CALL, Reminder.BY_RECUR_SMS -> {
        toSelect = if (com.elementary.tasks.core.utils.BuildParams.isPro) {
          UiSelectorType.RECUR
        } else {
          UiSelectorType.DATE
        }
      }

      else -> {
        toSelect = if (hasLocation) {
          when (reminder.type) {
            Reminder.BY_LOCATION, Reminder.BY_LOCATION_CALL, Reminder.BY_LOCATION_SMS,
            Reminder.BY_OUT_SMS, Reminder.BY_OUT_CALL, Reminder.BY_OUT, Reminder.BY_PLACES,
            Reminder.BY_PLACES_SMS, Reminder.BY_PLACES_CALL -> {
              UiSelectorType.GPS
            }

            else -> {
              UiSelectorType.GPS
            }
          }
        } else {
          UiSelectorType.DATE
        }
      }
    }
    if (current == toSelect) {
      openScreen(toSelect)
    } else {
      val index = selectorList.indexOfFirst { it.type == toSelect }
      if (index != -1) {
        binding.navSpinner.setSelection(index)
      }
    }
  }

  private fun initNavigation() {
    val list = mutableListOf<UiSelectorReminder>()
    list.add(UiSelectorReminder(getString(R.string.by_date), UiSelectorType.DATE))
    list.add(UiSelectorReminder(getString(R.string.timer), UiSelectorType.TIMER))
    list.add(UiSelectorReminder(getString(R.string.alarm), UiSelectorType.WEEK))
    list.add(UiSelectorReminder(getString(R.string.e_mail), UiSelectorType.EMAIL))
    if (Module.is12) {
      list.add(UiSelectorReminder(getString(R.string.open_link), UiSelectorType.APP))
    } else {
      list.add(UiSelectorReminder(getString(R.string.launch_application), UiSelectorType.APP))
    }
    list.add(UiSelectorReminder(getString(R.string.day_of_month), UiSelectorType.MONTH))
    list.add(UiSelectorReminder(getString(R.string.yearly), UiSelectorType.YEAR))
    list.add(UiSelectorReminder(getString(R.string.shopping_list), UiSelectorType.SHOP))
    if (hasLocation) {
      list.add(UiSelectorReminder(getString(R.string.location), UiSelectorType.GPS))
    }
    if (com.elementary.tasks.core.utils.BuildParams.isPro) {
      list.add(UiSelectorReminder(getString(R.string.recur_custom), UiSelectorType.RECUR))
    }

    selectorList = list
    val adapter = TitleNavigationAdapter(list)

    binding.navSpinner.adapter = adapter
    binding.navSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        prefs.lastUsedReminder = position
        openScreen(list[position].type)
      }

      override fun onNothingSelected(parent: AdapterView<*>) {
      }
    }
  }

  private fun changeGroup() {
    val groups = viewModel.getGroups()
    val names = groups.map { it.groupTitle }
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.choose_group)
    builder.setSingleChoiceItems(
      ArrayAdapter(
        this,
        android.R.layout.simple_list_item_single_choice,
        names
      ),
      names.indexOf(stateViewModel.reminder.groupTitle)
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

      R.id.action_delete -> {
        deleteReminder()
        return true
      }

      R.id.action_configure -> {
        startActivity(ConfigureActivity::class.java)
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
  }

  override fun attachFile() {
    permissionFlowDelegate.permissionFlow.askPermission(Permissions.READ_EXTERNAL) {
      selectAnyFile()
    }
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
      permissionFlowDelegate.permissionFlow.askPermission(Permissions.POST_NOTIFICATION) {
        askCopySaving()
      }
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
        Logger.d("save: $item")
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
    menu[1].isVisible = isEditing && !stateViewModel.isFromFile
    return true
  }

  private fun handleSendText(intent: Intent) {
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
      stateViewModel.reminder.summary = it
      editReminder(stateViewModel.reminder, false)
    }
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
    appWidgetUpdater.updateAllWidgets()
    appWidgetUpdater.updateCalendarWidget()
  }

  override fun handleBackPress(): Boolean {
    if (fragment != null && fragment?.onBackPressed() == true) {
      closeScreen()
    }
    return true
  }

  private data class UiSelectorReminder(val title: String, val type: UiSelectorType)

  private enum class UiSelectorType {
    DATE,
    TIMER,
    WEEK,
    EMAIL,
    APP,
    MONTH,
    YEAR,
    SHOP,
    GPS,
    RECUR
  }

  private inner class TitleNavigationAdapter(
    private val items: List<UiSelectorReminder>
  ) : BaseAdapter() {

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
}
