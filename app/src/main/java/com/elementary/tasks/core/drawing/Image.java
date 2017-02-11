package com.elementary.tasks.core.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

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

public class Image implements Drawing {

    private Bitmap bitmap = null;
    private float bitmapX = 0F;
    private float bitmapY = 0F;

    public Image(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.bitmap, bitmapX, bitmapY, new Paint());
    }

    @Override
    public float getX() {
        return bitmapX;
    }

    @Override
    public float getY() {
        return bitmapY;
    }

    @Override
    public void setX(float x) {
        this.bitmapX = x;
    }

    @Override
    public void setY(float y) {
        this.bitmapY = y;
    }
}
