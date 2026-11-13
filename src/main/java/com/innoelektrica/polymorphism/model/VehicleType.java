package com.innoelektrica.polymorphism.model;
public enum VehicleType {
    MOTOR,MOBIL;
    public Kendaraan create() {
        return this==MOTOR?new Motor():new Mobil();
    }
    @Override public String toString() {
        return create().getNama();
    }
}
