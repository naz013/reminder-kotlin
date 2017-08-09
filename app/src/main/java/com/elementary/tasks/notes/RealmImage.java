package com.elementary.tasks.notes;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

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

public class RealmImage extends RealmObject {

    @SerializedName("image")
    private byte[] image;

    public RealmImage() {}

    public RealmImage(NoteImage image) {
        this.image = image.getImage();
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = new byte[image.length];
        System.arraycopy(image, 0, this.image, 0, this.image.length);
    }
}
