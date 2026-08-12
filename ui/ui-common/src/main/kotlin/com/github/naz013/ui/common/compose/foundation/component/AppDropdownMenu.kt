package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun AppDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  items: List<PopupMenuItem>,
  onItemClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    shape = RoundedCornerShape(16.dp)
  ) {
    items.forEach { item ->
      DropdownMenuItem(
        text = {
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            item.iconRes?.let {
              Icon(
                painter = painterResource(it),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
            }
            Text(
              text = item.title,
              style = MaterialTheme.typography.bodyLarge
            )
          }
        },
        onClick = {
          onDismissRequest()
          onItemClick(item.id)
        }
      )
    }
  }
}
