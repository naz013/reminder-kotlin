package com.elementary.tasks.birthdays.dialog

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elementary.tasks.birthdays.actions.BirthdayAction
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.elementary.tasks.notes.ObserveEvent
import com.elementary.tasks.telephony.rememberPhoneCaller
import com.elementary.tasks.telephony.rememberSmsSender
import com.github.naz013.common.Permissions
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.DeviceScreenConfiguration
import com.github.naz013.ui.common.compose.foundation.SplitButton
import com.github.naz013.ui.common.compose.foundation.component.PopupMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.deviceScreenConfiguration
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayActionScreen(
  modifier: Modifier = Modifier,
  id: String,
  onFinish: () -> Unit = {},
  adsContent: @Composable () -> Unit,
) {
  val viewModel = koinViewModel<BirthdayActionViewModel> { parametersOf(id) }

  val screenConfiguration = deviceScreenConfiguration()
  val toastDispatcher = rememberToastDispatcher()
  val phoneCaller = rememberPhoneCaller()
  val smsSender = rememberSmsSender()
  val permissionRequester = rememberPermissionRequesterRationale()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      BirthdayActionViewModel.ViewModelEvent.Finish -> {
        onFinish()
      }

      is BirthdayActionViewModel.ViewModelEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }

      is BirthdayActionViewModel.ViewModelEvent.MakeCall -> {
        permissionRequester.request(
          Permissions.CALL_PHONE,
          onGranted = {
            phoneCaller.call(event.phoneNumber)
            onFinish()
          }
        )
      }

      is BirthdayActionViewModel.ViewModelEvent.SendSms -> {
        smsSender.send(event.phoneNumber, null)
        onFinish()
      }
    }
  }

  val state by viewModel.state.observeAsState()
  Scaffold { paddingValues ->
    Surface(
      modifier =
        modifier
          .fillMaxSize()
          .padding(paddingValues),
      color = MaterialTheme.colorScheme.background,
    ) {
      // Early return if state is not available yet
      val screenState = state ?: return@Surface

      Logger.d("ReminderActionScreen", "Rendering screen with configuration: $screenConfiguration")

      // Choose layout based on screen configuration
      when (screenConfiguration) {
        DeviceScreenConfiguration.MobileLandscape -> {
          BirthdayActionScreenLandscape(
            screenState = screenState,
            onActionClick = { action ->
              viewModel.onActionClick(action)
            },
            adsContent = adsContent,
          )
        }
        else -> {
          BirthdayActionScreenPortrait(
            screenState = screenState,
            onActionClick = { action ->
              viewModel.onActionClick(action)
            },
            adsContent = adsContent,
          )
        }
      }
    }
  }
}

@Composable
private fun BirthdayActionScreenPortrait(
  screenState: BirthdayActionScreenState,
  onActionClick: (BirthdayAction) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    // Main content
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header section
      BirthdayHeader(header = screenState.header)
    }

    adsContent()

    Spacer(modifier = Modifier.height(16.dp))

    // Action buttons section
    ActionsSection(
      mainAction = screenState.mainAction,
      secondaryActions = screenState.secondaryActions,
      onActionClick = onActionClick,
    )
  }
}

@Composable
private fun BirthdayActionScreenLandscape(
  screenState: BirthdayActionScreenState,
  onActionClick: (BirthdayAction) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Column(
      modifier =
        Modifier
          .weight(1f)
          .fillMaxSize()
          .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header section
      BirthdayHeader(header = screenState.header)

      adsContent()
    }

    Column(
      modifier =
        Modifier
          .width(280.dp)
          .fillMaxSize(),
      verticalArrangement = Arrangement.Bottom,
    ) {
      ActionsSection(
        mainAction = screenState.mainAction,
        secondaryActions = screenState.secondaryActions,
        onActionClick = onActionClick,
      )
    }
  }
}

/**
 * Displays the birthday header with contact information.
 *
 * @param header The header data containing birthday contact information
 */
@Composable
private fun BirthdayHeader(header: BirthdayActionScreenHeader) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(16.dp),
    ) {
      ContactHeaderContent(
        text = header.text,
        contactName = header.contactName,
        contactInfo = header.phoneNumber,
        contactPhoto = header.contactPhoto,
        birthdayDate = header.birthdayDate,
        age = header.age,
        icon = R.drawable.ic_fluent_phone,
      )
    }
  }
}

/**
 * Displays contact-related header content with optional photo.
 *
 * @param text The birthday person's name
 * @param contactName The contact name if available
 * @param contactInfo The phone number or contact info
 * @param contactPhoto Optional contact photo bitmap
 * @param birthdayDate Formatted birthday date
 * @param age Formatted age (null if year is ignored)
 * @param icon Icon resource for the contact type
 */
