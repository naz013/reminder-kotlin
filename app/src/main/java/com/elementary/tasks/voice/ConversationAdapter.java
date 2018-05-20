package com.elementary.tasks.voice;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.birthdays.BirthdayHolder;
import com.elementary.tasks.birthdays.BirthdayItem;
import com.elementary.tasks.core.utils.Language;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.AskListItemBinding;
import com.elementary.tasks.databinding.GroupListItemBinding;
import com.elementary.tasks.databinding.ListItemEventsBinding;
import com.elementary.tasks.databinding.NoteListItemBinding;
import com.elementary.tasks.databinding.ReminderListItemBinding;
import com.elementary.tasks.databinding.ShoppingListItemBinding;
import com.elementary.tasks.databinding.ShowReplyLayoutBinding;
import com.elementary.tasks.databinding.SimpleReplyLayoutBinding;
import com.elementary.tasks.databinding.SimpleResponseLayoutBinding;
import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.groups.GroupHolder;
import com.elementary.tasks.notes.NoteHolder;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.reminder.ReminderHolder;
import com.elementary.tasks.reminder.ShoppingHolder;
import com.elementary.tasks.core.data.models.Reminder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Copyright 2017 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Reply> mData;
    private Context mContext;
    private ThemeUtil themeUtil;
    private InsertCallback mCallback;

    ConversationAdapter(Context context) {
        this.mContext = context;
        mData = new LinkedList<>();
        themeUtil = ThemeUtil.getInstance(context);
    }

    void setInsertListener(InsertCallback callback) {
        this.mCallback = callback;
    }

    void addReply(Reply reply) {
        if (reply != null) {
            mData.add(0, reply);
            notifyItemInserted(0);
            notifyItemRangeChanged(0, mData.size());
            if (mCallback != null) {
                mCallback.onItemAdded();
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == Reply.REPLY) {
            return new VoiceHolder(SimpleReplyLayoutBinding.inflate(inflater, parent, false).getRoot());
        } else if (viewType == Reply.RESPONSE) {
            return new VoiceResponseHolder(SimpleResponseLayoutBinding.inflate(inflater, parent, false).getRoot());
        } else if (viewType == Reply.REMINDER) {
            return new ReminderHolder(ReminderListItemBinding.inflate(inflater, parent, false).getRoot(), null, themeUtil, false);
        } else if (viewType == Reply.NOTE) {
            return new NoteHolder(NoteListItemBinding.inflate(inflater, parent, false).getRoot(), null);
        } else if (viewType == Reply.GROUP) {
            return new GroupHolder(GroupListItemBinding.inflate(inflater, parent, false).getRoot(), null);
        } else if (viewType == Reply.SHOW_MORE) {
            return new ShowMoreHolder(ShowReplyLayoutBinding.inflate(inflater, parent, false).getRoot());
        } else if (viewType == Reply.BIRTHDAY) {
            return new BirthdayHolder(ListItemEventsBinding.inflate(inflater, parent, false).getRoot(), null, themeUtil);
        } else if (viewType == Reply.SHOPPING) {
            return new ShoppingHolder(ShoppingListItemBinding.inflate(inflater, parent, false).getRoot(), null, themeUtil);
        } else if (viewType == Reply.ASK) {
            return new AskHolder(AskListItemBinding.inflate(inflater, parent, false).getRoot());
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VoiceHolder) {
            ((VoiceHolder) holder).binding.replyText.setText((String) mData.get(position).getObject());
        } else if (holder instanceof VoiceResponseHolder) {
            ((VoiceResponseHolder) holder).binding.replyText.setText((String) mData.get(position).getObject());
        } else if (holder instanceof ReminderHolder) {
            ((ReminderHolder) holder).setData((Reminder) mData.get(position).getObject());
        } else if (holder instanceof NoteHolder) {
            ((NoteHolder) holder).setData((NoteItem) mData.get(position).getObject());
        } else if (holder instanceof GroupHolder) {
            ((GroupHolder) holder).setData((Group) mData.get(position).getObject());
        } else if (holder instanceof BirthdayHolder) {
            ((BirthdayHolder) holder).setData((BirthdayItem) mData.get(position).getObject());
            ((BirthdayHolder) holder).setColor(themeUtil.getColor(themeUtil.colorBirthdayCalendar()));
        } else if (holder instanceof ShoppingHolder) {
            ((ShoppingHolder) holder).setData((Reminder) mData.get(position).getObject());
        } else if (holder instanceof AskHolder) {
            ((AskHolder) holder).setAskAction((AskAction) mData.get(position).getObject());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void removeAsk() {
        for (int i = 1; i < mData.size(); i++) {
            Reply reply = mData.get(i);
            if (reply != null && reply.getViewType() == Reply.ASK) {
                mData.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(0, mData.size());
                break;
            }
        }
    }

    private class AskHolder extends RecyclerView.ViewHolder {

        private AskListItemBinding binding;
        private AskAction askAction;

        AskHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.replyYes.setOnClickListener(v -> {
                removeFirst();
                if (askAction != null) {
                    askAction.onYes();
                }
            });
            binding.replyNo.setOnClickListener(v -> {
                removeFirst();
                if (askAction != null) {
                    askAction.onNo();
                }
            });
            binding.replyNo.setBackgroundResource(themeUtil.getRectangle());
            binding.replyYes.setBackgroundResource(themeUtil.getRectangle());
            binding.replyNo.setText(Language.getLocalized(mContext, R.string.no));
            binding.replyYes.setText(Language.getLocalized(mContext, R.string.yes));
        }

        void setAskAction(AskAction askAction) {
            this.askAction = askAction;
        }
    }

    private void removeFirst() {
        mData.remove(0);
        notifyItemRemoved(0);
        notifyItemRangeChanged(0, mData.size());
    }

    private class VoiceHolder extends RecyclerView.ViewHolder {

        SimpleReplyLayoutBinding binding;

        VoiceHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    private class VoiceResponseHolder extends RecyclerView.ViewHolder {

        SimpleResponseLayoutBinding binding;

        VoiceResponseHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    private class ShowMoreHolder extends RecyclerView.ViewHolder {

        private ShowReplyLayoutBinding binding;

        ShowMoreHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.replyText.setOnClickListener(view -> addMoreItemsToList(getAdapterPosition()));
        }
    }

    private void addMoreItemsToList(int position) {
        Reply reply = mData.get(position);
        Container container = (Container) reply.getObject();
        if (container.getType() instanceof Group) {
            mData.remove(position);
            notifyItemRemoved(position);
            //noinspection unchecked
            for (Group item : ((Container<Group>) container).getList()) {
                mData.add(0, new Reply(Reply.GROUP, item));
                notifyItemInserted(0);
            }
            notifyItemRangeChanged(0, mData.size());
            if (mCallback != null) mCallback.onItemAdded();
        } else if (container.getType() instanceof NoteItem) {
            mData.remove(position);
            notifyItemRemoved(position);
            //noinspection unchecked
            for (NoteItem item : ((Container<NoteItem>) container).getList()) {
                mData.add(0, new Reply(Reply.NOTE, item));
                notifyItemInserted(0);
            }
            notifyItemRangeChanged(0, mData.size());
            if (mCallback != null) mCallback.onItemAdded();
        } else if (container.getType() instanceof Reminder) {
            mData.remove(position);
            notifyItemRemoved(position);
            addRemindersToList(container);
            notifyItemRangeChanged(0, mData.size());
            if (mCallback != null) mCallback.onItemAdded();
        } else if (container.getType() instanceof BirthdayItem) {
            mData.remove(position);
            notifyItemRemoved(position);
            //noinspection unchecked
            List<BirthdayItem> reversed = new ArrayList<>(((Container<BirthdayItem>) container).getList());
            Collections.reverse(reversed);
            for (BirthdayItem item : reversed) {
                mData.add(0, new Reply(Reply.BIRTHDAY, item));
                notifyItemInserted(0);
            }
            notifyItemRangeChanged(0, mData.size());
            if (mCallback != null) mCallback.onItemAdded();
        }
    }

    private void addRemindersToList(Container container) {
        //noinspection unchecked
        List<Reminder> reversed = new ArrayList<>(((Container<Reminder>) container).getList());
        Collections.reverse(reversed);
        for (Reminder item : reversed) {
            if (item.getViewType() == Reminder.REMINDER) {
                mData.add(0, new Reply(Reply.REMINDER, item));
            } else {
                mData.add(0, new Reply(Reply.SHOPPING, item));
            }
            notifyItemInserted(0);
        }
    }

    interface InsertCallback {
        void onItemAdded();
    }
}
