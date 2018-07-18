package com.elementary.tasks.voice

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.birthdays.BirthdayHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.groups.list.GroupHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.reminder.lists.ReminderHolder
import com.elementary.tasks.reminder.lists.ShoppingHolder
import java.util.*
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
class ConversationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mData = ArrayList<Reply>()
    private var mCallback: InsertCallback? = null
    @Inject
    var themeUtil: ThemeUtil? = null

    init {
        ReminderApp.appComponent!!.inject(this)
    }

    internal fun setInsertListener(callback: InsertCallback) {
        this.mCallback = callback
    }

    internal fun addReply(reply: Reply?) {
        if (reply != null) {
            mData.add(0, reply)
            notifyItemInserted(0)
            notifyItemRangeChanged(0, mData.size)
            if (mCallback != null) {
                mCallback!!.onItemAdded()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            Reply.REPLY -> VoiceHolder(ListItemSimpleReplyBinding.inflate(inflater, parent, false).root)
            Reply.RESPONSE -> VoiceResponseHolder(ListItemSimpleResponseBinding.inflate(inflater, parent, false).root)
            Reply.REMINDER -> ReminderHolder(parent, null, false)
            Reply.NOTE -> NoteHolder(ListItemNoteBinding.inflate(inflater, parent, false).root, null)
            Reply.GROUP -> GroupHolder(parent, null)
            Reply.SHOW_MORE -> ShowMoreHolder(ListItemShowReplyBinding.inflate(inflater, parent, false).root)
            Reply.BIRTHDAY -> BirthdayHolder(parent, null)
            Reply.SHOPPING -> ShoppingHolder(parent, null)
            else -> AskHolder(ListItemAskBinding.inflate(inflater, parent, false).root)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VoiceHolder -> holder.binding!!.replyText.text = mData[position].`object` as String
            is VoiceResponseHolder -> holder.binding!!.replyText.text = mData[position].`object` as String
            is ReminderHolder -> holder.setData(mData[position].`object` as Reminder)
            is NoteHolder -> holder.setData(mData[position].`object` as Note)
            is GroupHolder -> holder.setData(mData[position].`object` as Group)
            is BirthdayHolder -> {
                holder.setData(mData[position].`object` as Birthday)
                holder.setColor(themeUtil!!.getColor(themeUtil!!.colorBirthdayCalendar()))
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

    private inner class AskHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ListItemAskBinding?
        private var askAction: AskAction? = null

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.replyYes.setOnClickListener { v ->
                removeFirst()
                if (askAction != null) {
                    askAction!!.onYes()
                }
            }
            binding.replyNo.setOnClickListener { v ->
                removeFirst()
                if (askAction != null) {
                    askAction!!.onNo()
                }
            }
            binding.replyNo.setBackgroundResource(themeUtil!!.rectangle)
            binding.replyYes.setBackgroundResource(themeUtil!!.rectangle)
            binding.replyNo.text = Language.getLocalized(itemView.context, R.string.no)
            binding.replyYes.text = Language.getLocalized(itemView.context, R.string.yes)
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

    private inner class VoiceHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var binding: ListItemSimpleReplyBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
        }
    }

    private inner class VoiceResponseHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var binding: ListItemSimpleResponseBinding? = null

        init {
            binding = DataBindingUtil.bind(itemView)
        }
    }

    private inner class ShowMoreHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding: ListItemShowReplyBinding?

        init {
            binding = DataBindingUtil.bind(itemView)
            binding!!.replyText.setOnClickListener { view -> addMoreItemsToList(adapterPosition) }
        }
    }

    private fun addMoreItemsToList(position: Int) {
        val reply = mData[position]
        val container = reply.`object` as Container<*>
        if (container.type is Group) {
            mData.removeAt(position)
            notifyItemRemoved(position)

            for (item in (container as Container<Group>).list) {
                mData.add(0, Reply(Reply.GROUP, item))
                notifyItemInserted(0)
            }
            notifyItemRangeChanged(0, mData.size)
            if (mCallback != null) mCallback!!.onItemAdded()
        } else if (container.type is Note) {
            mData.removeAt(position)
            notifyItemRemoved(position)

            for (item in (container as Container<Note>).list) {
                mData.add(0, Reply(Reply.NOTE, item))
                notifyItemInserted(0)
            }
            notifyItemRangeChanged(0, mData.size)
            if (mCallback != null) mCallback!!.onItemAdded()
        } else if (container.type is Reminder) {
            mData.removeAt(position)
            notifyItemRemoved(position)
            addRemindersToList(container)
            notifyItemRangeChanged(0, mData.size)
            if (mCallback != null) mCallback!!.onItemAdded()
        } else if (container.type is Birthday) {
            mData.removeAt(position)
            notifyItemRemoved(position)

            val reversed = ArrayList((container as Container<Birthday>).list)
            Collections.reverse(reversed)
            for (item in reversed) {
                mData.add(0, Reply(Reply.BIRTHDAY, item))
                notifyItemInserted(0)
            }
            notifyItemRangeChanged(0, mData.size)
            if (mCallback != null) mCallback!!.onItemAdded()
        }
    }

    private fun addRemindersToList(container: Container<*>) {

        val reversed = ArrayList((container as Container<Reminder>).list)
        Collections.reverse(reversed)
        for (item in reversed) {
            if (item.viewType == Reminder.REMINDER) {
                mData.add(0, Reply(Reply.REMINDER, item))
            } else {
                mData.add(0, Reply(Reply.SHOPPING, item))
            }
            notifyItemInserted(0)
        }
    }

    internal interface InsertCallback {
        fun onItemAdded()
    }
}
