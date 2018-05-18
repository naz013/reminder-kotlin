package com.elementary.tasks.navigation.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.elementary.tasks.databinding.FragmentSettingsWebViewLayoutBinding;
import com.elementary.tasks.navigation.settings.BaseSettingsFragment;

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

public abstract class BaseWebViewFragment extends BaseSettingsFragment {

    private FragmentSettingsWebViewLayoutBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsWebViewLayoutBinding.inflate(inflater, container, false);
        setExtraParams(binding.webView);
        binding.webView.loadUrl(getUrl());
        return binding.getRoot();
    }

    protected WebView getWebView() {
        return binding.webView;
    }

    protected void setExtraParams(WebView webView) {

    }

    protected abstract String getUrl();
}
