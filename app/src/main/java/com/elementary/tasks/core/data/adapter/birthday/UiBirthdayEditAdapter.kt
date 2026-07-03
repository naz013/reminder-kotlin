package com.elementary.tasks.core.data.adapter.birthday

import com.elementary.tasks.core.data.ui.birthday.UiBirthdayEdit
import com.github.naz013.domain.Birthday

class UiBirthdayEditAdapter {
  fun convert(birthday: Birthday): UiBirthdayEdit =
    UiBirthdayEdit(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      isYearIgnored = birthday.ignoreYear,
    )
}
