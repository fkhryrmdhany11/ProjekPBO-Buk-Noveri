package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.*;
import java.util.*;
/** Satu data pemain; perubahan normal dilakukan lewat GameManager. */
public final class GameState {
    public static final long MAX_MONEY=1_000_000_000L;
    private final String playerName;
    private int day=1;
    private double minutes=420;
    private Location location=Location.HOME;
    private double x=500,y=440,mapX=265,mapY=470,facing;
    private long money=50000;
    private int sanity=100;
    private double sanityRemainder=0.0;
    private boolean mounted=true;
    private double parkedX=265,parkedY=470;
    private String activeQuestId="NONE";
    private int questStep=0;
    private int questsCompletedToday=0;
    private int totalQuestsCompleted=0;
    private long questEarningsToday=0;
    private final Inventory inventory=new Inventory();
    private VehicleType vehicleType;
    private final EnumMap<VehicleType,Integer> fuelMl=new EnumMap<>(VehicleType.class);
    private final EnumMap<VehicleType,Double> fuelRemainder=new EnumMap<>(VehicleType.class);
    private final EnumMap<AnimalType,Integer> friendship=new EnumMap<>(AnimalType.class);
    private final EnumMap<AnimalType,Double> cooldown=new EnumMap<>(AnimalType.class);
    private final EnumSet<AnimalType> fedAnimals=EnumSet.noneOf(AnimalType.class);
    private DayStatus dayStatus=DayStatus.ACTIVE;
    private boolean visitedPark,returnedHome,departed,cityExplored,cityWorkDone,lowFuelWarned,nightWarned,lowSanityWarned;
    private int foodBought,foodGiven,emergencyCount;
    private double drivingEventSeconds;
    private final List<String> activityLog=new ArrayList<>();
    public GameState(String name) {
        name=Objects.requireNonNull(name).trim();
        if(name.isEmpty()||name.length()>30||name.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Nama harus 1–30 karakter.");
        playerName=name;
        for(VehicleType v:VehicleType.values()) {
            fuelMl.put(v,(int)(v.create().getKapasitasTangki()*700));
            fuelRemainder.put(v,0.0);
        }
        for(AnimalType a:AnimalType.values()) {
            friendship.put(a,0);
            cooldown.put(a,0.0);
        }
    }
    public String getPlayerName() {
        return playerName;
    }
    public int getDay() {
        return day;
    }
    public double getMinutes() {
        return minutes;
    }
    public String getClock() {
        int m=(int)minutes;
        return String.format(java.util.Locale.ROOT,"%02d:%02d",m/60,m%60);
    }
    public TimePeriod getPeriod() {
        return TimePeriod.at(minutes);
    }
    public Location getLocation() {
        return location;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getMapX() {
        return mapX;
    }
    public double getMapY() {
        return mapY;
    }
    public double getFacing() {
        return facing;
    }
    public long getMoney() {
        return money;
    }
    public int getSanity() {
        return sanity;
    }
    public double getSanityRemainder() {
        return sanityRemainder;
    }
    public boolean isLowSanity() {
        return sanity<30;
    }
    public String getSanityMood() {
        if(sanity>=75)return "Segar";
        if(sanity>=45)return "Stabil";
        if(sanity>=30)return "Mulai Lelah";
        return "Stres (Ke Taman!)";
    }
    public boolean isMounted() {
        return mounted;
    }
    public double getParkedX() {
        return parkedX;
    }
    public double getParkedY() {
        return parkedY;
    }
    public String getActiveQuestId() {
        return activeQuestId;
    }
    public int getQuestStep() {
        return questStep;
    }
    public int getQuestsCompletedToday() {
        return questsCompletedToday;
    }
    public int getTotalQuestsCompleted() {
        return totalQuestsCompleted;
    }
    public long getQuestEarningsToday() {
        return questEarningsToday;
    }
    public Inventory getInventory() {
        return inventory;
    }
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    public Kendaraan getVehicle() {
        return vehicleType==null?null:vehicleType.create();
    }
    public int getFuelMl(VehicleType v) {
        return fuelMl.get(v);
    }
    public double getFuelRemainder(VehicleType v) {
        return fuelRemainder.get(v);
    }
    public double getFuelLiters() {
        return vehicleType==null?0:getFuelMl(vehicleType)/1000.0;
    }
    public int getFuelPercent() {
        return vehicleType==null?0:(int)Math.round(getFuelLiters()/getVehicle().getKapasitasTangki()*100);
    }
    public int getFriendship(AnimalType a) {
        return friendship.get(a);
    }
    public double getCooldown(AnimalType a) {
        return cooldown.get(a);
    }
    public Set<AnimalType> getFedAnimals() {
        return Collections.unmodifiableSet(EnumSet.copyOf(fedAnimals));
    }
    public DayStatus getDayStatus() {
        return dayStatus;
    }
    public boolean isVisitedPark() {
        return visitedPark;
    }
    public boolean isReturnedHome() {
        return returnedHome;
    }
    public boolean isDeparted() {
        return departed;
    }
    public boolean isCityExplored() {
        return cityExplored;
    }
    public boolean isCityWorkDone() {
        return cityWorkDone;
    }
    public boolean isLowFuelWarned() {
        return lowFuelWarned;
    }
    public boolean isNightWarned() {
        return nightWarned;
    }
    public boolean isLowSanityWarned() {
        return lowSanityWarned;
    }
    public int getFoodBought() {
        return foodBought;
    }
    public int getFoodGiven() {
        return foodGiven;
    }
    public int getEmergencyCount() {
        return emergencyCount;
    }
    public double getDrivingEventSeconds() {
        return drivingEventSeconds;
    }
    public List<String> getActivityLog() {
        return List.copyOf(activityLog);
    }
    public boolean isMissionComplete() {
        return visitedPark&&fedAnimals.size()>=2&&returnedHome;
    }
    public String getMissionText() {
        return "Taman "+(visitedPark?"1":"0")+"/1   |   Hewan "+fedAnimals.size()+"/2   |   Pulang "+(returnedHome?"1":"0")+"/1   |   Quest: "+questsCompletedToday;
    }
    void setDay(int v) {
        day=v;
    }
    void setMinutes(double v) {
        minutes=v;
    }
    void addMinutes(double v) {
        minutes=Math.min(1439,minutes+v);
    }
    void setLocation(Location v) {
        location=v;
    }
    void setPosition(double a,double b) {
        x=a;
        y=b;
        if(location==Location.MAP) {
            if(mounted) {
                mapX=a;
                mapY=b;
                parkedX=a;
                parkedY=b;
            }
        }
    }
    void setMapPosition(double a,double b) {
        mapX=a;
        mapY=b;
        parkedX=a;
        parkedY=b;
    }
    void setFacing(double v) {
        facing=v;
    }
    void setMoney(long v) {
        money=v;
    }
    void setSanity(int v) {
        sanity=Math.max(0,Math.min(100,v));
    }
    void setSanityRemainder(double v) {
        sanityRemainder=v;
    }
    void setMounted(boolean v) {
        mounted=v;
    }
    void setParkedPosition(double px,double py) {
        parkedX=px;
        parkedY=py;
    }
    void setActiveQuestId(String id) {
        activeQuestId=id==null||id.isBlank()?"NONE":id;
    }
    void setQuestStep(int s) {
        questStep=Math.max(0,s);
    }
    void setQuestsCompletedToday(int n) {
        questsCompletedToday=Math.max(0,n);
    }
    void setTotalQuestsCompleted(int n) {
        totalQuestsCompleted=Math.max(0,n);
    }
    void setQuestEarningsToday(long n) {
        questEarningsToday=Math.max(0,n);
    }
    void setVehicleType(VehicleType v) {
        vehicleType=v;
    }
    void setFuelMl(VehicleType v,int n) {
        fuelMl.put(v,n);
    }
    void setFuelRemainder(VehicleType v,double n) {
        fuelRemainder.put(v,n);
    }
    void setFriendship(AnimalType a,int n) {
        friendship.put(a,n);
    }
    void setCooldown(AnimalType a,double v) {
        cooldown.put(a,v);
    }
    void addFed(AnimalType a) {
        fedAnimals.add(a);
    }
    void setDayStatus(DayStatus v) {
        dayStatus=v;
    }
    void setVisitedPark(boolean v) {
        visitedPark=v;
    }
    void setReturnedHome(boolean v) {
        returnedHome=v;
    }
    void setDeparted(boolean v) {
        departed=v;
    }
    void setCityExplored(boolean v) {
        cityExplored=v;
    }
    void setCityWorkDone(boolean v) {
        cityWorkDone=v;
    }
    void setLowFuelWarned(boolean v) {
        lowFuelWarned=v;
    }
    void setNightWarned(boolean v) {
        nightWarned=v;
    }
    void setLowSanityWarned(boolean v) {
        lowSanityWarned=v;
    }
    void setFoodBought(int v) {
        foodBought=v;
    }
    void setFoodGiven(int v) {
        foodGiven=v;
    }
    void setEmergencyCount(int v) {
        emergencyCount=v;
    }
    void setDrivingEventSeconds(double v) {
        drivingEventSeconds=v;
    }
    void replaceLog(List<String> list) {
        activityLog.clear();
        activityLog.addAll(list);
    }
    void log(String message) {
        activityLog.add(getClock()+" — "+message);
        while(activityLog.size()>50)activityLog.remove(0);
    }
    void resetDailyProgress() {
        minutes=420;
        location=Location.HOME;
        x=500;
        y=440;
        mapX=265;
        mapY=470;
        parkedX=265;
        parkedY=470;
        mounted=true;
        facing=0;
        sanity=Math.max(75,Math.min(100,sanity+35));
        sanityRemainder=0;
        activeQuestId="NONE";
        questStep=0;
        questsCompletedToday=0;
        questEarningsToday=0;
        dayStatus=DayStatus.ACTIVE;
        visitedPark=returnedHome=departed=cityExplored=cityWorkDone=lowFuelWarned=nightWarned=lowSanityWarned=false;
        foodBought=foodGiven=emergencyCount=0;
        drivingEventSeconds=0;
        fedAnimals.clear();
        activityLog.clear();
        for(AnimalType a:AnimalType.values())cooldown.put(a,0.0);
    }
}
