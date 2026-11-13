package com.innoelektrica.polymorphism.model;
import com.innoelektrica.polymorphism.util.AudioPlayer;
public class Burung extends Hewan {
    public Burung() {
        super( "Burung", "Aves", "Pepohonan, taman, dan area terbuka", "Biji-bijian, buah, dan serangga kecil", "Memiliki bulu, paruh, dan sayap untuk terbang", "/audio/burung.wav" );
    }
    @Override public FoodType getMakananFavorit() {
        return FoodType.BIJI_BIJIAN;
    }
    @Override public int getSanityBonus() {
        return 14;
    }
    @Override public String sapaan() {
        return "Cuit! Dari pohon ini kota terlihat indah.";
    }
    @Override public void bersuara() {
        AudioPlayer.playShort(getAudioResource(), 2200);
    }
    @Override public String getSuara() {
        return "Cuit-cuit";
    }
    @Override public String getSuaraSebelumMakan() {
        return "Cuit... cuit! (lapar, mencari biji!)";
    }
    @Override public String getSuaraSesudahMakan() {
        return "Cuit-cuit merrdu~ ♪ (kenyang & riang!)";
    }
}
