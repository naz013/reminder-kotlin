package com.elementary.tasks.google_tasks.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.AdsProvider
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.interfaces.ActionsListener

class TasksRecyclerAdapter(private val refreshListener: () -> Unit) : ListAdapter<GoogleTask, RecyclerView.ViewHolder>(GoogleTaskDiffCallback()) {

    var actionsListener: ActionsListener<GoogleTask>? = null
    var googleTaskListMap: Map<String, GoogleTaskList> = mapOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private val adsProvider = AdsProvider()

    override fun getItem(position: Int): GoogleTask? {
        return try {
            super.getItem(position)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            AdsProvider.ADS_VIEW_TYPE -> GoogleTaskAdsHolder(parent, adsProvider, refreshListener)
            else -> GoogleTaskHolder(parent) { view, i, listActions ->
                actionsListener?.onAction(view, i, getItem(i), listActions)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        if (holder is GoogleTaskHolder) {
            holder.bind(item, googleTaskListMap)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item?.uuId == AdsProvider.GTASKS_BANNER_ID) {
            AdsProvider.ADS_VIEW_TYPE
        } else {
            0
        }
    }

    fun onDestroy() {
        adsProvider.destroy()
    }
}
