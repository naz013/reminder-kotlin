package com.elementary.tasks.navigation.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Module;

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

public class FeedbackFragment extends BaseWebViewFragment {

    @Override
    protected String getUrl() {
        return "https://docs.google.com/forms/d/1vOCBU-izJBQ8VAsA1zYtfHFxe9Q1-Qm9rp_pYG13B1s/viewform";
    }

    @Override
    protected void setExtraParams(WebView webView) {
        super.setExtraParams(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.contains("https://bitbucket.org/nazar_suhovich/just-reminder/issues?status=new&status=open")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getCallback() != null) {
            getCallback().onTitleChange(getString(R.string.feedback));
            getCallback().onFragmentSelect(this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_feedback, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                getWebView().reload();
                return true;
            case R.id.action_forward:
                if (getWebView().canGoForward()) {
                    getWebView().goForward();
                }
                return true;
            case R.id.action_back:
                if (getWebView().canGoBack()) {
                    getWebView().goBack();
                }
                return true;
            case R.id.action_email:
                sendEmail();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"feedback.cray@gmail.com"});
        if (Module.isPro()) {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder PRO");
        } else {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder");
        }
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
