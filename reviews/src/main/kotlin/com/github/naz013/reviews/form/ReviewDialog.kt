package com.github.naz013.reviews.form

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.common.android.readSerializable
import com.github.naz013.feature.common.livedata.observeEvent
import com.github.naz013.logging.Logger
import com.github.naz013.reviews.AppSource
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.ComposeBottomSheetDialogFragment
import com.github.naz013.ui.common.compose.foundation.PrimaryIconButton
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Bottom sheet dialog for collecting user feedback and reviews.
 *
 * This dialog presents a form for users to submit their feedback, including:
 * - Star rating (1-5 scale)
 * - Comment text
 * - Optional log file attachment
 * - Optional user email
 */
internal class ReviewDialog : ComposeBottomSheetDialogFragment() {

  private val viewModel by viewModel<ReviewDialogViewModel>()

  @Composable
  override fun FragmentContent() {
    val title = arguments?.getString(ARG_TITLE) ?: stringResource(R.string.feedback)
    val appSource = arguments?.readSerializable(ARG_SOURCE, AppSource::class.java) ?: AppSource.FREE
    val allowLogs = arguments?.getBoolean(ARG_ALLOW_LOGS) ?: false

    val isLoading by viewModel.isLoading.observeAsState(false)

    ReviewFormContent(
      title = title,
      isLoading = isLoading,
      allowLogs = allowLogs,
      onSubmit = { rating, comment, attachLog, email ->
        viewModel.submitReview(rating, comment, attachLog, email, appSource)
      },
      onDismiss = { dismiss() },
      onShowError = { message -> showMessage(message) }
    )
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Logger.i(TAG, "On view created.")
    initViewModel()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    Logger.i(TAG, "On attach.")
    setupBottomSheet()
  }

  /**
   * Initializes ViewModel observers for submission results.
   */
  private fun initViewModel() {
    viewModel.submitSuccess.observeEvent(viewLifecycleOwner) {
      Logger.i(TAG, "Review submitted successfully, closing dialog")
      showMessage(getString(com.github.naz013.reviews.R.string.thank_you_for_your_feedback))
      dismiss()
    }

    viewModel.submitError.observeEvent(viewLifecycleOwner) { errorMessage ->
      Logger.e(TAG, "Review submission failed: $errorMessage")
      showMessage(errorMessage)
    }
  }

  /**
   * Shows a message to the user using Toast.
   *
   * @param message The message to display
   */
  private fun showMessage(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
  }

  private fun setupBottomSheet() {
    // Open the bottom sheet expanded having half screen height
    dialog?.let { dlg ->
      dlg.setOnShowListener { dialogInterface ->
        val bottomSheetDialog = dialogInterface as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(
          com.google.android.material.R.id.design_bottom_sheet
        )
        bottomSheet?.let { sheet ->
          val behavior = BottomSheetBehavior.from(sheet)
          behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
          behavior.peekHeight = resources.displayMetrics.heightPixels / 2
        }
      }
    }
  }

  companion object {
    internal const val TAG = "ReviewDialog"
    private const val ARG_TITLE = "dialog_title"
    private const val ARG_SOURCE = "dialog_source"
    private const val ARG_ALLOW_LOGS = "allow_logs"

    /**
     * Creates a new instance of ReviewDialog.
     *
     * @param title The dialog title
     * @return New ReviewDialog instance
     */
    fun newInstance(
      title: String?,
      appSource: AppSource,
      allowLogsAttachment: Boolean
    ): ReviewDialog {
      return ReviewDialog().apply {
        arguments = Bundle().apply {
          putString(ARG_TITLE, title)
          putSerializable(ARG_SOURCE, appSource)
          putBoolean(ARG_ALLOW_LOGS, allowLogsAttachment)
        }
      }
    }
  }
}

/**
 * Validates email address format.
 *
 * @param email The email address to validate
 * @return true if the email is valid or empty, false otherwise
 */
private fun isValidEmail(email: String): Boolean {
  if (email.isBlank()) return true

  val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
  return email.matches(emailPattern.toRegex())
}

/**
 * Composable content for the review form.
 *
 * @param title The form title
 * @param isLoading Whether the form is currently submitting
 * @param onSubmit Callback when form is submitted
 * @param onDismiss Callback when form is dismissed
 * @param onShowError Callback to show error messages
 */
