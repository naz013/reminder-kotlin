package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.home.HeaderNavigationSection
import com.github.naz013.feature.settings.headeritems.HeaderItemsPreferences

class HeaderItemsPreferencesImpl(
  private val prefs: Prefs,
) : HeaderItemsPreferences {
  override var order: List<HeaderNavigationSection>
    get() = prefs.headerNavigationOrder
    set(value) { prefs.headerNavigationOrder = value }

  override var disabledSections: Set<HeaderNavigationSection>
    get() = prefs.disabledHeaderNavigationSections
    set(value) { prefs.disabledHeaderNavigationSections = value }
}
