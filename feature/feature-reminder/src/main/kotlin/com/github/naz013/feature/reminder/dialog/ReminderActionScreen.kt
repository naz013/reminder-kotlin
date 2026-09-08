package com.github.naz013.feature.reminder.dialog

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.github.naz013.feature.reminder.actions.ReminderAction
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppShapes
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.PrimaryIconButton
import com.github.naz013.ui.common.compose.foundation.component.ActionButtonsSection
import com.github.naz013.ui.common.compose.foundation.component.ActionDialogScaffold
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetItem
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetList
import com.github.naz013.ui.common.compose.foundation.component.ContactAvatarHeader
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.ui.common.permission.requestCallPermission
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderActionScreen(
  modifier: Modifier = Modifier,
  id: String,
  onFinish: () -> Unit,
  onEdit: (String) -> Unit,
  onCallClick: (String) -> Unit,
  onSmsClick: (target: String, message: String) -> Unit,
  onEmailClick: (email: String, subject: String, message: String) -> Unit,
  onAppClick: (String) -> Unit,
  onUrlClick: (String) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  val viewModel: ReminderActionActivityViewModel = koinViewModel { parametersOf(id) }

  var showSnoozeBottomSheet by remember { mutableStateOf(false) }
  val snoozeSheetState = rememberBottomSheetState(SheetValue.Hidden)
  val scope = rememberCoroutineScope()

  val permissionRequester = rememberPermissionRequesterRationale()
  val toastDispatcher = rememberToastDispatcher()

  viewModel.event.ObserveEvent { event ->
    when (event) {
      is ReminderActionActivityViewModel.ViewModelEvent.Finish -> onFinish()
      is ReminderActionActivityViewModel.ViewModelEvent.Edit -> onEdit(event.id)

      is ReminderActionActivityViewModel.ViewModelEvent.MakeCall -> {
        permissionRequester.requestCallPermission(
          onGranted = {
            onCallClick(event.target)
            onFinish()
          },
        )
      }

      is ReminderActionActivityViewModel.ViewModelEvent.SendSms -> {
        onSmsClick(event.target, event.message)
        onFinish()
      }

      is ReminderActionActivityViewModel.ViewModelEvent.SendEmail -> {
        onEmailClick(event.email, event.subject, event.message)
        onFinish()
      }

      is ReminderActionActivityViewModel.ViewModelEvent.ShowError -> {
        toastDispatcher.showToast(message = event.message)
      }

      is ReminderActionActivityViewModel.ViewModelEvent.OpenApp -> {
        onAppClick(event.target)
        onFinish()
      }

      is ReminderActionActivityViewModel.ViewModelEvent.OpenLink -> {
        onUrlClick(event.target)
        onFinish()
      }

      is ReminderActionActivityViewModel.ViewModelEvent.ShowSnoozeDialog -> {
        showSnoozeBottomSheet = true
      }
    }
  }

  val state by viewModel.state.observeAsState()

  ActionDialogScaffold(
    modifier = modifier,
    state = state,
    logTag = "ReminderActionScreen",
    overlayContent = {
      if (showSnoozeBottomSheet) {
        AppModalBottomSheet(
          onDismissRequest = { showSnoozeBottomSheet = false },
          sheetState = snoozeSheetState,
          dragHandle = null,
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
            },
          )
        }
      }
    },
    portrait = { screenState ->
      ReminderActionScreenPortrait(
        screenState = screenState,
        onTodoItemClick = { itemId ->
          if (itemId.isBlank()) return@ReminderActionScreenPortrait
          viewModel.onTodoItemClick(itemId)
        },
        onActionClick = { action -> viewModel.onActionClick(action) },
        adsContent = adsContent,
      )
    },
    landscape = { screenState ->
      ReminderActionScreenLandscape(
        screenState = screenState,
        onTodoItemClick = { itemId ->
          if (itemId.isBlank()) return@ReminderActionScreenLandscape
          viewModel.onTodoItemClick(itemId)
        },
        onActionClick = { action -> viewModel.onActionClick(action) },
        adsContent = adsContent,
      )
    },
  )
}

