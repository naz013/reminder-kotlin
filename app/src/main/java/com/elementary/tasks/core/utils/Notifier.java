package com.elementary.tasks.core.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.elementary.tasks.R;
import com.elementary.tasks.notes.NoteImage;
import com.elementary.tasks.notes.NoteItem;

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

public class Notifier {

    private Context mContext;

    public Notifier(Context context){
        this.mContext = context;
    }

    public static void hideNotification(Context context, int id) {
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(id);
    }

    public void showNoteNotification(NoteItem item){
        Prefs sPrefs = Prefs.getInstance(mContext);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentText(mContext.getString(R.string.note));
        if (Module.isLollipop()) {
            builder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
        }
        String content = item.getSummary();
        builder.setSmallIcon(R.drawable.ic_note_white);
        builder.setContentTitle(content);
        boolean isWear = sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION);
        if (isWear && Module.isJellyMR2()) {
            builder.setOnlyAlertOnce(true);
            builder.setGroup("GROUP");
            builder.setGroupSummary(true);
        }
        if (!item.getImages().isEmpty()) {
            NoteImage image = item.getImages().get(0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(image.getImage(), 0, image.getImage().length);
            builder.setLargeIcon(bitmap);
            NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle();
            s.bigLargeIcon(bitmap);
            s.bigPicture(bitmap);
            builder.setStyle(s);
        }
        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(mContext);
        mNotifyMgr.notify(item.getUniqueId(), builder.build());
        if (isWear && Module.isJellyMR2()){
            final NotificationCompat.Builder wearableNotificationBuilder = new NotificationCompat.Builder(mContext);
            wearableNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
            wearableNotificationBuilder.setContentTitle(content);
            wearableNotificationBuilder.setContentText(mContext.getString(R.string.note));
            wearableNotificationBuilder.setOngoing(false);
            if (Module.isLollipop()) {
                wearableNotificationBuilder.setColor(ViewUtils.getColor(mContext, R.color.bluePrimary));
            }
            wearableNotificationBuilder.setOnlyAlertOnce(true);
            wearableNotificationBuilder.setGroup("GROUP");
            wearableNotificationBuilder.setGroupSummary(false);
            mNotifyMgr.notify(item.getUniqueId(), wearableNotificationBuilder.build());
        }
    }
}
