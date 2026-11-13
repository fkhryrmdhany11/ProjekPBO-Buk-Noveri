package com.innoelektrica.polymorphism.model;
import com.innoelektrica.polymorphism.util.AudioPlayer;
public class Kucing extends Hewan {
    public Kucing() {
        super( "Kucing", "Felis catus", "Lingkungan rumah dan permukiman", "Karnivora; daging dan pakan hewan", "Lincah, memiliki kumis sensitif dan pendengaran tajam", "/audio/kucing.wav" );
    }
    @Override public FoodType getMakananFavorit() {
        return FoodType.DAGING;
    }
    @Override public int getSanityBonus() {
        return 18;
    }
    @Override public String sapaan() {
        return "Meong! Aku senang duduk di dekat bunga.";
    }
    @Override public void bersuara() {
        AudioPlayer.playShort(getAudioResource(), 2200);
    }
    @Override public String getSuara() {
        return "Meong";
    }
    @Override public String getSuaraSebelumMakan() {
        return "Meong... (lapar, minta daging!)";
    }
    @Override public String getSuaraSesudahMakan() {
        return "Meeooong~ ♪ Purrr~ (kenyang & senang!)";
    }
}
