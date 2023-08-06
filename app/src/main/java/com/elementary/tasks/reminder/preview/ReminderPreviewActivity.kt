package com.elementary.tasks.reminder.preview

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BindingActivity
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.UiReminderPreview
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.data.ui.reminder.UiAppTarget
import com.elementary.tasks.core.data.ui.reminder.UiCallTarget
import com.elementary.tasks.core.data.ui.reminder.UiEmailTarget
import com.elementary.tasks.core.data.ui.reminder.UiLinkTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderDueData
import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus
import com.elementary.tasks.core.data.ui.reminder.UiReminderTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.data.ui.reminder.UiSmsTarget
import com.elementary.tasks.core.fragments.AdvancedMapFragment
import com.elementary.tasks.core.interfaces.MapCallback
import com.elementary.tasks.core.os.Permissions
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.ImageLoader
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.gone
import com.elementary.tasks.core.utils.intentForClass
import com.elementary.tasks.core.utils.nonNullObserve
import com.elementary.tasks.core.utils.toast
import com.elementary.tasks.core.utils.visible
import com.elementary.tasks.core.utils.visibleGone
import com.elementary.tasks.core.utils.visibleInvisible
import com.elementary.tasks.databinding.ActivityReminderPreviewBinding
import com.elementary.tasks.googletasks.TasksConstants
import com.elementary.tasks.googletasks.list.GoogleTaskHolder
import com.elementary.tasks.googletasks.task.GoogleTaskActivity
import com.elementary.tasks.notes.list.NoteViewHolder
import com.elementary.tasks.notes.preview.ImagePreviewActivity
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.preview.NotePreviewActivity
import com.elementary.tasks.pin.PinLoginActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import com.elementary.tasks.reminder.lists.adapter.ShopListRecyclerAdapter
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalTime
import timber.log.Timber
import java.io.File
import java.util.Locale

class ReminderPreviewActivity : BindingActivity<ActivityReminderPreviewBinding>() {

  private var mGoogleMap: AdvancedMapFragment? = null
  private val viewModel by viewModel<ReminderPreviewViewModel> { parametersOf(getId()) }
  private val dateTimeManager by inject<DateTimeManager>()
  private val imagesSingleton by inject<ImagesSingleton>()
  private val imageLoader by inject<ImageLoader>()

  private var shoppingAdapter = ShopListRecyclerAdapter()
  private val adsProvider = AdsProvider()

