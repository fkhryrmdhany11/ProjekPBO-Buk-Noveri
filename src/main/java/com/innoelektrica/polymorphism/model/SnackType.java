package com.innoelektrica.polymorphism.model;
public enum SnackType {
    KOPI_KOTA("Es Kopi Kota", 8000, 22, "Segarkan pikiran di taman (+22% Sanity)"),
    ES_KRIM("Es Krim Vanilla", 6000, 15, "Manis dan menenangkan (+15% Sanity)"),
    ROTI_BAKAR("Roti Bakar Hangat", 12000, 32, "Kenyang dan rileks (+32% Sanity)");
    private final String label;
    private final int price;
    private final int sanityRestore;
    private final String description;
    SnackType(String l, int p, int s, String d) {
        label = l;
        price = p;
        sanityRestore = s;
        description = d;
    }
    public String getLabel() {
        return label;
    }
    public int getPrice() {
        return price;
    }
    public int getSanityRestore() {
        return sanityRestore;
    }
    public String getDescription() {
        return description;
    }
    @Override public String toString() {
        return label;
    }
}
