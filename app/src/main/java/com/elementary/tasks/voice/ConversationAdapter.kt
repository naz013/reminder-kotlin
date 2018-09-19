package com.elementary.tasks.voice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.BirthdayHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.groups.list.GroupHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingHolder
import kotlinx.android.synthetic.main.list_item_ask.view.*
import kotlinx.android.synthetic.main.list_item_show_reply.view.*
import kotlinx.android.synthetic.main.list_item_simple_reply.view.*
import kotlinx.android.synthetic.main.list_item_simple_response.view.*
import java.util.*
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
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
class ConversationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mData = ArrayList<Reply>()
    var mCallback: (() -> Unit)? = null

    @Inject
    lateinit var language: Language
    @Inject
    lateinit var themeUtil: ThemeUtil

    init {
        ReminderApp.appComponent.inject(this)
    }

    fun addReply(reply: Reply?) {
        if (reply != null) {
            mData.add(0, reply)
            notifyItemInserted(0)
            notifyItemRangeChanged(0, mData.size)
            mCallback?.invoke()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Reply.REPLY -> VoiceHolder(parent)
            Reply.RESPONSE -> VoiceResponseHolder(parent)
            Reply.REMINDER -> ReminderHolder(parent, null, false)
            Reply.NOTE -> NoteHolder(parent, null)
            Reply.GROUP -> GroupHolder(parent, null)
            Reply.SHOW_MORE -> ShowMoreHolder(parent)
            Reply.BIRTHDAY -> BirthdayHolder(parent, null)
            Reply.SHOPPING -> ShoppingHolder(parent, null, false)
            else -> AskHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VoiceHolder -> holder.bind(mData[position].`object` as String)
            is VoiceResponseHolder -> holder.bind(mData[position].`object` as String)
            is ReminderHolder -> holder.setData(mData[position].`object` as Reminder)
            is NoteHolder -> holder.setData(mData[position].`object` as Note)
            is GroupHolder -> holder.setData(mData[position].`object` as ReminderGroup)
            is BirthdayHolder -> {
                holder.setData(mData[position].`object` as Birthday)
                holder.setColor(themeUtil.getColor(themeUtil.colorBirthdayCalendar()))
            }
            is ShoppingHolder -> holder.setData(mData[position].`object` as Reminder)
            is AskHolder -> holder.setAskAction(mData[position].`object` as AskAction)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mData[position].viewType
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun removeAsk() {
        for (i in 1 until mData.size) {
            val reply = mData[i]
            if (reply.viewType == Reply.ASK) {
                mData.removeAt(i)
                notifyItemRemoved(i)
                notifyItemRangeChanged(0, mData.size)
                break
            }
        }
    }

    private inner class AskHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(inflate(parent, R.layout.list_item_ask)) {

        private var askAction: AskAction? = null

        init {
            itemView.replyYes.setOnClickListener {
                removeFirst()
                if (askAction != null) {
                    askAction!!.onYes()
                }
            }
            itemView.replyNo.setOnClickListener {
                removeFirst()
                if (askAction != null) {
                    askAction!!.onNo()
                }
            }
            itemView.replyNo.setBackgroundResource(themeUtil.rectangle)
            itemView.replyYes.setBackgroundResource(themeUtil.rectangle)
            itemView.replyNo.text = language.getLocalized(itemView.context, R.string.no)
            itemView.replyYes.text = language.getLocalized(itemView.context, R.string.yes)
        }

        internal fun setAskAction(askAction: AskAction) {
            this.askAction = askAction
        }
    }

    private fun removeFirst() {
        mData.removeAt(0)
        notifyItemRemoved(0)
        notifyItemRangeChanged(0, mData.size)
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
            itemView.replyText.setOnClickListener { addMoreItemsToList(adapterPosition) }
        }
    }

    private fun inflate(parent: ViewGroup, res: Int): View {
        return LayoutInflater.from(parent.context).inflate(res, parent, false)
    }

    private fun addMoreItemsToList(position: Int) {
        val reply = mData[position]
        val container = reply.`object` as Container<*>
        when {
            container.type is ReminderGroup -> {
                mData.removeAt(position)
                notifyItemRemoved(position)
                for (item in (container as Container<ReminderGroup>).list) {
                    mData.add(0, Reply(Reply.GROUP, item))
                    notifyItemInserted(0)
                }
                notifyItemRangeChanged(0, mData.size)
                mCallback?.invoke()
            }
            container.type is Note -> {
                mData.removeAt(position)
                notifyItemRemoved(position)
                for (item in (container as Container<Note>).list) {
                    mData.add(0, Reply(Reply.NOTE, item))
                    notifyItemInserted(0)
                }
                notifyItemRangeChanged(0, mData.size)
                mCallback?.invoke()
            }
            container.type is Reminder -> {
                mData.removeAt(position)
                notifyItemRemoved(position)
                addRemindersToList(container)
                notifyItemRangeChanged(0, mData.size)
                mCallback?.invoke()
            }
            container.type is Birthday -> {
                mData.removeAt(position)
                notifyItemRemoved(position)
                val reversed = ArrayList((container as Container<Birthday>).list)
                reversed.reverse()
                for (item in reversed) {
                    mData.add(0, Reply(Reply.BIRTHDAY, item))
                    notifyItemInserted(0)
                }
                notifyItemRangeChanged(0, mData.size)
                mCallback?.invoke()
            }
        }
    }

    private fun addRemindersToList(container: Container<*>) {
        val reversed = ArrayList((container as Container<Reminder>).list)
        reversed.reverse()
        for (item in reversed) {
            if (item.viewType == Reminder.REMINDER) {
                mData.add(0, Reply(Reply.REMINDER, item))
            } else {
                mData.add(0, Reply(Reply.SHOPPING, item))
            }
            notifyItemInserted(0)
        }
    }
}
