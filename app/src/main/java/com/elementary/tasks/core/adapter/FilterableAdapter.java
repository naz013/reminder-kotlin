package com.elementary.tasks.core.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
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

public abstract class FilterableAdapter<V, Q, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private List<V> originalData = new ArrayList<>();
    private List<V> usedData = new ArrayList<>();
    private Filter<V, Q> filter;
    private Q lastQuery;

    public FilterableAdapter(List<V> data, Filter<V, Q> filter) {
        this.originalData = data;
        this.usedData = new ArrayList<>(data);
        this.filter = filter;
    }

    public List<V> getUsedData() {
        return usedData;
    }

    public V getItem(int position) {
        return usedData.get(position);
    }

    public void addItem(V v) {
        originalData.add(v);
        if (filter != null) {
            if (lastQuery != null) {
                if (filter.filter(v, lastQuery)) {
                    addToList(v);
                }
            } else {
                addToList(v);
            }
        } else {
            notifyItemInserted(originalData.size() - 1);
            notifyItemRangeChanged(0, originalData.size());
        }
    }

    private void addToList(V v) {
        usedData.add(v);
        notifyItemInserted(usedData.size() - 1);
        notifyItemRangeChanged(0, usedData.size());
    }

    public void addItem(int position, V v) {
        if (position == originalData.size()) {
            addItem(v);
            return;
        }
        V current = originalData.get(position);
        originalData.add(position, v);
        if (filter != null) {
            if (lastQuery != null) {
                if (filter.filter(v, lastQuery)) {
                    int index = usedData.indexOf(current);
                    addToList(index, v);
                }
            } else {
                int index = usedData.indexOf(current);
                addToList(index, v);
            }
        } else {
            notifyItemInserted(position);
            notifyItemRangeChanged(0, originalData.size());
        }
    }

    private void addToList(int position, V v) {
        usedData.add(position, v);
        notifyItemInserted(position);
        notifyItemRangeChanged(0, usedData.size());
    }

    public void filter(Q q) {
        this.lastQuery = q;
        if (filter != null) {
            List<V> res = getFiltered(q);
            animateTo(res);
            filter.onFilterEnd(res, res.size(), q);
        }
    }

    @Nullable
    public V removeItem(int position) {
        if (position < usedData.size()) {
            V v = usedData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(0, usedData.size());
            int index = originalData.indexOf(v);
            if (index != -1) {
                originalData.remove(index);
            }
            return v;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return usedData.size();
    }

    private List<V> getFiltered(Q query) {
        if (query == null) {
            return originalData;
        }
        List<V> list = new ArrayList<>();
        for (V model : originalData) {
            if (filter.filter(model, query)) {
                list.add(model);
            }
        }
        return list;
    }

    private V remove(int position) {
        V model = usedData.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void add(int position, V model) {
        usedData.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        V model = usedData.remove(fromPosition);
        usedData.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    private void animateTo(List<V> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<V> newModels) {
        for (int i = usedData.size() - 1; i >= 0; i--) {
            V model = usedData.get(i);
            if (!newModels.contains(model)) {
                remove(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<V> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            V model = newModels.get(i);
            if (!usedData.contains(model)) {
                add(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<V> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            V model = newModels.get(toPosition);
            final int fromPosition = usedData.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public interface Filter<V, Q> {
        boolean filter(V v, Q query);
        void onFilterEnd(List<V> list, int size, Q query);
    }
}
