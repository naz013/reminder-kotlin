package com.github.naz013.common.intent

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment

abstract class LauncherCreator<I, O> {
  abstract fun createLauncher(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
  ): ActivityResultLauncher<I>

  abstract fun getActivity(): Activity
}

class FragmentLauncherCreator<I, O>(
  private val fragment: Fragment
) : LauncherCreator<I, O>() {

  override fun createLauncher(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
  ): ActivityResultLauncher<I> {
    return fragment.registerForActivityResult(contract, callback)
  }

  override fun getActivity(): Activity {
    return fragment.requireActivity()
  }
}

class ActivityLauncherCreator<I, O>(
  private val activity: ComponentActivity
) : LauncherCreator<I, O>() {

  override fun createLauncher(
    contract: ActivityResultContract<I, O>,
    callback: ActivityResultCallback<O>
  ): ActivityResultLauncher<I> {
    return activity.registerForActivityResult(contract, callback)
  }

  override fun getActivity(): Activity {
    return activity
  }
}
