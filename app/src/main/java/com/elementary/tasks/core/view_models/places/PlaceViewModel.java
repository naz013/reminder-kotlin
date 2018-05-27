package com.elementary.tasks.core.view_models.places;

import android.app.Application;

import com.elementary.tasks.core.data.models.Place;
import com.elementary.tasks.core.view_models.Commands;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
public class PlaceViewModel extends BasePlacesViewModel {

    public LiveData<Place> place;

    private PlaceViewModel(Application application, String key) {
        super(application);
        place = getAppDb().placesDao().loadByKey(key);
    }

    public void savePlace(@NonNull Place place) {
        isInProgress.postValue(true);
        run(() -> {
            getAppDb().placesDao().insert(place);
            end(() -> {
                isInProgress.postValue(false);
                result.postValue(Commands.SAVED);
            });
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private Application application;
        private String key;

        public Factory(Application application, String key) {
            this.application = application;
            this.key = key;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new PlaceViewModel(application, key);
        }
    }
}
