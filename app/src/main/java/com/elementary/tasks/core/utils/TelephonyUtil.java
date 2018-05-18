package com.elementary.tasks.core.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;

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

public final class TelephonyUtil {

    private TelephonyUtil() {
    }

    public static void sendNote(@NonNull File file, Context context, @Nullable String message) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String title = "Note";
        String note = "";
        if (message != null) {
            if (message.length() > 100) {
                title = message.substring(0, 48);
                title = title + "...";
            }
            if (message.length() > 150) {
                note = message.substring(0, 135);
                note = note + "...";
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, note);
        Uri uri = UriUtil.getUri(context, file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    public static void sendMail(Context context, @NonNull String email, String subject,
                                String message, String filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (filePath != null) {
            Uri uri = UriUtil.getUri(context, filePath);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        context.startActivity(Intent.createChooser(intent, "Send email..."));
    }

    public static void sendSms(String number, Context context) {
        if (TextUtils.isEmpty(number)) {
            return;
        }
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:" + number));
        context.startActivity(smsIntent);
    }

    @SuppressWarnings("MissingPermission")
    public static void makeCall(String number, Context context) {
        if (TextUtils.isEmpty(number)) {
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));
        context.startActivity(callIntent);
    }

    public static void openApp(@NonNull String appPackage, Context context) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appPackage);
        try {
            context.startActivity(launchIntent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static void openLink(@NonNull String link, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        try {
            context.startActivity(browserIntent);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static void skypeCall(String number, Context context) {
        String uri = "skype:" + number + "?call";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }

    public static void skypeVideoCall(String number, Context context) {
        String uri = "skype:" + number + "?call&video=true";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }

    public static void skypeChat(String number, Context context) {
        String uri = "skype:" + number + "?chat";
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse(uri));
        context.startActivity(sky);
    }
}
