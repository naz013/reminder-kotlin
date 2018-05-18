package com.elementary.tasks.core.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import androidx.annotation.ColorInt;

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

public class Background implements Drawing {

    @ColorInt
    private int color;
    private int opacity = 255;

    Background(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void draw(Canvas canvas, boolean scale) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        canvas.drawColor(Color.argb(opacity, red, green, blue));
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
        this.opacity = opacity;
    }

    @Override
    public int getOpacity() {
        return opacity;
    }

    @Override
    public void setStrokeWidth(float width) {

    }

    @Override
    public float getStrokeWidth() {
        return 0;
    }
}
