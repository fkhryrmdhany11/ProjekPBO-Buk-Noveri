package com.innoelektrica.polymorphism.model;
public abstract class Kendaraan {
    private final String nama;
    private final int jumlahRoda;
    private final String fungsi;
    protected Kendaraan(String nama, int jumlahRoda, String fungsi) {
        this.nama = nama;
        this.jumlahRoda = jumlahRoda;
        this.fungsi = fungsi;
    }
    public String getNama() {
        return nama;
    }
    public int getJumlahRoda() {
        return jumlahRoda;
    }
    public String getFungsi() {
        return fungsi;
    }
    // Polymorphism pergerakan
    public abstract int bergerak();
    public abstract String getKarakterGerak();
    // Polymorphism bahan bakar
    public abstract String getJenisBBM();
    public abstract double getKapasitasTangki();
    public abstract double getLajuPengisian();
    public abstract double getKonsumsiPerTick();
    // Polymorphism kapasitas kargo & pengali upah quest kurir
    public abstract int getKapasitasKargo();
    public abstract double getMultiplierKurir();
    // Polymorphism suara mesin & klakson kendaraan
    public abstract String getAudioResource();
    public abstract String getHornResource();
    public abstract String getSuaraMesin();
    public void bersuara() {
        com.innoelektrica.polymorphism.util.AudioPlayer.playEngineLoop(getAudioResource());
    }
    public void klakson() {
        com.innoelektrica.polymorphism.util.AudioPlayer.play(getHornResource());
    }
}
