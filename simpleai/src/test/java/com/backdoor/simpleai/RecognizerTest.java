package com.backdoor.simpleai;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNotEquals;

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
public class RecognizerTest {

    private static Recognizer recognizer;

    @BeforeClass
    public static void init() {
        recognizer = new Recognizer.Builder()
                .with(null)
                .setLocale("uk-UA")
                .setTimes(new String[]{"07:00", "12:00", "19:00", "23:00"})
                .build();
    }

    @Test
    public void parse() throws Exception {
        Model model = recognizer.parse("завтра о 8 вечора перевірити пошту");
        System.out.println(model);
        assertNotEquals(null, model);
        printTime(model.getDateTime());
    }

    private void printTime(long time) {
        System.out.println(new Date(time));
    }
}