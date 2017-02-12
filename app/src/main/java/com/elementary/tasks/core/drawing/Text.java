package com.elementary.tasks.core.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;

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

public class Text implements Drawing {

    private String text = "";
    private Typeface fontFamily = Typeface.DEFAULT;
    private float fontSize = 32F;
    private Paint textPaint = new Paint();
    private float textX = 0F;
    private float textY = 0F;

    public Text(String text, Typeface fontFamily, float fontSize, Paint textPaint) {
        this.text = text;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.textPaint = textPaint;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
        this.textPaint.setTextSize(fontSize);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTextColor(@ColorInt int color) {
        this.textPaint.setColor(color);
    }

    public void setFontFamily(Typeface fontFamily) {
        this.fontFamily = fontFamily;
        textPaint.setTypeface(fontFamily);
    }

    public float getFontSize() {
        return fontSize;
    }

    @Override
    public void draw(Canvas canvas) {
        drawText(canvas);
    }

    @Override
    public float getX() {
        return textX;
    }

    @Override
    public float getY() {
        return textY;
    }

    @Override
    public void setX(float x) {
        this.textX = x;
    }

    @Override
    public void setY(float y) {
        this.textY = y;
    }

    @Override
    public void setOpacity(int opacity) {
        this.textPaint.setAlpha(opacity);
    }

    @Override
    public int getOpacity() {
        return this.textPaint.getAlpha();
    }

    @Override
    public void setStrokeWidth(float width) {
        this.textPaint.setStrokeWidth(width);
    }

    @Override
    public float getStrokeWidth() {
        return this.textPaint.getStrokeWidth();
    }

    private void drawText(Canvas canvas) {
        if (this.text.length() <= 0) {
            return;
        }
        float textX = this.textX;
        float textY = this.textY;
        Paint paintForMeasureText = new Paint();
        float textLength = paintForMeasureText.measureText(this.text);
        float lengthOfChar = textLength / (float) this.text.length();
        float restWidth = canvas.getWidth() - textX;  // text-align : right
        int numChars = (lengthOfChar <= 0) ? 1 : (int) Math.floor((double) (restWidth / lengthOfChar));  // The number of characters at 1 line
        int modNumChars = (numChars < 1) ? 1 : numChars;
        float y = textY;
        for (int i = 0, len = this.text.length(); i < len; i += modNumChars) {
            String substring;
            if ((i + modNumChars) < len) {
                substring = this.text.substring(i, (i + modNumChars));
            } else {
                substring = this.text.substring(i, len);
            }
            y += this.fontSize;
            canvas.drawText(substring, textX, y, this.textPaint);
        }
    }
}
