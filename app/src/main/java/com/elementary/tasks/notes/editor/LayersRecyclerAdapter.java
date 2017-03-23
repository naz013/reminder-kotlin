package com.elementary.tasks.notes.editor;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elementary.tasks.R;
import com.elementary.tasks.core.drawing.Background;
import com.elementary.tasks.core.drawing.Drawing;
import com.elementary.tasks.core.drawing.Image;
import com.elementary.tasks.core.drawing.Text;
import com.elementary.tasks.databinding.LayerListItemBinding;

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

public class LayersRecyclerAdapter extends RecyclerView.Adapter<LayersRecyclerAdapter.ViewHolder> {

    private List<Drawing> mDataList;
    private Context mContext;

    public LayersRecyclerAdapter(Context context, List<Drawing> list) {
        this.mDataList = list;
        this.mContext = context;
    }

    public void deleteItem(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mDataList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LayerListItemBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
//            v.setOnClickListener(view -> {
//                if (mEventListener != null) {
//                    mEventListener.onItemClicked(getAdapterPosition(), view);
//                }
//            });
//            v.setOnLongClickListener(view -> {
//                if (mEventListener != null) {
//                    mEventListener.onItemLongClicked(getAdapterPosition(), view);
//                }
//                return true;
//            });
        }
    }

    @Override
    public LayersRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayerListItemBinding.inflate(LayoutInflater.from(mContext), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(final LayersRecyclerAdapter.ViewHolder holder, final int position) {
        Drawing item = mDataList.get(position);
        holder.binding.layerName.setText(getName(item));
        holder.binding.layerView.setDrawing(item);
    }

    private String getName(Drawing drawing) {
        if (drawing instanceof Background) {
            return mContext.getString(R.string.background);
        } else if (drawing instanceof Image) {
            return mContext.getString(R.string.image);
        } else if (drawing instanceof Text) {
            return ((Text) drawing).getText();
        } else {
            return mContext.getString(R.string.figure);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }
}
