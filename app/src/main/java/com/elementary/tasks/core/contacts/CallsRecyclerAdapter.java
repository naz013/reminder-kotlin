package com.elementary.tasks.core.contacts;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.elementary.tasks.R;
import com.elementary.tasks.core.file_explorer.FilterCallback;
import com.elementary.tasks.core.file_explorer.RecyclerClickListener;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.elementary.tasks.core.utils.TimeUtil;
import com.elementary.tasks.core.views.roboto.RoboTextView;
import com.elementary.tasks.databinding.CallsListItemBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

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

public class CallsRecyclerAdapter extends RecyclerView.Adapter<CallsRecyclerAdapter.ContactViewHolder> {

    private Context mContext;
    private List<CallsItem> mDataList;

    private RecyclerClickListener mListener;
    private FilterCallback mCallback;

    CallsRecyclerAdapter(Context context, List<CallsItem> dataItemList, RecyclerClickListener listener, FilterCallback callback) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
        this.mListener = listener;
        this.mCallback = callback;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ContactViewHolder(CallsListItemBinding.inflate(inflater, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        CallsItem item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        CallsListItemBinding binding;

        ContactViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.setClick(view -> {
                if (mListener != null) {
                    mListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public void filter(String q, List<CallsItem> list) {
        List<CallsItem> res = filter(list, q);
        animateTo(res);
        if (mCallback != null) mCallback.filter(res.size());
    }

    private List<CallsItem> filter(List<CallsItem> mData, String q) {
        q = q.toLowerCase();
        if (mData == null) mData = new ArrayList<>();
        List<CallsItem> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<CallsItem> getFiltered(List<CallsItem> models, String query) {
        List<CallsItem> list = new ArrayList<>();
        for (CallsItem model : models) {
            final String text = model.getNumberName().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    public CallsItem removeItem(int position) {
        final CallsItem model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, CallsItem model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final CallsItem model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<CallsItem> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<CallsItem> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final CallsItem model = mDataList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<CallsItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final CallsItem model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<CallsItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final CallsItem model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public CallsItem getItem(int position) {
        if (position < mDataList.size()) return mDataList.get(position);
        else return null;
    }

    @BindingAdapter({"loadCallDate"})
    public static void loadDate(RoboTextView textView, long date) {
        boolean is24 = Prefs.getInstance(textView.getContext()).is24HourFormatEnabled();
        textView.setText(TimeUtil.getSimpleDateTime(date, is24));
    }

    @BindingAdapter({"loadIcon"})
    public static void loadIcon(ImageView imageView, int type) {
        boolean isDark = ThemeUtil.getInstance(imageView.getContext()).isDark();
        if (type == CallLog.Calls.INCOMING_TYPE) {
            imageView.setImageResource(isDark ? R.drawable.ic_call_received_white_24dp : R.drawable.ic_call_received_black_24dp);
        } else if (type == CallLog.Calls.MISSED_TYPE) {
            imageView.setImageResource(isDark ? R.drawable.ic_call_missed_white_24dp : R.drawable.ic_call_missed_black_24dp);
        } else {
            imageView.setImageResource(isDark ? R.drawable.ic_call_made_white_24dp : R.drawable.ic_call_made_black_24dp);
        }
    }

    @BindingAdapter({"loadCallImage"})
    public static void loadImage(ImageView imageView, String v) {
        boolean isDark = ThemeUtil.getInstance(imageView.getContext()).isDark();
        if (v == null) {
            imageView.setImageResource(isDark ? R.drawable.ic_perm_identity_white_24dp : R.drawable.ic_perm_identity_black_24dp);
            return;
        }
        Picasso.with(imageView.getContext())
                .load(Uri.parse(v))
                .resize(100, 100)
                .transform(new CropCircleTransformation())
                .into(imageView);
    }
}
