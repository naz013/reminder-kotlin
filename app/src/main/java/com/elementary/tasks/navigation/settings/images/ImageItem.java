package com.elementary.tasks.navigation.settings.images;

import android.databinding.Bindable;
import android.databinding.Observable;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

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
@RealmClass
public class ImageItem implements RealmModel, Observable {
    @SerializedName("format")
    private String format;
    @SerializedName("width")
    private int width;
    @SerializedName("height")
    private int height;
    @SerializedName("filename")
    private String filename;
    @SerializedName("id")
    @PrimaryKey
    private long id;
    @SerializedName("author")
    private String author;
    @SerializedName("author_url")
    private String authorUrl;
    @SerializedName("post_url")
    private String postUrl;

    @Ignore
    private boolean selected;

    @Ignore
    private List<OnPropertyChangedCallback> mCallbacks = new LinkedList<>();

    public ImageItem() {}

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Bindable
    public boolean getSelected() {
        return selected;
    }

    @Bindable
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Bindable
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Bindable
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Bindable
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Bindable
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Bindable
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Bindable
    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    @Bindable
    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback onPropertyChangedCallback) {
        if (!mCallbacks.contains(onPropertyChangedCallback)) {
            mCallbacks.add(onPropertyChangedCallback);
        }
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback onPropertyChangedCallback) {
        if (mCallbacks.contains(onPropertyChangedCallback)) {
            mCallbacks.remove(onPropertyChangedCallback);
        }
    }
}
