package com.innoelektrica.polymorphism.model;
import com.innoelektrica.polymorphism.util.AudioPlayer;
public class Anjing extends Hewan {
    public Anjing() {
        super( "Anjing", "Canis lupus familiaris", "Lingkungan rumah, taman, dan permukiman", "Omnivora; pakan hewan dan daging", "Aktif, penciuman sangat kuat dan mudah dilatih", "/audio/anjing.wav" );
    }
    @Override public FoodType getMakananFavorit() {
        return FoodType.DAGING;
    }
    @Override public int getSanityBonus() {
        return 20;
    }
    @Override public String sapaan() {
        return "Guk! Mau menemani aku berjalan di taman?";
    }
    @Override public void bersuara() {
        AudioPlayer.playShort(getAudioResource(), 2200);
    }
    @Override public String getSuara() {
        return "Guk-guk";
    }
    @Override public String getSuaraSebelumMakan() {
        return "Guk-guk! (lapar, ekor bergoyang!)";
    }
    @Override public String getSuaraSesudahMakan() {
        return "Woof~ ♪ Guk senang! (kenyang & semangat!)";
    }
}
