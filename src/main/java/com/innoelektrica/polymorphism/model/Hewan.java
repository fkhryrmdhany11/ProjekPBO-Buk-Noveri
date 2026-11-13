package com.innoelektrica.polymorphism.model;
public abstract class Hewan {
    private final String nama;
    private final String namaIlmiah;
    private final String habitat;
    private final String makanan;
    private final String ciri;
    private final String audioResource;
    protected Hewan(String nama, String namaIlmiah, String habitat, String makanan, String ciri, String audioResource) {
        this.nama = nama;
        this.namaIlmiah = namaIlmiah;
        this.habitat = habitat;
        this.makanan = makanan;
        this.ciri = ciri;
        this.audioResource = audioResource;
    }
    public String getNama() {
        return nama;
    }
    public String getNamaIlmiah() {
        return namaIlmiah;
    }
    public String getHabitat() {
        return habitat;
    }
    public String getMakanan() {
        return makanan;
    }
    public String getCiri() {
        return ciri;
    }
    public String getAudioResource() {
        return audioResource;
    }
    public abstract FoodType getMakananFavorit();
    public abstract String sapaan();
    public abstract int getSanityBonus();
    public int getPetSanityBonus() {
        return 6;
    }
    public final boolean menerima(FoodType food) {
        return food==getMakananFavorit();
    }
    public abstract void bersuara();
    public abstract String getSuara();
    public abstract String getSuaraSebelumMakan();
    public abstract String getSuaraSesudahMakan();
    public String dialog(int friendship, com.innoelektrica.polymorphism.game.TimePeriod period) {
        String mood=period==com.innoelektrica.polymorphism.game.TimePeriod.MALAM?"Aku sedang beristirahat. Besok bermain lagi, ya.":getSuaraSebelumMakan()+" — "+sapaan();
        return mood+(friendship>=30?" Aku mengenalmu! Aku akan mengikuti sebentar setelah interaksi.":" Senang bertemu denganmu.");
    }
    public String reaksiMakanan(boolean accepted) {
        return accepted?getNama()+": \""+getSuaraSesudahMakan()+"\" Friendship +10, Sanity +"+getSanityBonus()+"%.":getNama()+" menolak. Makanan kembali ke tas; friendship -2.";
    }
}
