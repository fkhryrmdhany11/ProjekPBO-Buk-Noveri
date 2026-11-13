package com.innoelektrica.polymorphism.model;
public final class DeliveryQuest extends Quest {
    private final String destinationCode;
    public DeliveryQuest(String id, String title, String description, int baseReward, String destinationCode) {
        super(id, title, description, baseReward, -8, 20);
        this.destinationCode = destinationCode;
    }
    public String getDestinationCode() {
        return destinationCode;
    }
    @Override public String getKategori() {
        return "KURIR KOTA";
    }
    @Override public String getTargetLokasiLabel() {
        return switch (destinationCode) {
            case "PARK" -> "Taman Kota (Kios Taman)";
            case "CITY" -> "Pusat Kota (Plaza / Kios)";
            case "FUEL" -> "SPBU Kota";
            default -> "Rumah / Garasi";
        };
    }
    @Override public int hitungUpah(Kendaraan kendaraan) {
        if (kendaraan == null) return getBaseReward();
        return (int) Math.round(getBaseReward() * kendaraan.getMultiplierKurir());
    }
    @Override public boolean butuhKendaraan() {
        return true;
    }
}
