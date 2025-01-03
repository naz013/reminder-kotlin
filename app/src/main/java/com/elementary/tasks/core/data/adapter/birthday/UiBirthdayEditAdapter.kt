package com.elementary.tasks.core.data.adapter.birthday

import com.github.naz013.domain.Birthday
import com.elementary.tasks.core.data.ui.birthday.UiBirthdayEdit

class UiBirthdayEditAdapter {

  fun convert(birthday: Birthday): UiBirthdayEdit {
    return UiBirthdayEdit(
      uuId = birthday.uuId,
      name = birthday.name,
      number = birthday.number,
      isYearIgnored = birthday.ignoreYear
    )
  }
}
