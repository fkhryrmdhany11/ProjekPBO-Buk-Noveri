package com.innoelektrica.polymorphism.model;
import com.innoelektrica.polymorphism.util.AudioPlayer;
public class Angsa extends Hewan {
    public Angsa() {
        super( "Angsa", "Cygnus olor", "Danau, kolam, dan lahan basah", "Tumbuhan air, rumput, dan organisme kecil", "Berleher panjang, berbulu putih, dan pandai berenang", "/audio/angsa.wav" );
    }
    @Override public FoodType getMakananFavorit() {
        return FoodType.ROTI;
    }
    @Override public int getSanityBonus() {
        return 16;
    }
    @Override public String sapaan() {
        return "Honk! Aku menjaga kolam yang tenang ini.";
    }
    @Override public void bersuara() {
        AudioPlayer.playShort(getAudioResource(), 2200);
    }
    @Override public String getSuara() {
        return "Honk";
    }
    @Override public String getSuaraSebelumMakan() {
        return "Honk... honk! (lapar, menunggu roti!)";
    }
    @Override public String getSuaraSesudahMakan() {
        return "Hooonk~ ♪ (kenyang & berenang tenang!)";
    }
}
