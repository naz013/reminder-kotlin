package com.elementary.tasks.voice

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.databinding.ListItemAskBinding
import com.elementary.tasks.databinding.ListItemShowReplyBinding
import com.elementary.tasks.databinding.ListItemSimpleReplyBinding
import com.elementary.tasks.databinding.ListItemSimpleResponseBinding
import com.elementary.tasks.groups.list.GroupHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingHolder
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber

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
class ConversationAdapter : ListAdapter<Reply, RecyclerView.ViewHolder>(ReplyDiffCallback()), KoinComponent {

    var showMore: ((Int) -> Unit)? = null
    private val language: Language by inject()

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
            Reply.REMINDER -> ReminderHolder(parent, hasHeader = false, editable = false, showMore = false)
            Reply.NOTE -> NoteHolder(parent, null)
            Reply.GROUP -> GroupHolder(parent, null)
            Reply.SHOW_MORE -> ShowMoreHolder(parent)
            Reply.BIRTHDAY -> BirthdayHolder(parent, false)
            Reply.SHOPPING -> ShoppingHolder(parent, editable = false, showMore = false)
            else -> AskHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val content = getItem(position).content
        when  {
            holder is VoiceHolder -> holder.bind(content as String)
            holder is VoiceResponseHolder -> holder.bind(content as String)
            holder is ReminderHolder -> holder.setData(content as Reminder)
            holder is NoteHolder && content is NoteWithImages -> holder.setData(content)
            holder is GroupHolder -> holder.setData(content as ReminderGroup)
            holder is BirthdayHolder -> {
                holder.setData(content as Birthday)
            }
            holder is ShoppingHolder -> holder.setData(content as Reminder)
            holder is AskHolder -> holder.setAskAction(content as AskAction)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    private inner class AskHolder(parent: ViewGroup) : HolderBinding<ListItemAskBinding>(parent, R.layout.list_item_ask) {

        private var askAction: AskAction? = null

        init {
            binding.replyYes.setOnClickListener {
                askAction?.onYes()
            }
            binding.replyNo.setOnClickListener {
                askAction?.onNo()
            }
            binding.replyNo.text = language.getLocalized(itemView.context, R.string.no)
            binding.replyYes.text = language.getLocalized(itemView.context, R.string.yes)
        }

        internal fun setAskAction(askAction: AskAction) {
            this.askAction = askAction
        }
    }

    private inner class VoiceHolder(parent: ViewGroup) :
            HolderBinding<ListItemSimpleReplyBinding>(parent, R.layout.list_item_simple_reply) {
        fun bind(text: String) {
            binding.replyTextSimple.text = text
        }
    }

    private inner class VoiceResponseHolder(parent: ViewGroup) :
            HolderBinding<ListItemSimpleResponseBinding>(parent, R.layout.list_item_simple_response) {
        fun bind(text: String) {
            binding.replyTextResponse.text = text
        }
    }

    private inner class ShowMoreHolder(parent: ViewGroup) :
            HolderBinding<ListItemShowReplyBinding>(parent, R.layout.list_item_show_reply) {
        init {
            binding.replyText.setOnClickListener {
                showMore?.invoke(adapterPosition)
                showMore = null
            }
        }
    }
}
