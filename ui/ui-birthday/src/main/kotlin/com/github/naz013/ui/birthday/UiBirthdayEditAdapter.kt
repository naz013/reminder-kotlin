package com.github.naz013.ui.birthday

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
