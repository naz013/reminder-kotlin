package com.elementary.tasks.core.drawing;

import android.graphics.Canvas;

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

public interface Drawing {
    void draw(Canvas canvas);

    float getX();

    float getY();

    void setX(float x);

    void setY(float y);

    void setOpacity(int opacity);

    int getOpacity();

    void setStrokeWidth(float width);

    float getStrokeWidth();
}
