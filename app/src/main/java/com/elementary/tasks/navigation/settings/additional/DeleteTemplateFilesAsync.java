package com.elementary.tasks.navigation.settings.additional;

import android.content.Context;
import android.os.AsyncTask;

import com.elementary.tasks.core.cloud.Dropbox;
import com.elementary.tasks.core.cloud.FileConfig;
import com.elementary.tasks.core.cloud.Google;
import com.elementary.tasks.core.utils.MemoryUtil;
import com.elementary.tasks.core.utils.SuperUtil;

import java.io.File;
import java.io.IOException;

/**
 * Copyright 2017 Nazar Suhovich
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

public class DeleteTemplateFilesAsync extends AsyncTask<String, Void, Void> {

    private Context mContext;

    public DeleteTemplateFilesAsync(Context context) {
        this.mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        for (String uid : params) {
            String exportFileName = uid + FileConfig.FILE_NAME_TEMPLATE;
            File dir = MemoryUtil.getTemplatesDir();
            if (dir != null) {
                File file = new File(dir, exportFileName);
                if (file.exists()) file.delete();
            }
            dir = MemoryUtil.getDropboxTemplatesDir();
            if (dir != null) {
                File file = new File(dir, exportFileName);
                if (file.exists()) file.delete();
            }
            dir = MemoryUtil.getGoogleTemplatesDir();
            if (dir != null) {
                File file = new File(dir, exportFileName);
                if (file.exists()) file.delete();
            }
            boolean isConnected = SuperUtil.isConnected(mContext);
            if (isConnected){
                new Dropbox(mContext).deleteTemplate(exportFileName);
                Google google = Google.getInstance(mContext);
                if (google != null && google.getDrive() != null) {
                    try {
                        google.getDrive().deleteTemplateFileByName(exportFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
