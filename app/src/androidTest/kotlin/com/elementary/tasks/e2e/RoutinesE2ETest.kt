package com.elementary.tasks.e2e

import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.elementary.tasks.navigation.BottomNavActivity
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.feature.routine.edit.routineStepCardTestTag
import com.github.naz013.files.DataConverter
import com.github.naz013.repository.RoutineExecutionRepository
import com.github.naz013.repository.RoutineRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.repository.testfixtures.testRepositoryModule
import com.github.naz013.ui.common.R
import com.github.naz013.ui.routine.RoutineColors
import com.github.naz013.ui.routine.routineColorSliderTestTag
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

/**
 * Tier-B instrumented suite for the Routines feature ([docs/routines.md] §3's 22-case plan),
 * following the exact conventions [ReminderRecurrenceE2ETest]/[TodoEditorE2ETest] already
 * established: [createAndroidComposeRule] against the real [BottomNavActivity], [testRepositoryModule]
 * for an in-memory Room DB loaded once in [setUpClass], and [FakeNowDateTimeProvider] for the one
 * date-relative case (E19).
 *
 * **Feature-flag gate**: Routines is gated behind `RoutineConfig.isEnabled`
 * (`FeatureFlag.ROUTINE_ENABLED`, off by default - see `FeatureManager`/`Prefs`). No prior E2E test
 * had to deal with a flagged screen, so this class registers its own [FeatureFlags] override
 * ([TestFeatureFlags]) in [setUpClass], the same way [testRepositoryModule] overrides repositories:
 * `RoutineConfigImpl` is a plain `factory` that resolves `FeatureFlags` fresh via `get()` on every
 * call, so overriding the `FeatureFlags` binding before the first [BottomNavActivity] is created is
 * sufficient - no need to touch `FeatureFlag.ROUTINE_ENABLED`'s real production default. [enablesTheHomeTile]
 * is the first test below specifically because it's the thing every other test's navigation depends
 * on - if the flag override didn't work, every other test would fail at the first navigation step
 * anyway, but this makes that failure mode explicit and diagnosable on its own.
 *
 * **Execution-runner timing (E14-E18)**: `RoutineExecutionViewModel`'s ticker launches via
 * `viewModelScope.launch(dispatcherProvider.main())` and calls a real `delay(1000)` per tick (see
 * `docs/routines.md` §6.3's flag on this). That's a real hang risk in a *JVM unit test* driven by
 * `mockDispatcherProvider()`, where `dispatcherProvider.main()` resolves to plain
 * `Dispatchers.Unconfined` with no virtual-time scheduler to advance it. It is **not** the same risk
 * here: this is a real Android instrumented test running the production `DispatcherProvider`, so
 * `dispatcherProvider.main()` resolves to the real `Dispatchers.Main` and `delay(1000)` really does
 * complete after one real second, the same as any other coroutine on a real Looper. The tests below
 * use short (3-4s) step durations and generous (15-20s) `composeRule.waitUntil` timeouts rather than
 * any virtual-time control - bounded, not hang-prone - matching option (b) from this task's brief
 * rather than building new `TestDispatcher` injection plumbing for a single test file.
 *
 * All tests share one [composeRule]/one in-memory database for the whole class (see
 * [ReminderRecurrenceE2ETest]'s identical kdoc for why `@BeforeClass` is required here rather than
 * `@Before`), so - like that file - each test that needs to identify "the routine/tag I just made"
 * snapshots existing ids first and diffs afterward, or gives its routine a unique
 * [UUID]-suffixed title, rather than assuming an empty database.
 */
@RunWith(AndroidJUnit4::class)
class RoutinesE2ETest : KoinTest {

  @get:Rule
  val composeRule = createAndroidComposeRule<BottomNavActivity>()

  @get:Rule
  val notificationPermissionRule: TestRule =
    if (Build.VERSION.SDK_INT >= 33) {
      GrantPermissionRule.grant("android.permission.POST_NOTIFICATIONS")
    } else {
      TestRule { base, _ -> base }
    }

  private val routineRepository: RoutineRepository by inject()
  private val routineExecutionRepository: RoutineExecutionRepository by inject()
  private val tagRepository: TagRepository by inject()
  private val tagAssignmentRepository: TagAssignmentRepository by inject()
  private val dataConverter: DataConverter by inject()

  @Before
  fun resetFakeClock() {
    fakeNowDateTimeProvider.setDate(LocalDate.now())
  }

  private fun r(resId: Int): String = composeRule.activity.getString(resId)

  private fun r(
    resId: Int,
    vararg args: Any,
  ): String = composeRule.activity.getString(resId, *args)

  // ---------------------------------------------------------------------------------------
  // Navigation helpers
  // ---------------------------------------------------------------------------------------

  /** See [ReminderRecurrenceE2ETest.dismissPrivacyBannerIfShown] - same first-run banner, same
   *  reasoning for why it has to be dismissed defensively before any Home navigation. */
  private fun dismissPrivacyBannerIfShown() {
    val acceptLabel = r(R.string.accept)
    if (composeRule.onAllNodesWithText(acceptLabel).fetchSemanticsNodes().isNotEmpty()) {
      composeRule.onNodeWithText(acceptLabel).performClick()
      composeRule.waitForIdle()
    }
  }

  /** [BottomNavActivity] is declared `android:launchMode="singleInstance"` (`AndroidManifest.xml`)
   *  - relaunching it for a fresh test method's own `composeRule` does *not* get a new Activity
   *  instance (or a fresh in-memory nav backstack) the way a normal launch mode would, it's
   *  redirected to whichever instance/backstack state the *previous* test method's [composeRule]
   *  left behind (confirmed live: `homeTile_navigatesToRoutinesList` alone passes, but fails inside
   *  the full class run specifically at this wait, because an earlier test in the run ended
   *  somewhere other than Home - e.g. mid-Insights, or on a focus-runner "Finished" screen).
   *  [ReminderRecurrenceE2ETest]'s tests never hit this because every one of them happens to end
   *  back on Home by construction (Save always pops the builder back to Home); several tests in
   *  *this* file end elsewhere (List, a Finished summary, Settings/Insights), so this suite needs
   *  its own explicit "get back to Home" step other files haven't needed. Presses back (capped, and
   *  only when [R.string.acc_add] - Home's own "Add" menu, not present on any other Routines/
   *  Settings screen - isn't already visible) until Home is reached, rather than assuming a fresh
   *  Activity always starts there. */
  private fun ensureOnHomeScreen() {
    val addLabel = r(R.string.acc_add)
    fun onHome() = composeRule.onAllNodesWithContentDescription(addLabel).fetchSemanticsNodes().isNotEmpty()
    repeat(15) {
      dismissPrivacyBannerIfShown()
      // Give a loading Home screen a real chance to settle before concluding we're on some other
      // screen and need to press back - a false negative here would press back on an Activity with
      // nothing left to pop, which finishes it outright (confirmed live: on a genuinely fresh
      // launch, Home's own async tile load can transiently leave `addLabel` absent for a moment;
      // an over-eager first-iteration back-press in that window was exactly what caused a "Cannot
      // run onActivity since Activity has been destroyed already" failure in a *later*, unrelated
      // test - `singleInstance` means there's nothing below Home in this task for Back to pop to).
      val settled = runCatching { composeRule.waitUntil(timeoutMillis = 3_000) { onHome() } }.isSuccess
      if (settled) return
      runCatching { Espresso.pressBack() }
      composeRule.waitForIdle()
      // A plain waitForIdle() doesn't reliably outlast Nav3's own cross-fade/scale transition
      // (AppNavGraph.kt's NAV_ANIM_FADE_DURATION_MS) between screens - a short real wait here is
      // cheap insurance against the next iteration's checks racing that animation.
      Thread.sleep(300)
    }
    check(onHome()) { "Could not get back to Home after 15 back-presses" }
  }