@Composable
private fun ContactHeaderContent(
  text: String,
  contactName: String?,
  contactInfo: String,
  contactPhoto: Bitmap?,
  birthdayDate: String,
  age: String?,
  icon: Int,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Contact photo or icon
    if (contactPhoto != null) {
      Image(
        bitmap = contactPhoto.asImageBitmap(),
        contentDescription = contactName ?: contactInfo,
        modifier =
          Modifier
            .size(56.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
      )
    } else {
      Box(
        modifier =
          Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painter = painterResource(id = icon),
          contentDescription = null,
          modifier = Modifier.size(28.dp),
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
      }
    }

    // Contact info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )

      // Birthday date
      if (birthdayDate.isNotEmpty()) {
        Text(
          text = birthdayDate,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // Age
      if (age != null) {
        Text(
          text = age,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.primary,
        )
      }

      // Contact name if different from text
      if (contactName != null && contactName != text) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = contactName,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Normal,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // Phone number
      if (contactInfo.isNotEmpty()) {
        Text(
          text = contactInfo,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
      }
    }
  }
}

/**
 * Displays the action buttons section.
 *
 * @param mainAction The main action button
 * @param secondaryActions List of secondary action buttons
 * @param onActionClick Callback when an action is clicked
 */
@Composable
private fun ActionsSection(
  mainAction: BirthdayActionScreenActionItem,
  secondaryActions: List<BirthdayActionScreenActionItem>,
  onActionClick: (BirthdayAction) -> Unit,
) {
  if (secondaryActions.isEmpty()) {
    Button(
      onClick = { onActionClick(mainAction.action) },
      modifier =
        Modifier
          .fillMaxWidth()
          .height(56.dp),
      shape = ButtonDefaults.shape,
      colors =
        ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
      Icon(
        painter = painterResource(id = mainAction.iconRes),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = mainAction.text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
    }
  } else {
    var expanded by remember { mutableStateOf(false) }
    Box(
      modifier = Modifier.fillMaxWidth(),
    ) {
      SplitButton(
        onLeftClick = { onActionClick(mainAction.action) },
        onRightClick = { expanded = true },
        leftContent = {
          Icon(
            painter = painterResource(id = mainAction.iconRes),
            contentDescription = mainAction.text,
          )
          Text(
            text = mainAction.text,
            modifier = Modifier.padding(start = 8.dp),
          )
        },
        rightContent = {
          Box {
            Icon(
              painter = painterResource(id = R.drawable.ic_fluent_more_hor),
              contentDescription = stringResource(com.elementary.tasks.R.string.more_options),
            )
            PopupMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false },
              items =
                secondaryActions.mapIndexed { index, item ->
                  PopupMenuItem(
                    id = index,
                    title = item.text,
                    iconRes = item.iconRes,
                  )
                },
              onItemClick = { itemId ->
                val actionItem = secondaryActions.getOrNull(itemId)
                actionItem?.let {
                  onActionClick(it.action)
                }
              },
            )
          }
        },
        modifier =
          Modifier
            .fillMaxWidth()
            .height(56.dp),
        cornerRadius = 28.dp,
      )
    }
  }
}

// Preview composables for testing and demonstration
@Preview(showBackground = true)
@Composable
private fun BirthdayActionScreenPortraitPreview() {
  MaterialTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background,
    ) {
      BirthdayActionScreenPortrait(
        screenState =
          BirthdayActionScreenState(
            id = "preview-1",
            header =
              BirthdayActionScreenHeader(
                text = "John Doe",
                phoneNumber = "+1234567890",
                contactName = "John Doe",
                contactPhoto = null,
                birthdayDate = "December 23",
                age = "29 years",
              ),
            mainAction =
              BirthdayActionScreenActionItem(
                action = BirthdayAction.Ok,
                text = "OK",
                iconRes = R.drawable.ic_fluent_checkmark,
              ),
            secondaryActions =
              listOf(
                BirthdayActionScreenActionItem(
                  action = BirthdayAction.MakeCall,
                  text = "Call",
                  iconRes = R.drawable.ic_fluent_phone,
                ),
                BirthdayActionScreenActionItem(
                  action = BirthdayAction.SendSms,
                  text = "SMS",
                  iconRes = R.drawable.ic_fluent_send,
                ),
                BirthdayActionScreenActionItem(
                  action = BirthdayAction.Edit,
                  text = "Edit",
                  iconRes = R.drawable.ic_fluent_edit,
                ),
              ),
          ),
        onActionClick = {},
        adsContent = {},
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun BirthdayActionScreenLandscapePreview() {
  MaterialTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background,
    ) {
      BirthdayActionScreenLandscape(
        screenState =
          BirthdayActionScreenState(
            id = "preview-2",
            header =
              BirthdayActionScreenHeader(
                text = "Jane Smith",
                phoneNumber = "+0987654321",
                contactName = "Jane Smith",
                contactPhoto = null,
                birthdayDate = "March 15",
                age = null, // Year ignored, no age shown
              ),
            mainAction =
              BirthdayActionScreenActionItem(
                action = BirthdayAction.Ok,
                text = "Ok",
                iconRes = R.drawable.ic_fluent_checkmark,
              ),
            secondaryActions =
              listOf(
                BirthdayActionScreenActionItem(
                  action = BirthdayAction.Snooze,
                  text = "Snooze",
                  iconRes = R.drawable.ic_fluent_alert_snooze,
                ),
                BirthdayActionScreenActionItem(
                  action = BirthdayAction.Edit,
                  text = "Edit",
                  iconRes = R.drawable.ic_fluent_edit,
                ),
              ),
          ),
        onActionClick = {},
        adsContent = {},
      )
    }
  }
}
