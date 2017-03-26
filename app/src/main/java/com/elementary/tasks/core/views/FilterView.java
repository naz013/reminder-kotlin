package com.elementary.tasks.core.views;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.databinding.ChipViewBinding;

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
        HorizontalScrollView scrollView = new HorizontalScrollView(mContext);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollView.setHorizontalScrollBarEnabled(false);
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(MeasureUtils.dp2px(mContext, 8), 0, 0, 0);
        for (FilterElement element : filter) {
            View view = createChip(element, filter.getElementClick());
            layout.addView(view, layoutParams);
        }
        layout.setPadding(0, 0, MeasureUtils.dp2px(mContext, 8), 0);
        scrollView.addView(layout);
        return scrollView;
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

    public interface FilterElementClick {
        void onClick(View view, int id);
    }

    public static class Filter extends AbstractList<FilterElement> {

        private FilterElementClick elementClick;
        private List<FilterElement> elements = new ArrayList<>();

        public Filter(FilterElementClick elementClick) {
            this.elementClick = elementClick;
        }

        public FilterElementClick getElementClick() {
            return elementClick;
        }

        public void setElementClick(FilterElementClick elementClick) {
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

        public FilterElement(@DrawableRes int iconId, String title, int id) {
            this.iconId = iconId;
            this.title = title;
            this.id = id;
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
}
