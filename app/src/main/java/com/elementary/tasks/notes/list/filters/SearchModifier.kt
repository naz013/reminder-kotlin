package com.elementary.tasks.notes.list.filters

import com.elementary.tasks.core.filter.Modifier
import com.elementary.tasks.core.data.models.NoteWithImages

class SearchModifier(modifier: Modifier<NoteWithImages>? = null,
                     callback: ((List<NoteWithImages>) -> Unit)? = null) : Modifier<NoteWithImages>(modifier, callback) {

    private var searchValue: String = ""

    override fun apply(data: List<NoteWithImages>): List<NoteWithImages> {
        val list = mutableListOf<NoteWithImages>()
        for (note in super.apply(data)) {
            if (filter(note)) list.add(note)
        }
        return list
    }

    private fun filter(v: NoteWithImages): Boolean {
        return searchValue.isEmpty() || v.getSummary().toLowerCase().contains(searchValue.toLowerCase())
    }

    fun setSearchValue(value: String?) {
        searchValue = value ?: ""
        onChanged()
    }
}