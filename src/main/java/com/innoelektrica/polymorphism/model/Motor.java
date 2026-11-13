package com.innoelektrica.polymorphism.model;
public class Motor extends Kendaraan {
    public Motor() {
        super("Motor", 2, "Transportasi ringan dan lincah di jalan raya");
    }
    @Override public int bergerak() {
        return 7;
    }
    @Override public String getKarakterGerak() {
        return "Bergerak lebih lincah dengan dua roda";
    }
    @Override public String getJenisBBM() {
        return "Pertalite";
    }
    @Override public double getKapasitasTangki() {
        return 10.0;
    }
    @Override public double getLajuPengisian() {
        return 0.24;
    }
    @Override public double getKonsumsiPerTick() {
        return 0.006;
    }
    @Override public int getKapasitasKargo() {
        return 1;
    }
    @Override public double getMultiplierKurir() {
        return 1.0;
    }
    @Override public String getAudioResource() {
        return "/audio/motor.wav";
    }
    @Override public String getHornResource() {
        return "/audio/klakson_motor.wav";
    }
    @Override public String getSuaraMesin() {
        return "Brum-brum! Tin-tin!";
    }
}
