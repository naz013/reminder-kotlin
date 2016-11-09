package com.elementary.tasks.groups;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.GroupListItemBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2016 Nazar Suhovich
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

public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.ViewHolder> {

    private List<GroupItem> mDataList;
    private SimpleListener mEventListener;
    private Context mContext;

    public GroupsRecyclerAdapter(Context context, List<GroupItem> list, SimpleListener listener) {
        this.mDataList = new ArrayList<>(list);
        this.mContext = context;
        this.mEventListener = listener;
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        GroupListItemBinding binding;
        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            v.setOnClickListener(view -> openGroup(getAdapterPosition()));
            v.setOnLongClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(getAdapterPosition(), view);
                }
                return true;
            });
        }
    }

    public void deleteItem(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mDataList.size());
    }

    private void openGroup(int position) {
        mContext.startActivity(new Intent(mContext, CreateGroupActivity.class).putExtra(Constants.INTENT_ID, mDataList.get(position).getUuId()));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        GroupListItemBinding binding = GroupListItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        GroupItem item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    public GroupItem getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @BindingAdapter({"loadIndicator"})
    public static void loadIndicator(View view, int color) {
        view.setBackgroundResource(ThemeUtil.getInstance(view.getContext()).getCategoryIndicator(color));
    }
}
