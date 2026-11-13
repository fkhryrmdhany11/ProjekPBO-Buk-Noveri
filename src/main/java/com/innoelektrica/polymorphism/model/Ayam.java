package com.innoelektrica.polymorphism.model;
import com.innoelektrica.polymorphism.util.AudioPlayer;
public class Ayam extends Hewan {
    public Ayam() {
        super( "Ayam", "Gallus gallus domesticus", "Pekarangan dan peternakan", "Biji-bijian, dedak, dan serangga kecil", "Memiliki jengger, paruh pendek, dan kaki bercakar", "/audio/ayam.wav" );
    }
    @Override public FoodType getMakananFavorit() {
        return FoodType.BIJI_BIJIAN;
    }
    @Override public int getSanityBonus() {
        return 12;
    }
    @Override public String sapaan() {
        return "Petok! Aku sedang mencari biji di rumput.";
    }
    @Override public void bersuara() {
        AudioPlayer.playShort(getAudioResource(), 2200);
    }
    @Override public String getSuara() {
        return "Kukuruyuk / petok-petok";
    }
    @Override public String getSuaraSebelumMakan() {
        return "Petok-petok... (lapar, mematuk rumput!)";
    }
    @Override public String getSuaraSesudahMakan() {
        return "Kukuruyuk~! ♪ Petok kenyang!";
    }
}
