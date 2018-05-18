package com.elementary.tasks.core.views;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.MeasureUtils;
import com.elementary.tasks.databinding.ChipViewBinding;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

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
    private List<Filter> mFilters = new ArrayList<>();

    public FilterView(Context context) {
        super(context);
        init(context);
    }

    public FilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FilterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        this.mContext = context;
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
        this.mFilters.add(filter);
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
        HorizontalScrollView scrollView = new HorizontalScrollView(mContext);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scrollView.setHorizontalScrollBarEnabled(false);
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(MeasureUtils.dp2px(mContext, 8), 0, 0, 0);
        for (FilterElement element : filter) {
            View view = createChip(element, filter);
            layout.addView(view, layoutParams);
        }
        layout.setPadding(0, 0, MeasureUtils.dp2px(mContext, 8), 0);
        scrollView.addView(layout);
        return scrollView;
    }

    private View createChip(FilterElement element, Filter filter) {
        ChipViewBinding binding = ChipViewBinding.inflate(LayoutInflater.from(mContext));
        binding.chipTitle.setText(element.getTitle());
        if (element.getIconId() == 0) {
            binding.iconView.setVisibility(GONE);
        } else {
            binding.iconView.setImageResource(element.getIconId());
            binding.iconView.setVisibility(VISIBLE);
        }
        setStatus(binding, element.isChecked());
        element.setBinding(binding);
        binding.getRoot().setOnClickListener(v -> updateFilter(binding, element.getId(), filter.uuId));
        return binding.getRoot();
    }

    private void setStatus(ChipViewBinding binding, boolean checked) {
        if (checked) binding.rootView.setBackgroundResource(R.drawable.chip_selected_bg);
        else binding.rootView.setBackgroundResource(R.drawable.chip_bg);
    }

    @Nullable
    private Filter getCurrent(String id) {
        if (mFilters.isEmpty()) return null;
        for (Filter filter : mFilters) {
            if (filter.getUuId().equals(id)) return filter;
        }
        return null;
    }

    private void updateFilter(ChipViewBinding v, int id, String filterId) {
        Filter filter = getCurrent(filterId);
        Log.d(TAG, "updateFilter: before " + filter);
        if (filter != null) {
            if (filter.getChoiceMode() == ChoiceMode.SINGLE) {
                for (FilterElement element : filter) {
                    setStatus(element.getBinding(), false);
                    element.setChecked(false);
                }
                for (FilterElement element : filter) {
                    if (element.getId() == id) {
                        element.setChecked(true);
                        setStatus(element.getBinding(), element.isChecked());
                        break;
                    }
                }
                filter.getElementClick().onClick(v.getRoot(), id);
            } else {
                for (FilterElement element : filter) {
                    if (id == 0) {
                        setStatus(element.getBinding(), false);
                        element.setChecked(false);
                    } else {
                        if (element.getId() == 0) {
                            setStatus(element.getBinding(), false);
                            element.setChecked(false);
                        }
                    }
                }
                Log.d(TAG, "updateFilter: middle " + filter);
                for (FilterElement element : filter) {
                    if (element.getId() == id) {
                        element.setChecked(!element.isChecked());
                        setStatus(element.getBinding(), element.isChecked());
                        break;
                    }
                }
                Log.d(TAG, "updateFilter: " + filter);
                filter.getElementClick().onMultipleSelected(v.getRoot(), getSelected(filter));
            }
        }
    }

    private List<Integer> getSelected(Filter filter) {
        List<Integer> list = new ArrayList<>();
        for (FilterElement element : filter) if (element.isChecked()) list.add(element.getId());
        return list;
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
        private String uuId = UUID.randomUUID().toString();

        public Filter(@NonNull FilterElementClick elementClick) {
            this.elementClick = elementClick;
        }

        String getUuId() {
            return uuId;
        }

        ChoiceMode getChoiceMode() {
            return choiceMode;
        }

        public void setChoiceMode(ChoiceMode choiceMode) {
            this.choiceMode = choiceMode;
        }

        @NonNull
        FilterElementClick getElementClick() {
            return elementClick;
        }

        @Override
        public FilterElement get(int index) {
            return elements.get(index);
        }

        @Override
        public void clear() {
            elements.clear();
        }

        @Override
        public boolean addAll(@NonNull Collection<? extends FilterElement> c) {
            return elements.addAll(c);
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
        private final int iconId;
        private String title;
        private int id;
        private boolean isChecked;
        private ChipViewBinding binding;

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

        public void setBinding(ChipViewBinding binding) {
            this.binding = binding;
        }

        public ChipViewBinding getBinding() {
            return binding;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public boolean isChecked() {
            return isChecked;
        }

        @DrawableRes
        int getIconId() {
            return iconId;
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

        @Override
        public String toString() {
            return "FilterElement{" +
                    "title='" + title + '\'' +
                    ", id=" + id +
                    ", isChecked=" + isChecked +
                    '}';
        }
    }

    public enum ChoiceMode {
        SINGLE,
        MULTI
    }
}
