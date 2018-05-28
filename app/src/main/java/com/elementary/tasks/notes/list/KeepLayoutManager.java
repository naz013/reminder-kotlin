package com.elementary.tasks.notes.list;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
class KeepLayoutManager extends GridLayoutManager {

    private RecyclerView.Adapter mAdapter;

    KeepLayoutManager(Context context, int spanCount, RecyclerView.Adapter adapter) {
        super(context, spanCount);
        this.mAdapter = adapter;
        init();
    }

    private void init() {
        setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int size = mAdapter.getItemCount();
                switch (size % 3) {
                    case 1:
                        if (position == 0) {
                            return 6;
                        } else {
                            return 2;
                        }
                    case 2:
                        if (position < 2) {
                            return 3;
                        } else {
                            return 2;
                        }
                    default:
                        return 2;
                }
            }
        });
    }
}
