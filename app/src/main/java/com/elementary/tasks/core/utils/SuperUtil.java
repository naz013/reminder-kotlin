package com.elementary.tasks.core.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.widget.Toast;

import com.backdoor.engine.ObjectUtil;
import com.elementary.tasks.R;
import com.elementary.tasks.core.app_widgets.voice_control.VoiceWidgetDialog;
import com.elementary.tasks.core.contacts.ContactsActivity;
import com.elementary.tasks.core.interfaces.LCAMListener;
import com.elementary.tasks.core.services.GeolocationService;
import com.elementary.tasks.creators.fragments.ReminderInterface;
import com.elementary.tasks.voice.ConversationActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

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

    public static void stopService(Context context, Class clazz) {
        context.stopService(new Intent(context, clazz));
    }

    public static void startGpsTracking(Context context) {
        if (SuperUtil.isServiceRunning(context, GeolocationService.class)) {
            return;
        }
        Intent intent = new Intent(context, GeolocationService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Module.isO()) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static boolean isHeadsetUsing(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return manager.isBluetoothA2dpOn() || manager.isWiredHeadsetOn();
    }

    public static String getString(Fragment fragment, int id) {
        if (fragment.isAdded()) {
            return fragment.getString(id);
        } else return "";
    }

    public static boolean isDoNotDisturbEnabled(Context context) {
        if (!Module.isMarshmallow()) {
            return false;
        }
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_NONE) {
            LogUtil.d(TAG, "isDoNotDisturbEnabled: true");
            return true;
        } else {
            LogUtil.d(TAG, "isDoNotDisturbEnabled: false");
            return false;
        }
    }

    public static boolean checkNotificationPermission(Context activity) {
        NotificationManager notificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        return !(Module.isMarshmallow() && !notificationManager.isNotificationPolicyAccessGranted());
    }

    public static void askNotificationPermission(Activity activity) {
        if (Module.isMarshmallow()) {
            AlertDialog.Builder builder = Dialogues.getDialog(activity);
            builder.setMessage(R.string.for_correct_work_of_application);
            builder.setPositiveButton(R.string.grant, (dialog, which) -> {
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                activity.startActivity(intent);
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            builder.create().show();
        }
    }

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

    @NonNull
    public static String getObjectPrint(@NonNull Object o, Class<?> clazz) {
        return ObjectUtil.getObjectPrint(o, clazz);
    }

    public static void selectContact(final Activity activity, final int requestCode) {
        activity.startActivityForResult(new Intent(activity, ContactsActivity.class), requestCode);
    }

    public static String getAddress(double currentLat, double currentLong) {
        return String.format(Locale.getDefault(), "%.5f, %.5f", currentLat, currentLong);
    }

    public static boolean isGooglePlayServicesAvailable(Activity a) {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(a);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static boolean checkGooglePlayServicesAvailability(Activity a) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(a);
        LogUtil.d(TAG, "Result is: " + result);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(a, result, 69).show();
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void showLCAM(Context context, @Nullable LCAMListener listener, String... actions) {
        AlertDialog.Builder builder = Dialogues.getDialog(context);
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
        } else {
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
