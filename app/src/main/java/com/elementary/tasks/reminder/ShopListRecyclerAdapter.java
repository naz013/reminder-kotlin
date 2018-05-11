package com.elementary.tasks.reminder;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.databinding.ListItemTaskItemCardBinding;
import com.elementary.tasks.reminder.models.ShopItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class ShopListRecyclerAdapter extends RecyclerView.Adapter<ShopListRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<ShopItem> mDataList = new ArrayList<>();
    private ActionListener listener;
    private boolean onBind;

    public ShopListRecyclerAdapter(Context context, List<ShopItem> list, ActionListener listener) {
        this.mContext = context;
        this.mDataList.addAll(list);
        this.listener = listener;
        Collections.sort(mDataList, (item, t1) -> t1.getCreateTime().compareTo(item.getCreateTime()));
        sort(mDataList);
    }

    public void delete(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mDataList.size());
    }

    public void addItem(ShopItem item) {
        mDataList.add(0, item);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, mDataList.size());
    }

    public void updateData() {
        Collections.sort(mDataList, (item, t1) -> t1.getCreateTime().compareTo(item.getCreateTime()));
        sort(mDataList);
        notifyDataSetChanged();
    }

    private void sort(List<ShopItem> list) {
        int pos = -1;
        for (int i = 0; i < list.size(); i++) {
            ShopItem item = list.get(i);
            if (!item.isChecked() && i > pos + 1) {
                list.remove(i);
                list.add(pos + 1, item);
            }
        }
    }

    public ShopItem getItem(int position) {
        return mDataList.get(position);
    }

    public List<ShopItem> getData() {
        return mDataList;
    }

    public void setData(List<ShopItem> list) {
        this.mDataList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ListItemTaskItemCardBinding binding;

        public ViewHolder(final View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.clearButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDelete(getAdapterPosition());
                }
            });
            binding.itemCheck.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                if (!onBind && listener != null) {
                    listener.onItemCheck(getAdapterPosition(), isChecked1);
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(ListItemTaskItemCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        onBind = true;
        final ShopItem item = mDataList.get(position);
        String title = item.getSummary();
        if (item.isChecked()) {
            holder.binding.shopText.setPaintFlags(holder.binding.shopText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.binding.shopText.setPaintFlags(holder.binding.shopText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        holder.binding.itemCheck.setChecked(item.isChecked());
        holder.binding.shopText.setText(title);
        if (listener == null) {
            holder.binding.clearButton.setVisibility(View.GONE);
            holder.binding.itemCheck.setEnabled(false);
            holder.binding.shopText.setTextColor(ViewUtils.getColor(mContext, R.color.blackPrimary));
        } else {
            holder.binding.itemCheck.setVisibility(View.VISIBLE);
            holder.binding.clearButton.setVisibility(View.VISIBLE);
        }
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public interface ActionListener {

        void onItemCheck(int position, boolean isChecked);

        void onItemDelete(int position);
    }
}
