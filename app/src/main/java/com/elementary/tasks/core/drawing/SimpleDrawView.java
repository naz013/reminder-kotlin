package com.elementary.tasks.core.drawing;

import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

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

public class SimpleDrawView extends View {

    private Drawing drawing;

    public SimpleDrawView(Context context) {
        super(context);
    }

    public SimpleDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleDrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDrawing(Drawing drawing) {
        this.drawing = drawing;
        this.invalidate();
    }

    public Drawing getDrawing() {
        return this.drawing;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.drawing != null) {
            this.drawing.draw(canvas, true);
        }
    }
}
