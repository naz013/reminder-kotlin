package com.elementary.tasks.notes.create

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.cloud.usecase.ScheduleBackgroundWorkUseCase
import com.elementary.tasks.core.data.adapter.note.UiNoteEditAdapter
import com.elementary.tasks.core.data.repository.NoteImageRepository
import com.elementary.tasks.core.data.ui.note.UiNoteEdit
import com.elementary.tasks.core.data.ui.note.UiNoteImage
import com.elementary.tasks.core.data.ui.note.UiNoteImageState
import com.elementary.tasks.core.utils.ImageLoader
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.notes.NoteColorEngine
import com.elementary.tasks.notes.create.drop.DroppedContentParser
import com.elementary.tasks.notes.create.images.ImageDecoder
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.notes.usecase.CreateSharedNoteFileUseCase
import com.elementary.tasks.notes.usecase.DeleteNoteUseCase
import com.elementary.tasks.notes.usecase.SaveNoteUseCase
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.elementary.tasks.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.regex.Pattern

/**
 * Shared fixture for [NoteEditViewModel] tests, split across several `NoteEditViewModel*Test`
 * files by concern (this class intentionally has no `@Test` methods so it isn't picked up as its
 * own test class).
 *
 * [NoteEditViewModel.state] is a plain `MutableStateFlow` populated once from `init {}` - unlike
 * some other ViewModels in this app it does NOT rebuild on every `state` collection (no
 * `onStart { load() }`), so reading `viewModel.state.value` directly after construction/actions is
 * safe and does not re-trigger `load()`.
 */
