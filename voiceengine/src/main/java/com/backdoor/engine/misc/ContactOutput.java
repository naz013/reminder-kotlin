package com.backdoor.engine.misc;

public class ContactOutput {
    private final String output;
    private final String number;

    public ContactOutput(String output, String number) {
        this.output = output;
        this.number = number;
    }

    public String getOutput() {
        return output;
    }

    public String getNumber() {
        return number;
    }
}
