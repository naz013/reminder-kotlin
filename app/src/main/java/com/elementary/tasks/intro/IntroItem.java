package com.elementary.tasks.intro;

import androidx.annotation.DrawableRes;

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

class IntroItem {

    private String title;
    private String description;
    @DrawableRes
    private int[] images;

    IntroItem(String title, String description, @DrawableRes int... images) {
        this.title = title;
        this.description = description;
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @DrawableRes
    public int[] getImages() {
        return images;
    }

    public void setImages(@DrawableRes int[] images) {
        this.images = new int[images.length];
        System.arraycopy(images, 0, this.images, 0, this.images.length);
    }
}
