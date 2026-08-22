package com.github.naz013.feature.settings.headeritems

import com.github.naz013.domain.home.HeaderNavigationSection

interface HeaderItemsPreferences {
  var order: List<HeaderNavigationSection>
  var disabledSections: Set<HeaderNavigationSection>
}
