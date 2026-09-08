package com.github.naz013.feature.birthday.dialog

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.birthday.actions.BirthdayAction
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppShapes
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.ActionButtonsSection
import com.github.naz013.ui.common.compose.foundation.component.ActionDialogScaffold
import com.github.naz013.ui.common.compose.foundation.component.ContactAvatarHeader
import com.github.naz013.ui.common.compose.foundation.snackbar.rememberToastDispatcher
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.common.livedata.ObserveEvent
import com.github.naz013.ui.common.permission.rememberPermissionRequesterRationale
import com.github.naz013.ui.common.permission.requestCallPermission
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayActionScreen(
  modifier: Modifier = Modifier,
  id: String,
  onFinish: () -> Unit = {},
  onCallClick: (String) -> Unit,
  onSmsClick: (String) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  val viewModel = koinViewModel<BirthdayActionViewModel> { parametersOf(id) }

  val toastDispatcher = rememberToastDispatcher()
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
        permissionRequester.requestCallPermission(
          onGranted = {
            onCallClick(event.phoneNumber)
            onFinish()
          },
        )
      }

      is BirthdayActionViewModel.ViewModelEvent.SendSms -> {
        onSmsClick(event.phoneNumber)
        onFinish()
      }
    }
  }

  val state by viewModel.state.observeAsState()

  ActionDialogScaffold(
    modifier = modifier,
    state = state,
    logTag = "BirthdayActionScreen",
    portrait = { screenState ->
      BirthdayActionScreenPortrait(
        screenState = screenState,
        onActionClick = { action -> viewModel.onActionClick(action) },
        adsContent = adsContent,
      )
    },
    landscape = { screenState ->
      BirthdayActionScreenLandscape(
        screenState = screenState,
        onActionClick = { action -> viewModel.onActionClick(action) },
        adsContent = adsContent,
      )
    },
  )
}

@Composable
private fun BirthdayActionScreenPortrait(
  screenState: BirthdayActionScreenState,
  onActionClick: (BirthdayAction) -> Unit,
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
      BirthdayHeader(header = screenState.header)

      adsContent()
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
private fun BirthdayActionScreenLandscape(
  screenState: BirthdayActionScreenState,
  onActionClick: (BirthdayAction) -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Column(
      modifier = Modifier
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
private fun BirthdayHeader(header: BirthdayActionScreenHeader) {
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
      ContactAvatarHeader(
        text = header.text,
        textStyle = MaterialTheme.typography.titleLargeEmphasized,
        contactPhoto = header.contactPhoto,
        fallbackIconRes = DrawableCatalog.Fluent.Phone,
        extraContent = {
          if (header.birthdayDate.isNotEmpty()) {
            Text(
              text = header.birthdayDate,
              style = MaterialTheme.typography.bodyLargeEmphasized,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          if (header.age != null) {
            Text(
              text = header.age,
              style = MaterialTheme.typography.bodyMediumEmphasized,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          if (header.contactName != null && header.contactName != header.text) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = header.contactName,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          if (header.phoneNumber.isNotEmpty()) {
            Text(
              text = header.phoneNumber,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
      )
    }
  }
}

// Preview composables for testing and demonstration
@Preview(showBackground = true)
@Composable
private fun BirthdayActionScreenPortraitPreview() {
  AppTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background,
    ) {
      BirthdayActionScreenPortrait(
        screenState = BirthdayActionScreenState(
          id = "preview-1",
          header = BirthdayActionScreenHeader(
            text = "John Doe",
            phoneNumber = "+1234567890",
            contactName = "John Doe",
            contactPhoto = null,
            birthdayDate = "December 23",
            age = "29 years",
          ),
          mainAction = BirthdayActionScreenActionItem(
            action = BirthdayAction.Ok,
            text = "OK",
            iconRes = R.drawable.ic_fluent_checkmark,
          ),
          secondaryActions = listOf(
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
  AppTheme {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background,
    ) {
      BirthdayActionScreenLandscape(
        screenState = BirthdayActionScreenState(
          id = "preview-2",
          header = BirthdayActionScreenHeader(
            text = "Jane Smith",
            phoneNumber = "+0987654321",
            contactName = "Jane Smith",
            contactPhoto = null,
            birthdayDate = "March 15",
            age = null, // Year ignored, no age shown
          ),
          mainAction = BirthdayActionScreenActionItem(
            action = BirthdayAction.Ok,
            text = "Ok",
            iconRes = R.drawable.ic_fluent_checkmark,
          ),
          secondaryActions = listOf(
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
