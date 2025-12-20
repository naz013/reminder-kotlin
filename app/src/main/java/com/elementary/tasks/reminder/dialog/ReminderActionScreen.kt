package com.elementary.tasks.reminder.dialog

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.elementary.tasks.reminder.actions.ReminderAction
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.DeviceScreenConfiguration
import com.github.naz013.ui.common.compose.foundation.PrimaryIconButton
import com.github.naz013.ui.common.compose.foundation.SplitButton
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetItem
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetList
import com.github.naz013.ui.common.compose.foundation.component.PopupMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.deviceScreenConfiguration
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

/**
 * Main screen composable for displaying reminder action UI.
 *
 * This screen displays the reminder content with header information,
 * optional todo list, and action buttons. It handles window insets
 * to provide full-screen display while respecting system UI.
 *
 * Input validation and early returns:
 * - If state is null, returns early without rendering content
 * - Todo item click ignores blank item ids
 *
 * @param viewModel The ViewModel providing state and handling actions
 * @param modifier Optional modifier for the root container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderActionScreen(
  viewModel: ReminderActionActivityViewModel = koinViewModel(),
  modifier: Modifier = Modifier
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val state by viewModel.state.observeAsState()

  var showSnoozeBottomSheet by remember { mutableStateOf(false) }
  val snoozeSheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()

  val showSnoozeDialog by viewModel.showSnoozeDialog.observeAsState()

  // Observe showSnoozeDialog event and display bottom sheet
  showSnoozeDialog?.getContentIfNotHandled()?.let {
    showSnoozeBottomSheet = true
  }

  val screenConfiguration = deviceScreenConfiguration()

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { paddingValues ->
    Surface(
      modifier = modifier
        .fillMaxSize()
        .padding(paddingValues),
      color = MaterialTheme.colorScheme.background
    ) {
      // Early return if state is not available yet
      val screenState = state ?: return@Surface

      Logger.d("ReminderActionScreen", "Rendering screen with configuration: $screenConfiguration")

      // Choose layout based on screen configuration
      when (screenConfiguration) {
        DeviceScreenConfiguration.MobileLandscape -> {
          ReminderActionScreenLandscape(
            screenState = screenState,
            onTodoItemClick = { itemId ->
              // Input validation: ignore blank ids
              if (itemId.isBlank()) return@ReminderActionScreenLandscape
              viewModel.onTodoItemClick(itemId)
            },
            onActionClick = { action ->
              viewModel.onActionClick(action)
            }
          )
        }
        else -> {
          ReminderActionScreenPortrait(
            screenState = screenState,
            onTodoItemClick = { itemId ->
              // Input validation: ignore blank ids
              if (itemId.isBlank()) return@ReminderActionScreenPortrait
              viewModel.onTodoItemClick(itemId)
            },
            onActionClick = { action ->
              viewModel.onActionClick(action)
            }
          )
        }
      }
    }

    // Snooze Bottom Sheet
    if (showSnoozeBottomSheet) {
      AppModalBottomSheet(
        onDismissRequest = { showSnoozeBottomSheet = false },
        sheetState = snoozeSheetState,
        dragHandle = null
      ) {
        SnoozeDialogContent(
          onDismiss = {
            scope.launch {
              snoozeSheetState.hide()
              showSnoozeBottomSheet = false
            }
          },
          onSnooze = {
            viewModel.onCustomSnooze(it)
            scope.launch {
              snoozeSheetState.hide()
              showSnoozeBottomSheet = false
            }
          }
        )
      }
    }
  }
}

/**
 * Portrait layout for the reminder action screen.
 *
 * Displays the content in a vertical layout optimized for portrait orientation.
 * Content is arranged with header at top, todo list in middle, and actions at bottom.
 *
 * @param screenState The screen state containing all display data
 * @param onTodoItemClick Callback for todo item clicks
 * @param onActionClick Callback for action button clicks
 */
