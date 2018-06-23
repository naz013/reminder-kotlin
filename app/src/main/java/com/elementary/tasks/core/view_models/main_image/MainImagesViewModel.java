package com.elementary.tasks.core.view_models.main_image;

import android.app.Application;

import com.elementary.tasks.core.data.models.MainImage;
import com.elementary.tasks.core.view_models.BaseDbViewModel;
import com.elementary.tasks.core.view_models.Commands;

import java.util.List;

import androidx.lifecycle.LiveData;

/**
 * Copyright 2018 Nazar Suhovich
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
public class MainImagesViewModel extends BaseDbViewModel {

    public LiveData<List<MainImage>> images;

    public MainImagesViewModel(Application application) {
        super(application);
        images = getAppDb().mainImagesDao().loadAll();
    }

    public void saveImages(List<MainImage> mainImages) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().mainImagesDao().insertAll(mainImages);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
        });
    }
}
