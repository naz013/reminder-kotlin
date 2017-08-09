package com.elementary.tasks.core.async;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.elementary.tasks.core.utils.LogUtil;

import java.io.IOException;
import java.util.List;

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

public class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

    private static final String TAG = "GeocoderTask";

    private Context mContext;
    private GeocoderListener mListener;

    public GeocoderTask(Context mContext, GeocoderListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @Override
    protected List<Address> doInBackground(String... locationName) {
        // Creating an instance of Geocoder class
        Geocoder geocoder = new Geocoder(mContext);
        List<Address> addresses = null;

        try {
            // Getting a maximum of 3 Address that matches the input text
            addresses = geocoder.getFromLocationName(locationName[0], 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) {
        if (addresses == null || addresses.size() == 0) {
            LogUtil.d(TAG, "No Location found");
        } else {
            if (mListener != null) {
                mListener.onAddressReceived(addresses);
            }
        }
    }

    /**
     * Listener for found places list.
     */
    public interface GeocoderListener {
        void onAddressReceived(List<Address> addresses);
    }
}
