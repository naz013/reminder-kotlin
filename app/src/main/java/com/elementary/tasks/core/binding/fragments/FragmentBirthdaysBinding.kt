package com.elementary.tasks.core.binding.fragments

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.Binding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FragmentBirthdaysBinding(view: View) : Binding(view) {
    val recyclerView: RecyclerView by bindView(R.id.recyclerView)
    val fab: FloatingActionButton by bindView(R.id.fab)
    val emptyItem: View by bindView(R.id.emptyItem)
}