package com.elementary.tasks.core.binding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.utils.lazyUnSynchronized

abstract class HolderBinding<B : Binding>(parent: ViewGroup, @LayoutRes res: Int, builder: (View) -> B) :
        RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(res, parent, false)) {

    protected var binding: B = builder.invoke(itemView)

    fun <ViewT : View> bindView(@IdRes idRes: Int): Lazy<ViewT> {
        return lazyUnSynchronized {
            itemView.findViewById<ViewT>(idRes)
        }
    }
}