  /** Home screen -> "Routines" header tile (`GetNavigationItemsUseCase.getRoutineItem`, only added
   *  to the grid at all when [routineConfig.isEnabled][com.github.naz013.logic.routine.RoutineConfig]
   *  is true - see this class's kdoc for how that's forced on for this suite). */
  private fun navigateToRoutinesList() {
    ensureOnHomeScreen()
    val routinesLabel = r(R.string.routines)
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithText(routinesLabel).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText(routinesLabel).performClick()
    composeRule.waitForIdle()
    // Confirmed live (via a diagnostic node-text dump): this screen's own composition - including
    // its ViewModel's private `selectedTagId`/`searchQuery`/`sortOrder` state - can survive a
    // revisit within the same class run instead of being recreated from scratch, so a tag filter
    // another earlier test selected and never cleared can silently still be narrowing this list
    // down to a handful of routines. Clearing it via the "All" chip (`TagFilterRow`'s own
    // clear-filter affordance) is a no-op when nothing was filtered, so it's safe to do
    // unconditionally on every arrival here rather than only when a stale filter is suspected.
    val allLabel = r(R.string.all)
    if (composeRule.onAllNodesWithText(allLabel).fetchSemanticsNodes().isNotEmpty()) {
      composeRule.onNodeWithText(allLabel).performClick()
      composeRule.waitForIdle()
    }
  }

  /** Routines list -> "+" (`onAddClick`, contentDescription = [R.string.new_routine]) -> editor in
   *  create mode. */
  private fun navigateToNewRoutineEditor() {
    navigateToRoutinesList()
    val addLabel = r(R.string.new_routine)
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithContentDescription(addLabel).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithContentDescription(addLabel).performClick()
    composeRule.waitForIdle()
  }

  private fun captureExistingRoutineIds(): Set<String> = runBlocking { routineRepository.getAll().map { it.id }.toSet() }

