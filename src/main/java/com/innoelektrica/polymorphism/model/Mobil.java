package com.innoelektrica.polymorphism.model;
public class Mobil extends Kendaraan {
    public Mobil() {
        super("Mobil", 4, "Transportasi penumpang di jalan raya");
    }
    @Override public int bergerak() {
        return 5;
    }
    @Override public String getKarakterGerak() {
        return "Bergerak stabil dengan empat roda";
    }
    @Override public String getJenisBBM() {
        return "Pertamax";
    }
    @Override public double getKapasitasTangki() {
        return 40.0;
    }
    @Override public double getLajuPengisian() {
        return 0.55;
    }
    @Override public double getKonsumsiPerTick() {
        return 0.026;
    }
    @Override public int getKapasitasKargo() {
        return 2;
    }
    @Override public double getMultiplierKurir() {
        return 1.5;
    }
    @Override public String getAudioResource() {
        return "/audio/mobil.wav";
    }
    @Override public String getHornResource() {
        return "/audio/klakson_mobil.wav";
    }
    @Override public String getSuaraMesin() {
        return "Vroom-m! Beep-beep!";
    }
}
