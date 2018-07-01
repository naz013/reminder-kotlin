package com.elementary.tasks.notes.list;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elementary.tasks.core.interfaces.ActionsListener;
import com.elementary.tasks.core.utils.ListActions;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.databinding.ListItemNoteImageBinding;
import com.elementary.tasks.notes.create.NoteImage;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
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
public class ImagesGridAdapter extends RecyclerView.Adapter<ImagesGridAdapter.PhotoViewHolder> {

    private List<NoteImage> mDataList = new ArrayList<>();
    private boolean isEditable;
    @Nullable
    private ActionsListener<NoteImage> actionsListener;

    public ImagesGridAdapter() {
    }

    public void setActionsListener(ActionsListener<NoteImage> actionsListener) {
        this.actionsListener = actionsListener;
    }

    private ActionsListener<NoteImage> getActionsListener() {
        return actionsListener;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public NoteImage getItem(int position) {
        return mDataList.get(position);
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhotoViewHolder(ListItemNoteImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        loadImage(holder.binding.photoView, mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public List<NoteImage> getData() {
        return mDataList;
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ListItemNoteImageBinding binding;

        PhotoViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.photoView.setOnClickListener(view -> performClick(view, getAdapterPosition()));
            if (isEditable) {
                binding.removeButton.setVisibility(View.VISIBLE);
                binding.removeButton.setBackgroundResource(ThemeUtil.getInstance(itemView.getContext()).getIndicator());
                binding.removeButton.setOnClickListener(view -> removeImage(getAdapterPosition()));
                if (getActionsListener() != null && Module.isPro()) {
                    binding.editButton.setVisibility(View.VISIBLE);
                    binding.editButton.setBackgroundResource(ThemeUtil.getInstance(itemView.getContext()).getIndicator());
                    binding.editButton.setOnClickListener(view -> {
                        getActionsListener().onAction(view, getAdapterPosition(), getItem(getAdapterPosition()), ListActions.EDIT);
                    });
                } else {
                    binding.editButton.setVisibility(View.GONE);
                }
            } else {
                binding.removeButton.setVisibility(View.GONE);
                binding.editButton.setVisibility(View.GONE);
            }
        }
    }

    private void removeImage(int position) {
        mDataList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(0, mDataList.size());
    }

    public void setImages(List<NoteImage> list) {
        mDataList.clear();
        mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void addNextImages(List<NoteImage> list) {
        mDataList.addAll(list);
        notifyItemRangeChanged(0, mDataList.size());
    }

    public void setImage(NoteImage image, int position) {
        mDataList.set(position, image);
        notifyItemChanged(position);
    }

    public void addImage(NoteImage image) {
        mDataList.add(image);
        notifyDataSetChanged();
    }

    private void performClick(View view, int position) {
        if (getActionsListener() != null) {
            getActionsListener().onAction(view, position, null, ListActions.OPEN);
        }
    }

    public void loadImage(ImageView imageView, NoteImage image) {
        new Thread(() -> {
            Bitmap bmp = BitmapFactory.decodeByteArray(image.getImage(), 0, image.getImage().length);
            imageView.post(() -> imageView.setImageBitmap(bmp));
        }).start();
    }
}
