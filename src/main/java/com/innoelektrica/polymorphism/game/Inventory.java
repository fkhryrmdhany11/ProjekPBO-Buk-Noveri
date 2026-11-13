package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.FoodType;
import java.util.*;
public final class Inventory {
    public static final int MAX_ITEMS=999;
    private final EnumMap<FoodType,Integer> items=new EnumMap<>(FoodType.class);
    public Inventory() {
        for(FoodType f:FoodType.values())items.put(f,0);
    }
    public int get(FoodType f) {
        return items.get(Objects.requireNonNull(f));
    }
    public Map<FoodType,Integer> getItems() {
        return Collections.unmodifiableMap(new EnumMap<>(items));
    }
    public int totalCount() {
        int sum=0;
        for(int v:items.values())sum+=v;
        return sum;
    }
    void set(FoodType f,int n) {
        if(n<0||n>MAX_ITEMS)throw new IllegalArgumentException("Stok di luar batas");
        items.put(f,n);
    }
    void add(FoodType f,int n) {
        set(f,Math.addExact(get(f),n));
    }
    void removeOne(FoodType f) {
        set(f,get(f)-1);
    }
}
