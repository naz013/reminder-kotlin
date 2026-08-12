package com.github.naz013.ui.tag

import com.github.naz013.domain.Tag
import com.github.naz013.ui.common.theme.ThemeProvider

class TagChipStateAdapter(
  private val themeProvider: ThemeProvider,
) {
  operator fun invoke(tag: Tag): TagChipState {
    return TagChipState(
      id = tag.id,
      name = tag.name,
      color = themeProvider.themedColor(tag.color),
    )
  }
}
