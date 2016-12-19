package com.cray.software.justreminder;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.wearable.view.CircularButton;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.backdoor.shared.SharedConst;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class BirthdayActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WearReminder";
    private GoogleApiClient mGoogleApiClient;

    BroadcastReceiver broadcast_reciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_birthday")) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final String task = intent.getStringExtra(Const.INTENT_TEXT);
        final boolean dark = intent.getBooleanExtra(Const.INTENT_THEME, false);
        final int color = intent.getIntExtra(Const.INTENT_COLOR, 0);

        if (dark) {
            setTheme(R.style.HomeDark);
        } else {
            setTheme(R.style.HomeWhite);
        }
        setContentView(R.layout.activity_reminder);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                TextView mTextView = (TextView) stub.findViewById(R.id.text);

                CircularButton buttonOk = (CircularButton) findViewById(R.id.buttonOk);
                CircularButton buttonCall = (CircularButton) findViewById(R.id.buttonCall);
                CircularButton buttonNotification = (CircularButton) findViewById(R.id.buttonNotification);
                CircularButton buttonCancel = (CircularButton) findViewById(R.id.buttonCancel);
                CircularButton buttonSnooze = (CircularButton) findViewById(R.id.buttonSnooze);
                buttonSnooze.setVisibility(View.GONE);
                buttonNotification.setVisibility(View.GONE);

                mTextView.setText(task);
                buttonCancel.setVisibility(View.VISIBLE);
                buttonCancel.setImageResource(R.drawable.ic_send_black_24dp);

                buttonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendRequest(SharedConst.KEYCODE_OK);
                    }
                });

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendRequest(SharedConst.KEYCODE_MESSAGE);
                    }
                });
                buttonCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendRequest(SharedConst.KEYCODE_CALL);
                    }
                });
                Resources res = getResources();
                if (dark) mTextView.setTextColor(res.getColor(R.color.whitePrimary));
                else mTextView.setTextColor(res.getColor(R.color.blackPrimary));
                int colored = res.getColor(ColorUtil.colorAccent(dark, color));
                changeColor(colored, buttonCall, buttonCancel, buttonNotification, buttonOk, buttonSnooze);

            }
        });

        Log.d(TAG, "On service create");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void changeColor(int color, CircularButton... buttons) {
        for (CircularButton button : buttons) {
            button.setColor(color);
        }
    }

    private void sendRequest(int keyCode) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.PHONE_BIRTHDAY);
        putDataMapReq.getDataMap().putInt(SharedConst.REQUEST_KEY, keyCode);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        Log.d(TAG, "Data sent");

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        registerReceiver(broadcast_reciever, new IntentFilter("finish_birthday"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
        unregisterReceiver(broadcast_reciever);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "On connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "On connection suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "On connection failed");
    }
}
