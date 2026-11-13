package com.innoelektrica.polymorphism.model;
public final class ParkCareQuest extends Quest {
    private final AnimalType targetAnimal;
    public ParkCareQuest(String id, String title, String description, int baseReward, AnimalType targetAnimal) {
        super(id, title, description, baseReward, +14, 15);
        this.targetAnimal = targetAnimal;
    }
    public AnimalType getTargetAnimal() {
        return targetAnimal;
    }
    @Override public String getKategori() {
        return "PENJAGA TAMAN";
    }
    @Override public String getTargetLokasiLabel() {
        return "Taman Kota (" + targetAnimal + ")";
    }
    @Override public int hitungUpah(Kendaraan kendaraan) {
        return getBaseReward();
    }
    @Override public boolean butuhKendaraan() {
        return false;
    }
}
