package com.elementary.tasks.core.view_models.places

import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.Prefs

class PlacesViewModel(
  appDb: AppDb,
  prefs: Prefs
) : BasePlacesViewModel(appDb, prefs) {
  val places = appDb.placesDao().loadAll()
}
