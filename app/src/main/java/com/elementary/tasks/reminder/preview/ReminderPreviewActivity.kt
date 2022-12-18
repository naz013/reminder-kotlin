package com.elementary.tasks.reminder.preview

import android.app.ActivityOptions
import android.app.AlarmManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiLinkTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.data.ui.UiReminderPreview
import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus
import com.elementary.tasks.core.data.ui.reminder.UiReminderTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.os.PermissionFlow
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.Permissions
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.datetime.TimeUtil
import com.elementary.tasks.core.utils.ui.ViewUtils
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.utils.visibleInvisible
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.reminders.ReminderPreviewViewModel
import com.elementary.tasks.databinding.ActivityReminderPreviewBinding
import com.elementary.tasks.google_tasks.create.TaskActivity
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.google_tasks.list.GoogleTaskHolder
import com.elementary.tasks.notes.list.NoteViewHolder
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.io.File
import java.util.Calendar
import java.util.Locale

class ReminderPreviewActivity : BindingActivity<ActivityReminderPreviewBinding>() {

  private var mGoogleMap: AdvancedMapFragment? = null
  private val viewModel by viewModel<ReminderPreviewViewModel> { parametersOf(getId()) }
  private val permissionFlow = PermissionFlow(this, dialogues)

  private val list = ArrayList<Long>()

  private var shoppingAdapter = ShopListRecyclerAdapter()
  private val adsProvider = AdsProvider()

  private val mOnMarkerClick = GoogleMap.OnMarkerClickListener {
    openFullMap()
    false
  }