  private val mOnMarkerClick = GoogleMap.OnMarkerClickListener {
    openFullMap()
    false
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
    if (!Module.isPro && AdsProvider.hasAds()) {
      binding.adsCard.visible()
      adsProvider.showNativeBanner(
        binding.adsHolder,
        AdsProvider.REMINDER_PREVIEW_BANNER_ID,
        R.layout.list_item_ads_hor
      ) {
        binding.adsCard.gone()
      }
    } else {
      binding.adsCard.gone()
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
      context = this,
      email = action.target,
      subject = action.subject,
      message = action.summary,
      filePath = action.attachmentFile
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
        GoogleEventViewHolder(
          binding.dataContainer,
          currentStateHolder,
          dateTimeManager
        ) { _, event, listActions ->
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

  private fun showTask(googleTask: UiGoogleTaskList) {
    val binding = GoogleTaskHolder(binding.dataContainer) { _, _, listActions ->
      if (listActions == ListActions.EDIT) {
        PinLoginActivity.openLogged(
          this@ReminderPreviewActivity,
          Intent(this@ReminderPreviewActivity, GoogleTaskActivity::class.java)
            .putExtra(Constants.INTENT_ID, googleTask.id)
            .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)
        )
      }
    }
    binding.bind(googleTask)
    this.binding.dataContainer.addView(binding.itemView)
  }

  private fun showNote(note: UiNoteList) {
    val binding = NoteViewHolder(
      binding.dataContainer,
      { _, _, listActions ->
        if (listActions == ListActions.OPEN) {
          startActivity(
            intentForClass(NotePreviewActivity::class.java)
              .putExtra(Constants.INTENT_ID, note.id)
          )
        }
      }
    ) { _, _, imageId ->
      val imagePosition = note.images.indexOfFirst { it.id == imageId }.takeIf { it != -1 } ?: 0
      imagesSingleton.setCurrent(
        images = note.images,
        color = note.colorPosition,
        palette = note.colorPalette
      )
      startActivity(
        intentForClass(ImagePreviewActivity::class.java)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          .putExtra(Constants.INTENT_ID, note.id)
          .putExtra(Constants.INTENT_POSITION, imagePosition)
      )
    }
    binding.hasMore = false
    binding.setData(note)
    this.binding.dataContainer.addView(binding.itemView)
  }

  private fun showMapData(reminder: UiReminderPreview) {
    binding.mapCard.visible()
    binding.location.visible()

    var places = ""
    reminder.places.forEach {
      val lat = it.latitude
      val lon = it.longitude
      mGoogleMap?.addMarker(
        pos = LatLng(lat, lon),
        title = reminder.summary,
        clear = false,
        animate = false,
        radius = it.radius
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

    binding.group.text = reminder.group?.title
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
      binding.mapCard.gone()
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
    updateMenu()
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

    due?.recurRule?.let {
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
      binding.attachmentImage,
      "image"
    )
    startActivity(
      intentForClass(AttachmentPreviewActivity::class.java)
        .putExtra(Constants.INTENT_ITEM, attachmentFile),
      options.toBundle()
    )
  }

  private fun showAttachment(reminder: UiReminderPreview) {
    Timber.d("showAttachment: ${reminder.attachmentFile}")
    reminder.attachmentFile?.let { path ->
      binding.attachment.text = path
      binding.attachmentView.visible()
      val onSuccess: (Drawable) -> Unit = { drawable ->
        Timber.d("onBitmapLoaded: ")
        binding.attachmentsView.visible()
        binding.attachmentImage.setImageDrawable(drawable)
        binding.attachmentsView.setOnClickListener {
          withReminder { openAttachmentPreview(it.attachmentFile) }
        }
      }
      val onFail: (Drawable?) -> Unit = {
        Timber.d("onBitmapFailed: ")
        binding.attachmentsView.gone()
      }
      val file = File(path)
      if (file.exists()) {
        imageLoader.loadFromFile(
          file = file,
          onSuccess = onSuccess,
          onFail = onFail
        )
      } else {
        imageLoader.loadFromUri(
          uri = Uri.parse(path),
          onSuccess = onSuccess,
          onFail = onFail
        )
      }
    } ?: run {
      binding.attachmentView.gone()
      binding.attachmentsView.gone()
    }
  }

  private fun shareReminder() {
    viewModel.shareReminder()
  }

  private fun editReminder() {
    withReminder {
      PinLoginActivity.openLogged(
        this,
        intentForClass(CreateReminderActivity::class.java)
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
          if (it) viewModel.deleteReminder(true)
        }
      }
    }
  }

  private fun makeCopy() {
    withReminder {
      if (it.type.isBase(UiReminderType.Base.DATE)) {
        showDialog()
      }
    }
  }

  private fun closeWindow() {
    postUi { finishAfterTransition() }
  }

  private fun showDialog() {
    var time = LocalTime.of(0, 0)
    val list = mutableListOf<LocalTime>()
    val times = mutableListOf<String>()
    var isRunning = true
    do {
      if (time.hour == 23 && time.minute == 30) {
        isRunning = false
      } else {
        list.add(time)
        times.add(dateTimeManager.getTime(time))
        time = time.plusMinutes(30)
      }
    } while (isRunning)
    val builder = dialogues.getMaterialDialog(this)
    builder.setTitle(R.string.choose_time)
    builder.setItems(times.toTypedArray()) { dialog, which ->
      dialog.dismiss()
      saveCopy(list[which])
    }
    builder.create().show()
  }

  private fun saveCopy(time: LocalTime) {
    Timber.d("saveCopy: $time")
    withReminder {
      viewModel.copyReminder(time, it.summary + " - " + getString(R.string.copy))
    }
  }

  private fun initViews() {
    binding.switchWrapper.setOnClickListener { switchClick() }
    binding.fab.setOnClickListener { fabClick() }
    binding.mapCard.gone()
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
      val options = ActivityOptions.makeSceneTransitionAnimation(this, binding.mapCard, "map")
      startActivity(
        intentForClass(FullscreenMapActivity::class.java)
          .putExtra(Constants.INTENT_ID, it.id),
        options.toBundle()
      )
    }
  }

  private fun initActionBar() {
    binding.toolbar.setNavigationOnClickListener { closeWindow() }
    binding.toolbar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.action_delete -> {
          removeReminder()
          true
        }

        R.id.action_make_copy -> {
          makeCopy()
          true
        }

        R.id.action_share -> {
          shareReminder()
          true
        }

        R.id.action_edit -> {
          editReminder()
          true
        }

        else -> false
      }
    }
    updateMenu()
  }

  private fun updateMenu() {
    binding.toolbar.menu.also { menu ->
      menu.getItem(2)?.isVisible = getReminder()?.type?.isBase(UiReminderType.Base.DATE) == true
    }
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
