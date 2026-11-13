package com.innoelektrica.polymorphism.model;
public enum FoodType {
    DAGING("Daging",10000), BIJI_BIJIAN("Biji-bijian",5000), ROTI("Roti",7000);
    private final String label;
    private final int price;
    FoodType(String label,int price) {
        this.label=label;
        this.price=price;
    }
    public String getLabel() {
        return label;
    }
    public int getPrice() {
        return price;
    }
    @Override public String toString() {
        return label;
    }
}
