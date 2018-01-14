package com.elementary.tasks.core.views;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.databinding.ChipViewBinding;
import com.elementary.tasks.databinding.ViewRecyclerBinding;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright 2017 Nazar Suhovich
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
public class FilterView extends LinearLayout {

    private static final String TAG = "FilterView";
    private Context mContext;
    private int numOfFilters;

    public FilterView(Context context) {
        super(context);
        init(context, null);
    }

    public FilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FilterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        this.mContext = context;
        if (isInEditMode()) return;
        setOrientation(VERTICAL);
    }

    public void clear() {
        this.removeAllViewsInLayout();
        this.numOfFilters = 0;
    }

    public void addFilter(Filter filter) {
        if (filter == null) return;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, MeasureUtils.dp2px(mContext, 8), 0, MeasureUtils.dp2px(mContext, 8));
        if (numOfFilters > 0) {
            this.addDivider();
        }
        this.addView(createFilter(filter), layoutParams);
        this.numOfFilters++;
        this.requestLayout();
    }

    private void addDivider() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, MeasureUtils.dp2px(mContext, 1));
        layoutParams.setMargins(MeasureUtils.dp2px(mContext, 16), 0, MeasureUtils.dp2px(mContext, 16), 0);
        View view = new View(mContext);
        view.setBackgroundColor(getResources().getColor(R.color.whitePrimary));
        this.addView(view, layoutParams);
    }

    private View createFilter(Filter filter) {
        ViewRecyclerBinding view = ViewRecyclerBinding.inflate(LayoutInflater.from(getContext()));
        view.recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        view.recyclerView.setHorizontalScrollBarEnabled(false);
        view.recyclerView.setPadding(0, 0, MeasureUtils.dp2px(mContext, 8), 0);
        view.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        view.recyclerView.setAdapter(new FilterAdapter(filter));
        return view.getRoot();
    }

    private View createChip(FilterElement element, FilterElementClick click) {
        ChipViewBinding binding = ChipViewBinding.inflate(LayoutInflater.from(mContext));
        binding.chipTitle.setText(element.getTitle());
        if (element.getIconId() == 0) {
            binding.iconView.setVisibility(GONE);
        } else {
            binding.iconView.setImageResource(element.getIconId());
            binding.iconView.setVisibility(VISIBLE);
        }
        binding.getRoot().setOnClickListener(v -> click.onClick(v, element.getId()));
        return binding.getRoot();
    }

    private class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.Holder> {
        private Filter filter;

        FilterAdapter(Filter filter) {
            this.filter = filter;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(ChipViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot());
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            Log.d(TAG, "onBindViewHolder: ");
            FilterElement element = filter.get(position);
            holder.binding.chipTitle.setText(element.getTitle());
            if (element.getIconId() == 0) {
                holder.binding.iconView.setVisibility(GONE);
            } else {
                holder.binding.iconView.setImageResource(element.getIconId());
                holder.binding.iconView.setVisibility(VISIBLE);
            }
            if (filter.getChoiceMode() == ChoiceMode.SINGLE) {
                holder.binding.statusView.setVisibility(GONE);
            } else {
                if (element.isChecked) holder.binding.statusView.setVisibility(VISIBLE);
                else holder.binding.statusView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "getItemCount: " + filter.size());
            return filter.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            ChipViewBinding binding;

            Holder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
                binding.getRoot().setOnClickListener(v -> updateFilter(v, getAdapterPosition()));
            }
        }

        private void updateFilter(View v, int position) {
            if (filter.getChoiceMode() == ChoiceMode.SINGLE) {
                filter.getElementClick().onClick(v, filter.get(position).id);
            } else {
                if (filter.get(position).getId() == 0) {
                    unCheckAll();
                }
                filter.get(position).setChecked(!filter.get(position).isChecked());
                notifyItemChanged(position);
                filter.getElementClick().onMultipleSelected(v, getSelected());
            }
        }

        private void unCheckAll() {
            for (FilterElement element : filter) element.setChecked(false);
            notifyDataSetChanged();
        }

        private List<Integer> getSelected() {
            List<Integer> list = new ArrayList<>();
            for (FilterElement element : filter) if (element.isChecked()) list.add(element.getId());
            return list;
        }
    }

    public interface FilterElementClick {
        void onClick(View view, int id);

        void onMultipleSelected(View view, List<Integer> ids);
    }

    public static class Filter extends AbstractList<FilterElement> {
        @NonNull
        private FilterElementClick elementClick;
        private List<FilterElement> elements = new ArrayList<>();
        private ChoiceMode choiceMode = ChoiceMode.SINGLE;

        public Filter(@NonNull FilterElementClick elementClick) {
            this.elementClick = elementClick;
        }

        public ChoiceMode getChoiceMode() {
            return choiceMode;
        }

        public void setChoiceMode(ChoiceMode choiceMode) {
            this.choiceMode = choiceMode;
        }

        @NonNull
        FilterElementClick getElementClick() {
            return elementClick;
        }

        public void setElementClick(@NonNull FilterElementClick elementClick) {
            this.elementClick = elementClick;
        }

        @Override
        public FilterElement get(int index) {
            return elements.get(index);
        }

        @Override
        public int size() {
            return elements.size();
        }

        @Override
        public boolean add(FilterElement filterElement) {
            return elements.add(filterElement);
        }

        @NonNull
        @Override
        public Iterator<FilterElement> iterator() {
            return elements.iterator();
        }
    }

    public static class FilterElement {
        @DrawableRes
        private int iconId;
        private String title;
        private int id;
        private boolean isChecked;

        public FilterElement(@DrawableRes int iconId, String title, int id) {
            this.iconId = iconId;
            this.title = title;
            this.id = id;
        }

        public FilterElement(@DrawableRes int iconId, String title, int id, boolean isChecked) {
            this.iconId = iconId;
            this.title = title;
            this.id = id;
            this.isChecked = isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public boolean isChecked() {
            return isChecked;
        }

        @DrawableRes
        public int getIconId() {
            return iconId;
        }

        public void setIconId(@DrawableRes int iconId) {
            this.iconId = iconId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public enum ChoiceMode {
        SINGLE,
        MULTI
    }
}
