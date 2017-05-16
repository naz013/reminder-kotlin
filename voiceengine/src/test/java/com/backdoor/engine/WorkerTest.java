package com.backdoor.engine;

import org.junit.Test;

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
public class WorkerTest {

    private static final String[] TEST = new String[]{
            "in two and half hours read inbox",
            "in five minutes and a half check mail",
            "in five and a half seconds check mail",
            "tomorrow at 15 30 check mail",
            "after tomorrow at 15 30 check mail",
            "on may 30 at 15 30 check mail",
            "after five minutes check mail"
    };

    private static final String[] TEST_UK = new String[]{
            "через дві з половиною години перевірити пошту",
            "через п'ять хвилин перевірити пошту",
            "через півтори хвилини перевірити пошту",
            "через півгодини перевірити пошту",
            "завтра о 15 30 перевірити пошту",
            "післязавтра о 15 30 перевірити пошту",
            "30 травня о 15 30 перевірити пошту",
            "о 20:40 перевірити пошту"
    };

    private static final String[] TEST_RU = new String[]{
            "через два с половиной часа проверить почту",
            "через пять минут проверить почту",
            "через полторы минуты проверить почту",
            "через полчаса проверить почту",
            "завтра в 15 30 проверить почту",
            "послезавтра в 15 30 проверить почту",
            "30 мая в 15 30 проверить почту",
            "в 20:40 проверить почту"
    };

    @Test
    public void replaceNumbers() throws Exception {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.EN).setTimes(null).build();
        for (String in : TEST) {
            Model out = recognizer.parse(in);
            System.out.print(out);
        }
    }

    @Test
    public void checkUk() throws Exception {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.UK).setTimes(null).build();
        for (String in : TEST_UK) {
            System.out.println("Input " + in);
            Model out = recognizer.parse(in);
            System.out.print(out);
        }
    }

    @Test
    public void checkRu() throws Exception {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.RU).setTimes(null).build();
        for (String in : TEST_RU) {
            System.out.println("Input " + in);
            Model out = recognizer.parse(in);
            System.out.print(out);
        }
    }
}