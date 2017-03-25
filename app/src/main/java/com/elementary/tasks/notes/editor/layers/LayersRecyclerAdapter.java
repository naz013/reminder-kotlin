package com.elementary.tasks.notes.editor.layers;

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
import com.elementary.tasks.core.interfaces.Observer;
import com.elementary.tasks.databinding.LayerListItemBinding;

import java.util.Collections;
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

public class LayersRecyclerAdapter extends RecyclerView.Adapter<LayersRecyclerAdapter.ViewHolder> implements Observer {

    private List<Drawing> mDataList;
    private Context mContext;
    private OnStartDragListener onStartDragListener;
    private AdapterCallback mCallback;
    private int index;

    public LayersRecyclerAdapter(Context context, List<Drawing> list, OnStartDragListener listener, AdapterCallback callback) {
        this.mDataList = list;
        this.mContext = context;
        this.onStartDragListener = listener;
        this.mCallback = callback;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    public int getIndex() {
        return index;
    }

    void onItemDismiss(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mDataList.size());
        if (mCallback != null) {
            mCallback.onItemRemoved(position);
        }
    }

    void onItemMove(int from, int to) {
        Collections.swap(mDataList, from, to);
        notifyItemMoved(from, to);
        if (mCallback != null) {
            mCallback.onChanged();
        }
        notifyDataSetChanged();
    }

    @Override
    public void setUpdate(Object o) {
        if (o instanceof Integer) {
            this.index = ((Integer) (o)) - 1;
        }
        notifyDataSetChanged();
        if (mCallback != null) {
            mCallback.onItemAdded();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LayerListItemBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            v.setOnClickListener(view -> {
                if (mCallback != null) {
                    mCallback.onItemSelect(getAdapterPosition());
                }
            });
            v.setOnLongClickListener(view -> {
                onStartDragListener.onStartDrag(this);
                return true;
            });
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
        if (position == index) {
            holder.binding.selectionView.setBackgroundResource(R.color.redPrimary);
        } else {
            holder.binding.selectionView.setBackgroundResource(android.R.color.transparent);
        }
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

    public interface AdapterCallback {
        void onChanged();

        void onItemSelect(int position);

        void onItemRemoved(int position);

        void onItemAdded();
    }
}
