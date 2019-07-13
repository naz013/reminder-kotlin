package com.backdoor.engine.lang;

import com.backdoor.engine.misc.Locale;

public class WorkerFactory {

    public static WorkerInterface getWorker(String locale) {
        System.out.println("getWorker: " + locale);
        if (locale.matches(Locale.EN)) {
            return new EnWorker();
        } else if (locale.matches(Locale.UK)) {
            return new UkWorker();
        } else if (locale.matches(Locale.RU)) {
            return new RuWorker();
        } else if (locale.matches(Locale.DE)) {
            return new DeWorker();
        } else if (locale.matches(Locale.ES)) {
            return new EsWorker();
        } else if (locale.matches(Locale.PT)) {
            return new PtWorker();
        } else return new EnWorker();
    }
}
