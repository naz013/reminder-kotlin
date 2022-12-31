package com.elementary.tasks.voice

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.data.ui.UiBirthdayList
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemAskBinding
import com.elementary.tasks.databinding.ListItemShowReplyBinding
import com.elementary.tasks.databinding.ListItemSimpleReplyBinding
import com.elementary.tasks.databinding.ListItemSimpleResponseBinding
import com.elementary.tasks.groups.list.GroupHolder
import com.elementary.tasks.notes.list.NoteViewHolder
import com.elementary.tasks.notes.preview.ImagesSingleton
import com.elementary.tasks.reminder.lists.adapter.ReminderViewHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingViewHolder
import timber.log.Timber

class ConversationAdapter(
  private val currentStateHolder: CurrentStateHolder,
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
        runCatching {
          if (oldSize == list.size) {
            notifyItemChanged(0)
          } else {
            notifyDataSetChanged()
          }
        }
      }
    }, 250)
  }

  fun getItem(position: Int): Reply? =
    if (position != -1 && position < data.size) data[position] else null

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      Reply.REPLY -> VoiceHolder(parent)
      Reply.RESPONSE -> VoiceResponseHolder(parent)
      Reply.REMINDER -> ReminderViewHolder(parent, editable = false, showMore = false)
      Reply.NOTE -> NoteViewHolder(parent, currentStateHolder, imagesSingleton, null)
      Reply.GROUP -> GroupHolder(parent, null)
      Reply.SHOW_MORE -> ShowMoreHolder(parent)
      Reply.BIRTHDAY -> BirthdayHolder(parent, showMore = false)
      Reply.SHOPPING -> ShoppingViewHolder(
        parent,
        editable = false,
        showMore = false,
        isDark = currentStateHolder.theme.isDark
      )

      else -> AskHolder(parent)
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val content = getItem(position)?.content
    when {
      holder is VoiceHolder -> holder.bind(content as String)
      holder is VoiceResponseHolder -> holder.bind(content as String)
      holder is ReminderViewHolder -> holder.setData(content as UiReminderListActive)
      holder is NoteViewHolder && content is NoteWithImages -> holder.setData(content)
      holder is GroupHolder -> holder.setData(content as ReminderGroup)
      holder is BirthdayHolder -> holder.setData(content as UiBirthdayList)
      holder is ShoppingViewHolder -> holder.setData(content as UiReminderListActiveShop)
      holder is AskHolder -> holder.setAskAction(content as AskAction)
    }
  }

  override fun getItemViewType(position: Int): Int {
    return getItem(position)?.viewType ?: 0
  }

  private inner class AskHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemAskBinding>(
    ListItemAskBinding.inflate(parent.inflater(), parent, false)
  ) {

    private var askAction: AskAction? = null

    init {
      binding.replyYes.setOnClickListener {
        askAction?.onYes()
      }
      binding.replyNo.setOnClickListener {
        askAction?.onNo()
      }
      binding.replyNo.text = currentStateHolder.language.getLocalized(itemView.context, R.string.no)
      binding.replyYes.text =
        currentStateHolder.language.getLocalized(itemView.context, R.string.yes)
    }

    fun setAskAction(askAction: AskAction) {
      this.askAction = askAction
    }
  }

  private inner class VoiceHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemSimpleReplyBinding>(
    ListItemSimpleReplyBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(text: String) {
      binding.replyTextSimple.text = text
    }
  }

  private inner class VoiceResponseHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemSimpleResponseBinding>(
    ListItemSimpleResponseBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(text: String) {
      binding.replyTextResponse.text = text
    }
  }

  private inner class ShowMoreHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemShowReplyBinding>(
    ListItemShowReplyBinding.inflate(parent.inflater(), parent, false)
  ) {
    init {
      binding.replyText.setOnClickListener {
        showMore?.invoke(bindingAdapterPosition)
        showMore = null
      }
    }
  }
}
