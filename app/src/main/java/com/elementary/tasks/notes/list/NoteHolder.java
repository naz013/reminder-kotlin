package com.elementary.tasks.notes.list;

import android.view.View;

import com.elementary.tasks.core.data.models.Note;
import com.elementary.tasks.core.interfaces.ActionsListener;
import com.elementary.tasks.core.utils.ListActions;
import com.elementary.tasks.databinding.ListItemNoteBinding;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

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
public class NoteHolder extends RecyclerView.ViewHolder {

    private ListItemNoteBinding binding;

    public NoteHolder(View v, @Nullable ActionsListener<Note> listener) {
        super(v);
        binding = DataBindingUtil.bind(v);
        binding.noteClick.setOnClickListener(v12 -> {
            if (listener != null) {
                listener.onAction(v12, getAdapterPosition(), null, ListActions.OPEN);
            }
        });
        binding.noteClick.setOnLongClickListener(view -> {
            if (listener != null) {
                listener.onAction(view, getAdapterPosition(), null, ListActions.MORE);
            }
            return true;
        });
    }

    public void setData(Note item) {
        binding.setNoteItem(item);
    }
}
