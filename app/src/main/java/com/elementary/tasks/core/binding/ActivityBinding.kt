package com.elementary.tasks.core.binding

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.lazyUnSynchronized
import com.google.android.material.appbar.AppBarLayout

abstract class ActivityBinding(val activity: Activity) {

    val appBar: AppBarLayout by bindView(R.id.appBar)
    val toolbar: Toolbar by bindView(R.id.toolbar)

    fun <ViewT : View> bindView(@IdRes idRes: Int): Lazy<ViewT> {
        return lazyUnSynchronized {
            activity.findViewById<ViewT>(idRes)
        }
    }
}