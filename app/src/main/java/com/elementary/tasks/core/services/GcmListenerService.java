package com.elementary.tasks.core.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.LogUtil;
import com.elementary.tasks.core.utils.Module;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class GcmListenerService extends FirebaseMessagingService {

    public static final String TOPIC_NAME = "updates";
    private static final String VERSION_NAME = "version";
    private static final String CHANGES = "changes";
    private static final String TYPE = "type";
    private static final String TAG = "GcmListenerService";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String type = getValue(message.getData(), TYPE);
        String version = getValue(message.getData(), VERSION_NAME);
        String changes = getValue(message.getData(), CHANGES);
        LogUtil.d(TAG, "onMessageReceived: " + type + ", " + version);
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (!pInfo.versionName.equalsIgnoreCase(version)) {
                showUpdateNotification(getApplicationContext(), version, changes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getValue(Map<String, String> data, String key) {
        if (data.containsKey(key)) return data.get(key);
        return null;
    }

    private void showUpdateNotification(Context context, String versionName, String changes) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent intent = PendingIntent.getActivity(this, 24242, goToMarket, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(getString(R.string.update_available) + " -> " + versionName);
        builder.setPriority(5);
        builder.setAutoCancel(true);
        builder.setContentIntent(intent);
        if (!TextUtils.isEmpty(changes)) {
            builder.setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(changes));
        }
        if (Module.isPro()) {
            builder.setContentText(context.getString(R.string.app_name_pro));
        } else builder.setContentText(context.getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.ic_shop_white_24dp);
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(context);
        mNotifyMgr.notify(24242, builder.build());
    }
}
