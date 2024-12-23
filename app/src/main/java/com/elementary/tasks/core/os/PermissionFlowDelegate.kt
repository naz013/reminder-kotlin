package com.elementary.tasks.core.os

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.github.naz013.ui.common.Dialogues
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface PermissionFlowDelegate {
  val permissionFlow: PermissionFlow

  fun with(block: PermissionFlow.() -> Unit)
}

class PermissionFlowDelegateImpl(
  private val activity: FragmentActivity
) : PermissionFlowDelegate, KoinComponent {

  private val dialogues by inject<Dialogues>()
  private var _permissionFlow: PermissionFlow? = null

  override val permissionFlow: PermissionFlow
    get() {
      return _permissionFlow ?: createPermissionFlow().also {
        _permissionFlow = it
      }
    }

  private val lifecycleObserver = object : DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {
      super.onCreate(owner)
      _permissionFlow = createPermissionFlow()
    }
  }

  init {
    activity.lifecycle.addObserver(lifecycleObserver)
  }

  override fun with(block: PermissionFlow.() -> Unit) {
    permissionFlow.block()
  }

  private fun createPermissionFlow(): PermissionFlow {
    return PermissionFlow(activity, dialogues)
  }
}
