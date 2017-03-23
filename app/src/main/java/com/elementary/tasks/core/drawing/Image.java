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
    private int opacity = 255;
    private int percentage = 100;

    public Image(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas, boolean scale) {
        Paint paint = new Paint();
        paint.setAlpha(this.opacity);
        canvas.drawBitmap(getBitmap(scale), getBitmapX(scale), getBitmapY(scale), paint);
    }

    private float getBitmapY(boolean scale) {
        if (scale) {
            return this.bitmapY / 10;
        }
        return this.bitmapY;
    }

    private float getBitmapX(boolean scale) {
        if (scale) {
            return this.bitmapX / 10;
        }
        return this.bitmapX;
    }

    private Bitmap getBitmap(boolean scale) {
        if (this.percentage >= 100) {
            return this.bitmap;
        } else {
            int scalar = scale ? 10 : 1;
            int dstWidth = (this.bitmap.getWidth() * (this.percentage / scalar)) / 100;
            int dstHeight = (this.bitmap.getHeight() * (this.percentage / scalar)) / 100;
            return Bitmap.createScaledBitmap(this.bitmap, dstWidth, dstHeight, true);
        }
    }

    @Override
    public float getX() {
        return this.bitmapX;
    }

    @Override
    public float getY() {
        return this.bitmapY;
    }

    @Override
    public void setX(float x) {
        this.bitmapX = x;
    }

    @Override
    public void setY(float y) {
        this.bitmapY = y;
    }

    @Override
    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    @Override
    public int getOpacity() {
        return this.opacity;
    }

    @Override
    public void setStrokeWidth(float width) {

    }

    @Override
    public float getStrokeWidth() {
        return 0;
    }

    void setScalePercentage(int percentage) {
        this.percentage = percentage;
    }

    int getPercentage() {
        return this.percentage;
    }
}
