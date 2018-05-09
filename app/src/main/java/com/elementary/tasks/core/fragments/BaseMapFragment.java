package com.elementary.tasks.core.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;

import com.elementary.tasks.core.app_widgets.WidgetUtils;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.core.utils.ThemeUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MapStyleOptions;

import androidx.fragment.app.Fragment;

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
public abstract class BaseMapFragment extends Fragment {

    private Activity mContext;
    private ThemeUtil mColor;
    private Prefs mPrefs;

    private int mMapType = GoogleMap.MAP_TYPE_TERRAIN;

    public Activity getContext() {
        return mContext;
    }

    public Prefs getPrefs() {
        return mPrefs;
    }

    public ThemeUtil getThemeUtil() {
        return mColor;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
    }

    protected void setStyle(@NonNull GoogleMap map, int mapType) {
        mMapType = mapType;
        map.setMapStyle(null);
        if (mapType == 3) {
            boolean res = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    getActivity(), mColor.getMapStyleJson()));
            if (!res) {
                map.setMapType(mapType);
            }
        } else {
            map.setMapType(mapType);
        }
    }

    protected void setStyle(@NonNull GoogleMap map) {
        setStyle(map, mMapType);
    }

    protected void setMapType(@NonNull GoogleMap map, int type, @Nullable Function function) {
        setStyle(map, type);
        getPrefs().setMapType(type);
        if (function != null) {
            function.apply();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mColor = ThemeUtil.getInstance(mContext);
        mPrefs = Prefs.getInstance(mContext);
        mMapType = mPrefs.getMapType();
    }

    protected BitmapDescriptor getDescriptor(int resId) {
        if (Module.isLollipop()) {
            return getBitmapDescriptor(resId);
        } else {
            return getBDPreLollipop(resId);
        }
    }

    private float convertDpToPixel(float dp) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    @NonNull
    private BitmapDescriptor getBDPreLollipop(@DrawableRes int id) {
        return BitmapDescriptorFactory.fromBitmap(WidgetUtils.getIcon(getContext(), id));
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private BitmapDescriptor getBitmapDescriptor(@DrawableRes int id) {
        Drawable vectorDrawable = mContext.getDrawable(id);
        int h = ((int) convertDpToPixel(24));
        int w = ((int) convertDpToPixel(24));
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    interface Function {
        void apply();
    }
}
