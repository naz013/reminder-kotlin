package com.backdoor.engine;

import org.jetbrains.annotations.NotNull;

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
class WorkerFactory {

    @NotNull
    static WorkerInterface getWorker(@NotNull String locale) {
        if (locale.matches(Locale.EN)) {
            return new EnLocale();
        } else if (locale.matches(Locale.UK)) {
            return new UkLocale();
        } else if (locale.matches(Locale.RU)) {
            return new RuLocale();
        } else return new EnLocale();
    }
}
