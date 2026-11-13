package com.innoelektrica.polymorphism.game;
public enum TimePeriod {
    PAGI("Pagi"),SIANG("Siang"),SORE("Sore"),MALAM("Malam");
    private final String label;
    TimePeriod(String s) {
        label=s;
    }
    public String getLabel() {
        return label;
    }
    public static TimePeriod at(double minutes) {
        int h=(int)Math.floor(minutes/60)%24;
        return h>=6&&h<11?PAGI:h>=11&&h<14?SIANG:h>=14&&h<18?SORE:MALAM;
    }
}
