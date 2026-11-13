package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
/** Properties UTF-8, validasi penuh sebelum penggantian state/save. */
public final class SaveService {
    private final Path path;
    public SaveService() {
        this(Path.of(System.getProperty("city.save.path",Path.of(System.getProperty("user.home"),".a-day-in-my-city","save.properties").toString())));
    }
    public SaveService(Path p) {
        path=p.toAbsolutePath();
    }
    public Path getPath() {
        return path;
    }
    public boolean exists() {
        return Files.isRegularFile(path);
    }
    public void save(GameManager g)throws IOException {
        if(!g.canPersist())throw new IOException("Save tidak tersedia selama demo, antrean, pengisian, pekerjaan atau event aktif.");
        Properties p=toProperties(g.getState());
        try {
            fromProperties(p);
        }
        catch(IllegalArgumentException ex) {
            throw new IOException("State tidak valid: "+ex.getMessage(),ex);
        }
        Files.createDirectories(path.getParent());
        Path temp=Files.createTempFile(path.getParent(),"city-save-",".tmp");
        try {
            try(Writer w=Files.newBufferedWriter(temp,StandardCharsets.UTF_8)) {
                p.store(w,"A Day in My City - save v1");
            }
            try {
                Files.move(temp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
            }
            catch(AtomicMoveNotSupportedException ex) {
                Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }
    public GameState load()throws IOException {
        if(!exists())throw new FileNotFoundException("Belum ada save. Mulai permainan baru dahulu.");
        if(Files.size(path)>65536)throw new IOException("File save terlalu besar/rusak.");
        Properties p=new Properties();
        try(Reader r=Files.newBufferedReader(path,StandardCharsets.UTF_8)) {
            p.load(r);
        }
        catch(IllegalArgumentException ex) {
            throw new IOException("Format save rusak.",ex);
        }
        try {
            return fromProperties(p);
        }
        catch(IllegalArgumentException ex) {
            throw new IOException("Save tidak valid: "+ex.getMessage(),ex);
        }
    }
    private static void set(Properties p,String k,Object v) {
        p.setProperty(k,String.valueOf(v));
    }
    public static Properties toProperties(GameState s) {
        Properties p=new Properties();
        set(p,"version",1);
        set(p,"name",s.getPlayerName());
        set(p,"day",s.getDay());
        set(p,"minutes",s.getMinutes());
        set(p,"location",s.getLocation().name());
        set(p,"x",s.getX());
        set(p,"y",s.getY());
        set(p,"mapX",s.getMapX());
        set(p,"mapY",s.getMapY());
        set(p,"facing",s.getFacing());
        set(p,"money",s.getMoney());
        set(p,"sanity",s.getSanity());
        set(p,"mounted",s.isMounted());
        set(p,"parkedX",s.getParkedX());
        set(p,"parkedY",s.getParkedY());
        set(p,"activeQuestId",s.getActiveQuestId());
        set(p,"questStep",s.getQuestStep());
        set(p,"questsCompletedToday",s.getQuestsCompletedToday());
        set(p,"totalQuestsCompleted",s.getTotalQuestsCompleted());
        set(p,"questEarningsToday",s.getQuestEarningsToday());
        set(p,"vehicle",s.getVehicleType()==null?"NONE":s.getVehicleType().name());
        for(VehicleType v:VehicleType.values()) {
            set(p,"fuel."+v.name(),s.getFuelMl(v));
            set(p,"remainder."+v.name(),s.getFuelRemainder(v));
        }
        for(FoodType f:FoodType.values())set(p,"food."+f.name(),s.getInventory().get(f));
        for(AnimalType a:AnimalType.values()) {
            set(p,"friend."+a.name(),s.getFriendship(a));
            set(p,"cooldown."+a.name(),s.getCooldown(a));
        }
        set(p,"fed",String.join(",",s.getFedAnimals().stream().map(Enum::name).toList()));
        set(p,"status",s.getDayStatus().name());
        set(p,"visitedPark",s.isVisitedPark());
        set(p,"returnedHome",s.isReturnedHome());
        set(p,"departed",s.isDeparted());
        set(p,"cityExplored",s.isCityExplored());
        set(p,"cityWorkDone",s.isCityWorkDone());
        set(p,"lowFuelWarned",s.isLowFuelWarned());
        set(p,"nightWarned",s.isNightWarned());
        set(p,"lowSanityWarned",s.isLowSanityWarned());
        set(p,"foodBought",s.getFoodBought());
        set(p,"foodGiven",s.getFoodGiven());
        set(p,"emergencyCount",s.getEmergencyCount());
        set(p,"eventSeconds",s.getDrivingEventSeconds());
        set(p,"logCount",s.getActivityLog().size());
        for(int i=0;i<s.getActivityLog().size();i++)set(p,"log."+i,s.getActivityLog().get(i));
        return p;
    }
    private static String required(Properties p,String k) {
        String v=p.getProperty(k);
        if(v==null)throw new IllegalArgumentException("Data hilang: "+k);
        return v;
    }
    private static long integer(Properties p,String k,long lo,long hi) {
        try {
            long v=Long.parseLong(required(p,k));
            if(v<lo||v>hi)throw new NumberFormatException();
            return v;
        }
        catch(NumberFormatException ex) {
            throw new IllegalArgumentException("Bilangan di luar batas: "+k);
        }
    }
    private static double decimal(Properties p,String k,double lo,double hi) {
        try {
            double v=Double.parseDouble(required(p,k));
            if(!Double.isFinite(v)||v<lo||v>hi)throw new NumberFormatException();
            return v;
        }
        catch(NumberFormatException ex) {
            throw new IllegalArgumentException("Angka tidak valid: "+k);
        }
    }
    private static boolean bool(Properties p,String k) {
        String v=required(p,k);
        if(v.equals("true"))return true;
        if(v.equals("false"))return false;
        throw new IllegalArgumentException("Boolean tidak valid: "+k);
    }
    public static GameState fromProperties(Properties p) {
        if(integer(p,"version",1,1)!=1)throw new IllegalArgumentException("Versi save berbeda");
        GameState s=new GameState(required(p,"name"));
        s.setDay((int)integer(p,"day",1,9999));
        s.setMinutes(decimal(p,"minutes",0,1439));
        Location loc=Location.valueOf(required(p,"location"));
        s.setLocation(loc);
        double x=decimal(p,"x",0,1000),y=decimal(p,"y",0,600),mx=decimal(p,"mapX",0,1000),my=decimal(p,"mapY",0,600);
        boolean mounted=p.getProperty("mounted")==null||bool(p,"mounted");
        s.setMounted(mounted);
        s.setPosition(x,y);
        s.setMapPosition(mx,my);
        if(p.getProperty("parkedX")!=null&&p.getProperty("parkedY")!=null) {
            s.setParkedPosition(decimal(p,"parkedX",0,1000),decimal(p,"parkedY",0,600));
        }
        s.setFacing(decimal(p,"facing",-Math.PI,Math.PI));
        s.setMoney(integer(p,"money",0,GameState.MAX_MONEY));
        if(p.getProperty("sanity")!=null)s.setSanity((int)integer(p,"sanity",0,100));
        if(p.getProperty("activeQuestId")!=null)s.setActiveQuestId(required(p,"activeQuestId"));
        if(p.getProperty("questStep")!=null)s.setQuestStep((int)integer(p,"questStep",0,10));
        if(p.getProperty("questsCompletedToday")!=null)s.setQuestsCompletedToday((int)integer(p,"questsCompletedToday",0,100000));
        if(p.getProperty("totalQuestsCompleted")!=null)s.setTotalQuestsCompleted((int)integer(p,"totalQuestsCompleted",0,1000000));
        if(p.getProperty("questEarningsToday")!=null)s.setQuestEarningsToday(integer(p,"questEarningsToday",0,GameState.MAX_MONEY));
        String v=required(p,"vehicle");
        s.setVehicleType(v.equals("NONE")?null:VehicleType.valueOf(v));
        for(VehicleType t:VehicleType.values()) {
            s.setFuelMl(t,(int)integer(p,"fuel."+t.name(),0,(int)(t.create().getKapasitasTangki()*1000)));
            s.setFuelRemainder(t,decimal(p,"remainder."+t.name(),0,Math.nextDown(1.0)));
        }
        for(FoodType f:FoodType.values())s.getInventory().set(f,(int)integer(p,"food."+f.name(),0,Inventory.MAX_ITEMS));
        for(AnimalType a:AnimalType.values()) {
            s.setFriendship(a,(int)integer(p,"friend."+a.name(),0,100));
            s.setCooldown(a,decimal(p,"cooldown."+a.name(),0,2));
        }
        String fed=required(p,"fed");
        if(!fed.isEmpty())for(String a:fed.split(",",-1))s.addFed(AnimalType.valueOf(a));
        s.setDayStatus(DayStatus.valueOf(required(p,"status")));
        s.setVisitedPark(bool(p,"visitedPark"));
        s.setReturnedHome(bool(p,"returnedHome"));
        s.setDeparted(bool(p,"departed"));
        s.setCityExplored(bool(p,"cityExplored"));
        s.setCityWorkDone(bool(p,"cityWorkDone"));
        s.setLowFuelWarned(bool(p,"lowFuelWarned"));
        s.setNightWarned(bool(p,"nightWarned"));
        if(p.getProperty("lowSanityWarned")!=null)s.setLowSanityWarned(bool(p,"lowSanityWarned"));
        s.setFoodBought((int)integer(p,"foodBought",0,1_000_000));
        s.setFoodGiven((int)integer(p,"foodGiven",0,1_000_000));
        s.setEmergencyCount((int)integer(p,"emergencyCount",0,1_000_000));
        s.setDrivingEventSeconds(decimal(p,"eventSeconds",0,8));
        int logs=(int)integer(p,"logCount",0,50);
        List<String> lines=new ArrayList<>();
        for(int i=0;i<logs;i++) {
            String line=required(p,"log."+i);
            if(line.length()>500)throw new IllegalArgumentException("Log terlalu panjang");
            lines.add(line);
        }
        s.replaceLog(lines);
        if(!WorldLayout.canDrive(mx,my))throw new IllegalArgumentException("Posisi jalan tidak valid");
        if(loc==Location.MAP&&mounted&&(!WorldLayout.canDrive(x,y)||Math.abs(x-mx)>1e-6||Math.abs(y-my)>1e-6))throw new IllegalArgumentException("Posisi kendaraan tidak konsisten");
        if(loc==Location.MAP&&!mounted&&!WorldLayout.canWalk(Location.MAP,x,y))throw new IllegalArgumentException("Posisi jalan kaki di kota tidak valid");
        if((loc==Location.PARK||loc==Location.CITY)&&!WorldLayout.canWalk(loc,x,y))throw new IllegalArgumentException("Posisi karakter terhalang");
        if(loc!=Location.HOME&&(s.getVehicleType()==null||!s.isDeparted()))throw new IllegalArgumentException("Perjalanan tanpa kendaraan");
        if(s.getDayStatus()==DayStatus.ENDED&&loc!=Location.HOME)throw new IllegalArgumentException("Ending harus di rumah");
        if(s.isReturnedHome()&&!s.isDeparted())throw new IllegalArgumentException("Progres pulang tidak valid");
        if((!s.getFedAnimals().isEmpty()||loc==Location.PARK)&&!s.isVisitedPark())throw new IllegalArgumentException("Progres taman tidak valid");
        return s;
    }
}
