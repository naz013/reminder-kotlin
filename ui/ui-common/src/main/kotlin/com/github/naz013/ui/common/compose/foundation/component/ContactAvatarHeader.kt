package com.github.naz013.ui.common.compose.foundation.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Row layout shared by action-screen headers that show a contact's photo (or a fallback
 * icon in a colored circle) next to a primary line of text. Feature-specific detail lines
 * (contact name, phone number, birthday date, age, ...) go in [extraContent].
 */
@Composable
fun ContactAvatarHeader(
  modifier: Modifier = Modifier,
  text: String,
  contactPhoto: Bitmap?,
  fallbackIconRes: Int,
  textStyle: TextStyle = MaterialTheme.typography.bodyLargeEmphasized,
  textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  avatarSize: Dp = 56.dp,
  avatarBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
  avatarIconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
  extraContent: @Composable ColumnScope.() -> Unit = {},
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (contactPhoto != null) {
      Image(
        bitmap = contactPhoto.asImageBitmap(),
        contentDescription = text,
        modifier = Modifier
          .size(avatarSize)
          .clip(CircleShape),
        contentScale = ContentScale.Crop,
      )
    } else {
      Box(
        modifier = Modifier
          .size(avatarSize)
          .clip(CircleShape)
          .background(avatarBackgroundColor),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painter = painterResource(id = fallbackIconRes),
          contentDescription = null,
          modifier = Modifier.size(avatarSize / 2),
          tint = avatarIconTint,
        )
      }
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = text,
        style = textStyle,
        color = textColor,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      extraContent()
    }
  }
}
