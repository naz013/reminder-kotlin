package com.elementary.tasks.navigation.settings.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.list_item_theme.view.*

class ThemesAdapter : RecyclerView.Adapter<ThemesAdapter.ThemeHolder>() {

    private val themes: MutableList<Theme> = mutableListOf()
    var selectedListener: ((theme: Theme) -> Unit)? = null

    fun setThemes(themes: List<Theme>) {
        this.themes.clear()
        this.themes.addAll(themes)
        notifyDataSetChanged()
    }

    fun getTheme(position: Int) : Theme = themes[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeHolder {
        return ThemeHolder(parent)
    }

    override fun getItemCount(): Int = themes.size

    override fun onBindViewHolder(holder: ThemeHolder, position: Int) {
        holder.bind(themes[position])
    }

    inner class ThemeHolder(parent: ViewGroup)
        : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_theme, parent, false)) {
        fun bind(theme: Theme) {
            itemView.titleView.text = theme.name
            itemView.bgView.setBackgroundColor(theme.bgColor)
            itemView.barView.setBackgroundColor(theme.barColor)
            itemView.statusView.setBackgroundColor(theme.statusColor)
            if (theme.isLocked) {
                itemView.lockView.visibility = View.VISIBLE
            } else {
                itemView.lockView.visibility = View.GONE
            }
            if (theme.isSelected) {
                itemView.selectionView.visibility = View.VISIBLE
            } else {
                itemView.selectionView.visibility = View.GONE
            }
            if (theme.isDark) {
                itemView.borderView.setBackgroundResource(R.color.pureWhite)
            } else {
                itemView.borderView.setBackgroundResource(R.color.pureBlack)
            }
        }

        init {
            itemView.setOnClickListener {
                handleClick(adapterPosition)
            }
        }
    }

    private fun handleClick(position: Int) {
        val theme = themes[position]
        if (!theme.isLocked && !theme.isSelected) {
            deselectPrev()
            theme.isSelected = true
            notifyItemChanged(position)
            selectedListener?.invoke(theme)
        }
    }

    private fun deselectPrev() {
        for (theme in themes) {
            if (theme.isSelected) {
                theme.isSelected = false
                notifyItemChanged(theme.id)
            }
        }
    }
}