  private val imageTarget: Target = object : Target {
    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
      Timber.d("onPrepareLoad: ")
    }

    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
      Timber.d("onBitmapFailed: $e")
      binding.attachmentsView.gone()
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
      Timber.d("onBitmapLoaded: ${bitmap != null}")
      binding.attachmentsView.visible()
      binding.attachmentImage.setImageBitmap(bitmap)
      binding.attachmentsView.setOnClickListener {
        withReminder { openAttachmentPreview(it.attachmentFile) }
      }
    }
  }

  override fun inflateBinding() = ActivityReminderPreviewBinding.inflate(layoutInflater)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding.dataContainer.removeAllViewsInLayout()
    initActionBar()
    initViews()
    initViewModel()
    loadAds()
  }

  private fun getId() = intentString(Constants.INTENT_ID)

  private fun loadAds() {
    if (!Module.isPro) {
      binding.adsCard.visible()
      adsProvider.showBanner(
        binding.adsHolder,
        AdsProvider.REMINDER_PREVIEW_BANNER_ID,
        R.layout.list_item_ads_hor
      ) {
        binding.adsCard.visible()
      }
    } else {
      binding.adsCard.visible()
    }
  }

  private fun sendSMS(action: UiSmsTarget) {
    TelephonyUtil.sendSms(this, action.target, action.summary)
  }

  private fun makeCall(action: UiCallTarget) {
    permissionFlow.askPermission(Permissions.CALL_PHONE) {
      TelephonyUtil.makeCall(action.target, this)
    }
  }

  private fun openApp(action: UiAppTarget) {
    TelephonyUtil.openApp(action.target, this)
  }

  private fun openLink(action: UiLinkTarget) {
    TelephonyUtil.openLink(action.target, this)
  }

  private fun sendEmail(action: UiEmailTarget) {
    TelephonyUtil.sendMail(
      this, action.target, action.subject,
      action.summary, action.attachmentFile
    )
  }

  private fun initViewModel() {
    lifecycle.addObserver(viewModel)
    viewModel.reminder.nonNullObserve(this) { reminder ->
      showInfo(reminder)
      viewModel.loadExtra()
    }
    viewModel.result.nonNullObserve(this) { commands ->
      when (commands) {
        Commands.DELETED -> closeWindow()
        Commands.FAILED -> toast(getString(R.string.reminder_is_outdated), Toast.LENGTH_SHORT)
        else -> {
        }
      }
    }
    viewModel.googleTask.nonNullObserve(this) { showTask(it) }
    viewModel.note.nonNullObserve(this) { showNote(it) }
    viewModel.calendarEvent.nonNullObserve(this) { showCalendarEvents(it) }
    viewModel.clearExtraData.nonNullObserve(this) {
      if (it) {
        binding.dataContainer.removeAllViewsInLayout()
      }
    }
    viewModel.sharedFile.nonNullObserve(this) {
      TelephonyUtil.sendFile(this@ReminderPreviewActivity, it)
    }
  }

  private fun showCalendarEvents(events: List<GoogleCalendarUtils.EventItem>) {
    Timber.d("showCalendarEvents: $events")
    for (e in events) {
      val binding =
        GoogleEventViewHolder(binding.dataContainer, currentStateHolder) { _, event, listActions ->
          if (listActions == ListActions.OPEN && event != null) {
            openCalendar(event.id)
          } else if (listActions == ListActions.REMOVE && event != null) {
            viewModel.deleteEvent(event)
          }
        }
      binding.bind(e)
      this.binding.dataContainer.addView(binding.itemView)
    }
  }

  private fun openCalendar(id: Long) {
    if (id <= 0L) return
    val uri = Uri.parse("content://com.android.calendar/events/$id")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    runCatching {
      startActivity(intent)
    }
  }

  private fun showTask(pair: Pair<GoogleTaskList?, GoogleTask?>) {
    val googleTask = pair.second ?: return
    val googleTaskList = pair.first ?: return
    val binding = GoogleTaskHolder(binding.dataContainer) { _, _, listActions ->
      if (listActions == ListActions.EDIT) {
        PinLoginActivity.openLogged(
          this@ReminderPreviewActivity,
          Intent(this@ReminderPreviewActivity, TaskActivity::class.java)
            .putExtra(Constants.INTENT_ID, googleTask.taskId)
            .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)
        )
      }
    }
    binding.bind(googleTask, mapOf(Pair(googleTask.listId, googleTaskList)))
    this.binding.dataContainer.addView(binding.itemView)
  }

  private fun showNote(note: NoteWithImages) {
    val binding =
      NoteViewHolder(binding.dataContainer, currentStateHolder, get()) { _, _, listActions ->
        if (listActions == ListActions.OPEN) {
          startActivity(
            Intent(this@ReminderPreviewActivity, NotePreviewActivity::class.java)
              .putExtra(Constants.INTENT_ID, note.getKey())
          )
        }
      }
    binding.hasMore = false
    binding.setData(note)
    this.binding.dataContainer.addView(binding.itemView)
  }

  private fun showMapData(reminder: UiReminderPreview) {
    binding.mapContainer.visible()
    binding.location.visible()

    var places = ""
    reminder.places.forEach {
      val lat = it.latitude
      val lon = it.longitude
      mGoogleMap?.addMarker(
        LatLng(lat, lon), reminder.summary, clear = false,
        animate = false, radius = it.radius
      )
      places += String.format(Locale.getDefault(), "%.5f %.5f", lat, lon)
      places += "\n"
    }
    binding.location.text = places

    val place = reminder.places[0]
    val lat = place.latitude
    val lon = place.longitude
    mGoogleMap?.moveCamera(LatLng(lat, lon), 0, 0, 0, 0)
  }

  private fun showInfo(reminder: UiReminderPreview) {
    Timber.d("showInfo: $reminder")

    binding.group.text = reminder.group?.name
    showStatus(reminder.status)
    binding.windowTypeView.text = reminder.windowType
    binding.taskText.text = reminder.summary
    binding.type.text = reminder.illustration.title
    binding.itemPhoto.setImageResource(reminder.illustration.icon)
    binding.idView.text = reminder.id

    showDue(reminder.due)
    showPhoneContact(reminder.actionTarget)
    showMelody(reminder.melodyName)
    showAttachment(reminder)

    if (reminder.type.isGpsType()) {
      initMap()
    } else {
      binding.locationView.gone()
      binding.mapContainer.gone()
    }

    if (reminder.shopList.isNotEmpty()) {
      binding.todoList.visible()
      loadData(reminder.shopList)
    } else {
      binding.todoList.gone()
    }

    if (reminder.status.canMakeAction) {
      when {
        reminder.type.isSms() -> {
          if (prefs.isTelephonyAllowed) {
            binding.fab.setIconResource(R.drawable.ic_twotone_send_24px)
            binding.fab.text = getString(R.string.send_sms)
            binding.fab.visible()
          } else {
            binding.fab.gone()
          }
        }

        reminder.type.isCall() -> {
          if (prefs.isTelephonyAllowed) {
            binding.fab.setIconResource(R.drawable.ic_twotone_call_24px)
            binding.fab.text = getString(R.string.make_call)
            binding.fab.visible()
          } else {
            binding.fab.gone()
          }
        }

        reminder.type.isApp() -> {
          binding.fab.setIconResource(R.drawable.ic_twotone_open_in_new_24px)
          binding.fab.text = getString(R.string.open_app)
          binding.fab.visible()
        }

        reminder.type.isLink() -> {
          binding.fab.setIconResource(R.drawable.ic_twotone_open_in_browser_24px)
          binding.fab.text = getString(R.string.open_link)
          binding.fab.visible()
        }

        reminder.type.isEmail() -> {
          binding.fab.setIconResource(R.drawable.ic_twotone_local_post_office_24px)
          binding.fab.text = getString(R.string.send)
          binding.fab.visible()
        }

        else -> binding.fab.gone()
      }
    } else {
      binding.fab.gone()
    }
  }

  private fun loadData(shopList: List<ShopItem>) {
    shoppingAdapter.listener = object : ShopListRecyclerAdapter.ActionListener {
      override fun onItemCheck(position: Int, isChecked: Boolean) {
        val item = shoppingAdapter.getItem(position)
        item.isChecked = !item.isChecked
        shoppingAdapter.updateData()
        viewModel.saveNewShopList(shoppingAdapter.data)
      }

      override fun onItemDelete(position: Int) {
        shoppingAdapter.delete(position)
        viewModel.saveNewShopList(shoppingAdapter.data)
      }
    }
    shoppingAdapter.data = shopList
    binding.todoList.layoutManager = LinearLayoutManager(this)
    binding.todoList.isNestedScrollingEnabled = false
    binding.todoList.adapter = shoppingAdapter
  }

  private fun showDue(due: UiReminderDueData?) {
    due?.before?.let {
      binding.beforeView.visible()
      binding.before.text = it
    } ?: run { binding.beforeView.gone() }

    due?.dateTime?.let {
      binding.timeView.visible()
      binding.time.text = it
    } ?: run { binding.timeView.gone() }

    due?.repeat?.let {
      binding.repeatView.visible()
      binding.repeat.text = it
    } ?: run { binding.repeatView.gone() }
  }

  private fun showPhoneContact(target: UiReminderTarget?) {
    if (target == null) {
      binding.numberView.gone()
      return
    }
    when (target) {
      is UiCallTarget -> {
        binding.number.text = target.target
        binding.numberView.visible()
      }
      is UiSmsTarget -> {
        binding.number.text = target.target
        binding.numberView.visible()
      }
      is UiEmailTarget -> {
        binding.number.text = target.target
        binding.numberView.visible()
      }
      else -> binding.numberView.gone()
    }
  }

  private fun showMelody(melodyName: String?) {
    binding.melody.text = melodyName
    binding.melody.visibleGone(melodyName != null)
  }

  private fun showStatus(status: UiReminderStatus) {
    binding.statusSwitch.isChecked = status.active
    binding.statusSwitch.visibleInvisible(status.canToggle)
    binding.statusText.text = status.title
  }

  private fun openAttachmentPreview(attachmentFile: String?) {
    attachmentFile ?: return
    val options = ActivityOptions.makeSceneTransitionAnimation(
      this@ReminderPreviewActivity,
      binding.attachmentImage, "image"
    )
    startActivity(
      Intent(this@ReminderPreviewActivity, AttachmentPreviewActivity::class.java)
        .putExtra(Constants.INTENT_ITEM, attachmentFile),
      options.toBundle()
    )
  }

  private fun showAttachment(reminder: UiReminderPreview) {
    Timber.d("showAttachment: ${reminder.attachmentFile}")
    reminder.attachmentFile?.let {
      binding.attachment.text = it
      binding.attachmentView.visible()
      val file = File(it)
      if (file.exists()) {
        Picasso.get().load(file).into(imageTarget)
      } else {
        val uri = Uri.parse(it)
        Picasso.get().load(uri).into(imageTarget)
      }
    } ?: run {
      binding.attachmentView.gone()
      binding.attachmentsView.gone()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_reminder_preview, menu)

    ViewUtils.tintMenuIcon(this, menu, 0, R.drawable.ic_twotone_edit_24px, isDarkMode)
    ViewUtils.tintMenuIcon(this, menu, 1, R.drawable.ic_twotone_share_24px, isDarkMode)
    ViewUtils.tintMenuIcon(this, menu, 2, R.drawable.ic_twotone_file_copy_24px, isDarkMode)
    ViewUtils.tintMenuIcon(this, menu, 3, R.drawable.ic_twotone_delete_24px, isDarkMode)

    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.action_delete -> removeReminder()
      android.R.id.home -> closeWindow()
      R.id.action_make_copy -> makeCopy()
      R.id.action_share -> shareReminder()
      R.id.action_edit -> editReminder()
    }
    return super.onOptionsItemSelected(item)
  }

  private fun shareReminder() {
    viewModel.shareReminder()
  }

  private fun editReminder() {
    withReminder {
      PinLoginActivity.openLogged(
        this, Intent(this, CreateReminderActivity::class.java)
          .putExtra(Constants.INTENT_ID, it.id)
      )
    }
  }

  private fun removeReminder() {
    withReminder { reminder ->
      if (reminder.isRunning) {
        dialogues.askConfirmation(this, getString(R.string.move_to_trash)) {
          if (it) viewModel.moveToTrash()
        }
      } else {
        dialogues.askConfirmation(this, getString(R.string.delete)) {
          if (it) viewModel.deleteReminder( true)
        }
      }
    }
  }

  private fun makeCopy() {
    withReminder {
      if (it.type.isBase(UiReminderType.Base.TIMER)) {
        showDialog()
      }
    }
  }

  private fun closeWindow() {
    postUi { finishAfterTransition() }
  }

  private fun showDialog() {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    var hour = 0
    var minute = 0
    list.clear()
    val time = ArrayList<String>()
    val is24 = prefs.is24HourFormat
    do {
      if (hour == 23 && minute == 30) {
        hour = -1
      } else {
        val tmp = calendar.timeInMillis
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
        list.add(tmp)
        time.add(TimeUtil.getTime(calendar.time, is24, prefs.appLanguage))
        calendar.timeInMillis = tmp + AlarmManager.INTERVAL_HALF_HOUR
      }
    } while (hour != -1)
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.choose_time)
    builder.setItems(time.toTypedArray()) { dialog, which ->
      dialog.dismiss()
      saveCopy(which)
    }
    builder.create().show()
  }

  private fun saveCopy(which: Int) {
    Timber.d("saveCopy: $which")
    withReminder {
      viewModel.copyReminder(list[which], it.summary + " - " + getString(R.string.copy))
    }
  }

  private fun initViews() {
    binding.switchWrapper.setOnClickListener { switchClick() }
    binding.fab.setOnClickListener { fabClick() }
    binding.mapContainer.gone()
    binding.attachmentsView.gone()
    binding.fab.gone()
  }

  private fun fabClick() {
    getReminder()?.actionTarget?.also {
      when (it) {
        is UiSmsTarget -> sendSMS(it)
        is UiCallTarget -> makeCall(it)
        is UiAppTarget -> openApp(it)
        is UiLinkTarget -> openLink(it)
        is UiEmailTarget -> sendEmail(it)
      }
    }
  }

  private fun switchClick() {
    withReminder {
      if (!it.status.canToggle) return@withReminder
      if (it.type.isGpsType()) {
        permissionFlow.askPermission(Permissions.FOREGROUND_SERVICE) {
          viewModel.toggleReminder()
        }
      } else {
        viewModel.toggleReminder()
      }
    }
  }

  private fun initMap() {
    val googleMap = AdvancedMapFragment.newInstance(
      isTouch = false,
      isPlaces = false,
      isSearch = false,
      isStyles = false,
      isBack = false,
      isZoom = false,
      isDark = isDarkMode
    )
    googleMap.setCallback(object : MapCallback {
      override fun onMapReady() {
        googleMap.setSearchEnabled(false)
        googleMap.setOnMapClickListener { openFullMap() }
        googleMap.setOnMarkerClick(mOnMarkerClick)
        withReminder { showMapData(it) }
      }
    })
    supportFragmentManager.beginTransaction()
      .replace(binding.mapContainer.id, googleMap)
      .addToBackStack(null)
      .commit()
    this.mGoogleMap = googleMap
  }

  private fun openFullMap() {
    withReminder {
      val options = ActivityOptions.makeSceneTransitionAnimation(this, binding.mapContainer, "map")
      startActivity(
        Intent(this, FullscreenMapActivity::class.java)
          .putExtra(Constants.INTENT_ID, it.id), options.toBundle()
      )
    }
  }

  private fun initActionBar() {
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    ViewUtils.listenScrollableView(binding.scrollView) {
      binding.appBar.isSelected = it > 0
    }
    binding.toolbar.navigationIcon = ViewUtils.backIcon(this, isDarkMode)
  }

  private fun withReminder(action: (UiReminderPreview) -> Unit) {
    getReminder()?.also(action)
  }

  private fun getReminder() = viewModel.reminder.value

  override fun onDestroy() {
    super.onDestroy()
    adsProvider.destroy()
  }
}