  private fun awaitNewRoutine(idsBefore: Set<String>): Routine {
    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { routineRepository.getAll().any { it.id !in idsBefore } }
    }
    return runBlocking { routineRepository.getAll().first { it.id !in idsBefore } }
  }

  private fun setRoutineTitle(title: String) {
    // The title OutlinedTextField is the first (index 0) set-text-action node on the editor -
    // the description field is index 1, step title fields start at index 2+ (see
    // routineStepCardTestTag's kdoc for why step rows are scoped by tag instead of index).
    composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)[0].performTextReplacement(title)
    composeRule.waitForIdle()
  }

  /** Drives [com.github.naz013.ui.routine.RoutineColorPicker]'s `ColorSlider` - a bare `Canvas`
   *  with a raw `pointerInput` gesture and no semantics `OnClick` action, so `performClick()` can't
   *  target it even with a tag. Locates it by [routineColorSliderTestTag] and computes a tap
   *  position from the node's own reported width instead - a coordinate *relative to the element*,
   *  not a fixed screen percentage, so it stays correct regardless of device size (see
   *  `docs/e2e-testing.md` §1c's caution about screen-percentage coordinates specifically). */
  private fun selectRoutineColor(index: Int) {
    val totalColors = RoutineColors.ALL.size
    composeRule.onNodeWithTag(routineColorSliderTestTag).performTouchInput {
      val itemWidth = width / totalColors.toFloat()
      click(Offset(itemWidth * (index + 0.5f), height / 2f))
    }
    composeRule.waitForIdle()
  }

  private val routineStepCardTagPrefix = routineStepCardTestTag("")

  private fun routineStepCardMatcher(): SemanticsMatcher = SemanticsMatcher("routine step card") { node ->
    node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(routineStepCardTagPrefix) == true
  }

  private fun currentRoutineStepCount(): Int =
    composeRule.onAllNodes(routineStepCardMatcher(), useUnmergedTree = true).fetchSemanticsNodes().size

  /** Taps "Add step" (`RoutineEditViewModel.onAddStepClick` always appends, and rows stay fully
   *  composed in a plain `Column.verticalScroll` - never a lazy layout - regardless of scroll
   *  position) and returns the new row's own full tag string, found as the [index]-th (top to
   *  bottom) node whose tag starts with [routineStepCardTagPrefix] - see [routineStepCardTestTag]'s
   *  kdoc for why a prefix-then-order lookup is needed instead of a tag known ahead of time.
   *
   *  The "Add step" `TextButton` sits at the bottom of a plain scrollable `Column`, below however
   *  many rows already exist - `performScrollTo()` first (confirmed live to matter once 2+ steps
   *  already exist: without it, the button's on-screen position can land outside what
   *  `performClick()`'s touch-injection actually hits, so the click silently does nothing and the
   *  row count never advances) and [waitUntil] on the row count actually increasing, rather than
   *  trusting a single `waitForIdle()`, makes this robust to that. */
  private fun addRoutineStep(index: Int): String {
    val countBefore = currentRoutineStepCount()
    composeRule.onNodeWithText(r(R.string.add_step)).performScrollTo().performClick()
    composeRule.waitForIdle()
    composeRule.waitUntil(timeoutMillis = 5_000) { currentRoutineStepCount() > countBefore }
    val sorted = composeRule
      .onAllNodes(routineStepCardMatcher(), useUnmergedTree = true)
      .fetchSemanticsNodes()
      .sortedBy { it.boundsInRoot.top }
    return sorted[index].config.getOrNull(SemanticsProperties.TestTag)!!
  }

  private fun setStepTitle(cardTag: String, title: String) {
    composeRule
      .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag(cardTag)), useUnmergedTree = true)
      .performTextReplacement(title)
    composeRule.waitForIdle()
  }

  /** Mirrors `RoutineStepRow`'s own (private) `durationPresetLabel` composable exactly - can't
   *  import it (it's `private` to `RoutineEditScreen.kt`), so this duplicates its formatting. */
  private fun durationPresetLabelText(seconds: Int): String = when (seconds) {
    0 -> r(R.string.duration_none)
    else -> if (seconds % 60 == 0) "${seconds / 60}m" else "${seconds}s"
  }

  private fun selectStepDuration(cardTag: String, seconds: Int) {
    val label = durationPresetLabelText(seconds)
    composeRule
      .onNode(hasText(label) and hasAnyAncestor(hasTestTag(cardTag)), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
  }

  /** Opens the step's time chip (label = [R.string.no_time] until a time is picked) and drives the
   *  Material3 dial `TimePicker` dialog (`DateTimePickerState.kt`'s `showTimePicker`, `is24Hour =
   *  true` by default and never overridden by `RoutineStepRow`). Unlike
   *  [ReminderRecurrenceE2ETest.setTime]'s 12-hour dial (`"10 o'clock"`/`"30 minutes"`
   *  content-descriptions, plus an "AM"/"PM" segment), this 24-hour dial's positions are described
   *  as plain `"<hour> hours"` (confirmed live via a diagnostic node dump - `"...cd=13 hours..."`
   *  through `"...cd=23 hours..."`, one flat 0-23 range) with no AM/PM segment at all, so both the
   *  format string and the missing AM tap are deliberate differences from that helper, not
   *  omissions. [minute] is deliberately always 30 - the one minute-dial value already confirmed
   *  live to work in [ReminderRecurrenceE2ETest.setTime]; only [hour] varies between calls here. */
  private fun setStepTime(cardTag: String, hour: Int, minute: Int = 30) {
    composeRule
      .onNode(hasText(r(R.string.no_time)) and hasAnyAncestor(hasTestTag(cardTag)), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
    composeRule
      .onNode(hasContentDescription("$hour hours") and !hasText(hour.toString()), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
    composeRule
      .onNode(hasContentDescription("Select minutes"), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
    composeRule
      .onNode(hasContentDescription("$minute minutes") and !hasText(minute.toString()), useUnmergedTree = true)
      .performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithText(r(R.string.ok)).performClick()
    composeRule.waitForIdle()
  }

  private fun tapSaveRoutine() {
    composeRule.onNodeWithText(r(R.string.save)).performClick()
  }

  private fun clickText(text: String) {
    composeRule.onNodeWithText(text, useUnmergedTree = true).performClick()
    composeRule.waitForIdle()
  }

  /** Like [clickText], but scrolls the node into view first - needed for anything below the fold
   *  in the editor's own scrollable `Column` (recurrence chips, tag chips), confirmed live to
   *  matter the same way [addRoutineStep]'s own kdoc describes: `performClick()`'s touch injection
   *  silently misses a node whose on-screen position falls outside the visible viewport instead of
   *  failing loudly, so a plain `clickText` on those elements can appear to succeed while doing
   *  nothing. Not used for popup/dropdown-menu items (`AppDropdownMenu`) - those aren't inside a
   *  scrollable container, and `performScrollTo()` throws if there is no scroll parent to find. */
  private fun scrollAndClickText(text: String) {
    val node = composeRule.onNodeWithText(text, useUnmergedTree = true)
    node.performScrollTo()
    composeRule.waitForIdle()
    node.performClick()
    composeRule.waitForIdle()
  }

  /** Same as [scrollAndClickText], but additionally requires the matched node to be a
   *  `Role.Checkbox` (Material3's `FilterChip` sets this role) - needed on the routines list
   *  screen, where a tagged routine's own `RoutineCard` and `TagFilterRow`'s clickable filter chip
   *  can both show the exact same tag name text at the same time, which makes a plain by-text
   *  lookup ambiguous. `hasClickAction()` alone doesn't disambiguate them either (confirmed live,
   *  the harder way): `RoutineCard`'s own root `Surface` is `combinedClickable`, and once merged,
   *  its merged node's `OnClick` action *and* its merged text (title + step/duration badges + any
   *  `TagChipRow` tag names, all concatenated) both survive - so `hasText(tagName) and
   *  hasClickAction()` matched the whole card too, not just the filter chip. `Role.Checkbox` is
   *  unique to the chip. */
  private fun scrollAndClickClickableText(text: String) {
    val node = composeRule.onNode(
      hasText(text) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox),
    )
    node.performScrollTo()
    composeRule.waitForIdle()
    node.performClick()
    composeRule.waitForIdle()
  }

  private val routineCardTagPrefix = "routine_card_"

  private fun routineCardMatcher(): SemanticsMatcher = SemanticsMatcher("routine card") { node ->
    node.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(routineCardTagPrefix) == true
  }

  private fun visibleRoutineCardIdsInOrder(): List<String> =
    composeRule
      .onAllNodes(routineCardMatcher(), useUnmergedTree = true)
      .fetchSemanticsNodes()
      .sortedBy { it.boundsInRoot.top }
      .mapNotNull { it.config.getOrNull(SemanticsProperties.TestTag)?.removePrefix(routineCardTagPrefix) }

  /** Scrolls the routines `LazyColumn` until the card keyed by [id] (`items(listState.routines, key
   *  = { it.id })`) is composed - unlike a plain `onNodeWithTag(...).performScrollTo()`, this also
   *  works when the card isn't already inside whatever window the `LazyColumn` happens to have
   *  composed, which a large enough accumulated list (this class's shared database - see
   *  [navigateToRoutinesList]'s kdoc) can easily exceed. `.onLast()` disambiguates from
   *  `TagFilterRow`'s own horizontal `LazyRow`, which also exposes a keyed scroll action once any
   *  tag exists - same reasoning as the identical trick in [navigateToRoutinesList]. */
  private fun scrollToRoutineCard(id: String) {
    composeRule.onAllNodes(hasScrollToIndexAction(), useUnmergedTree = true).onLast().performScrollToKey(id)
    composeRule.waitForIdle()
  }

  /** From the routines list, taps the card whose title is [title] (`RoutineCard`'s own `Text` -
   *  unique as long as callers use a fresh [UUID]-suffixed title, same convention this whole file
   *  follows) to open its preview screen. The card may be scrolled off the (real, `LazyColumn`-
   *  backed, unlike the editor's plain `verticalScroll`) list's viewport by the time this runs -
   *  this class's shared in-memory database accumulates one row per routine created by every test
   *  that ran before this one in the same class run - so this scrolls to it first. Retries the tap
   *  (checking for [R.string.edit]'s content description, Preview-only) rather than trusting a
   *  single attempt, the same reasoning and pattern
   *  [ReminderRecurrenceE2ETest.navigateToEditReminderBuilder]'s kdoc documents for its own
   *  confirmed-live Home -> Preview navigation flake. */
  private fun navigateToRoutinePreview(title: String) {
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
    }
    var reachedPreview = false
    repeat(3) {
      if (reachedPreview) return@repeat
      composeRule.onNodeWithText(title).performScrollTo().performClick()
      composeRule.waitForIdle()
      reachedPreview = runCatching {
        composeRule.waitUntil(timeoutMillis = 5_000) {
          composeRule.onAllNodesWithContentDescription(r(R.string.edit)).fetchSemanticsNodes().isNotEmpty()
        }
      }.isSuccess
    }
    check(reachedPreview) { "Did not land on the routine preview screen for '$title' after 3 attempts" }
  }

  private fun navigateToRoutineEditFromPreview() {
    composeRule.onNodeWithContentDescription(r(R.string.edit)).performClick()
    composeRule.waitForIdle()
  }

  private fun openPreviewOverflowMenu() {
    composeRule.onNodeWithContentDescription(r(R.string.more_options)).performClick()
    composeRule.waitForIdle()
  }

  private fun openEditOverflowMenu() {
    composeRule.onNodeWithContentDescription(r(R.string.more_options)).performClick()
    composeRule.waitForIdle()
  }

  /** Creates a routine with [title] and [stepTitles] (each with a 5-minute duration) through the
   *  real editor UI and saves it, returning the persisted [Routine]. Shared setup for every test
   *  below that just needs "some existing routine" rather than being about the editor itself. */
  private fun createRoutineViaUi(
    title: String,
    stepTitles: List<String> = listOf("Step 1"),
  ): Routine {
    val idsBefore = captureExistingRoutineIds()
    navigateToNewRoutineEditor()
    setRoutineTitle(title)
    stepTitles.forEachIndexed { index, stepTitle ->
      val cardTag = addRoutineStep(index)
      setStepTitle(cardTag, stepTitle)
      selectStepDuration(cardTag, 300)
    }
    tapSaveRoutine()
    return awaitNewRoutine(idsBefore)
  }

  // ---------------------------------------------------------------------------------------
  // E1-E6: Editor
  // ---------------------------------------------------------------------------------------

  @Test
  fun createRoutine_withTitleStepsAndColor() {
    val idsBefore = captureExistingRoutineIds()
    val title = "Morning routine ${UUID.randomUUID()}"
    navigateToNewRoutineEditor()

    setRoutineTitle(title)
    selectRoutineColor(3)

    val stepTitles = listOf("Stretch", "Journal", "Meditate")
    stepTitles.forEachIndexed { index, stepTitle ->
      val cardTag = addRoutineStep(index)
      setStepTitle(cardTag, stepTitle)
      selectStepDuration(cardTag, 300)
    }

    tapSaveRoutine()

    val created = awaitNewRoutine(idsBefore)
    assertEquals(title, created.title)
    assertEquals(3, created.color)
    assertEquals(3, created.steps.size)
    assertEquals(stepTitles.toSet(), created.steps.map { it.title }.toSet())

    // The colored card actually renders in the list - can't assert the rendered pixel color
    // through the semantics tree (Card's containerColor isn't exposed there), so this proves the
    // card exists and is displayed for the persisted routine id instead, the same scope reduction
    // A5/B15 already apply elsewhere in this suite for "can't observe through semantics" cases.
    // Scrolled to first: RoutinesListScreen is a real LazyColumn (unlike the editor's plain
    // verticalScroll Column), and this class's shared database accumulates one row per routine
    // every earlier test in the run created, so the freshly-created card isn't guaranteed to
    // already be within the initially-composed viewport.
    composeRule.onNodeWithTag("routine_card_${created.id}").performScrollTo().assertIsDisplayed()
  }

  /** E2: two timed steps, added in reverse chronological order (16:30 before 08:30), must persist
   *  in chronological order via [com.github.naz013.domain.routine.RoutineStepComparator] regardless
   *  of add-order - asserted both directly against the repository (`sortedSteps`) and against the
   *  preview screen's own rendered row order, which is what the row order in
   *  `RoutinePreviewViewModel.toReadyState` (`sortedSteps.map { ... }`) actually drives. */
  @Test
  fun routineEditor_stepScheduledTimes_autoSorts() {
    val idsBefore = captureExistingRoutineIds()
    val title = "Sorted routine ${UUID.randomUUID()}"
    navigateToNewRoutineEditor()
    setRoutineTitle(title)

    val laterCardTag = addRoutineStep(0)
    setStepTitle(laterCardTag, "Afternoon step")
    setStepTime(laterCardTag, hour = 16)

    val earlierCardTag = addRoutineStep(1)
    setStepTitle(earlierCardTag, "Morning step")
    setStepTime(earlierCardTag, hour = 8)

    tapSaveRoutine()

    val created = awaitNewRoutine(idsBefore)
    assertEquals(listOf("Morning step", "Afternoon step"), created.sortedSteps.map { it.title })

    navigateToRoutinePreview(title)
    val morningTop = composeRule.onNodeWithText("Morning step").fetchSemanticsNode().boundsInRoot.top
    val afternoonTop = composeRule.onNodeWithText("Afternoon step").fetchSemanticsNode().boundsInRoot.top
    assertTrue("Expected the 08:00 step to render above the 16:00 step", morningTop < afternoonTop)
  }

  @Test
  fun routineEditor_reorderUntimedSteps() {
    val idsBefore = captureExistingRoutineIds()
    val title = "Reorder routine ${UUID.randomUUID()}"
    navigateToNewRoutineEditor()
    setRoutineTitle(title)

    val firstCardTag = addRoutineStep(0)
    setStepTitle(firstCardTag, "First step")
    val secondCardTag = addRoutineStep(1)
    setStepTitle(secondCardTag, "Second step")

    // Move the second (bottom) row up one position via its own move-up button, scoped to that
    // row's tag since the icon button's contentDescription (move_step_up) repeats on every row.
    composeRule
      .onNode(
        hasContentDescription(r(R.string.move_step_up)) and hasAnyAncestor(hasTestTag(secondCardTag)),
        useUnmergedTree = true,
      )
      .performClick()
    composeRule.waitForIdle()

    tapSaveRoutine()

    val created = awaitNewRoutine(idsBefore)
    assertEquals(listOf("Second step", "First step"), created.steps.sortedBy { it.order }.map { it.title })
  }

  @Test
  fun routineEditor_tagAssignment() {
    val tagA = Tag(name = "Health ${UUID.randomUUID()}", color = 0xFF00FF00.toInt())
    val tagB = Tag(name = "Focus ${UUID.randomUUID()}", color = 0xFFFF00FF.toInt())
    runBlocking {
      tagRepository.save(tagA)
      tagRepository.save(tagB)
    }

    val idsBefore = captureExistingRoutineIds()
    val title = "Tagged routine ${UUID.randomUUID()}"
    navigateToNewRoutineEditor()
    setRoutineTitle(title)
    val cardTag = addRoutineStep(0)
    setStepTitle(cardTag, "Step 1")

    scrollAndClickText(tagA.name)
    scrollAndClickText(tagB.name)

    tapSaveRoutine()

    val created = awaitNewRoutine(idsBefore)
    val assignedTags = runBlocking {
      tagAssignmentRepository.getTagsForItem(created.id, TaggedItemType.ROUTINE)
    }
    assertEquals(setOf(tagA.id, tagB.id), assignedTags.map { it.id }.toSet())

    // Chips render on the card back on the list screen (LazyColumn - scroll to the card first,
    // same reasoning as createRoutine_withTitleStepsAndColor's card-tag assertion above).
    composeRule.onNodeWithTag("routine_card_${created.id}").performScrollTo()
    composeRule.onNodeWithText(tagA.name).assertIsDisplayed()
    composeRule.onNodeWithText(tagB.name).assertIsDisplayed()
  }

  /** The 22-case plan's literal description ("Pin from the editor") doesn't match the shipped
   *  screen - `RoutineEditScreen.kt` has no pin control anywhere (`RoutineEditState.isPinned`
   *  exists but nothing in the composable reads or toggles it). The only reachable pin control is
   *  the Preview screen's overflow menu (`RoutinePreviewViewModel.onPinToggleClick`,
   *  `OverflowAction.TOGGLE_PIN`) - this test uses that real path instead, the same kind of
   *  "corrected against the actual screen" note `docs/e2e-testing.md` already carries for A11/D9/G9. */
  @Test
  fun routineEditor_pinToggling() {
    val title = "Pin routine ${UUID.randomUUID()}"
    val created = createRoutineViaUi(title)
    assertFalse(created.isPinned)

    navigateToRoutinePreview(title)
    openPreviewOverflowMenu()
    clickText(r(R.string.pin))

    composeRule.waitUntil(timeoutMillis = 5_000) {
      runBlocking { routineRepository.getById(created.id)?.isPinned == true }
    }
    val pinned = runBlocking { routineRepository.getById(created.id) }
    assertTrue(pinned?.isPinned == true)

    // Pinned sorts to the top of the list regardless of sort order (RoutinesListViewModel:
    // compareByDescending<Routine> { it.isPinned }.then(...)).
    val otherTitle = "Unrelated routine ${UUID.randomUUID()}"
    createRoutineViaUi(otherTitle)
    navigateToRoutinesList()
    val order = visibleRoutineCardIdsInOrder()
    assertEquals(created.id, order.first())
  }

  @Test
  fun routineEditor_deleteRoutine_cleansUpData() {
    val title = "Delete routine ${UUID.randomUUID()}"
    val created = createRoutineViaUi(title)

    navigateToRoutinePreview(title)
    navigateToRoutineEditFromPreview()
    openEditOverflowMenu()
    clickText(r(R.string.delete))

    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { routineRepository.getById(created.id) == null }
    }
    val stillExists = runBlocking { routineRepository.getById(created.id) }
    assertEquals(null, stillExists)

    val tagsAfterDelete = runBlocking {
      tagAssignmentRepository.getTagsForItem(created.id, TaggedItemType.ROUTINE)
    }
    assertTrue(tagsAfterDelete.isEmpty())

    // Deleting navigates Back to the (now stale) Preview entry, not to the list - its ViewModel
    // isn't destroyed by this navigation and never re-reads the repository on its own, so it can
    // keep showing its last-loaded Ready state with the old title for a while. The list screen's
    // own RoutineRepository.observeAll() Flow does react, so this needs to get there rather than
    // asserting on whatever Preview happens to still be showing. Goes via [navigateToRoutinesList]
    // (Home -> Routines tile) rather than a single raw `Espresso.pressBack()` deliberately - a
    // lone back-press from here was confirmed live to occasionally over-pop past List and (since
    // `BottomNavActivity` is `android:launchMode="singleInstance"` and the root of its own task,
    // per this class's `ensureOnHomeScreen` kdoc) tear down the Activity outright, which then
    // surfaced as an unrelated `NullPointerException: Cannot run onActivity since Activity has
    // been destroyed already` failure in whichever test ran next.
    navigateToRoutinesList()
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isEmpty()
    }
    composeRule.onAllNodesWithText(title).assertCountEquals(0)
  }

  // ---------------------------------------------------------------------------------------
  // E7-E10: List screen
  // ---------------------------------------------------------------------------------------

  /** Also the first real proof the [FeatureFlags] override in [setUpClass] actually works - every
   *  other test's navigation depends on the "Routines" tile existing at all.
   *
   *  Doesn't compare the *rendered* card count against `routineRepository.getAll().size` the way
   *  an earlier version of this test did - `RoutinesListScreen` is a real `LazyColumn`, and this
   *  class's shared in-memory database accumulates one row per routine every earlier test in the
   *  class run created, so by the time this test runs there can easily be more total routines than
   *  a `LazyColumn` keeps composed at once, and that mismatch (not a navigation failure) is what
   *  made the count comparison flaky. Asserting that the just-created routine's own card is
   *  reachable (scrolled to) on the list this tile opened is the part that's actually specific to
   *  *this* test, and is robust regardless of how large the shared database has grown by now. */
  @Test
  fun homeTile_navigatesToRoutinesList() {
    val created = createRoutineViaUi("Home tile routine ${UUID.randomUUID()}")

    navigateToRoutinesList()

    composeRule.onNodeWithTag("routine_card_${created.id}").performScrollTo().assertIsDisplayed()
  }

  @Test
  fun routinesList_searchQuery_filtersCards() {
    val matchTitle = "Findable routine ${UUID.randomUUID()}"
    val otherTitle = "Different habit ${UUID.randomUUID()}"
    val match = createRoutineViaUi(matchTitle)
    val other = createRoutineViaUi(otherTitle)

    navigateToRoutinesList()
    // Confirmed live (via a diagnostic node-text dump): a *different* test (E19,
    // routineRecurrence_newCycle_autoResetsSteps) pins the fake clock forward to 2027 before
    // saving its own routine, so that routine's persisted createdAt permanently outranks every
    // later, real-clock (2026) routine under this list's default "most recent first" sort for the
    // rest of this class's shared-database run - not a bug in either test, just a real interaction
    // between two independent design choices (a fake-clock test and a shared in-memory DB) that
    // pushes these two freshly-created cards further down than a plain `performScrollTo()` (which
    // only searches whatever the LazyColumn already has composed) can reliably reach.
    // performScrollToKey (via scrollToRoutineCard) scrolls until the keyed item exists instead.
    scrollToRoutineCard(match.id)
    composeRule.onNodeWithText(matchTitle).assertIsDisplayed()
    scrollToRoutineCard(other.id)
    composeRule.onNodeWithText(otherTitle).assertIsDisplayed()

    composeRule.onNode(hasSetTextAction(), useUnmergedTree = true).performTextInput("Findable")
    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText(otherTitle).fetchSemanticsNodes().isEmpty()
    }
    composeRule.onNodeWithText(matchTitle).performScrollTo().assertIsDisplayed()
    composeRule.onAllNodesWithText(otherTitle).assertCountEquals(0)
  }

  @Test
  fun routinesList_tagFilterRow_filtersByTag() {
    val tag = Tag(name = "FilterTag ${UUID.randomUUID()}", color = 0xFF123456.toInt())
    runBlocking { tagRepository.save(tag) }

    val taggedTitle = "Tagged for filter ${UUID.randomUUID()}"
    val untaggedTitle = "Not tagged ${UUID.randomUUID()}"

    val idsBefore = captureExistingRoutineIds()
    navigateToNewRoutineEditor()
    setRoutineTitle(taggedTitle)
    val cardTag = addRoutineStep(0)
    setStepTitle(cardTag, "Step 1")
    scrollAndClickText(tag.name)
    tapSaveRoutine()
    awaitNewRoutine(idsBefore)

    createRoutineViaUi(untaggedTitle)

    navigateToRoutinesList()
    composeRule.onNodeWithText(taggedTitle).performScrollTo().assertIsDisplayed()
    composeRule.onNodeWithText(untaggedTitle).performScrollTo().assertIsDisplayed()

    // TagFilterRow is a LazyRow that accumulates one chip per tag ever created across this whole
    // shared-database test class run (see this class's kdoc), so the target chip may need a
    // horizontal scroll by the time this test runs, not just the vertical scroll
    // scrollAndClickText was originally written for - performScrollTo() handles both axes.
    // scrollAndClickClickableText (not plain scrollAndClickText) because this tagged routine's own
    // card also renders the same tag name via the (non-interactive) TagChipRow - see that helper's
    // kdoc for why a plain by-text lookup is ambiguous here.
    scrollAndClickClickableText(tag.name)

    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText(untaggedTitle).fetchSemanticsNodes().isEmpty()
    }
    composeRule.onNodeWithText(taggedTitle).performScrollTo().assertIsDisplayed()
    composeRule.onAllNodesWithText(untaggedTitle).assertCountEquals(0)
  }

  /** Seeds a pinned routine directly through the repository - deterministic, and sidesteps
   *  re-testing the editor's own pin flow, already covered by [routineEditor_pinToggling] - whose
   *  title/`createdAt` are both deliberately chosen to sort *last* under either raw sort mode ("Zzz"
   *  alphabetically, 30 days old by "creation date"), so it only ever lands at the top of the list
   *  because pinning overrides sort order (`RoutinesListViewModel`:
   *  `compareByDescending<Routine> { it.isPinned }.then(...)`), not by coincidence. Checking the
   *  very first (always-composed, scroll-independent) card is what makes this robust regardless of
   *  how large this class's shared, ever-growing database has gotten by the time this test runs -
   *  an earlier version of this test instead searched for two specific *unpinned* routines by name
   *  across the whole (real `LazyColumn`, not fully composed) list, which went flaky once enough
   *  other tests had accumulated enough rows that both routines were never simultaneously
   *  composed. */
  @Test
  fun routinesList_sortOrder_toggleDateAndName() {
    val now = LocalDateTime.now()
    val pinned = Routine(
      title = "Zzz pinned ${UUID.randomUUID()}",
      isPinned = true,
      steps = listOf(RoutineStep(title = "Step", order = 0)),
      createdAt = now.minusDays(30),
      updatedAt = now.minusDays(30),
    )
    runBlocking { routineRepository.save(pinned) }
    createRoutineViaUi("Aaa unpinned ${UUID.randomUUID()}")

    navigateToRoutinesList()
    composeRule.waitUntil(timeoutMillis = 10_000) { visibleRoutineCardIdsInOrder().isNotEmpty() }
    assertEquals(pinned.id, visibleRoutineCardIdsInOrder().first())

    composeRule.onNodeWithContentDescription(r(R.string.sort)).performClick()
    composeRule.waitForIdle()
    clickText(r(R.string.sort_by_name))
    composeRule.waitUntil(timeoutMillis = 5_000) { visibleRoutineCardIdsInOrder().firstOrNull() == pinned.id }
    assertEquals(pinned.id, visibleRoutineCardIdsInOrder().first())
  }

  // ---------------------------------------------------------------------------------------
  // E11-E13: Preview screen
  // ---------------------------------------------------------------------------------------

  @Test
  fun routinePreview_displaysColoredBannerAndSteps() {
    val tag = Tag(name = "PreviewTag ${UUID.randomUUID()}", color = 0xFF445566.toInt())
    runBlocking { tagRepository.save(tag) }

    val title = "Preview routine ${UUID.randomUUID()}"
    val idsBefore = captureExistingRoutineIds()
    navigateToNewRoutineEditor()
    setRoutineTitle(title)
    selectRoutineColor(2)
    val step1Tag = addRoutineStep(0)
    setStepTitle(step1Tag, "First")
    selectStepDuration(step1Tag, 300)
    val step2Tag = addRoutineStep(1)
    setStepTitle(step2Tag, "Second")
    selectStepDuration(step2Tag, 600)
    scrollAndClickText(tag.name)
    tapSaveRoutine()
    val created = awaitNewRoutine(idsBefore)

    navigateToRoutinePreview(title)

    composeRule.onNodeWithText(title).assertIsDisplayed()
    composeRule.onNodeWithText(tag.name).assertIsDisplayed()
    composeRule.onNodeWithText("First").assertIsDisplayed()
    composeRule.onNodeWithText("Second").assertIsDisplayed()
    assertEquals(2, created.steps.size)
  }

  @Test
  fun routinePreview_stepCheckbox_togglesState() {
    val title = "Checkbox routine ${UUID.randomUUID()}"
    val created = createRoutineViaUi(title, listOf("Only step"))

    navigateToRoutinePreview(title)
    composeRule.onAllNodes(isToggleable(), useUnmergedTree = true)[0].performClick()

    composeRule.waitUntil(timeoutMillis = 5_000) {
      runBlocking { routineRepository.getById(created.id)?.steps?.first()?.isCompleted == true }
    }
    val updated = runBlocking { routineRepository.getById(created.id) }
    assertTrue(updated?.steps?.first()?.isCompleted == true)
  }

  @Test
  fun routinePreview_resetSteps_unchecksAll() {
    val title = "Reset routine ${UUID.randomUUID()}"
    val created = createRoutineViaUi(title, listOf("Step A", "Step B"))

    navigateToRoutinePreview(title)
    composeRule.onAllNodes(isToggleable(), useUnmergedTree = true)[0].performClick()
    composeRule.onAllNodes(isToggleable(), useUnmergedTree = true)[1].performClick()
    composeRule.waitUntil(timeoutMillis = 5_000) {
      runBlocking { routineRepository.getById(created.id)?.steps?.all { it.isCompleted } == true }
    }

    openPreviewOverflowMenu()
    clickText(r(R.string.reset_steps))

    composeRule.waitUntil(timeoutMillis = 5_000) {
      runBlocking { routineRepository.getById(created.id)?.steps?.none { it.isCompleted } == true }
    }
    val reset = runBlocking { routineRepository.getById(created.id) }
    assertTrue(reset?.steps?.none { it.isCompleted } == true)
  }

  // ---------------------------------------------------------------------------------------
  // E14-E18: Focus-runner execution screen
  // ---------------------------------------------------------------------------------------

  /** Creates a routine with two short timed steps (real seconds - see this class's kdoc on why
   *  real waits are used instead of virtual time) and opens the focus runner via the preview
   *  screen's FAB. */
  private fun createAndStartTimedRoutine(
    title: String,
    stepDurationsSeconds: List<Int> = listOf(3, 3),
  ): Routine {
    val idsBefore = captureExistingRoutineIds()
    navigateToNewRoutineEditor()
    setRoutineTitle(title)
    stepDurationsSeconds.indices.forEach { index ->
      val cardTag = addRoutineStep(index)
      setStepTitle(cardTag, "Step ${index + 1}")
    }
    tapSaveRoutine()
    val created = awaitNewRoutine(idsBefore)
    // RoutineStepRow's UI only offers fixed 5/10/15/30-minute presets (plus "None") - none
    // practical to really wait out in a test - so the short, real per-step durations under test
    // here are set directly through the repository instead. The execution runner itself doesn't
    // care how a step's durationSeconds got set, only that it's > 0, so this still exercises the
    // real ticker/auto-advance logic under test, not a fake of it.
    val withShortSteps = created.copy(
      steps = created.steps.mapIndexed { index, step -> step.copy(durationSeconds = stepDurationsSeconds[index]) },
    )
    runBlocking { routineRepository.save(withShortSteps) }

    navigateToRoutinePreview(title)
    // Confirmed live: the ExtendedFloatingActionButton's own merged semantics node doesn't expose
    // its text the way a plain Text/clickable row does - the merged-tree finder reports 0 matches
    // even though the unmerged tree has exactly 1, so this needs useUnmergedTree explicitly.
    composeRule.onNodeWithText(r(R.string.start_routine), useUnmergedTree = true).performClick()
    composeRule.waitForIdle()
    return withShortSteps
  }

  @Test
  fun focusRunner_countdownTimer_ticksAndPauses() {
    createAndStartTimedRoutine("Countdown routine ${UUID.randomUUID()}", listOf(8, 8))

    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText(r(R.string.step_of_count, 1, 2)).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithContentDescription(r(R.string.pause)).assertIsDisplayed()

    // Let it tick for a couple of real seconds, then pause and confirm the countdown stops
    // changing.
    Thread.sleep(2_500)
    composeRule.onNodeWithContentDescription(r(R.string.pause)).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithContentDescription(r(R.string.resume)).assertIsDisplayed()

    Thread.sleep(1_500)
    // Still paused (resume button, not pause) after waiting - the countdown genuinely stopped
    // rather than continuing to tick down in the background.
    composeRule.onNodeWithContentDescription(r(R.string.resume)).assertIsDisplayed()

    composeRule.onNodeWithContentDescription(r(R.string.resume)).performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithContentDescription(r(R.string.pause)).assertIsDisplayed()
  }

  @Test
  fun focusRunner_stepCompletionAndAutoAdvance() {
    createAndStartTimedRoutine("Auto-advance routine ${UUID.randomUUID()}", listOf(3, 3))

    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText(r(R.string.step_of_count, 1, 2)).fetchSemanticsNodes().isNotEmpty()
    }
    // Step 1's 3s countdown expires naturally - RoutineExecutionViewModel.tick() auto-advances and
    // marks it completed (Routine.autoAdvance defaults to true, never changed by this test).
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithText(r(R.string.step_of_count, 2, 2)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  @Test
  fun focusRunner_skipStep_advancesWithoutCompletion() {
    val routine = createAndStartTimedRoutine("Skip routine ${UUID.randomUUID()}", listOf(20, 3))

    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText(r(R.string.step_of_count, 1, 2)).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithContentDescription(r(R.string.skip_step)).performClick()
    composeRule.waitForIdle()

    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText(r(R.string.step_of_count, 2, 2)).fetchSemanticsNodes().isNotEmpty()
    }
    // Finish the run (last step's own short countdown) and confirm the skipped first step never
    // made it into completedStepIds.
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithText(r(R.string.routine_finished_title)).fetchSemanticsNodes().isNotEmpty()
    }
    val records = runBlocking { routineExecutionRepository.getByRoutineId(routine.id) }
    val record = records.maxByOrNull { it.executedAt }
    assertNotNull(record)
    assertFalse(routine.steps[0].id in (record?.completedStepIds ?: emptyList()))
  }

  @Test
  fun focusRunner_plusOneMinute_extendsTimer() {
    createAndStartTimedRoutine("Plus one minute routine ${UUID.randomUUID()}", listOf(8, 3))

    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText(r(R.string.step_of_count, 1, 2)).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithContentDescription(r(R.string.pause)).performClick()
    composeRule.waitForIdle()
    // Paused with ~8s remaining, so the countdown label reads 00:0x.
    composeRule.onNodeWithText("00:0", substring = true).assertIsDisplayed()

    composeRule.onNodeWithText(r(R.string.add_one_minute)).performClick()
    composeRule.waitForIdle()

    // 8s remaining + 60s = 01:0x, no longer matching the original 00:0x label.
    composeRule.onNodeWithText("01:0", substring = true).assertIsDisplayed()
  }

  @Test
  fun focusRunner_completion_recordsCompletedStepIds() {
    val routine = createAndStartTimedRoutine("Completion routine ${UUID.randomUUID()}", listOf(3, 20))

    composeRule.waitUntil(timeoutMillis = 5_000) {
      composeRule.onAllNodesWithText(r(R.string.step_of_count, 1, 2)).fetchSemanticsNodes().isNotEmpty()
    }
    // Step 1 auto-completes on expiry.
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithText(r(R.string.step_of_count, 2, 2)).fetchSemanticsNodes().isNotEmpty()
    }
    // Step 2 is skipped explicitly.
    composeRule.onNodeWithContentDescription(r(R.string.skip_step)).performClick()
    composeRule.waitForIdle()

    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(r(R.string.routine_execution_steps_completed, 1, 2)).fetchSemanticsNodes()
        .isNotEmpty()
    }
    composeRule.onNodeWithText(r(R.string.routine_execution_steps_completed, 1, 2)).assertIsDisplayed()

    val records = runBlocking { routineExecutionRepository.getByRoutineId(routine.id) }
    val record = records.maxByOrNull { it.executedAt }
    assertNotNull(record)
    assertEquals(1, record?.completedStepIds?.size)
    assertTrue(routine.steps[0].id in (record?.completedStepIds ?: emptyList()))
    assertFalse(routine.steps[1].id in (record?.completedStepIds ?: emptyList()))
  }

  // ---------------------------------------------------------------------------------------
  // E19: Recurrence reset at next cycle
  // ---------------------------------------------------------------------------------------

  /** Pins "now" (see [FakeNowDateTimeProvider]) to day 1, creates a Daily routine (which anchors
   *  `lastResetAt` to that save time - `RoutineEditViewModel.onSaveClick`'s "recurrence period
   *  starts after you save" comment), completes one of its two steps, then advances the fake clock
   *  to day 2 and reopens the list, which is what actually triggers
   *  `RoutineRecurrenceResetUseCase` (`RoutinesListViewModel.loadRoutines`: `allRoutines.map {
   *  routineRecurrenceResetUseCase(it) }` runs on every list load, not just in the background). */
  @Test
  fun routineRecurrence_newCycle_autoResetsSteps() {
    val day1 = LocalDate.of(2027, 3, 10)
    fakeNowDateTimeProvider.setDate(day1)

    val title = "Recurring routine ${UUID.randomUUID()}"
    val idsBefore = captureExistingRoutineIds()
    navigateToNewRoutineEditor()
    setRoutineTitle(title)
    val step1Tag = addRoutineStep(0)
    setStepTitle(step1Tag, "Step A")
    val step2Tag = addRoutineStep(1)
    setStepTitle(step2Tag, "Step B")
    scrollAndClickText(r(R.string.repeat_daily))
    tapSaveRoutine()
    val created = awaitNewRoutine(idsBefore)
    assertEquals(RecurrenceRule.Daily(), created.recurrence)

    navigateToRoutinePreview(title)
    composeRule.onAllNodes(isToggleable(), useUnmergedTree = true)[0].performClick()
    composeRule.waitUntil(timeoutMillis = 5_000) {
      runBlocking { routineRepository.getById(created.id)?.steps?.any { it.isCompleted } == true }
    }

    val day2 = day1.plusDays(1)
    fakeNowDateTimeProvider.setDate(day2)
    navigateToRoutinesList()

    composeRule.waitUntil(timeoutMillis = 10_000) {
      runBlocking { routineRepository.getById(created.id)?.steps?.none { it.isCompleted } == true }
    }
    val afterReset = runBlocking { routineRepository.getById(created.id) }
    assertTrue(afterReset?.steps?.none { it.isCompleted } == true)

    val records = runBlocking { routineExecutionRepository.getByRoutineId(created.id) }
    assertTrue(records.any { it.completedStepIds.size == 1 && it.totalStepsCount == 2 })
  }

  // ---------------------------------------------------------------------------------------
  // E20-E21: PRO Insights
  // ---------------------------------------------------------------------------------------

  /** Settings -> Reminders -> Insights, the only in-app path (`SettingsCrossFeatureEntries.kt`'s
   *  `onInsightsClick`). PRO-gated (`state.isInsightsLocked`) but this suite always builds/installs
   *  the `pro` flavor per `CLAUDE.md`, and no test Koin override anywhere in this suite (or
   *  `testRepositoryModule`) mocks `BuildInfo.isPro` to false - confirmed by grepping the test
   *  sources - so the real pro-flavor `BuildInfo` should leave it unlocked. */
  private fun navigateToInsights() {
    ensureOnHomeScreen()
    composeRule.waitUntil(timeoutMillis = 15_000) {
      composeRule.onAllNodesWithContentDescription(r(R.string.action_settings)).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithContentDescription(r(R.string.action_settings)).performClick()
    composeRule.waitForIdle()
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(r(R.string.reminders_)).fetchSemanticsNodes().isNotEmpty()
    }
    scrollAndClickText(r(R.string.reminders_))
    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(r(R.string.insights)).fetchSemanticsNodes().isNotEmpty()
    }
    scrollAndClickText(r(R.string.insights))
  }

  /** Seeds [count] consecutive fully-completed days directly through the repositories (creating a
   *  routine through the UI first so it has real step ids) - the streak-worthy history itself has
   *  no in-scope UI path (it would take literally waiting `count` real days), the same class of
   *  exclusion already applied to G12/B12/B13 elsewhere in this suite. */
  private fun seedRoutineWithStreak(title: String, streakDays: Int, today: LocalDate): Routine {
    val routine = createRoutineViaUi(title, listOf("Step 1", "Step 2"))
    val stepIds = routine.steps.map { it.id }
    runBlocking {
      (0 until streakDays).forEach { daysAgo ->
        routineExecutionRepository.save(
          RoutineExecutionRecord(
            routineId = routine.id,
            executedAt = today.minusDays(daysAgo.toLong()).atTime(9, 0),
            totalTimeSpentSeconds = 300,
            completedStepIds = stepIds,
            totalStepsCount = stepIds.size,
          ),
        )
      }
    }
    return routine
  }

  @Test
  fun insights_routineStreaks_updatesConsecutiveDays() {
    val today = LocalDate.now()
    seedRoutineWithStreak("Streak routine ${UUID.randomUUID()}", streakDays = 4, today = today)

    navigateToInsights()

    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(r(R.string.streak_current, 4)).fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText(r(R.string.streak_current, 4)).assertIsDisplayed()
  }

  /** Seeds a routine whose second step is skipped every day (0% completion for that step, 100% for
   *  the first) so `RoutineStepDropoffCalculator`'s "most skipped step" is deterministic. */
  @Test
  fun insights_stepDropoff_analyzesCompletionRatio() {
    val title = "Dropoff routine ${UUID.randomUUID()}"
    val idsBefore = captureExistingRoutineIds()
    navigateToNewRoutineEditor()
    setRoutineTitle(title)
    val step1Tag = addRoutineStep(0)
    setStepTitle(step1Tag, "Always done")
    val step2Tag = addRoutineStep(1)
    setStepTitle(step2Tag, "Always skipped")
    tapSaveRoutine()
    val routine = awaitNewRoutine(idsBefore)
    val doneStepId = routine.steps.first { it.title == "Always done" }.id

    val today = LocalDate.now()
    runBlocking {
      (0 until 3).forEach { daysAgo ->
        routineExecutionRepository.save(
          RoutineExecutionRecord(
            routineId = routine.id,
            executedAt = today.minusDays(daysAgo.toLong()).atTime(9, 0),
            totalTimeSpentSeconds = 300,
            completedStepIds = listOf(doneStepId),
            totalStepsCount = routine.steps.size,
          ),
        )
      }
    }

    navigateToInsights()

    composeRule.waitUntil(timeoutMillis = 10_000) {
      composeRule.onAllNodesWithText(r(R.string.routine_insight_most_skipped, "Always skipped", 0))
        .fetchSemanticsNodes().isNotEmpty()
    }
    composeRule.onNodeWithText(r(R.string.routine_insight_most_skipped, "Always skipped", 0)).assertIsDisplayed()
  }

  // ---------------------------------------------------------------------------------------
  // E22: Cloud sync / local backup round-trip
  // ---------------------------------------------------------------------------------------

  /** Exercises the exact conversion cloud sync (`RoutineRepositoryCaller`, `data:sync`) and the PRO
   *  local encrypted backup both actually serialize through - `DataConverter` (`data:files`),
   *  in-process, rather than driving a real Google Drive/Dropbox OAuth flow or the system file
   *  picker a full local-backup-file round trip would need (same exclusion already applied to
   *  B12/B13 elsewhere in this suite). `DataConverter` uses `android.util.Base64`
   *  (`DataConverterImpl.toOutputStream`/`toData`), so this only runs on a real Android runtime -
   *  the reason `docs/routines.md` §4.5 flags no equivalent test exists at the `data:files` JVM
   *  unit-test level (Robolectric isn't configured there). The full encrypted-backup-file and
   *  `BackupArchiveWriter`/`BackupArchiveReader` round trip already has dedicated coverage
   *  (`BackupArchiveReaderWriterTest`/`LocalBackupApiImplTest`, §4.5) - this test's scope is
   *  specifically the `Routine`/`RoutineJson`/`RoutineExecutionRecord`/`RoutineExecutionJson`
   *  conversion functions that both of those higher-level paths depend on. */
  @Test
  fun cloudSyncAndLocalBackup_routineRoundTrip() {
    val now = LocalDateTime.of(2027, 5, 1, 8, 0)
    val routine = Routine(
      id = UUID.randomUUID().toString(),
      title = "Backup routine ${UUID.randomUUID()}",
      description = "Round-trips through DataConverter",
      color = 4,
      isPinned = true,
      icon = 2,
      steps = listOf(
        RoutineStep(id = UUID.randomUUID().toString(), title = "Step A", durationSeconds = 300, order = 0),
        RoutineStep(
          id = UUID.randomUUID().toString(),
          title = "Step B",
          durationSeconds = 0,
          scheduledTime = "09:15",
          order = 1,
        ),
      ),
      recurrence = RecurrenceRule.Weekly(weekdays = listOf(0, 1, 0, 1, 0, 0, 0)),
      lastResetAt = now,
      createdAt = now,
      updatedAt = now,
      sync = SyncMetadata(version = 3),
    )

    val roundTrippedRoutine = runBlocking {
      val stream = dataConverter.toInputStream(routine)
      dataConverter.toData(stream) as Routine
    }

    assertEquals(routine.id, roundTrippedRoutine.id)
    assertEquals(routine.title, roundTrippedRoutine.title)
    assertEquals(routine.description, roundTrippedRoutine.description)
    assertEquals(routine.color, roundTrippedRoutine.color)
    assertEquals(routine.isPinned, roundTrippedRoutine.isPinned)
    assertEquals(routine.icon, roundTrippedRoutine.icon)
    assertEquals(routine.steps, roundTrippedRoutine.steps)
    assertEquals(routine.recurrence, roundTrippedRoutine.recurrence)
    assertEquals(routine.lastResetAt, roundTrippedRoutine.lastResetAt)
    assertEquals(routine.createdAt, roundTrippedRoutine.createdAt)
    assertEquals(routine.updatedAt, roundTrippedRoutine.updatedAt)
    assertEquals(routine.sync.version, roundTrippedRoutine.sync.version)

    val record = RoutineExecutionRecord(
      id = UUID.randomUUID().toString(),
      routineId = routine.id,
      executedAt = now,
      totalTimeSpentSeconds = 900,
      completedStepIds = listOf(routine.steps[0].id),
      totalStepsCount = 2,
    )
    val roundTrippedRecord = runBlocking {
      val stream = dataConverter.toInputStream(record)
      dataConverter.toData(stream) as RoutineExecutionRecord
    }
    assertEquals(record, roundTrippedRecord)
  }

  private class TestFeatureFlags : FeatureFlags {
    override fun isEnabled(feature: FeatureFlag): Boolean =
      if (feature == FeatureFlag.ROUTINE_ENABLED) true else feature.defaultValue
  }

  companion object {
    /** Runs before the very first [BottomNavActivity] is created for this class (see
     *  [ReminderRecurrenceE2ETest.setUpClass]'s identical kdoc for why `@BeforeClass` matters
     *  here). [testFeatureFlagsModule] overrides the production `FeatureFlags` binding
     *  (`FeatureManager`, backed by `Prefs`/`SharedPreferences`, defaults `ROUTINE_ENABLED` to
     *  `false`) so `RoutineConfigImpl` - a plain `factory` that resolves `FeatureFlags` fresh via
     *  `get()` on every call - picks up the override transitively without needing its own binding
     *  touched at all. */
    private val fakeNowDateTimeProvider = FakeNowDateTimeProvider()

    private fun testFeatureFlagsModule(): Module = module {
      factory<FeatureFlags> { TestFeatureFlags() }
    }

    @JvmStatic
    @BeforeClass
    fun setUpClass() {
      val context = InstrumentationRegistry.getInstrumentation().targetContext
      loadKoinModules(testRepositoryModule(context))
      loadKoinModules(testDateTimeModule(fakeNowDateTimeProvider))
      loadKoinModules(testFeatureFlagsModule())
    }
  }
}
