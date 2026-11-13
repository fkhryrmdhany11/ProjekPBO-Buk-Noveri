package com.innoelektrica.polymorphism.model;
public final class ServiceQuest extends Quest {
    public ServiceQuest(String id, String title, String description, int baseReward) {
        super(id, title, description, baseReward, -16, 25);
    }
    @Override public String getKategori() {
        return "KERJA KOTA";
    }
    @Override public String getTargetLokasiLabel() {
        return "Pusat Kota (Kios Pak Raka / Plaza)";
    }
    @Override public int hitungUpah(Kendaraan kendaraan) {
        return getBaseReward();
    }
    @Override public boolean butuhKendaraan() {
        return false;
    }
}
