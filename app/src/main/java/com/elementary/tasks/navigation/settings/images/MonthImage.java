package com.elementary.tasks.navigation.settings.images;

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

public class MonthImage {
    private long[] photos = new long[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    public MonthImage() {}

    public MonthImage(long[] photos) {
        this.photos = photos;
    }

    public long[] getPhotos() {
        return photos;
    }

    public void setPhotos(long[] photos) {
        this.photos = photos;
    }

    public void setPhoto(int month, long value) {
        this.photos[month] = value;
    }
}
