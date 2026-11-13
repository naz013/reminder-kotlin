package com.github.naz013.feature.search

import androidx.annotation.StringRes
import com.github.naz013.logic.search.SearchResultType
import com.github.naz013.ui.common.R

@StringRes
internal fun SearchResultType.labelRes(): Int = when (this) {
  SearchResultType.REMINDER -> R.string.reminders
  SearchResultType.NOTE -> R.string.notes
  SearchResultType.BIRTHDAY -> R.string.birthdays
  SearchResultType.GOOGLE_TASK -> R.string.google_tasks
  SearchResultType.GROUP -> R.string.groups
  SearchResultType.PLACE -> R.string.places
  SearchResultType.ROUTINE -> R.string.routines
  SearchResultType.HABIT -> R.string.habits
}
