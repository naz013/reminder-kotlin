package com.github.naz013.localbackup.transfer.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.localbackup.R
import com.github.naz013.localbackup.transfer.PreparedTransfer
import com.github.naz013.localbackup.transfer.TransferContract
import com.github.naz013.localbackup.transfer.TransferSender
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Drives [SendDataScreen]: reads every repository and (for large note libraries with many image
 * attachments, on a slow device) packages the transfer zip in the background while the screen
 * shows progress, then hands the ready [Intent] to the screen via [event] so it can launch the
 * sibling app through `ActivityResultContracts` - a ViewModel can't launch an activity-for-result
 * itself, only a composable holding the launcher can.
 */
internal class SendDataViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val context: Context,
  private val transferSender: TransferSender,
) : ViewModel() {

  private val _state = MutableStateFlow(SendDataState())
  val state = _state.asStateFlow()

  private val _event = MutableLiveData<Event<SendDataEvent>>()
  val event: LiveData<Event<SendDataEvent>> = _event

  private var preparedTransfer: PreparedTransfer? = null

  init {
    viewModelScope.launch(dispatcherProvider.io()) {
      val prepared = transferSender.prepareTransfer()
      withContext(dispatcherProvider.main()) {
        if (prepared != null) {
          preparedTransfer = prepared
          _event.value = Event(SendDataEvent.LaunchTransfer(prepared.intent))
        } else {
          Logger.e(TAG, "Failed to prepare the transfer package")
          _state.update { it.copy(status = TransferStatus.Error(R.string.transfer_send_failed)) }
        }
      }
    }
  }

  fun onTransferResult(resultCode: Int, data: Intent?) {
    preparedTransfer?.cleanUp(context)
    preparedTransfer = null

    val count = data?.getIntExtra(TransferContract.EXTRA_RESULT_COUNT, -1) ?: -1
    _state.update {
      it.copy(
        status = if (resultCode == Activity.RESULT_OK && count >= 0) {
          TransferStatus.Success(count)
        } else {
          TransferStatus.Error(R.string.transfer_failed)
        }
      )
    }
  }

  override fun onCleared() {
    preparedTransfer?.cleanUp(context)
  }

  companion object {
    private const val TAG = "SendDataViewModel"
  }
}
