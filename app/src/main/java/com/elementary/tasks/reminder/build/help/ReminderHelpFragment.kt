package com.elementary.tasks.reminder.build.help

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.ComposeFragment
import com.github.naz013.ui.common.compose.TopAppbarColor

/**
 * Fragment that displays the "How to Create a Reminder" help guide.
 *
 * This fragment presents a comprehensive HTML-based guide that explains the reminder builder
 * system, including:
 * - Basic workflow for creating reminders
 * - Builder item categories (Core, Parameters, Actions, Extra)
 * - Common use cases with step-by-step examples
 * - Preset templates usage
 * - Validation and error handling
 * - Permissions requirements
 * - Best practices and troubleshooting
 *
 * The content is displayed in a WebView within a Material 3 Scaffold for consistent
 * navigation and styling with the rest of the application.
 *
 * @see com.github.naz013.ui.common.compose.ComposeFragment
 */
class ReminderHelpFragment : ComposeFragment() {
  /**
   * Provides the main content for this fragment.
   *
   * Sets up the help screen with back navigation support using the activity's
   * back dispatcher.
   */
  @Composable
  override fun FragmentContent() {
    ReminderHelpScreen(
      onBackClick = { activity?.onBackPressedDispatcher?.onBackPressed() },
    )
  }

  /**
   * Main screen composable that displays the help content in a WebView.
   *
   * Creates a Material 3 Scaffold with:
   * - Top app bar with title and back navigation
   * - WebView displaying the HTML help guide from raw resources
   * - Proper padding to accommodate system bars
   * - Scroll-responsive elevation that appears when content is scrolled
   *
   * @param onBackClick Callback invoked when the back button is pressed
   */
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  private fun ReminderHelpScreen(onBackClick: () -> Unit) {
    // Track whether the WebView has been scrolled
    var isScrolled by remember { mutableStateOf(false) }

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(text = stringResource(com.elementary.tasks.R.string.how_to_create_a_reminder))
          },
          navigationIcon = {
            IconButton(onClick = onBackClick) {
              Icon(
                painter = painterResource(R.drawable.ic_builder_arrow_left),
                contentDescription = stringResource(R.string.cd_back),
              )
            }
          },
          colors = TopAppbarColor,
          scrollBehavior =
            TopAppBarDefaults.enterAlwaysScrollBehavior().apply {
              state.contentOffset = if (isScrolled) -1f else 0f
            },
        )
      },
    ) { paddingValues ->
      // WebView to display the HTML help document
      AndroidView(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(paddingValues),
        factory = { context ->
          WebView(context).apply {
            layoutParams =
              ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
              )

            // Set up scroll listener to detect when content is scrolled
            setOnScrollChangeListener { _, _, scrollY, _, _ ->
              isScrolled = scrollY > 0
            }

            // Load the HTML file from raw resources
            loadUrl("file:///android_res/raw/how_to_create_a_reminder.html")
          }
        },
        update = { webView ->
          // Update scroll listener to track state changes
          webView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            isScrolled = scrollY > 0
          }
        },
      )
    }
  }
}
