package com.elementary.tasks.backups;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;

import com.elementary.tasks.R;
import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.Module;
import com.elementary.tasks.core.utils.SuperUtil;

import java.io.File;
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

public class UserInfoAsync extends AsyncTask<UserInfoAsync.Info, Integer, List<UserItem>> {

    public enum Info {
        Dropbox, Google, Local
    }

    private Context mContext;
    private ProgressDialog mDialog;
    private DataListener listener;
    private int count;

    public UserInfoAsync(Context context, DataListener listener, int count){
        this.mContext = context;
        this.listener = listener;
        this.count = count;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getString(R.string.retrieving_data));
        mDialog.setCancelable(false);
        if (count > 1) {
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setMax(count);
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
    protected List<UserItem> doInBackground(Info... infos) {
        List<UserItem> list = new ArrayList<>();
        for (int i = 0; i < infos.length; i++) {
            Info info = infos[i];
            if (info == Info.Dropbox) {
                addDropboxData(list);
            } else if (info == Info.Google) {
                addGoogleData(list);
            } else if (info == Info.Local) {
                addLocalData(list);
            }
            publishProgress(i + 1);
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<UserItem> list) {
        super.onPostExecute(list);
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        if (listener != null && mContext != null) {
            listener.onReceive(list);
        }
    }

    private void addLocalData(List<UserItem> list) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize;
        long totalBlocks;
        long availableBlocks;
        if (Module.isJellyMR2()) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
            availableBlocks = stat.getAvailableBlocksLong();
        } else {
            blockSize = stat.getBlockSize();
            totalBlocks = stat.getBlockCount();
            availableBlocks = stat.getBlockCount();
        }
        long totalSize = blockSize * totalBlocks;
        UserItem userItem = new UserItem();
        userItem.setQuota(totalSize);
        userItem.setUsed(totalSize - (availableBlocks * blockSize));
        userItem.setKind(Info.Local);
        getCountFiles(userItem);
        list.add(userItem);
    }

    private void addDropboxData(List<UserItem> list) {
        Dropbox dbx = new Dropbox(mContext);
        dbx.startSession();
        if (dbx.isLinked() && SuperUtil.isConnected(mContext)) {
            long quota = dbx.userQuota();
            long quotaUsed = dbx.userQuotaNormal();
            String name = dbx.userName();
            long count = dbx.countFiles();
            UserItem userItem = new UserItem(name, quota, quotaUsed, count, null);
            userItem.setKind(Info.Dropbox);
            list.add(userItem);
        }
    }

    private void addGoogleData(List<UserItem> list) {
        Google gdx = Google.getInstance(mContext);
        if (gdx != null && SuperUtil.isConnected(mContext)) {
            UserItem userItem = gdx.getDrive().getData();
            if (userItem != null) {
                userItem.setKind(Info.Google);
                list.add(userItem);
            }
        }
    }

    private void getCountFiles(UserItem item) {
        int count = 0;
        File dir = MemoryUtil.getRemindersDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                count += files.length;
            }
        }
        dir = MemoryUtil.getNotesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                count += files.length;
            }
        }
        dir = MemoryUtil.getBirthdaysDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                count += files.length;
            }
        }
        dir = MemoryUtil.getGroupsDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                count += files.length;
            }
        }
        dir = MemoryUtil.getPlacesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                count += files.length;
            }
        }
        dir = MemoryUtil.getTemplatesDir();
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                count += files.length;
            }
        }
        item.setCount(count);
    }

    public interface DataListener {
        void onReceive(List<UserItem> result);
    }
}
