package com.elementary.tasks.navigation.settings.additional;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.data.models.SmsTemplate;
import com.elementary.tasks.core.interfaces.ActionsListener;
import com.elementary.tasks.core.utils.ListActions;
import com.elementary.tasks.databinding.ListItemMessageBinding;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
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
class TemplatesAdapter extends RecyclerView.Adapter<TemplatesAdapter.ViewHolder> {

    private List<SmsTemplate> mData = new ArrayList<>();
    @Nullable
    private ActionsListener<SmsTemplate> actionsListener;

    TemplatesAdapter() {
    }

    void setActionsListener(@Nullable ActionsListener<SmsTemplate> actionsListener) {
        this.actionsListener = actionsListener;
    }

    @Nullable
    private ActionsListener<SmsTemplate> getActionsListener() {
        return actionsListener;
    }

    public void setData(List<SmsTemplate> list) {
        this.mData = list;
        notifyDataSetChanged();
    }

    public List<SmsTemplate> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public SmsTemplate getItem(int position) {
        return mData.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.setItem(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ListItemMessageBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.getRoot().setOnClickListener(view -> openTemplate(view, getAdapterPosition()));
            binding.getRoot().setOnLongClickListener(view -> {
                if (getActionsListener() != null) {
                    getActionsListener().onAction(view, getAdapterPosition(), getItem(getAdapterPosition()), ListActions.MORE);
                }
                return true;
            });
        }
    }

    private void openTemplate(View view, int position) {
        if (getActionsListener() != null) {
            getActionsListener().onAction(view, position, getItem(position), ListActions.OPEN);
        }
    }
}
