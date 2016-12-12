package com.elementary.tasks.reminder;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.ViewUtils;
import com.elementary.tasks.databinding.ListItemTaskItemCardBinding;
import com.elementary.tasks.reminder.models.ShopItem;

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

public class ShopListRecyclerAdapter extends RecyclerView.Adapter<ShopListRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<ShopItem> mDataList = new ArrayList<>();
    private boolean isDark;
    private ActionListener listener;

    public ShopListRecyclerAdapter(Context context, List<ShopItem> list,
                                   ActionListener listener) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(list);
        this.listener = listener;
        isDark = ThemeUtil.getInstance(context).isDark();
        setHasStableIds(true);
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
        public ViewHolder(final View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }
    }

    public void addItem(ShopItem item) {
        mDataList.add(item);
        notifyItemInserted(mDataList.size() - 1);
        notifyItemRangeChanged(0, mDataList.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(ListItemTaskItemCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ShopItem item = mDataList.get(position);
        String title = item.getSummary();
        if (item.isChecked()){
            holder.binding.shopText.setPaintFlags(holder.binding.shopText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.binding.shopText.setPaintFlags(holder.binding.shopText.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        holder.binding.itemCheck.setChecked(item.isChecked());
        holder.binding.shopText.setText(title);
        if (listener == null){
            holder.binding.clearButton.setVisibility(View.GONE);
            holder.binding.itemCheck.setEnabled(false);
            holder.binding.shopText.setTextColor(ViewUtils.getColor(mContext, R.color.blackPrimary));
        } else {
            if (isDark) {
                holder.binding.clearButton.setImageResource(R.drawable.ic_clear_white_24dp);
            } else {
                holder.binding.clearButton.setImageResource(R.drawable.ic_clear_black_24dp);
            }

            holder.binding.itemCheck.setVisibility(View.VISIBLE);
            holder.binding.clearButton.setVisibility(View.VISIBLE);
            holder.binding.clearButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDelete(holder.getAdapterPosition());
                }
            });

            holder.binding.shopText.setOnClickListener(v -> {
                if (listener != null && item.isDeleted()) {
                    listener.onItemChange(holder.getAdapterPosition());
                }
            });

            holder.binding.itemCheck.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                if (listener != null) {
                    listener.onItemCheck(holder.getAdapterPosition(), isChecked1);
                }
            });
        }
        if (item.isDeleted()){
            holder.binding.shopText.setTextColor(ViewUtils.getColor(mContext, R.color.redPrimaryDark));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public interface ActionListener{

        void onItemCheck(int position, boolean isChecked);

        void onItemDelete(int position);

        void onItemChange(int position);
    }
}
