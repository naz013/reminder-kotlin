package com.elementary.tasks.core.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.Constants;

import java.util.Locale;

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

public class VoiceHelpDialog extends BaseDialog {

    private DialogInterface.OnCancelListener mCancelListener = dialogInterface -> finish();
    private DialogInterface.OnDismissListener mOnDismissListener = dialogInterface -> finish();

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.help));
        WebView wv = new WebView(this);
        wv.setBackgroundColor(themeUtil.getBackgroundStyle());
        String localeCheck = Locale.getDefault().toString().toLowerCase();
        String url;
        if (localeCheck.startsWith("uk")) {
            url = Constants.WEB_URL + "voice_help/voice_uk.html";
        } else if (localeCheck.startsWith("ru")) {
            url = Constants.WEB_URL + "voice_help/voice_ru.html";
        } else {
            url = Constants.WEB_URL + "voice_help/voice_en.html";
        }
        wv.loadUrl(url);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                view.loadUrl(url);
                return true;
            }
        });
        alert.setView(wv);
        alert.setCancelable(true);
        alert.setNegativeButton(R.string.ok, (dialog, id) -> {
            dialog.dismiss();
            finish();
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.setOnCancelListener(mCancelListener);
        alertDialog.setOnDismissListener(mOnDismissListener);
        alertDialog.show();
    }
}
