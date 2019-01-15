package com.elementary.tasks.voice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.groups.list.GroupHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingHolder
import kotlinx.android.synthetic.main.list_item_ask.view.*
import kotlinx.android.synthetic.main.list_item_show_reply.view.*
import kotlinx.android.synthetic.main.list_item_simple_reply.view.*
import kotlinx.android.synthetic.main.list_item_simple_response.view.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Copyright 2017 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ConversationAdapter : ListAdapter<Reply, RecyclerView.ViewHolder>(ReplyDiffCallback()) {

    var showMore: ((Int) -> Unit)? = null
    @Inject
    lateinit var language: Language

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun submitList(list: List<Reply>?) {
        Timber.d("submitList: $list")
        val oldSize = itemCount
        super.submitList(list)
        if (!list.isNullOrEmpty()) {
            if (oldSize == list.size) {
                notifyItemChanged(0)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Reply.REPLY -> VoiceHolder(parent)
            Reply.RESPONSE -> VoiceResponseHolder(parent)
            Reply.REMINDER -> ReminderHolder(parent, false, false, false)
            Reply.NOTE -> NoteHolder(parent, null)
            Reply.GROUP -> GroupHolder(parent, null)
            Reply.SHOW_MORE -> ShowMoreHolder(parent)
            Reply.BIRTHDAY -> BirthdayHolder(parent, false)
            Reply.SHOPPING -> ShoppingHolder(parent, false, false)
            else -> AskHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VoiceHolder -> holder.bind(getItem(position).content as String)
            is VoiceResponseHolder -> holder.bind(getItem(position).content as String)
            is ReminderHolder -> holder.setData(getItem(position).content as Reminder)
            is NoteHolder -> holder.setData(getItem(position).content as NoteWithImages)
            is GroupHolder -> holder.setData(getItem(position).content as ReminderGroup)
            is BirthdayHolder -> {
                holder.setData(getItem(position).content as Birthday)
            }
            is ShoppingHolder -> holder.setData(getItem(position).content as Reminder)
            is AskHolder -> holder.setAskAction(getItem(position).content as AskAction)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    private inner class AskHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(inflate(parent, R.layout.list_item_ask)) {

        private var askAction: AskAction? = null

        init {
            itemView.replyYes.setOnClickListener {
                askAction?.onYes()
            }
            itemView.replyNo.setOnClickListener {
                askAction?.onNo()
            }
            itemView.replyNo.text = language.getLocalized(itemView.context, R.string.no)
            itemView.replyYes.text = language.getLocalized(itemView.context, R.string.yes)
        }

        internal fun setAskAction(askAction: AskAction) {
            this.askAction = askAction
        }
    }

    private inner class VoiceHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(inflate(parent, R.layout.list_item_simple_reply)) {
        fun bind(text: String) {
            itemView.replyTextSimple.text = text
        }
    }

    private inner class VoiceResponseHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(inflate(parent, R.layout.list_item_simple_response)) {
        fun bind(text: String) {
            itemView.replyTextResponse.text = text
        }
    }

    private inner class ShowMoreHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(inflate(parent, R.layout.list_item_show_reply)) {
        init {
            itemView.replyText.setOnClickListener {
                showMore?.invoke(adapterPosition)
                showMore = null
            }
        }
    }

    private fun inflate(parent: ViewGroup, res: Int): View {
        return LayoutInflater.from(parent.context).inflate(res, parent, false)
    }
}
