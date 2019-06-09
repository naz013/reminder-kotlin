package com.elementary.tasks.navigation.fragments

import android.app.Activity
import android.content.Context
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.arch.BindingFragment
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.navigation.FragmentCallback
import org.koin.android.ext.android.inject

abstract class BaseFragment<B : ViewDataBinding> : BindingFragment<B>() {

    var callback: FragmentCallback? = null
        private set
    protected val prefs: Prefs by inject()
    protected val dialogues: Dialogues by inject()
    protected val themeUtil: ThemeUtil by inject()
    protected val buttonObservable: GlobalButtonObservable by inject()
    protected val notifier: Notifier by inject()
    var isDark = false
        private set
    private var mLastAlpha: Float = 0f

    override fun onAttach(context: Context) {
        super.onAttach(context)
        isDark = ThemeUtil.isDarkMode(context)
        if (callback == null) {
            try {
                callback = context as FragmentCallback?
            } catch (e: ClassCastException) {
            }
        }
    }

    protected fun toAlpha(scroll: Float, max: Float = 255f): Float = scroll / max

    protected fun setToolbarAlpha(alpha: Float) {
        if (isRemoving) return
        this.mLastAlpha = alpha
        callback?.onAlphaUpdate(alpha)
    }

    protected fun moveBack() {
        activity?.onBackPressed()
    }

    protected fun withActivity(action: (Activity) -> Unit) {
        activity?.let {
            action.invoke(it)
        }
    }

    protected fun withContext(action: (Context) -> Unit) {
        context?.let {
            action.invoke(it)
        }
    }

    open fun canGoBack(): Boolean = true

    open fun onBackStackResume() {
        callback?.setCurrentFragment(this)
        callback?.onTitleChange(getTitle())
        setToolbarAlpha(mLastAlpha)
    }

    override fun onResume() {
        super.onResume()
        onBackStackResume()
    }

    abstract fun getTitle(): String

    companion object {
        const val NESTED_SCROLL_MAX = 255f
    }
}
