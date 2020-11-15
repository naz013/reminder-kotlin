package com.elementary.tasks.voice

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemAskBinding
import com.elementary.tasks.databinding.ListItemShowReplyBinding
import com.elementary.tasks.databinding.ListItemSimpleReplyBinding
import com.elementary.tasks.databinding.ListItemSimpleResponseBinding
import com.elementary.tasks.groups.list.GroupHolder
import com.elementary.tasks.notes.list.NoteHolder
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingHolder
import timber.log.Timber

class ConversationAdapter(
  private val language: Language,
  private val prefs: Prefs,
  private val themeUtil: ThemeUtil,
  private val imagesSingleton: ImagesSingleton
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val data = mutableListOf<Reply>()
  var showMore: ((Int) -> Unit)? = null
  private val handler = Handler(Looper.getMainLooper())

  fun submitList(list: List<Reply>?) {
    Timber.d("submitList: $list")
    if (list == null) {
      this.data.clear()
      notifyDataSetChanged()
      return
    }
    val oldSize = this.data.size
    val diffResult = DiffUtil.calculateDiff(ReplyDiffCallback(this.data, list))
    this.data.clear()
    this.data.addAll(list)
    diffResult.dispatchUpdatesTo(this)
    handler.postDelayed({
      if (!list.isNullOrEmpty()) {
        try {
          if (oldSize == list.size) {
            notifyItemChanged(0)
          } else {
            notifyDataSetChanged()
          }
        } catch (e: Exception) {
        }
      }
    }, 250)
  }

  fun getItem(position: Int): Reply? = if (position != -1 && position < data.size) data[position] else null

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      Reply.REPLY -> VoiceHolder(parent)
      Reply.RESPONSE -> VoiceResponseHolder(parent)
      Reply.REMINDER -> ReminderHolder(parent, prefs, hasHeader = false, editable = false, showMore = false)
      Reply.NOTE -> NoteHolder(parent, prefs, themeUtil, imagesSingleton, null)
      Reply.GROUP -> GroupHolder(parent, null)
      Reply.SHOW_MORE -> ShowMoreHolder(parent)
      Reply.BIRTHDAY -> BirthdayHolder(parent, prefs, false)
      Reply.SHOPPING -> ShoppingHolder(parent, prefs, editable = false, showMore = false)
      else -> AskHolder(parent)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val content = getItem(position)?.content
    when {
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
    return getItem(position)?.viewType ?: 0
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

    fun setAskAction(askAction: AskAction) {
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
