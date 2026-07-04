package com.elementary.tasks.settings.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.legal.LegalDocumentRepository
import com.github.naz013.legal.LegalDocumentType

class DeveloperViewModel(
  private val legalDocumentRepository: LegalDocumentRepository,
  private val prefs: Prefs,
) : ViewModel() {

  val bannersReset: LiveData<Event<Unit>> field = mutableLiveEventOf()

  fun onResetBannersClick() {
    legalDocumentRepository.resetSeen(LegalDocumentType.PRIVACY_POLICY)
    prefs.isUserLogged = false
    prefs.lastVersionCode = 0
    bannersReset.value = Event(Unit)
  }
}
