package com.cray.software.justreminder;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.view.CircularButton;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.backdoor.shared.SharedConst;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                CircularButton buttonVoice = (CircularButton) findViewById(R.id.buttonVoice);
                buttonVoice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isConnected) {
                            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.PHONE_VOICE);
                            putDataMapReq.getDataMap().putInt(SharedConst.KEY_LANGUAGE, 1);
                            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
                        } else {
                            mGoogleApiClient.connect();
                        }
                    }
                });

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        startService(new Intent(this, WearService.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        //startActivity(new Intent(this, HelpActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("MAIN", "On connected");
        isConnected = true;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("MAIN", "On connection suspend");
        isConnected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("MAIN", "On connection failed");
        isConnected = false;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d("MAIN", "Data received");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo(SharedConst.WEAR_VOICE) == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    String language = dataMap.getString(SharedConst.KEY_LANGUAGE);
                    if (language != null) {
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
                        try {
                            startActivityForResult(intent, 105);
                        } catch (ActivityNotFoundException e){
                            Toast.makeText(getApplicationContext(), "No recognition engine found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 105 && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayList<String> res = new ArrayList<>();
            for (Object object : matches) {
                res.add(object.toString());
            }
            if (isConnected) {
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create(SharedConst.PHONE_VOICE_RES);
                putDataMapReq.getDataMap().putStringArrayList(SharedConst.KEY_VOICE_RES, res);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
            } else {
                mGoogleApiClient.connect();
            }
        }
    }
}
