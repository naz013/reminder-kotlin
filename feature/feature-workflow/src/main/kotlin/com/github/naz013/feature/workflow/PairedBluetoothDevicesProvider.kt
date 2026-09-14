package com.github.naz013.feature.workflow

import android.bluetooth.BluetoothManager
import android.content.Context
import com.github.naz013.common.Permissions
import com.github.naz013.common.system.Module
import com.github.naz013.feature.workflow.builder.UiWorkflowBluetoothDeviceOption

/** Lists paired Bluetooth devices for the `WorkflowTrigger.BluetoothConnected`/
 * `BluetoothDisconnected` picker - bonded devices only (no live scan, so no `BLUETOOTH_SCAN`
 * needed). Returns an empty list rather than throwing when `BLUETOOTH_CONNECT` isn't granted (API
 * 31+ only - below that, the legacy, install-time `BLUETOOTH` permission already covers reading a
 * bonded device's name) - the picker's empty state then offers to request it. */
internal class PairedBluetoothDevicesProvider(
  private val context: Context
) {
  fun get(): List<UiWorkflowBluetoothDeviceOption> {
    if (Module.is12 && !Permissions.checkPermission(context, Permissions.BLUETOOTH_CONNECT)) return emptyList()
    val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
      ?: return emptyList()
    return runCatching {
      adapter.bondedDevices.map { device ->
        UiWorkflowBluetoothDeviceOption(address = device.address, name = device.name ?: device.address)
      }
    }.getOrDefault(emptyList())
  }
}
