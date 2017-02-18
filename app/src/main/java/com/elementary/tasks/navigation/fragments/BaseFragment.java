package com.elementary.tasks.navigation.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Prefs;
import com.elementary.tasks.navigation.FragmentCallback;

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

public abstract class BaseFragment extends Fragment {

    protected Context mContext;
    protected FragmentCallback mCallback;
    protected Prefs mPrefs;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = context;
        }
        if (mCallback == null) {
            try {
                mCallback = (FragmentCallback) context;
            } catch (ClassCastException e) {}
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mCallback == null) {
            try {
                mCallback = (FragmentCallback) activity;
            } catch (ClassCastException e) {}
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = Prefs.getInstance(mContext);
    }

    protected void replaceFragment(Fragment fragment, String title) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.main_container, fragment, title);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(title);
        ft.commit();
        if (mCallback != null) {
            mCallback.onTitleChange(title);
            mCallback.onFragmentSelect(fragment);
        }
    }
}
