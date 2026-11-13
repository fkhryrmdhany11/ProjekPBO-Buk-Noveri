package com.innoelektrica.polymorphism.game;
public enum Location {
    HOME("Rumah"),MAP("Jalan Kota"),PARK("Taman"),CITY("Pusat Kota"),FUEL("SPBU");
    private final String label;
    Location(String label) {
        this.label=label;
    }
    public String getLabel() {
        return label;
    }
}
