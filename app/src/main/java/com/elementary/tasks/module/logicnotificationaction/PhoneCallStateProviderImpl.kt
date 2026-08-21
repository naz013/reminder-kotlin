package com.elementary.tasks.module.logicnotificationaction

import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.common.ContextProvider
import com.github.naz013.logic.notificationaction.PhoneCallStateProvider

class PhoneCallStateProviderImpl(
  private val contextProvider: ContextProvider,
) : PhoneCallStateProvider {
  override fun isPhoneCallActive(): Boolean = SuperUtil.isPhoneCallActive(contextProvider.context)
}
