package com.cray.software.justreminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.backdoor.shared.SharedConst;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class WearService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {
    private static final String TAG = "WearService";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        Log.d(TAG, "Create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Log.d(TAG, "Destroy");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "Data received");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(SharedConst.WEAR_REMINDER) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    startActivity(new Intent(getApplicationContext(), ReminderActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Const.INTENT_TEXT, dataMap.getString(SharedConst.KEY_TASK))
                            .putExtra(Const.INTENT_TIMED, dataMap.getBoolean(SharedConst.KEY_TIMED))
                            .putExtra(Const.INTENT_REPEAT, dataMap.getBoolean(SharedConst.KEY_REPEAT))
                            .putExtra(Const.INTENT_THEME, dataMap.getBoolean(SharedConst.KEY_THEME))
                            .putExtra(Const.INTENT_COLOR, dataMap.getInt(SharedConst.KEY_COLOR))
                            .putExtra(Const.INTENT_TYPE, dataMap.getString(SharedConst.KEY_TYPE)));
                } else if (item.getUri().getPath().compareTo(SharedConst.WEAR_BIRTHDAY) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    startActivity(new Intent(getApplicationContext(), BirthdayActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Const.INTENT_TEXT, dataMap.getString(SharedConst.KEY_TASK))
                            .putExtra(Const.INTENT_THEME, dataMap.getBoolean(SharedConst.KEY_THEME))
                            .putExtra(Const.INTENT_COLOR, dataMap.getInt(SharedConst.KEY_COLOR)));
                } else if (item.getUri().getPath().compareTo(SharedConst.WEAR_STOP) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    if (dataMap.getBoolean(SharedConst.KEY_STOP)) {
                        Intent intent = new Intent("finish_activity");
                        sendBroadcast(intent);
                    } else if (dataMap.getBoolean(SharedConst.KEY_STOP_B)) {
                        Intent intent = new Intent("finish_birthday");
                        sendBroadcast(intent);
                    }
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "connected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connection failed \n" + connectionResult.getErrorMessage());
    }
}
