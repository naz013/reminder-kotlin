package com.elementary.tasks.core.additional;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.core.data.models.SmsTemplate;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.ListItemMessageBinding;

import java.util.ArrayList;
import java.util.List;

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

class SelectableTemplatesAdapter extends RecyclerView.Adapter<SelectableTemplatesAdapter.ViewHolder> {

    private List<SmsTemplate> mDataList = new ArrayList<>();
    private int selectedPosition = -1;
    private ThemeUtil themeUtil;

    SelectableTemplatesAdapter(Context context) {
        themeUtil = ThemeUtil.getInstance(context);
    }

    public void setData(List<SmsTemplate> list) {
        this.mDataList.clear();
        this.mDataList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final SmsTemplate item = mDataList.get(position);
        holder.binding.setItem(item);
        if (item.isSelected()) {
            holder.binding.cardView.setCardBackgroundColor(themeUtil.getColor(themeUtil.colorAccent()));
        } else {
            holder.binding.cardView.setCardBackgroundColor(themeUtil.getCardStyle());
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ListItemMessageBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.getRoot().setOnClickListener(view -> selectItem(getAdapterPosition()));
        }
    }

    int getSelectedPosition() {
        return selectedPosition;
    }

    public SmsTemplate getItem(int position) {
        return mDataList.get(position);
    }

    void selectItem(int position) {
        if (position == selectedPosition) return;
        if (selectedPosition != -1 && selectedPosition < mDataList.size()) {
            mDataList.get(selectedPosition).setSelected(false);
            notifyItemChanged(selectedPosition);
        }
        this.selectedPosition = position;
        if (position < mDataList.size()) {
            mDataList.get(position).setSelected(true);
            notifyItemChanged(position);
        }
    }
}
