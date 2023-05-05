package com.elementary.tasks.core.services.action.missedcall

import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.data.repository.MissedCallRepository
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.services.JobScheduler
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.TelephonyUtil
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Deprecated("After S")
class MissedCallActionProcessor(
  private val dispatcherProvider: DispatcherProvider,
  private val missedCallHandlerFactory: MissedCallHandlerFactory,
  private val missedCallRepository: MissedCallRepository,
  private val prefs: Prefs,
  private val doNotDisturbManager: DoNotDisturbManager,
  private val jobScheduler: JobScheduler,
  private val analyticsEventSender: AnalyticsEventSender,
  private val contextProvider: ContextProvider
) {

  private val scope = CoroutineScope(dispatcherProvider.default())

  fun sendSms(phoneNumber: String) {
    Timber.d("sendSms: $phoneNumber")
    scope.launch {
      jobScheduler.cancelMissedCall(phoneNumber)

      val missedCall = missedCallRepository.getByNumber(phoneNumber) ?: return@launch
      missedCallHandlerFactory.createCancel().handle(missedCall)

      withContext(dispatcherProvider.main()) {
        TelephonyUtil.sendSms(phoneNumber, contextProvider.context)
      }
    }
  }

  fun makeCall(phoneNumber: String) {
    Timber.d("makeCall: $phoneNumber")
    scope.launch {
      jobScheduler.cancelMissedCall(phoneNumber)

      val missedCall = missedCallRepository.getByNumber(phoneNumber) ?: return@launch
      missedCallHandlerFactory.createCancel().handle(missedCall)

      withContext(dispatcherProvider.main()) {
        TelephonyUtil.makeCall(phoneNumber, contextProvider.context)
      }
    }
  }

  fun cancel(phoneNumber: String) {
    Timber.d("cancel: $phoneNumber")
    scope.launch {
      jobScheduler.cancelMissedCall(phoneNumber)

      val missedCall = missedCallRepository.getByNumber(phoneNumber) ?: return@launch
      missedCallHandlerFactory.createCancel().handle(missedCall)
    }
  }

  fun process(phoneNumber: String) {
    Timber.d("process: $phoneNumber")
    scope.launch {
      jobScheduler.cancelMissedCall(phoneNumber)

      val missedCall = missedCallRepository.getByNumber(phoneNumber) ?: return@launch

      if (!doNotDisturbManager.applyDoNotDisturb(prefs.missedCallPriority)) {
        jobScheduler.scheduleMissedCall(phoneNumber)
        analyticsEventSender.send(FeatureUsedEvent(Feature.MISSED_CALL))
        missedCallHandlerFactory.createAction(!SuperUtil.isPhoneCallActive(contextProvider.context))
          .handle(missedCall)
      } else if (prefs.doNotDisturbAction == 0) {
        jobScheduler.scheduleMissedCall(phoneNumber)
      }
    }
  }
}