@Composable
private fun ReminderActionScreenPortrait(
  screenState: ReminderActionScreenState,
  onTodoItemClick: (String) -> Unit,
  onActionClick: (ReminderAction) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Main content
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header section
      ReminderHeader(header = screenState.header)

      // Todo list section (if present)
      screenState.todoList?.let { todoList ->
        TodoListSection(
          todoList = todoList,
          onItemClick = onTodoItemClick
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Action buttons section
    ActionsSection(
      mainAction = screenState.mainAction,
      secondaryActions = screenState.secondaryActions,
      onActionClick = onActionClick
    )
  }
}

/**
 * Landscape layout for the reminder action screen.
 *
 * Displays the content in a horizontal two-column layout optimized for landscape orientation.
 * Left column contains header and todo list, right column contains action buttons.
 *
 * @param screenState The screen state containing all display data
 * @param onTodoItemClick Callback for todo item clicks
 * @param onActionClick Callback for action button clicks
 */
@Composable
private fun ReminderActionScreenLandscape(
  screenState: ReminderActionScreenState,
  onTodoItemClick: (String) -> Unit,
  onActionClick: (ReminderAction) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Left column: Content (header + todo list)
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header section
      ReminderHeader(header = screenState.header)

      // Todo list section (if present)
      screenState.todoList?.let { todoList ->
        TodoListSection(
          todoList = todoList,
          onItemClick = onTodoItemClick
        )
      }
    }

    // Right column: Actions
    Column(
      modifier = Modifier
        .width(280.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.Bottom
    ) {
      ActionsSection(
        mainAction = screenState.mainAction,
        secondaryActions = screenState.secondaryActions,
        onActionClick = onActionClick
      )
    }
  }
}

@Composable
private fun SnoozeDialogContent(
  onDismiss: () -> Unit,
  onSnooze: (Int) -> Unit
) {
  Column(
    modifier = Modifier.padding(16.dp)
  ) {
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
        enabled = true
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(R.string.action_snooze_custom),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
        textAlign = TextAlign.Center,
        maxLines = 2,
      )
      Spacer(modifier = Modifier.width(8.dp))
      PrimaryIconButton(
        icon = AppIcons.Ok,
        contentDescription = stringResource(R.string.action_snooze),
        onClick = {

        },
        enabled = false,
        color = Color.Transparent,
        iconColor = Color.Transparent,
        disabledColor = Color.Transparent,
        disabledIconColor = Color.Transparent
      )
    }

    HorizontalDivider(
      modifier = Modifier.padding(vertical = 8.dp),
      color = MaterialTheme.colorScheme.outlineVariant
    )

    BottomSheetList(
      items = listOf(
        BottomSheetItem(
          id = 5,
          title = stringResource(R.string.x_minutes, "5")
        ),
        BottomSheetItem(
          id = 10,
          title = stringResource(R.string.x_minutes, "10")
        ),
        BottomSheetItem(
          id = 15,
          title = stringResource(R.string.x_minutes, "15")
        ),
        BottomSheetItem(
          id = 30,
          title = stringResource(R.string.x_minutes, "30")
        ),
        BottomSheetItem(
          id = 60,
          title = stringResource(R.string.x_hours, "1")
        ),
        BottomSheetItem(
          id = 120,
          title = stringResource(R.string.x_hours, "2")
        ),
        BottomSheetItem(
          id = 180,
          title = stringResource(R.string.x_hours, "3")
        ),
        BottomSheetItem(
          id = 1440,
          title = stringResource(R.string.x_days, "1")
        )
      ),
      onItemClick = { minutes ->
        onSnooze(minutes)
      },
      modifier = Modifier.padding(bottom = 16.dp)
    )
  }
}

/**
 * Displays the reminder header based on its type.
 *
 * @param header The header data containing reminder information
 */
@Composable
private fun ReminderHeader(header: ReminderActionScreenHeader) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      when (header) {
        is ReminderActionScreenHeader.SimpleWithSummary -> {
          SimpleHeaderContent(text = header.text)
        }

        is ReminderActionScreenHeader.MakeCall -> {
          ContactHeaderContent(
            text = header.text,
            contactName = header.contactName,
            contactInfo = header.phoneNumber,
            contactPhoto = header.contactPhoto,
            icon = R.drawable.ic_fluent_phone
          )
        }

        is ReminderActionScreenHeader.SendSms -> {
          ContactHeaderContent(
            text = header.text,
            contactName = header.contactName,
            contactInfo = header.phoneNumber,
            contactPhoto = header.contactPhoto,
            icon = R.drawable.ic_fluent_send
          )
        }

        is ReminderActionScreenHeader.SendEmail -> {
          EmailHeaderContent(
            text = header.text,
            emailAddress = header.emailAddress,
            contactName = header.contactName,
            subject = header.subject,
            contactPhoto = header.contactPhoto
          )
        }

        is ReminderActionScreenHeader.OpenApplication -> {
          AppHeaderContent(
            text = header.text,
            appName = header.appName,
            appIcon = header.appIcon?.toBitmap()
          )
        }

        is ReminderActionScreenHeader.OpenLink -> {
          LinkHeaderContent(
            text = header.text,
            url = header.url
          )
        }
      }
    }
  }
}

