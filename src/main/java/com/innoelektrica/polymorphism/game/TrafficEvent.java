package com.innoelektrica.polymorphism.game;
public enum TrafficEvent {
    NORMAL("Jalan lancar",0,0),RED_LIGHT("Lampu merah: berhenti 3 detik",3,3),DOG_CROSSING("Anjing menyeberang: berhenti 2 detik",2,2),JAM("Kemacetan: kecepatan turun 5 detik",5,8);
    private final String label;
    private final double seconds,minutes;
    TrafficEvent(String s,double t,double m) {
        label=s;
        seconds=t;
        minutes=m;
    }
    public String getLabel() {
        return label;
    }
    public double getSeconds() {
        return seconds;
    }
    public double getMinutes() {
        return minutes;
    }
    public static TrafficEvent choose(int roll) {
        if(roll<0||roll>99)throw new IllegalArgumentException();
        return roll<70?NORMAL:roll<80?RED_LIGHT:roll<90?DOG_CROSSING:JAM;
    }
}
