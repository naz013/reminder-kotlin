package com.elementary.tasks.e2e

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import com.elementary.tasks.core.utils.params.Prefs
import org.koin.core.context.GlobalContext

/**
 * Puts a fresh emulator (CI) into the state the tests were written against: first-run work already
 * done. Otherwise `BottomNavInitViewModel` treats the first launch as a fresh install - it routes
 * to Onboarding instead of Home (and `BottomNavActivity` is `singleInstance`, so that stuck
 * instance is reused by every later test), and it inserts demo reminders one by one, which keeps
 * the splash up for longer than the tests' Home wait and pollutes the lists they assert on.
 * Call from `@BeforeClass`, before the first activity launch.
 */
internal fun skipFirstRunSetup() {
  GlobalContext.get().get<Prefs>().apply {
    isDemoDataInserted = true
    hasSeenOnboarding = true
  }
}

/**
 * `BottomNavActivity` is `singleInstance`, so a test's rule is redirected to whatever instance and
 * backstack the previous test left behind (e.g. a list screen with a search query still typed in).
 * Presses back (capped) until a content description only Home has - [homeMarkerLabel] - is
 * visible, which also pops the leftover screens and clears their ViewModel state.
 *
 * A loading Home gets a real chance to settle before concluding it's elsewhere: pressing back on
 * the Activity's only screen finishes it, which fails a *later* test with "Cannot run onActivity
 * since Activity has been destroyed already".
 */
internal fun ComposeTestRule.ensureOnHomeScreen(homeMarkerLabel: String, acceptLabel: String) {
  fun onHome() = onAllNodesWithContentDescription(homeMarkerLabel).fetchSemanticsNodes().isNotEmpty()
  repeat(15) {
    // No Compose hierarchy exists yet while a cold launch is still on its first frame.
    val acceptShown = runCatching { onAllNodesWithText(acceptLabel).fetchSemanticsNodes().isNotEmpty() }
    if (acceptShown.getOrDefault(false)) {
      onNodeWithText(acceptLabel).performClick()
      waitForIdle()
    }
    val settled = runCatching { waitUntil(timeoutMillis = 3_000) { onHome() } }.isSuccess
    if (settled) return
    runCatching { Espresso.pressBack() }
    waitForIdle()
    // waitForIdle() doesn't reliably outlast Nav3's cross-fade between screens.
    Thread.sleep(300)
  }
  check(onHome()) { "Could not get back to Home after 15 back-presses" }
}
