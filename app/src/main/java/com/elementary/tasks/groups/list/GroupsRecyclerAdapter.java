package com.elementary.tasks.groups.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.data.models.Group;
import com.elementary.tasks.core.interfaces.SimpleListener;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.ListItemGroupBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

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
public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupHolder> {

    private List<Group> mDataList = new ArrayList<>();
    private SimpleListener mEventListener;

    GroupsRecyclerAdapter(SimpleListener listener) {
        this.mEventListener = listener;
    }

    public void setData(List<Group> list) {
        this.mDataList.clear();
        this.mDataList.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupHolder(ListItemGroupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot(), mEventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupHolder holder, final int position) {
        Group item = mDataList.get(position);
        holder.setData(item);
    }

    public Group getItem(int position) {
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
