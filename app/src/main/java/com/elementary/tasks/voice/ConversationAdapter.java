package com.elementary.tasks.voice;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.GroupListItemBinding;
import com.elementary.tasks.databinding.NoteListItemBinding;
import com.elementary.tasks.databinding.ReminderListItemBinding;
import com.elementary.tasks.databinding.SimpleReplyLayoutBinding;
import com.elementary.tasks.groups.GroupHolder;
import com.elementary.tasks.groups.GroupItem;
import com.elementary.tasks.notes.NoteHolder;
import com.elementary.tasks.notes.NoteItem;
import com.elementary.tasks.reminder.ReminderHolder;
import com.elementary.tasks.reminder.models.Reminder;

import java.util.LinkedList;
import java.util.List;

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
            if (mCallback != null) mCallback.onItemAdded();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        if (viewType == Reply.REPLY) {
            return new VoiceHolder(SimpleReplyLayoutBinding.inflate(inflater, parent, false).getRoot(), false);
        } else if (viewType == Reply.RESPONSE) {
            return new VoiceHolder(SimpleReplyLayoutBinding.inflate(inflater, parent, false).getRoot(), true);
        } else if (viewType == Reply.REMINDER) {
            return new ReminderHolder(ReminderListItemBinding.inflate(inflater, parent, false).getRoot(), null, themeUtil, false);
        } else if (viewType == Reply.NOTE) {
            return new NoteHolder(NoteListItemBinding.inflate(inflater, parent, false).getRoot(), null);
        } else if (viewType == Reply.GROUP) {
            return new GroupHolder(GroupListItemBinding.inflate(inflater, parent, false).getRoot(), null);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VoiceHolder) {
            ((VoiceHolder) holder).binding.replyText.setText((String) mData.get(position).getObject());
        } else if (holder instanceof ReminderHolder) {
            ((ReminderHolder) holder).setData((Reminder) mData.get(position).getObject());
        } else if (holder instanceof NoteHolder) {
            ((NoteHolder) holder).setData((NoteItem) mData.get(position).getObject());
        } else if (holder instanceof GroupHolder) {
            ((GroupHolder) holder).setData((GroupItem) mData.get(position).getObject());
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

    private class VoiceHolder extends RecyclerView.ViewHolder {

        SimpleReplyLayoutBinding binding;

        VoiceHolder(View itemView, boolean right) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            if (right) {
                binding.replyText.setGravity(Gravity.END);
            }
        }
    }

    interface InsertCallback {
        void onItemAdded();
    }
}
