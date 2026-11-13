package com.innoelektrica.polymorphism.model;
public enum AnimalType {
    KUCING,ANJING,BURUNG,AYAM,ANGSA;
    public Hewan create() {
        return switch(this) {
            case KUCING->new Kucing();
            case ANJING->new Anjing();
            case BURUNG->new Burung();
            case AYAM->new Ayam();
            case ANGSA->new Angsa();
        }
        ;
    }
    @Override public String toString() {
        return create().getNama();
    }
}
