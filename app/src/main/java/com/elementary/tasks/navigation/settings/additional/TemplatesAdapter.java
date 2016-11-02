package com.elementary.tasks.navigation.settings.additional;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
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

public class TemplatesAdapter extends RecyclerView.Adapter<TemplatesAdapter.ViewHolder> {

    private List<TemplateItem> mDataList = new ArrayList<>();
    private Context mContext;

    public TemplatesAdapter(List<TemplateItem> mDataList, Context mContext) {
        this.mDataList = mDataList;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemMessageBinding.inflate(LayoutInflater.from(mContext), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.binding.setItem(mDataList.get(position));
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
        RealmDb.getInstance().deleteTemplates(mDataList.get(position));
        mDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mDataList.size());
    }

    private void openTemplate(int position) {
        mContext.startActivity(new Intent(mContext, TemplateActivity.class).putExtra(Constants.INTENT_ID, mDataList.get(position).getKey()));
    }
}
