package com.elementary.tasks.navigation.fragments

import android.app.Activity
import android.content.Context
import androidx.databinding.ViewDataBinding
import com.elementary.tasks.core.BindingFragment
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
    private var mLastScroll: Int = 0

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

    protected fun setScroll(scroll: Int) {
        if (isRemoving) return
        this.mLastScroll = scroll
        callback?.onScrollUpdate(scroll)
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
        callback?.onTitleChange(getTitle())
        setScroll(mLastScroll)
    }

    override fun onResume() {
        super.onResume()
        onBackStackResume()
    }

    abstract fun getTitle(): String
}
