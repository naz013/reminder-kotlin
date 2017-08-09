package com.elementary.tasks.core.utils;

import android.content.Context;
import android.support.annotation.Px;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.Dictionary;
import java.util.Hashtable;

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
public final class MeasureUtils {

    private static Dictionary<Integer, Integer> sRecyclerViewItemHeights = new Hashtable<>();

    private MeasureUtils() {
    }

    @Px
    public static int dp2px(Context context, int dp) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);

        return (int) (dp * displaymetrics.density + 0.5f);
    }

    public static int getScrollY(RecyclerView rv, int columnCount, boolean mIsGrid) {
        View c = rv.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition;
        if (mIsGrid) {
            try {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) rv.getLayoutManager();
                int[] pos = new int[4];
                layoutManager.findFirstVisibleItemPositions(pos);
                firstVisiblePosition = pos[0];
            } catch (ClassCastException e) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            }
        } else {
            try {
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv.getLayoutManager();
                firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            } catch (ClassCastException e) {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) rv.getLayoutManager();
                int[] pos = new int[4];
                layoutManager.findFirstVisibleItemPositions(pos);
                firstVisiblePosition = pos[0];
            }
        }
        int scrollY = -(c.getTop());
        if (columnCount > 1) {
            sRecyclerViewItemHeights.put(firstVisiblePosition, c.getHeight() + MeasureUtils.dp2px(rv.getContext(), 8) / columnCount);
        } else {
            sRecyclerViewItemHeights.put(firstVisiblePosition, c.getHeight());
        }
        if (scrollY < 0) {
            scrollY = 0;
        }
        for (int i = 0; i < firstVisiblePosition; ++i) {
            if (sRecyclerViewItemHeights.get(i) != null) {
                scrollY += sRecyclerViewItemHeights.get(i);
            }
        }
        return scrollY;
    }
}
