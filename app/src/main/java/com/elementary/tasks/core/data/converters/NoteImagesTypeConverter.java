package com.elementary.tasks.core.data.converters;

import com.elementary.tasks.notes.create.NoteImage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import androidx.room.TypeConverter;

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
public class NoteImagesTypeConverter {

    @TypeConverter
    public String toJson(List<NoteImage> list) {
        return new Gson().toJson(list);
    }

    @TypeConverter
    public List<NoteImage> toList(String json) {
        return new Gson().fromJson(json, new TypeToken<List<NoteImage>>(){}.getType());
    }
}
