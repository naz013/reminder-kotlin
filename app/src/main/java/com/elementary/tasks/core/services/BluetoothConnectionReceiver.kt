package com.elementary.tasks.core.services

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import com.github.naz013.feature.common.android.readParcelable
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.inject

/**
 * Fires [WorkflowTriggerRunner.onBluetoothConnected]/[WorkflowTriggerRunner.onBluetoothDisconnected]
 * for `WorkflowTrigger.BluetoothConnected`/`BluetoothDisconnected` rules. Matching is by MAC address
 * only - a connecting device's display name isn't read here (avoids needing the `BLUETOOTH_CONNECT`
 * runtime permission just to receive this broadcast); the name shown in a rule's UI is captured once,
 * at rule-creation time, from the paired-device picker.
 *
 * Unlike [BootReceiver], the work here (Room queries via [WorkflowTriggerRunner]) is suspend/IO work
 * that must outlive [onReceive]'s main-thread call - [BaseBroadcast] provides no coroutine scaffolding
 * of its own, so this uses `goAsync()` directly and finishes it once the coroutine completes.
 */
class BluetoothConnectionReceiver : BaseBroadcast() {
  private val workflowTriggerRunner by inject<WorkflowTriggerRunner>()
  private val dispatcherProvider by inject<DispatcherProvider>()

  override fun onReceive(context: Context, intent: Intent) {
    val device = intent.readParcelable(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java) ?: return
    val address = device.address ?: return
    val connected = intent.action == BluetoothDevice.ACTION_ACL_CONNECTED
    Logger.i(TAG, "Bluetooth device ${if (connected) "connected" else "disconnected"}: $address")

    val pendingResult = goAsync()
    CoroutineScope(dispatcherProvider.io()).launch {
      try {
        if (connected) {
          workflowTriggerRunner.onBluetoothConnected(address)
        } else {
          workflowTriggerRunner.onBluetoothDisconnected(address)
        }
      } finally {
        pendingResult.finish()
      }
    }
  }

  companion object {
    private const val TAG = "BluetoothConnectionReceiver"
  }
}
