package com.elementary.tasks.backups;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.SuperUtil;

import java.io.File;
import java.io.IOException;

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

public class DeleteAsync extends AsyncTask<File, Void, Integer> {

    private Context mContext;
    private ProgressDialog progressDialog;
    private DeleteCallback listener;

    private UserInfoAsync.Info type;

    public DeleteAsync(Context context, DeleteCallback listener, UserInfoAsync.Info type) {
        this.mContext = context;
        this.listener = listener;
        this.type = type;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.deleting), false);
    }

    @Override
    protected Integer doInBackground(File... params) {
        int res = 0;
        if (type == UserInfoAsync.Info.Dropbox) {
            Dropbox dbx = new Dropbox(mContext);
            dbx.startSession();
            boolean isLinked = dbx.isLinked();
            boolean isConnected = SuperUtil.isConnected(mContext);
            for (File file : params) {
                if (file == null) {
                    continue;
                }
                if (!file.exists()) {
                    continue;
                }
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files == null) {
                        continue;
                    }
                    for (File f : files) {
                        f.delete();
                    }
                    res = 2;
                } else {
                    if (file.delete()) {
                        res = 1;
                    }
                }
            }
            if (isLinked && isConnected) {
                dbx.cleanFolder();
            }
        } else if (type == UserInfoAsync.Info.Google) {
            Google gdx = Google.getInstance(mContext);
            boolean isLinked = gdx != null;
            boolean isConnected = SuperUtil.isConnected(mContext);
            for (File file : params) {
                if (file == null) {
                    continue;
                }
                if (!file.exists()) {
                    continue;
                }
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files == null) {
                        continue;
                    }
                    for (File f : files) {
                        f.delete();
                    }
                    res = 2;
                } else {
                    if (file.delete()) {
                        res = 1;
                    }
                }
            }
            if (isLinked && isConnected && gdx.getDrive() != null) {
                try {
                    gdx.getDrive().cleanFolder();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (type == UserInfoAsync.Info.Local) {
            for (File file : params) {
                if (file == null) {
                    continue;
                }
                if (!file.exists()) {
                    continue;
                }
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files == null) {
                        continue;
                    }
                    for (File f : files) {
                        f.delete();
                    }
                    res = 2;
                } else {
                    if (file.delete()) {
                        res = 1;
                    }
                }
            }
        }
        return res;
    }

    @Override
    protected void onPostExecute(Integer aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (mContext != null) {
            Toast.makeText(mContext, R.string.all_files_removed, Toast.LENGTH_SHORT).show();
        }
        if (listener != null && mContext != null) {
            listener.onFinish();
        }
    }

    public interface DeleteCallback {
        void onFinish();
    }
}
