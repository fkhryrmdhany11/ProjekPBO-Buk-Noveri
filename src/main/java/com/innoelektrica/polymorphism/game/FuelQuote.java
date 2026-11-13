package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.VehicleType;
public record FuelQuote(long requestedRupiah,int milliliters,long cost,int beforeMl,long beforeMoney,VehicleType vehicleType) {
    public double liters() {
        return milliliters/1000.0;
    }
}