@Composable
private fun ReminderActionScreenPortrait(
  screenState: ReminderActionScreenState,
  onTodoItemClick: (String) -> Unit,
  onActionClick: (ReminderAction) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Column(
    modifier = Modifier
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
      ReminderHeader(header = screenState.header)

      adsContent()

      // Todo list section (if present)
      screenState.todoList?.let { todoList ->
        TodoListSection(
          todoList = todoList,
          onItemClick = onTodoItemClick,
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Action buttons section
    ActionButtonsSection(
      mainAction = screenState.mainAction,
      secondaryActions = screenState.secondaryActions,
      onActionClick = onActionClick,
    )
  }
}

@Composable
private fun ReminderActionScreenLandscape(
  screenState: ReminderActionScreenState,
  onTodoItemClick: (String) -> Unit,
  onActionClick: (ReminderAction) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Left column: Content (header + todo list)
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header section
      ReminderHeader(header = screenState.header)

      adsContent()

      // Todo list section (if present)
      screenState.todoList?.let { todoList ->
        TodoListSection(
          todoList = todoList,
          onItemClick = onTodoItemClick,
        )
      }
    }

    // Right column: Actions
    Column(
      modifier = Modifier
        .width(280.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.Bottom,
    ) {
      ActionButtonsSection(
        mainAction = screenState.mainAction,
        secondaryActions = screenState.secondaryActions,
        onActionClick = onActionClick,
      )
    }
  }
}

@Composable
private fun SnoozeDialogContent(
  onDismiss: () -> Unit,
  onSnooze: (Int) -> Unit,
) {
  Column(
    modifier = Modifier.padding(16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      PrimaryIconButton(
        icon = AppIcons.Fluent.Dismiss,
        contentDescription = stringResource(R.string.cancel),
        onClick = onDismiss,
        color = MaterialTheme.colorScheme.errorContainer,
        iconColor = MaterialTheme.colorScheme.onErrorContainer,
        enabled = true,
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
        icon = AppIcons.Fluent.Checkmark,
        contentDescription = stringResource(R.string.action_snooze),
        onClick = { },
        enabled = false,
        color = Color.Transparent,
        iconColor = Color.Transparent,
        disabledColor = Color.Transparent,
        disabledIconColor = Color.Transparent,
      )
    }

    HorizontalDivider(
      modifier = Modifier.padding(vertical = 8.dp),
      color = MaterialTheme.colorScheme.outlineVariant,
    )

    BottomSheetList(
      items = listOf(
        BottomSheetItem(
          id = 5,
          title = stringResource(R.string.x_minutes, "5"),
        ),
        BottomSheetItem(
          id = 10,
          title = stringResource(R.string.x_minutes, "10"),
        ),
        BottomSheetItem(
          id = 15,
          title = stringResource(R.string.x_minutes, "15"),
        ),
        BottomSheetItem(
          id = 30,
          title = stringResource(R.string.x_minutes, "30"),
        ),
        BottomSheetItem(
          id = 60,
          title = stringResource(R.string.x_hours, "1"),
        ),
        BottomSheetItem(
          id = 120,
          title = stringResource(R.string.x_hours, "2"),
        ),
        BottomSheetItem(
          id = 180,
          title = stringResource(R.string.x_hours, "3"),
        ),
        BottomSheetItem(
          id = 1440,
          title = stringResource(R.string.x_days, "1"),
        ),
      ),
      onItemClick = { minutes ->
        onSnooze(minutes)
      },
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
private fun ReminderHeader(header: ReminderActionScreenHeader) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = AppShapes.tile,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      when (header) {
        is ReminderActionScreenHeader.SimpleWithSummary -> {
          SimpleHeaderContent(text = header.text)
        }

        is ReminderActionScreenHeader.MakeCall -> {
          ContactAvatarHeader(
            text = header.text,
            contactPhoto = header.contactPhoto,
            fallbackIconRes = DrawableCatalog.Fluent.Phone,
            extraContent = { ContactDetailLines(header.contactName, header.phoneNumber) },
          )
        }

        is ReminderActionScreenHeader.SendSms -> {
          ContactAvatarHeader(
            text = header.text,
            contactPhoto = header.contactPhoto,
            fallbackIconRes = DrawableCatalog.Fluent.Send,
            extraContent = { ContactDetailLines(header.contactName, header.phoneNumber) },
          )
        }

        is ReminderActionScreenHeader.SendEmail -> {
          EmailHeaderContent(
            text = header.text,
            emailAddress = header.emailAddress,
            contactName = header.contactName,
            subject = header.subject,
            contactPhoto = header.contactPhoto,
          )
        }

        is ReminderActionScreenHeader.OpenApplication -> {
          AppHeaderContent(
            text = header.text,
            appName = header.appName,
            appIcon = header.appIcon?.toBitmap(),
          )
        }

        is ReminderActionScreenHeader.OpenLink -> {
          LinkHeaderContent(
            text = header.text,
            url = header.url,
          )
        }
      }
    }
  }
}

@Composable
private fun SimpleHeaderContent(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.headlineSmallEmphasized,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun ContactDetailLines(contactName: String?, contactInfo: String) {
  if (contactName != null) {
    Text(
      text = contactName,
      style = MaterialTheme.typography.titleMediumEmphasized,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }

  Text(
    text = contactInfo,
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

@Composable
private fun EmailHeaderContent(
  text: String,
  emailAddress: String,
  contactName: String?,
  subject: String?,
  contactPhoto: Bitmap?,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Contact photo or email icon
    if (contactPhoto != null) {
      Image(
        bitmap = contactPhoto.asImageBitmap(),
        contentDescription = contactName ?: emailAddress,
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape),
        contentScale = ContentScale.Crop,
      )
    } else {
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painter = AppIcons.Fluent.Send,
          contentDescription = null,
          modifier = Modifier.size(28.dp),
          tint = MaterialTheme.colorScheme.onTertiaryContainer,
        )
      }
    }

    // Email info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLargeEmphasized,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )

      if (contactName != null) {
        Text(
          text = contactName,
          style = MaterialTheme.typography.titleMediumEmphasized,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Text(
        text = emailAddress,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      subject?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontStyle = FontStyle.Italic,
        )
      }
    }
  }
}

@Composable
private fun AppHeaderContent(
  text: String,
  appName: String,
  appIcon: Bitmap?,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // App icon
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.secondaryContainer),
      contentAlignment = Alignment.Center,
    ) {
      if (appIcon != null) {
        Image(
          bitmap = appIcon.asImageBitmap(),
          contentDescription = appName,
          modifier = Modifier.size(32.dp),
          contentScale = ContentScale.Fit,
        )
      } else {
        Icon(
          painter = AppIcons.Fluent.Apps,
          contentDescription = appName,
          modifier = Modifier.size(32.dp),
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
        )
      }
    }

    // App info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLargeEmphasized,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )

      Text(
        text = appName,
        style = MaterialTheme.typography.titleMediumEmphasized,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun LinkHeaderContent(
  text: String,
  url: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Link icon
    Box(
      modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.secondaryContainer),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        painter = AppIcons.Fluent.Globe,
        contentDescription = null,
        modifier = Modifier.size(28.dp),
        tint = MaterialTheme.colorScheme.onSecondaryContainer,
      )
    }

    // Link info
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLargeEmphasized,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )

      Text(
        text = url,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun TodoListSection(
  todoList: ReminderActionScreenTodoList,
  onItemClick: ((String) -> Unit)?,
) {
  Card(
    modifier = Modifier
      .fillMaxWidth(),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
  ) {
    // Removed internal verticalScroll to avoid nested scroll causing infinite height constraints.
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      todoList.items.forEach { item ->
        TodoItemRow(
          item = item,
          onItemClick = onItemClick,
        )
      }
    }
  }
}

