package com.elementary.tasks.sms

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.adapter.sms.UiSmsListAdapter
import com.elementary.tasks.core.data.dao.SmsTemplatesDao
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.contacts.ContactsReader
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import kotlinx.coroutines.launch

class QuickSmsViewModel(
  dispatcherProvider: DispatcherProvider,
  smsTemplatesDao: SmsTemplatesDao,
  private val contactsReader: ContactsReader,
  private val uiSmsListAdapter: UiSmsListAdapter,
  private val analyticsEventSender: AnalyticsEventSender
) : BaseProgressViewModel(dispatcherProvider) {

  private val _contactInfo = mutableLiveDataOf<String>()
  val contactInfo = _contactInfo.toLiveData()

  val smsTemplates = smsTemplatesDao.loadAll().map { list ->
    list.map { uiSmsListAdapter.convert(it) }
  }
  var number: String = ""
    private set

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    analyticsEventSender.send(FeatureUsedEvent(Feature.QUICK_SMS))
  }

  fun loadContactInfo(number: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val name = contactsReader.getNameFromNumber(number) ?: ""
      if (name.isEmpty()) {
        _contactInfo.postValue(number)
      } else {
        _contactInfo.postValue(number)
      }
    }
  }
}