/**
 * Displays simple text header content.
 *
 * @param text The reminder text to display
 */
@Composable
private fun SimpleHeaderContent(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.headlineSmall,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSurfaceVariant
  )
}

/**
 * Displays contact-related header content with optional photo.
 *
 * @param text The reminder text
 * @param contactName The contact name if available
 * @param contactInfo The phone number or contact info
 * @param contactPhoto Optional contact photo bitmap
 * @param icon Icon resource for the contact type
 */
@Composable
private fun ContactHeaderContent(
  text: String,
  contactName: String?,
  contactInfo: String,
  contactPhoto: Bitmap?,
  icon: Int
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Contact photo or icon
    if (contactPhoto != null) {
      Image(
        bitmap = contactPhoto.asImageBitmap(),
        contentDescription = contactName ?: contactInfo,
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape),
        contentScale = ContentScale.Crop
      )
    } else {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(id = icon),
          contentDescription = null,
          modifier = Modifier.size(28.dp),
          tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }

    // Contact info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      if (contactName != null) {
        Text(
          text = contactName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Text(
        text = contactInfo,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
      )
    }
  }
}

/**
 * Displays email header content.
 *
 * @param text The reminder text
 * @param emailAddress The email address
 * @param contactName Optional contact name
 * @param subject Optional email subject
 * @param contactPhoto Optional contact photo
 */
@Composable
private fun EmailHeaderContent(
  text: String,
  emailAddress: String,
  contactName: String?,
  subject: String?,
  contactPhoto: Bitmap?
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Contact photo or email icon
    if (contactPhoto != null) {
      Image(
        bitmap = contactPhoto.asImageBitmap(),
        contentDescription = contactName ?: emailAddress,
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape),
        contentScale = ContentScale.Crop
      )
    } else {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_fluent_send),
          contentDescription = null,
          modifier = Modifier.size(28.dp),
          tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
      }
    }

    // Email info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      if (contactName != null) {
        Text(
          text = contactName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Text(
        text = emailAddress,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
      )

      subject?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
      }
    }
  }
}

/**
 * Displays app header content.
 *
 * @param text The reminder text
 * @param appName The application name
 * @param appIcon Optional app icon resource
 */
@Composable
private fun AppHeaderContent(
  text: String,
  appName: String,
  appIcon: Bitmap?
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // App icon
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.secondaryContainer),
      contentAlignment = Alignment.Center
    ) {
      if (appIcon != null) {
        Image(
          bitmap = appIcon.asImageBitmap(),
          contentDescription = appName,
          modifier = Modifier.size(32.dp),
          contentScale = ContentScale.Fit
        )
      } else {
        Icon(
          painter = painterResource(id = R.drawable.ic_fluent_apps),
          contentDescription = appName,
          modifier = Modifier.size(32.dp),
          tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
      }
    }

    // App info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = appName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

/**
 * Displays link header content.
 *
 * @param text The reminder text
 * @param url The URL to open
 */
@Composable
private fun LinkHeaderContent(
  text: String,
  url: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Link icon
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.secondaryContainer),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(id = R.drawable.ic_fluent_globe),
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint = MaterialTheme.colorScheme.onSecondaryContainer
      )
    }

    // Link info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = url,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * Displays the todo list section.
 *
 * Scrollable when content exceeds available space to ensure all items are accessible.
 *
 * @param todoList The list of todo items
 * @param onItemClick Callback when a todo item is clicked
 */
