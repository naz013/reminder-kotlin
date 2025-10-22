package com.github.naz013.ui.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.github.naz013.ui.common.R

object AppIcons {
  val Ok: Painter @Composable get() = painterResource(R.drawable.ic_fluent_checkmark)
  val Clear: Painter @Composable get() = painterResource(R.drawable.ic_fluent_dismiss)
}
