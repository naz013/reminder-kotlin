package com.elementary.tasks.core.interfaces

import android.view.View

interface SimpleListener {
    fun onItemClicked(position: Int, view: View)

    fun onItemLongClicked(position: Int, view: View)
}
