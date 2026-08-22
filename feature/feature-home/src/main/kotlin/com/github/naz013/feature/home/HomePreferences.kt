package com.github.naz013.feature.home

import com.github.naz013.domain.home.HeaderNavigationSection

interface HomePreferences {
  var isUserLogged: Boolean
  var lastVersionCode: Long
  val birthdayColor: Int
  var headerNavigationOrder: List<HeaderNavigationSection>
  var disabledHeaderNavigationSections: Set<HeaderNavigationSection>
}
