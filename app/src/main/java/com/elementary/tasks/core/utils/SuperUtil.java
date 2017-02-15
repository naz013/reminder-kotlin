package com.elementary.tasks.core.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.widget.Toast;

import com.backdoor.simpleai.ObjectUtil;
import com.elementary.tasks.R;
import com.elementary.tasks.core.app_widgets.voice_control.VoiceWidgetDialog;
import com.elementary.tasks.core.contacts.ContactsActivity;
import com.elementary.tasks.creators.fragments.ReminderInterface;
import com.elementary.tasks.voice.ConversationActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

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

public class SuperUtil {

    private static final String TAG = "SuperUtil";

    public static boolean checkLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return !(!isGPSEnabled && !isNetworkEnabled);
    }

    public static void showLocationAlert(final Context context, ReminderInterface callbacks) {
        callbacks.showSnackbar(context.getString(R.string.gps_not_enabled), context.getString(R.string.action_settings), v -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        });
    }

    public static String getObjectPrint(Object o, Class<?> clazz) {
        return ObjectUtil.getObjectPrint(o, clazz);
    }

    public static void selectContact(final Activity activity, final int requestCode) {
        activity.startActivityForResult(new Intent(activity, ContactsActivity.class), requestCode);
    }

    public static String getAddress(double currentLat, double currentLong) {
        return String.format(Locale.getDefault(), "%.5f, %.5f", currentLat, currentLong);
    }

    public static boolean isGooglePlayServicesAvailable(Activity a) {
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(a.getApplicationContext());
            return resultCode == ConnectionResult.SUCCESS;
        } catch (NoSuchMethodError e) {
            return false;
        }
    }

    public static boolean checkGooglePlayServicesAvailability(Activity a) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(a.getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, a, 69);
            dialog.setCancelable(false);
            dialog.setOnDismissListener(DialogInterface::dismiss);
            dialog.show();
            return false;
        } else {
            LogUtil.d(TAG, "Result is: " + resultCode);
            return true;
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void showLCAM(Context context, final LCAMListener listener, String... actions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(actions, (dialog, item) -> {
            dialog.dismiss();
            if (listener != null) listener.onAction(item);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String appendString(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            if (string != null) {
                stringBuilder.append(string);
            }
        }
        return stringBuilder.toString();
    }

    public static long getAfterTime(String timeString) {
        if (timeString.length() == 6 && !timeString.matches("000000")) {
            String hours = timeString.substring(0, 2);
            String minutes = timeString.substring(2, 4);
            String seconds = timeString.substring(4, 6);
            int hour = Integer.parseInt(hours);
            int minute = Integer.parseInt(minutes);
            int sec = Integer.parseInt(seconds);
            long s = 1000;
            long m = s * 60;
            long h = m * 60;
            return (hour * h) + (minute * m) + (sec * s);
        } else return 0;
    }

    public static void startVoiceRecognitionActivity(Activity activity, int requestCode, boolean isLive) {
        Intent intent;
        if (isLive) {
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.say_something));
        } else if (Prefs.getInstance(activity).isLiveEnabled()) {
            if (activity instanceof VoiceWidgetDialog) activity.finish();
            intent = new Intent(activity, ConversationActivity.class);
        }else {
            intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Language.getLanguage(Prefs.getInstance(activity).getVoiceLocale()));
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, activity.getString(R.string.say_something));
        }
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, activity.getString(R.string.no_recognizer_found), Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public static void installSkype(Context context) {
        Uri marketUri = Uri.parse("market://details?id=com.skype.raider");
        Intent intent = new Intent(Intent.ACTION_VIEW, marketUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean isSkypeClientInstalled(Context context) {
        PackageManager myPackageMgr = context.getPackageManager();
        try {
            myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            return (false);
        }
        return (true);
    }

    public static String decrypt(String string) {
        String result = "";
        byte[] byte_string = Base64.decode(string, Base64.DEFAULT);
        try {
            result = new String(byte_string, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    public static String encrypt(String string) {
        byte[] string_byted = null;
        try {
            string_byted = string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(string_byted, Base64.DEFAULT).trim();
    }

    public static void launchMarket(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.could_not_launch_market), Toast.LENGTH_SHORT).show();
        }
    }
}
