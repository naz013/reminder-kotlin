package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.ui.common.compose.foundation.TooltipIconButton
import com.github.naz013.ui.common.compose.foundation.component.PhoneNumberVisualTransformation

/**
 * App picker: current app icon/name (if any) + a button that navigates to
 * [com.elementary.tasks.reminder.build.BuildReminderNavKey.SelectApplication]. Replaces
 * `ApplicationController`. That destination can't update this [builderItem] directly (it's a
 * separate Nav3 entry), so the pick result is applied back through the view model - see
 * [onPickApplication]'s call site in `BuildReminderNavGraph` for the round trip.
 */
@Composable
internal fun ApplicationValueEditor(
  builderItem: BuilderItem<String>,
  packageManagerWrapper: PackageManagerWrapper,
  onPickApplication: () -> Unit,
) {
  val packageName = builderItem.modifier.getValue()
  val appInfo = remember(packageName) {
    packageName?.let { runCatching { packageManagerWrapper.getAppInfo(it) }.getOrNull() }
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      if (appInfo != null) {
        val bitmap = remember(appInfo) {
          runCatching {
            appInfo.loadIcon(packageManagerWrapper.packageManager).toBitmap().asImageBitmap()
          }.getOrNull()
        }
        if (bitmap != null) {
          Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(32.dp))
          Spacer(modifier = Modifier.width(16.dp))
        }
      }
      Text(
        text = appInfo?.let {
          runCatching { packageManagerWrapper.packageManager.getApplicationLabel(it).toString() }.getOrNull()
        } ?: stringResource(R.string.application_not_selected),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = onPickApplication,
    ) {
      Text(stringResource(R.string.acc_select_application))
    }
  }
}

/**
 * Phone number field + a "pick from contacts" button. Replaces `PhoneInputController`, used by
 * both [com.elementary.tasks.reminder.build.PhoneCallBuilderItem] and
 * [com.elementary.tasks.reminder.build.SmsBuilderItem]. Picking is delegated to [onPickContact],
 * which owns the READ_CONTACTS permission check and the Fragment-registered launcher.
 */
@Composable
internal fun PhoneInputValueEditor(
  builderItem: BuilderItem<String>,
  onPickContact: (onResult: (phone: String) -> Unit) -> Unit,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var text by remember(builderItem) { mutableStateOf(builderItem.modifier.getValue() ?: "") }
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = text,
      onValueChange = {
        text = it
        builderItem.modifier.update(it)
        onValueChange(builderItem)
      },
      modifier = Modifier.weight(1f),
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
      visualTransformation = PhoneNumberVisualTransformation,
    )
    Spacer(modifier = Modifier.width(8.dp))
    TooltipIconButton(
      contentDescription = stringResource(R.string.acc_select_number_from_contacts),
    ) {
      IconButton(
        onClick = {
          onPickContact { phone ->
            text = phone
            builderItem.modifier.update(phone)
            onValueChange(builderItem)
          }
        },
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_contacts),
          contentDescription = stringResource(R.string.acc_select_number_from_contacts),
        )
      }
    }
  }
}