@Composable
fun ReviewFormContent(
  title: String,
  isLoading: Boolean = false,
  allowLogs: Boolean = false,
  onSubmit: (rating: Float, comment: String, attachLog: Boolean, email: String?) -> Unit,
  onDismiss: () -> Unit,
  onShowError: ((String) -> Unit)? = null
) {
  var rating by remember { mutableFloatStateOf(5f) }
  var comment by remember { mutableStateOf("") }
  var attachLog by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var emailError by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Header with action buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      PrimaryIconButton(
        icon = AppIcons.Clear,
        contentDescription = stringResource(R.string.cancel),
        onClick = onDismiss,
        color = MaterialTheme.colorScheme.errorContainer,
        iconColor = MaterialTheme.colorScheme.onErrorContainer,
        enabled = !isLoading
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center,
        maxLines = 2,
      )
      Spacer(modifier = Modifier.width(8.dp))
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.padding(12.dp),
          color = MaterialTheme.colorScheme.primary
        )
      } else {
        PrimaryIconButton(
          icon = AppIcons.Ok,
          contentDescription = stringResource(R.string.feedback_submit),
          onClick = {
            // Validate email if provided
            if (email.isNotBlank() && !isValidEmail(email)) {
              emailError = true
              onShowError?.invoke("Please enter a valid email address")
              return@PrimaryIconButton
            }

            emailError = false
            val emailValue = if (email.isBlank()) null else email
            onSubmit(rating, comment, attachLog, emailValue)
          },
          color = MaterialTheme.colorScheme.primaryContainer,
          iconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }

    HorizontalDivider(
      modifier = Modifier.padding(vertical = 8.dp),
      color = MaterialTheme.colorScheme.outlineVariant
    )

    // Form content
    Column(
      modifier = Modifier
        .verticalScroll(rememberScrollState())
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Rating section
      RatingSection(
        rating = rating,
        onRatingChanged = { rating = it }
      )

      // Comment section
      CommentSection(
        comment = comment,
        onCommentChanged = { comment = it }
      )

      if (allowLogs) {
        // Log file attachment option
        LogAttachmentSection(
          attachLog = attachLog,
          onAttachLogChanged = { attachLog = it }
        )
      }

      // Optional email field
      EmailSection(
        email = email,
        onEmailChanged = {
          email = it
          // Clear error when user types
          if (emailError) {
            emailError = false
          }
        },
        isError = emailError
      )

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

/**
 * Section for displaying and adjusting the rating.
 *
 * @param rating Current rating value
 * @param onRatingChanged Callback when rating changes
 */
@Composable
fun RatingSection(
  rating: Float,
  onRatingChanged: (Float) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = stringResource(R.string.feedback_rating),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "⭐".repeat(rating.toInt()),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
      )
      Text(
        text = "${rating.toInt()}/5",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Slider(
      value = rating,
      onValueChange = onRatingChanged,
      valueRange = 1f..5f,
      steps = 3,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

/**
 * Section for entering comment text.
 *
 * @param comment Current comment text
 * @param onCommentChanged Callback when comment changes
 */
@Composable
fun CommentSection(
  comment: String,
  onCommentChanged: (String) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = stringResource(R.string.feedback_comment),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface
    )

    OutlinedTextField(
      value = comment,
      onValueChange = onCommentChanged,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text(stringResource(R.string.share_your_feedback)) },
      minLines = 4,
      maxLines = 8,
      textStyle = MaterialTheme.typography.bodyMedium
    )
  }
}

/**
 * Section for toggling log file attachment.
 *
 * @param attachLog Current state of log attachment
 * @param onAttachLogChanged Callback when state changes
 */
@Composable
fun LogAttachmentSection(
  attachLog: Boolean,
  onAttachLogChanged: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.attach_log_files),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = stringResource(R.string.help_us_diagnose_issues_by_including_app_logs),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
    Spacer(modifier = Modifier.width(16.dp))
    Switch(
      checked = attachLog,
      onCheckedChange = onAttachLogChanged
    )
  }
}

/**
 * Section for entering optional email address.
 *
 * @param email Current email value
 * @param onEmailChanged Callback when email changes
 * @param isError Whether the email is in error state
 */
@Composable
fun EmailSection(
  email: String,
  onEmailChanged: (String) -> Unit,
  isError: Boolean = false
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Text(
      text = stringResource(R.string.feedback_email_optional),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface
    )

    OutlinedTextField(
      value = email,
      onValueChange = onEmailChanged,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("your.email@example.com") },
      singleLine = true,
      textStyle = MaterialTheme.typography.bodyMedium,
      isError = isError,
      supportingText = if (isError) {
        { Text(
          text = "Please enter a valid email address",
          color = MaterialTheme.colorScheme.error
        ) }
      } else null
    )

    if (!isError) {
      Text(
        text = stringResource(R.string.we_ll_use_this_to_follow_up_on_your_feedback),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
