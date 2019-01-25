package com.backdoor.engine;

import com.backdoor.engine.misc.Locale;
import com.backdoor.engine.misc.TimeUtil;

import org.junit.Before;
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
            "on may twenty second at 15 30 check mail",
            "every sunday at 15 30 check mail",
            "after five minutes check mail",
            "after ten hours call to Mary"
    };

    private static final String[] TEST_PT = new String[]{
            "em duas horas e meia, leia a caixa de entrada",
            "em cinco minutos e meio cheque mail",
            "em cinco segundos e meio verificar mail",
            "amanhã às 15 30 cheque correio",
            "depois de amanhã às 15 30 check mail",
            "no dia 30 de maio, às 15h30, verifique a correspondência",
            "em maio de vinte e dois em 15 30 check mail",
            "todos os domingos às 15 30 cheque correio",
            "depois de cinco minutos verifique o correio"
    };

    private static final String[] TEST_ES = new String[]{
            "en dos horas y media leer bandeja de entrada",
            "En cinco minutos y medio revisar el correo",
            "en cinco segundos y medio consultar el correo",
            "mañana a las 15 30 verifique el correo",
            "después de mañana a las 15 30 verifique el correo",
            "el 30 de mayo a las 15 30 cheque por correo",
            "el veinte de mayo a las 15 30 cheque",
            "Todos los domingos a las 15 30 cheque correo",
            "después de cinco minutos revisar el correo"
    };

    private static final String[] TEST_DE = new String[]{
            "in zweieinhalb Stunden Posteingang lesen",
            "in sechs Minuten und eine halbe lese Post",
            "in sechseinhalb Sekunden lese Post",
            "morgen um 15 30 lese Post",
            "nach morgen um 15 30 lese Post",
            "am 30 Mai um 15 Uhr 30 lese Post",
            "jeden Sonntag um 15 30 lese Post",
            "Nach sechs Minuten lese Post"
    };

    private static final String[] TEST_UK = new String[]{
            "через дві з половиною години перевірити пошту",
            "через п'ять хвилин перевірити пошту",
            "через півтори хвилини перевірити пошту",
            "через півгодини перевірити пошту",
            "завтра о 15 30 перевірити пошту",
            "післязавтра о 15 30 перевірити пошту",
            "30 травня о 15 30 перевірити пошту",
            "щодня о 7 годині перевірити пошту",
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

    @Before
    public void before() {
        System.out.println("GMT " + TimeUtil.getGmtFromDateTime(System.currentTimeMillis()));
    }

    @Test
    public void replaceNumbers() {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.EN).setTimes(null).build();
        for (String in : TEST) {
            Model out = recognizer.parse(in);
            System.out.println(out);
        }
    }

    @Test
    public void checkPt() {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.PT).setTimes(null).build();
        for (String in : TEST_PT) {
            System.out.println("Input " + in);
            Model out = recognizer.parse(in);
            System.out.println("Output " + out);
        }
    }

    @Test
    public void checkEs() {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.ES).setTimes(null).build();
        for (String in : TEST_ES) {
            System.out.println("Input " + in);
            Model out = recognizer.parse(in);
            System.out.println("Output " + out);
        }
    }

    @Test
    public void checkDe() {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.DE).setTimes(null).build();
        for (String in : TEST_DE) {
            System.out.println("Input " + in);
            Model out = recognizer.parse(in);
            System.out.println("Output " + out);
        }
    }

    @Test
    public void checkEn() {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.EN).setTimes(null).build();
        for (String in : TEST) {
            System.out.println("Input " + in);
            Model out = recognizer.parse(in);
            System.out.println("Output " + out);
        }
    }

    @Test
    public void checkUk() {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.UK).setTimes(null).build();
        for (String in : TEST_UK) {
            System.out.println("Input " + in);
            Model out = recognizer.parse(in);
            System.out.println("Output " + out);
        }
    }

    @Test
    public void checkRu() {
        Recognizer recognizer = new Recognizer.Builder().setLocale(Locale.RU).setTimes(null).build();
        for (String in : TEST_RU) {
            System.out.println("Input " + in);
            Model out = recognizer.parse(in);
            System.out.println("Output " + out);
        }
    }
}