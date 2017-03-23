package com.elementary.tasks.core.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

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

class Figure implements Drawing {

    private Path path;
    private Paint paint;

    Figure(Path path, Paint paint) {
        this.path = path;
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void draw(Canvas canvas, boolean scale) {
        canvas.drawPath(path, paint);
    }

    @Override
    public float getX() {
        return 0;
    }

    @Override
    public float getY() {
        return 0;
    }

    @Override
    public void setX(float x) {

    }

    @Override
    public void setY(float y) {

    }

    @Override
    public void setOpacity(int opacity) {
        this.paint.setAlpha(opacity);
    }

    @Override
    public int getOpacity() {
        return this.paint.getAlpha();
    }

    @Override
    public void setStrokeWidth(float width) {
        this.paint.setStrokeWidth(width);
    }

    @Override
    public float getStrokeWidth() {
        return this.paint.getStrokeWidth();
    }
}
