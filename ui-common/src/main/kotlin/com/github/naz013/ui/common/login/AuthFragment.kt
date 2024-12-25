package com.github.naz013.ui.common.login

import android.content.Context
import androidx.fragment.app.Fragment

internal open class AuthFragment : Fragment() {

  protected var authCallback: AuthCallback? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    authCallback = context as? AuthCallback?
  }

  interface AuthCallback {
    fun onSuccess()
    fun changeScreen(auth: Int)
  }

  companion object {
    const val AUTH_FINGER = 1
  }
}
