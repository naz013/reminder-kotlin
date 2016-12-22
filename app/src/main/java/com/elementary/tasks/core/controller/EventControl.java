package com.elementary.tasks.core.controller;

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

public interface EventControl {
    boolean start();

    boolean stop();

    boolean pause();

    boolean skip();

    boolean resume();

    boolean next();

    boolean onOff();

    boolean canSkip();

    boolean isRepeatable();

    boolean isActive();

    void setDelay(int delay);
}
