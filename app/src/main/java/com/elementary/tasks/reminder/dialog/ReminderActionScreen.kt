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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.elementary.tasks.reminder.actions.ReminderAction
import com.github.naz013.ui.common.R
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
@Composable
fun ReminderActionScreen(
  viewModel: ReminderActionActivityViewModel = koinViewModel(),
  modifier: Modifier = Modifier
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val state by viewModel.state.observeAsState()

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
              onItemClick = { itemId ->
                // Input validation: ignore blank ids
                if (itemId.isBlank()) return@TodoListSection
                viewModel.onTodoItemClick(itemId)
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons section
        ActionsSection(
          mainAction = screenState.mainAction,
          secondaryActions = screenState.secondaryActions,
          onActionClick = { action ->
            viewModel.onActionClick(action)
          }
        )
      }
    }
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
        Icon(
          bitmap = appIcon.asImageBitmap(),
          contentDescription = appName,
          modifier = Modifier.size(32.dp),
          tint = MaterialTheme.colorScheme.onSecondaryContainer
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
      .fillMaxWidth(), // Max height to allow scrolling
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
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
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Main action button
    Button(
      onClick = { onActionClick(mainAction.action) },
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp),
      shape = RoundedCornerShape(12.dp),
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

    // Secondary action buttons
    if (secondaryActions.isNotEmpty()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        secondaryActions.forEach { action ->
          OutlinedButton(
            onClick = { onActionClick(action.action) },
            modifier = Modifier
              .weight(1f)
              .height(48.dp),
            shape = RoundedCornerShape(12.dp)
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                painter = painterResource(id = action.iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = action.text,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}