@Composable
private fun TodoListSection(
  todoList: ReminderActionScreenTodoList,
  onItemClick: ((String) -> Unit)?
) {
  Card(
    modifier = Modifier
      .fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    // Removed internal verticalScroll to avoid nested scroll causing infinite height constraints.
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      todoList.items.forEach { item ->
        TodoItemRow(
          item = item,
          onItemClick = onItemClick
        )
      }
    }
  }
}

/**
 * Displays a single todo item row.
 *
 * @param item The todo item data
 * @param onItemClick Callback when the item is clicked
 */
@Composable
private fun TodoItemRow(
  item: ReminderActionScreenTodoItem,
  onItemClick: ((String) -> Unit)?
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = item.isCompleted,
      onCheckedChange = { onItemClick?.invoke(item.id) },
      enabled = onItemClick != null
    )

    Text(
      text = item.text,
      style = MaterialTheme.typography.bodyMedium,
      color = if (item.isCompleted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
      } else {
        MaterialTheme.colorScheme.onSurface
      },
      textDecoration = if (item.isCompleted) {
        androidx.compose.ui.text.style.TextDecoration.LineThrough
      } else {
        null
      }
    )
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
  mainAction: ReminderActionScreenActionItem,
  secondaryActions: List<ReminderActionScreenActionItem>,
  onActionClick: (ReminderAction) -> Unit
) {
  if (secondaryActions.isEmpty()) {
    Button(
      onClick = { onActionClick(mainAction.action) },
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
      shape = ButtonDefaults.shape,
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
      )
    ) {
      Icon(
        painter = painterResource(id = mainAction.iconRes),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = mainAction.text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
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
            contentDescription = mainAction.text
          )
          Text(
            text = mainAction.text,
            modifier = Modifier.padding(start = 8.dp)
          )
        },
        rightContent = {
          Box {
            Icon(
              painter = painterResource(id = R.drawable.ic_fluent_more_hor),
              contentDescription = stringResource(com.elementary.tasks.R.string.more_options)
            )
            PopupMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false },
              items = secondaryActions.mapIndexed { index, item ->
                PopupMenuItem(
                  id = index,
                  title = item.text,
                  iconRes = item.iconRes
                )
              },
              onItemClick = { itemId ->
                val actionItem = secondaryActions.getOrNull(itemId)
                actionItem?.let {
                  onActionClick(it.action)
                }
              }
            )
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp),
        cornerRadius = 28.dp
      )
    }
  }
}