@Composable
private fun TodoItemRow(
  item: ReminderActionScreenTodoItem,
  onItemClick: ((String) -> Unit)?,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(
      checked = item.isCompleted,
      onCheckedChange = { onItemClick?.invoke(item.id) },
      enabled = onItemClick != null,
    )

    Text(
      text = item.text,
      style = MaterialTheme.typography.bodyMedium,
      color = if (item.isCompleted) {
        MaterialTheme.colorScheme.onSurfaceVariant
      } else {
        MaterialTheme.colorScheme.onSurface
      },
      textDecoration = if (item.isCompleted) {
        TextDecoration.LineThrough
      } else {
        null
      },
    )
  }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ActionsSectionPreview() {
  AppTheme {
    Surface(
      modifier = Modifier.padding(16.dp),
      color = MaterialTheme.colorScheme.background,
    ) {
      ActionButtonsSection(
        mainAction = ReminderActionScreenActionItem(
          action = ReminderAction.Complete,
          text = "Mark as Complete",
          iconRes = R.drawable.ic_fluent_checkmark,
        ),
        secondaryActions = listOf(
          ReminderActionScreenActionItem(
            action = ReminderAction.Snooze,
            text = "Snooze",
            iconRes = R.drawable.ic_fluent_alert_snooze,
          ),
          ReminderActionScreenActionItem(
            action = ReminderAction.Edit,
            text = "Edit",
            iconRes = R.drawable.ic_fluent_edit,
          ),
          ReminderActionScreenActionItem(
            action = ReminderAction.Dismiss,
            text = "Dismiss",
            iconRes = R.drawable.ic_fluent_dismiss,
          ),
        ),
        onActionClick = { },
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ActionsSectionWithoutSecondaryPreview() {
  AppTheme {
    Surface(
      modifier = Modifier.padding(16.dp),
      color = MaterialTheme.colorScheme.background,
    ) {
      ActionButtonsSection(
        mainAction = ReminderActionScreenActionItem(
          action = ReminderAction.MakeCall,
          text = "Make Call",
          iconRes = R.drawable.ic_fluent_phone,
        ),
        secondaryActions = emptyList(),
        onActionClick = { },
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ActionsSectionTwoSecondaryPreview() {
  AppTheme {
    Surface(
      modifier = Modifier.padding(16.dp),
      color = MaterialTheme.colorScheme.background,
    ) {
      ActionButtonsSection(
        mainAction = ReminderActionScreenActionItem(
          action = ReminderAction.SendEmail,
          text = "Send Email",
          iconRes = R.drawable.ic_fluent_send,
        ),
        secondaryActions = listOf(
          ReminderActionScreenActionItem(
            action = ReminderAction.Edit,
            text = "Edit",
            iconRes = R.drawable.ic_fluent_edit,
          ),
          ReminderActionScreenActionItem(
            action = ReminderAction.Dismiss,
            text = "Cancel",
            iconRes = R.drawable.ic_fluent_dismiss,
          ),
        ),
        onActionClick = { },
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SnoozeDialogContentPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
    ) {
      SnoozeDialogContent(
        onDismiss = { },
        onSnooze = { },
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ReminderActionScreenPortraitPreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
    ) {
      ReminderActionScreenPortrait(
        screenState = ReminderActionScreenState(
          id = "preview-1",
          header = ReminderActionScreenHeader.SimpleWithSummary(
            text = "Meeting with team at 3 PM",
          ),
          todoList = ReminderActionScreenTodoList(
            items = listOf(
              ReminderActionScreenTodoItem(
                id = "1",
                text = "Prepare presentation",
                isCompleted = false,
              ),
              ReminderActionScreenTodoItem(
                id = "2",
                text = "Review documents",
                isCompleted = true,
              ),
            ),
          ),
          mainAction = ReminderActionScreenActionItem(
            action = ReminderAction.Complete,
            text = "Complete",
            iconRes = R.drawable.ic_fluent_checkmark,
          ),
          secondaryActions = listOf(
            ReminderActionScreenActionItem(
              action = ReminderAction.Snooze,
              text = "Snooze",
              iconRes = R.drawable.ic_fluent_alert_snooze,
            ),
            ReminderActionScreenActionItem(
              action = ReminderAction.Edit,
              text = "Edit",
              iconRes = R.drawable.ic_fluent_edit,
            ),
          ),
        ),
        onTodoItemClick = {},
        onActionClick = {},
        adsContent = {},
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun ReminderActionScreenLandscapePreview() {
  AppTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
    ) {
      ReminderActionScreenLandscape(
        screenState = ReminderActionScreenState(
          id = "preview-2",
          header = ReminderActionScreenHeader.SimpleWithSummary(
            text = "Meeting with team at 3 PM",
          ),
          todoList = ReminderActionScreenTodoList(
            items = listOf(
              ReminderActionScreenTodoItem(
                id = "1",
                text = "Prepare presentation",
                isCompleted = false,
              ),
              ReminderActionScreenTodoItem(
                id = "2",
                text = "Review documents",
                isCompleted = true,
              ),
            ),
          ),
          mainAction = ReminderActionScreenActionItem(
            action = ReminderAction.Complete,
            text = "Complete",
            iconRes = R.drawable.ic_fluent_checkmark,
          ),
          secondaryActions = listOf(
            ReminderActionScreenActionItem(
              action = ReminderAction.Snooze,
              text = "Snooze",
              iconRes = R.drawable.ic_fluent_alert_snooze,
            ),
            ReminderActionScreenActionItem(
              action = ReminderAction.Edit,
              text = "Edit",
              iconRes = R.drawable.ic_fluent_edit,
            ),
          ),
        ),
        onTodoItemClick = {},
        onActionClick = {},
        adsContent = {},
      )
    }
  }
}
