package com.elementary.tasks.navigation.settings.additional;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;
import com.elementary.tasks.core.utils.RealmDb;
import com.elementary.tasks.core.utils.SuperUtil;
import com.elementary.tasks.databinding.ListItemMessageBinding;

import java.util.ArrayList;
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
class TemplatesAdapter extends RecyclerView.Adapter<TemplatesAdapter.ViewHolder> {

    private Context mContext;
    private List<TemplateItem> mData = new ArrayList<>();

    TemplatesAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<TemplateItem> list) {
        this.mData = list;
        notifyDataSetChanged();
    }

    public List<TemplateItem> getData() {
        return mData;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public TemplateItem getItem(int position) {
        return mData.get(position);
    }

    public void removeItem(int position) {
        if (position < mData.size()) {
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, mData.size());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemMessageBinding.inflate(LayoutInflater.from(mContext), parent, false).getRoot());
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
            binding.getRoot().setOnClickListener(view -> openTemplate(getAdapterPosition()));
            binding.getRoot().setOnLongClickListener(view -> {
                showMenu(getAdapterPosition());
                return true;
            });
        }
    }

    private void showMenu(int position) {
        String[] items = new String[]{mContext.getString(R.string.edit), mContext.getString(R.string.delete)};
        SuperUtil.showLCAM(mContext, item -> {
            switch (item) {
                case 0:
                    openTemplate(position);
                    break;
                case 1:
                    deleteTemplate(position);
                    break;
            }
        }, items);
    }

    private void deleteTemplate(int position) {
        TemplateItem item = getItem(position);
        RealmDb.getInstance().deleteTemplates(item);
        new DeleteTemplateFilesAsync(mContext).execute(item.getKey());
        removeItem(position);
    }

    private void openTemplate(int position) {
        mContext.startActivity(new Intent(mContext, TemplateActivity.class).putExtra(Constants.INTENT_ID, getItem(position).getKey()));
    }
}
