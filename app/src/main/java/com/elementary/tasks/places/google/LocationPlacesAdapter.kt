package com.elementary.tasks.places.google

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.DrawableHelper
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemLocationBinding
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class LocationPlacesAdapter : RecyclerView.Adapter<LocationPlacesAdapter.ViewHolder>(), KoinComponent {

    private val mDataList = ArrayList<Reminder>()
    var actionsListener: ActionsListener<Reminder>? = null

    private val themeUtil: ThemeUtil by inject()

    fun setData(list: List<Reminder>) {
        this.mDataList.clear()
        this.mDataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(parent: ViewGroup) : HolderBinding<ListItemLocationBinding>(parent, R.layout.list_item_location) {
        fun bind(item: Reminder) {
            val place = item.places[0]
            var name = place.name
            if (item.places.size > 1) {
                name = item.summary + " (" + item.places.size + ")"
            }
            binding.textView.text = name
            loadMarker(binding.markerImage, place.marker)
        }

        init {
            binding.itemCard.setOnClickListener { view ->
                actionsListener?.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
            }
            binding.itemCard.setOnLongClickListener { view ->
                actionsListener?.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.MORE)
                true
            }
        }
    }

    fun loadMarker(view: ImageView, color: Int) {
        DrawableHelper.withContext(view.context)
                .withDrawable(R.drawable.ic_twotone_place_24px)
                .withColor(themeUtil.getNoteLightColor(color))
                .tint()
                .applyTo(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mDataList[position])
    }

    fun getItem(position: Int): Reminder {
        return mDataList[position]
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}