open class NoteEditViewModelTestSupport : BaseTest() {
  protected val imageDecoder = mockk<ImageDecoder>()
  protected val noteRepository = mockk<NoteRepository>()
  protected val reminderV2Repository = mockk<ReminderV2Repository>()
  protected val deleteReminderUseCase = mockk<DeleteReminderUseCase>(relaxed = true)
  protected val prefs = mockk<Prefs>(relaxed = true)
  protected val dateTimeManager = mockk<DateTimeManager>()
  protected val textProvider = mockk<TextProvider>(relaxed = true)
  protected val contextProvider = mockk<ContextProvider>()
  protected val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  protected val uiNoteEditAdapter = mockk<UiNoteEditAdapter>()
  protected val noteImageRepository = mockk<NoteImageRepository>()
  protected val intentDataReader = mockk<IntentDataReader>()
  protected val deleteNoteUseCase = mockk<DeleteNoteUseCase>(relaxed = true)
  protected val saveNoteUseCase = mockk<SaveNoteUseCase>(relaxed = true)
  protected val createSharedNoteFileUseCase = mockk<CreateSharedNoteFileUseCase>()
  protected val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  protected val droppedContentParser = mockk<DroppedContentParser>()
  protected val imagesSingleton = mockk<ImagesSingleton>(relaxed = true)
  protected val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  protected val systemInfo = mockk<SystemInfo>(relaxed = true)
  protected val imageLoader = mockk<ImageLoader>()
  protected val noteColorEngine = mockk<NoteColorEngine>()
  protected val tagRepository = mockk<TagRepository>()
  protected val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  protected val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)

  protected val fakeContext = mockk<Context>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()

    // android.util.Patterns.WEB_URL is a real static final field that is left null by the
    // android-stub jar used for JVM unit tests (verified empirically - referencing it directly
    // throws an NPE, even with `isReturnDefaultValues = true`, since that flag only affects stub
    // *method* bodies, not static *field* initializers). downloadImageFromUrl() dereferences it
    // unconditionally, so every test exercising that method needs it patched to a real, working
    // Pattern first. This uses sun.misc.Unsafe to set the field directly (bypasses the normal
    // "cannot set final field" reflection check) - purely test-side, does not touch production
    // code.
    ensureWebUrlPatternIsUsable()

    every { contextProvider.context } returns fakeContext

    coEvery { noteRepository.getById(any()) } returns null
    coEvery { reminderV2Repository.getByNoteId(any()) } returns emptyList()
    coEvery { reminderV2Repository.getById(any()) } returns null

    every { dateTimeManager.getTime(any<LocalTime>()) } returns "10:00"
    every { dateTimeManager.getDate(any<LocalDate>()) } returns "24 Jul 2026"
    every { dateTimeManager.getNowGmtDateTime() } returns "2026-07-24 00:00:00.000+0000"
    every { dateTimeManager.fromGmtToLocal(any()) } returns null
    every { dateTimeManager.isCurrent(any<LocalDateTime>()) } returns true
    every { dateTimeManager.getGmtFromDateTime(any<LocalDateTime>()) } returns "2026-07-24 10:00:00.000+0000"
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }
    every { dateTimeManager.utcToLocal(any()) } answers { firstArg() }

    // Deterministic, internally-consistent fake color math - real blend/luminance computation is
    // NoteColorEngine's own concern, not this ViewModel's.
    every { noteColorEngine.getLastPalette() } returns 0
    every { noteColorEngine.getLastColorCode() } returns 2
    every { noteColorEngine.getLasterOpacity() } returns 80
    every { noteColorEngine.getColorCode(any(), any()) } answers { firstArg<Int>() * 100 + secondArg<Int>() }
    every { noteColorEngine.getLegacyColorCode(any()) } answers { firstArg<Int>() % 100 }
    every { noteColorEngine.getLegacyPalette(any()) } answers { firstArg<Int>() / 100 }
    every { noteColorEngine.colorsFor(any(), any()) } answers {
      NoteColorEngine.Colors(background = Color.White, content = Color.Black)
    }
    every { noteColorEngine.allColors() } returns listOf(Color.Red, Color.Green, Color.Blue)

    every { tagRepository.observeAll() } returns flowOf(emptyList())
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
  }

  private fun ensureWebUrlPatternIsUsable() {
    val field = android.util.Patterns::class.java.getDeclaredField("WEB_URL")
    field.isAccessible = true
    val existing = field.get(null)
    if (existing != null) return
    val unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe")
    unsafeField.isAccessible = true
    val unsafe = unsafeField.get(null)
    val unsafeClass = unsafe.javaClass
    val base = unsafeClass.getMethod("staticFieldBase", java.lang.reflect.Field::class.java).invoke(unsafe, field)
    val offset = unsafeClass.getMethod("staticFieldOffset", java.lang.reflect.Field::class.java).invoke(unsafe, field) as Long
    val pattern = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].\\S*$", Pattern.CASE_INSENSITIVE)
    unsafeClass.getMethod("putObject", Any::class.java, Long::class.javaPrimitiveType, Any::class.java)
      .invoke(unsafe, base, offset, pattern)
  }

  protected fun uiNoteEdit(
    id: String = "note-1",
    text: String = "",
    title: String = "",
    typeface: Int = 0,
    titleTypeface: Int = 0,
    titleFontSize: Int = 20,
    fontSize: Int = 16,
    colorPosition: Int = 0,
    colorPalette: Int = 0,
    opacity: Int = 100,
    images: List<com.elementary.tasks.core.data.ui.note.UiNoteImage> = emptyList(),
    isArchived: Boolean = false,
  ) = UiNoteEdit(
    id = id,
    text = text,
    typeface = typeface,
    title = title,
    titleTypeface = titleTypeface,
    titleFontSize = titleFontSize,
    images = images,
    colorPosition = colorPosition,
    colorPalette = colorPalette,
    opacity = opacity,
    fontSize = fontSize,
    isArchived = isArchived,
  )

  /**
   * Stubs [ImageDecoder.startDecoding] to synchronously invoke its `onLoading` callback with a
   * placeholder LOADING entry per uri, then its `onReady` callback for each uri (in order) with
   * whatever [resultFor] returns for that uri - mirroring the real decoder's two-phase callback
   * shape without touching a real ContentResolver.
   */
  protected fun stubImageDecoder(resultFor: (Uri) -> UiNoteImage) {
    every {
      imageDecoder.startDecoding(any(), any(), any(), any(), any())
    } answers {
      val uris = secondArg<List<Uri>>()
      val startCount = thirdArg<Int>()
      @Suppress("UNCHECKED_CAST")
      val onLoading = arg<(List<UiNoteImage>) -> Unit>(3)
      @Suppress("UNCHECKED_CAST")
      val onReady = arg<(Int, UiNoteImage) -> Unit>(4)
      val loading = uris.map { UiNoteImage(id = 0, fileName = "loading", state = UiNoteImageState.LOADING) }
      onLoading(loading)
      uris.forEachIndexed { index, uri -> onReady(index + startCount, resultFor(uri)) }
    }
  }

  protected fun buildViewModel(
    id: String? = null,
    sharedText: String? = null,
    sharedImageUris: List<String>? = null,
    fromIntentData: Boolean = false,
  ): NoteEditViewModel =
    NoteEditViewModel(
      id = id,
      sharedText = sharedText,
      sharedImageUris = sharedImageUris,
      fromIntentData = fromIntentData,
      imageDecoder = imageDecoder,
      dispatcherProvider = mockDispatcherProvider(),
      noteRepository = noteRepository,
      reminderV2Repository = reminderV2Repository,
      deleteReminderUseCase = deleteReminderUseCase,
      prefs = prefs,
      dateTimeManager = dateTimeManager,
      textProvider = textProvider,
      contextProvider = contextProvider,
      analyticsEventSender = analyticsEventSender,
      uiNoteEditAdapter = uiNoteEditAdapter,
      noteImageRepository = noteImageRepository,
      intentDataReader = intentDataReader,
      deleteNoteUseCase = deleteNoteUseCase,
      saveNoteUseCase = saveNoteUseCase,
      createSharedNoteFileUseCase = createSharedNoteFileUseCase,
      activateReminderUseCase = activateReminderUseCase,
      droppedContentParser = droppedContentParser,
      imagesSingleton = imagesSingleton,
      appWidgetUpdater = appWidgetUpdater,
      systemInfo = systemInfo,
      imageLoader = imageLoader,
      noteColorEngine = noteColorEngine,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      scheduleBackgroundWorkUseCase = scheduleBackgroundWorkUseCase,
    )
}
