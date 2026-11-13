package com.innoelektrica.polymorphism.model;
/** Fondasi PBO untuk misi perkotaan; di-override oleh DeliveryQuest, ServiceQuest, dan ParkCareQuest. */
public abstract class Quest {
    private final String id;
    private final String title;
    private final String description;
    private final int baseReward;
    private final int sanityDelta;
    private final int durationMinutes;
    protected Quest(String id, String title, String description, int baseReward, int sanityDelta, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.baseReward = baseReward;
        this.sanityDelta = sanityDelta;
        this.durationMinutes = durationMinutes;
    }
    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public int getBaseReward() {
        return baseReward;
    }
    public int getSanityDelta() {
        return sanityDelta;
    }
    public int getDurationMinutes() {
        return durationMinutes;
    }
    public abstract String getKategori();
    public abstract String getTargetLokasiLabel();
    public abstract int hitungUpah(Kendaraan kendaraan);
    public abstract boolean butuhKendaraan();
    public boolean bisaDiambil(int currentSanity, Kendaraan kendaraan) {
        if (butuhKendaraan() && kendaraan == null) return false;
        if (sanityDelta < 0 && currentSanity < 30) return false;
        return true;
    }
}