/**
 * Preview for ActionsSection showing different action button layouts.
 *
 * Displays the main action button with various secondary action combinations
 * to visualize button layout and spacing.
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ActionsSectionPreview() {
  AppTheme {
    Surface(
      modifier = Modifier.padding(16.dp),
      color = MaterialTheme.colorScheme.background
    ) {
      ActionsSection(
        mainAction = ReminderActionScreenActionItem(
          action = ReminderAction.Complete,
          text = "Mark as Complete",
          iconRes = R.drawable.ic_fluent_checkmark
        ),
        secondaryActions = listOf(
          ReminderActionScreenActionItem(
            action = ReminderAction.Snooze,
            text = "Snooze",
            iconRes = R.drawable.ic_fluent_alert_snooze
          ),
          ReminderActionScreenActionItem(
            action = ReminderAction.Edit,
            text = "Edit",
            iconRes = R.drawable.ic_fluent_edit
          ),
          ReminderActionScreenActionItem(
            action = ReminderAction.Dismiss,
            text = "Dismiss",
            iconRes = R.drawable.ic_fluent_dismiss
          )
        ),
        onActionClick = { }
      )
    }
  }
}

/**
 * Preview for ActionsSection with only main action (no secondary actions).
 *
 * Shows the layout when there are no secondary actions available.
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ActionsSectionWithoutSecondaryPreview() {
  AppTheme {
    Surface(
      modifier = Modifier.padding(16.dp),
      color = MaterialTheme.colorScheme.background
    ) {
      ActionsSection(
        mainAction = ReminderActionScreenActionItem(
          action = ReminderAction.MakeCall,
          text = "Make Call",
          iconRes = R.drawable.ic_fluent_phone
        ),
        secondaryActions = emptyList(),
        onActionClick = { }
      )
    }
  }
}

/**
 * Preview for ActionsSection with two secondary actions.
 *
 * Shows a common layout with the main action and two secondary buttons.
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ActionsSectionTwoSecondaryPreview() {
  AppTheme {
    Surface(
      modifier = Modifier.padding(16.dp),
      color = MaterialTheme.colorScheme.background
    ) {
      ActionsSection(
        mainAction = ReminderActionScreenActionItem(
          action = ReminderAction.SendEmail,
          text = "Send Email",
          iconRes = R.drawable.ic_fluent_send
        ),
        secondaryActions = listOf(
          ReminderActionScreenActionItem(
            action = ReminderAction.Edit,
            text = "Edit",
            iconRes = R.drawable.ic_fluent_edit
          ),
          ReminderActionScreenActionItem(
            action = ReminderAction.Dismiss,
            text = "Cancel",
            iconRes = R.drawable.ic_fluent_dismiss
          )
        ),
        onActionClick = { }
      )
    }
  }
}

/**
 * Preview for SnoozeDialogContent showing snooze time options.
 *
 * Displays the snooze dialog content with various time options
 * including minutes, hours, and days to visualize the layout.
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SnoozeDialogContentPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background
    ) {
      SnoozeDialogContent(
        onDismiss = { },
        onSnooze = { }
      )
    }
  }
}

/**
 * Preview for ReminderActionScreenPortrait layout.
 *
 * Shows the vertical layout optimized for portrait orientation
 * with header, todo list, and actions arranged vertically.
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ReminderActionScreenPortraitPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background
    ) {
      ReminderActionScreenPortrait(
        screenState = ReminderActionScreenState(
          id = "preview-1",
          header = ReminderActionScreenHeader.SimpleWithSummary(
            text = "Meeting with team at 3 PM"
          ),
          todoList = ReminderActionScreenTodoList(
            items = listOf(
              ReminderActionScreenTodoItem(
                id = "1",
                text = "Prepare presentation",
                isCompleted = false
              ),
              ReminderActionScreenTodoItem(
                id = "2",
                text = "Review documents",
                isCompleted = true
              )
            )
          ),
          mainAction = ReminderActionScreenActionItem(
            action = ReminderAction.Complete,
            text = "Complete",
            iconRes = R.drawable.ic_fluent_checkmark
          ),
          secondaryActions = listOf(
            ReminderActionScreenActionItem(
              action = ReminderAction.Snooze,
              text = "Snooze",
              iconRes = R.drawable.ic_fluent_alert_snooze
            ),
            ReminderActionScreenActionItem(
              action = ReminderAction.Edit,
              text = "Edit",
              iconRes = R.drawable.ic_fluent_edit
            )
          )
        ),
        onTodoItemClick = { },
        onActionClick = { }
      )
    }
  }
}

/**
 * Preview for ReminderActionScreenLandscape layout.
 *
 * Shows the horizontal two-column layout optimized for landscape orientation
 * with content on left and actions on right.
 */
@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun ReminderActionScreenLandscapePreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background
    ) {
      ReminderActionScreenLandscape(
        screenState = ReminderActionScreenState(
          id = "preview-2",
          header = ReminderActionScreenHeader.SimpleWithSummary(
            text = "Meeting with team at 3 PM"
          ),
          todoList = ReminderActionScreenTodoList(
            items = listOf(
              ReminderActionScreenTodoItem(
                id = "1",
                text = "Prepare presentation",
                isCompleted = false
              ),
              ReminderActionScreenTodoItem(
                id = "2",
                text = "Review documents",
                isCompleted = true
              )
            )
          ),
          mainAction = ReminderActionScreenActionItem(
            action = ReminderAction.Complete,
            text = "Complete",
            iconRes = R.drawable.ic_fluent_checkmark
          ),
          secondaryActions = listOf(
            ReminderActionScreenActionItem(
              action = ReminderAction.Snooze,
              text = "Snooze",
              iconRes = R.drawable.ic_fluent_alert_snooze
            ),
            ReminderActionScreenActionItem(
              action = ReminderAction.Edit,
              text = "Edit",
              iconRes = R.drawable.ic_fluent_edit
            )
          )
        ),
        onTodoItemClick = { },
        onActionClick = { }
      )
    }
  }
}
