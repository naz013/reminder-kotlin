package com.github.naz013.feature.settings.protrial

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.common.system.ProTrialController
import com.github.naz013.common.system.ProTrialResult
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ProTrialViewModel(
  private val proTrialController: ProTrialController,
) : ViewModel() {

  private val _state = MutableStateFlow(ProTrialState(requiredAds = proTrialController.requiredAds))
  val state: StateFlow<ProTrialState> = _state.asStateFlow()

  val event: LiveData<Event<ProTrialEvent>> field = mutableLiveEventOf()

  init {
    viewModelScope.launch {
      proTrialController.observeRemainingMillis().collect { remaining ->
        _state.update {
          it.copy(isTrialActive = remaining > 0L, remainingTime = formatRemaining(remaining))
        }
      }
    }
    viewModelScope.launch {
      proTrialController.adsWatched.collect { watched -> _state.update { it.copy(adsWatched = watched) } }
    }
  }

  fun onActivateClick() {
    if (_state.value.isRunning) return
    event.emit(ProTrialEvent.RequestActivity)
  }

  fun onActivityAvailable(activity: Activity) {
    if (_state.value.isRunning) return
    _state.update { it.copy(isRunning = true, message = null) }
    viewModelScope.launch {
      try {
        val result = proTrialController.watchAdsAndActivate(activity)
        _state.update { it.copy(message = result.toMessage()) }
      } finally {
        _state.update { it.copy(isRunning = false) }
      }
    }
  }

  private fun ProTrialResult.toMessage(): ProTrialMessage? {
    return when (this) {
      ProTrialResult.SUCCESS -> null
      ProTrialResult.ADS_UNAVAILABLE -> ProTrialMessage.ADS_UNAVAILABLE
      ProTrialResult.CANCELLED -> ProTrialMessage.CANCELLED
    }
  }

  private fun formatRemaining(remainingMillis: Long): String {
    if (remainingMillis <= 0L) return ""
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis + MILLIS_IN_MINUTE - 1)
    return String.format(Locale.ROOT, "%02d:%02d", totalMinutes / MINUTES_IN_HOUR, totalMinutes % MINUTES_IN_HOUR)
  }

  private companion object {
    const val MILLIS_IN_MINUTE = 60_000L
    const val MINUTES_IN_HOUR = 60L
  }
}

internal sealed interface ProTrialEvent {
  data object RequestActivity : ProTrialEvent
}
