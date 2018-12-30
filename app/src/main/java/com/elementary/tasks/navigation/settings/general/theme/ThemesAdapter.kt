package com.elementary.tasks.navigation.settings.general.theme

import android.view.LayoutInflater
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
        deselectPrev()
        theme.isSelected = true
        notifyItemChanged(position)
        selectedListener?.invoke(theme)
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