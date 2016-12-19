package com.elementary.tasks.notes;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.elementary.tasks.R;
import com.elementary.tasks.core.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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

public class DecodeImagesAsync extends AsyncTask<ClipData, Integer, List<NoteImage>>{

    private Context mContext;
    private DecodeListener mCallback;
    private int max;
    private ProgressDialog mDialog;

    public DecodeImagesAsync(Context context, DecodeListener listener, int max) {
        this.mContext = context;
        this.mCallback = listener;
        this.max = max;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = new ProgressDialog(mContext);
        mDialog.setTitle(R.string.please_wait);
        mDialog.setMessage(mContext.getString(R.string.decoding_images));
        mDialog.setCancelable(false);
        if (max > 1) {
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setMax(max);
            mDialog.setIndeterminate(false);
            mDialog.setProgress(1);
        } else {
            mDialog.setIndeterminate(false);
        }
        mDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.setProgress(values[0]);
        }
    }

    @Override
    protected List<NoteImage> doInBackground(ClipData... clipDatas) {
        List<NoteImage> list = new ArrayList<>();
        ClipData mClipData = clipDatas[0];
        for (int i = 0; i < mClipData.getItemCount(); i++) {
            publishProgress(i + 1);
            ClipData.Item item = mClipData.getItemAt(i);
            addImageFromUri(list, item.getUri());
        }
        return list;
    }

    private void addImageFromUri(List<NoteImage> images, Uri uri) {
        if (uri == null) return;
        Bitmap bitmapImage = null;
        try {
            bitmapImage = BitmapUtils.decodeUriToBitmap(mContext, uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bitmapImage != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            images.add(new NoteImage(outputStream.toByteArray()));
        }
    }

    @Override
    protected void onPostExecute(List<NoteImage> noteImages) {
        super.onPostExecute(noteImages);
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
        if (mCallback != null) mCallback.onDecode(noteImages);
    }

    public interface DecodeListener {
        void onDecode(List<NoteImage> result);
    }
}
