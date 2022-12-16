package com.elementary.tasks.core.os.datapicker

import androidx.activity.result.contract.ActivityResultContract

abstract class IntentPicker<I, O>(
  contract: ActivityResultContract<I, O>,
  private val launcherCreator: LauncherCreator<I, O>
) {

  private val launcher = launcherCreator.createLauncher(contract) { dispatchResult(it) }

  protected abstract fun dispatchResult(result: O)

  protected fun launch(data: I) {
    launcher.launch(data)
  }

  protected fun getActivity() = launcherCreator.getActivity()
}