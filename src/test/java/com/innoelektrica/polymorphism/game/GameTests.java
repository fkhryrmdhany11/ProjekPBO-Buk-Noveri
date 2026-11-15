package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.*;
import com.innoelektrica.polymorphism.util.*;
import com.innoelektrica.polymorphism.view.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import javax.imageio.ImageIO;
public final class GameTests {
    private static int assertions;
    private static final Path QA=Path.of("out/qa");
    @FunctionalInterface interface Checked {
        void run()throws Exception;
    }
    static void check(boolean value,String label) {
        assertions++;
        if(!value)throw new AssertionError(label);
    }
    static void close(double a,double b,String label) {
        check(Math.abs(a-b)<1e-6,label+" "+a+" != "+b);
    }
    static void rejects(Checked r,String label)throws Exception {
        boolean failed=false;
        try {
            r.run();
        }
        catch(IllegalArgumentException|IllegalStateException|IOException ex) {
            failed=true;
        }
        check(failed,label);
    }
    static final class ControlledRandom extends Random {
        private final int roll,queue;
        ControlledRandom(int r,int q) {
            roll=r;
            queue=q;
        }
        @Override public int nextInt(int bound) {
            return bound==4?queue:roll;
        }
    }
    static GameManager fresh() {
        return new GameManager(new GameState("Pemain Uji"),new ControlledRandom(0,0),false);
    }
    static GameManager drive() {
        GameManager g=fresh();
        g.chooseVehicle(VehicleType.MOTOR);
        g.departHome();
        return g;
    }
    static GameManager park() {
        GameManager g=drive();
        g.getState().setPosition(650,150);
        g.enterNearbyLocation();
        return g;
    }
    static GameManager fuel(int queue) {
        GameManager g=new GameManager(new GameState("Uji SPBU"),new ControlledRandom(0,queue),false);
        g.chooseVehicle(VehicleType.MOTOR);
        g.departHome();
        g.getState().setPosition(695,470);
        g.enterNearbyLocation();
        return g;
    }
    static GameManager city() {
        GameManager g=drive();
        g.getState().setPosition(370,150);
        g.enterNearbyLocation();
        return g;
    }
    static void seconds(GameManager g,double s) {
        for(int i=0;i<(int)Math.ceil(s/.05);i++)g.tick(.05,0,0);
    }
    static void nearAnimal(GameManager g,AnimalType a) {
        var p=g.getAnimalPosition(a);
        g.getState().setPosition(p.x-30,p.y+20);
    }
    static void basics()throws Exception {
        GameManager g=fresh();
        GameState s=g.getState();
        check(s.getDay()==1,"hari awal");
        check(s.getClock().equals("07:00"),"jam awal");
        check(s.getMoney()==50000,"saldo awal");
        check(s.getLocation()==Location.HOME,"rumah awal");
        check(s.getVehicleType()==null,"pilihan belum dibuat");
        for(FoodType f:FoodType.values())check(s.getInventory().get(f)==0,"tas kosong "+f);
        check(s.getFuelMl(VehicleType.MOTOR)==7000,"motor70%");
        check(s.getFuelMl(VehicleType.MOBIL)==28000,"mobil70%");
        check(!g.departHome().success(),"harus pilih kendaraan");
        check(g.chooseVehicle(VehicleType.MOTOR).success(),"pilihmotor");
        check(g.departHome().success(),"berangkat");
        check(!g.chooseVehicle(VehicleType.MOBIL).success(),"pilihan dikunci");
        check(s.isDeparted(),"progresberangkat");
        check(new Motor().bergerak()>new Mobil().bergerak(),"motorspeed");
        check(new Motor().getKonsumsiPerTick()<new Mobil().getKonsumsiPerTick(),"hematmotor");
        close(new Motor().getKapasitasTangki(),10,"kap10");
        close(new Mobil().getKapasitasTangki(),40,"kap40");
        s.setPosition(265,470);
        g.enterNearbyLocation();
        check(g.endDay().success(),"endingbolehbelumlengkap");
        check(!s.isMissionComplete(),"tidak mengaku sukses");
        check(g.nextDay().success(),"nextday");
        check(s.getDay()==2&&s.getClock().equals("07:00"),"reset hari/jam");
        rejects(()->new GameState("  "),"namakosong");
        rejects(()->new GameState("x\ny"),"namacontrol");
        try {
            s.getInventory().getItems().put(FoodType.ROTI,1);
            check(false,"readonly inventory");
        }
        catch(UnsupportedOperationException ex) {
            check(true,"readonly inventory");
        }
        System.out.println("OK awal, pilihan kendaraan, ending belum lengkap, hari berikutnya");
    }
    static void movement()throws Exception {
        GameManager g=drive();
        GameState s=g.getState();
        double x=s.getX();
        g.tick(.05,1,0);
        close(s.getX()-x,7,"gerakmotor7px");
        check(s.getFuelMl(VehicleType.MOTOR)==6994,"konsumsi6ml");
        int f=s.getFuelMl(VehicleType.MOTOR);
        seconds(g,1);
        check(s.getFuelMl(VehicleType.MOTOR)==f,"idlehemat");
        double t=s.getMinutes();
        g.setPaused(true);
        g.tick(.25,1,0);
        close(s.getMinutes(),t,"pausejam");
        check(s.getFuelMl(VehicleType.MOTOR)==f,"pausebbm");
        g.setPaused(false);
        g.setUiBlocked(true);
        g.tick(.25,1,0);
        close(s.getMinutes(),t,"dialogjam");
        g.setUiBlocked(false);
        s.setPosition(500,470);
        g.tick(.05,1,-1);
        close(Math.hypot(s.getX()-500,s.getY()-470),7,"diagonalnormalisasi");
        s.setPosition(265,455);
        f=s.getFuelMl(VehicleType.MOTOR);
        g.tick(.05,0,-1);
        close(s.getY(),455,"tepi jalan");
        check(s.getFuelMl(VehicleType.MOTOR)==f,"terhalang hemat");
        s.setPosition(265,470);
        s.setFuelMl(VehicleType.MOTOR,1);
        s.setFuelRemainder(VehicleType.MOTOR,0);
        g.tick(.05,1,0);
        check(s.getFuelMl(VehicleType.MOTOR)==0,"bbmnol");
        x=s.getX();
        g.tick(.25,1,0);
        close(s.getX(),x,"berhentinol");
        check(s.isLowFuelWarned(),"warningsekali");
        s.setMoney(5000);
        check(g.emergencyFuel().success(),"bantuanberbayar");
        check(s.getMoney()==0&&s.getFuelMl(VehicleType.MOTOR)==2000,"biayavolumedarurat");
        check(!g.emergencyFuel().success(),"batasbantuan");
        s.setFuelMl(VehicleType.MOTOR,0);
        t=s.getMinutes();
        check(g.emergencyFuel().success(),"subsididarurat");
        check(s.getMoney()==0&&s.getMinutes()==t+90,"subsidiwaktu");
        s.setPosition(500,470);
        check(!g.enterNearbyLocation().success(),"pintujauhditolak");
        GameManager p=park();
        f=p.getState().getFuelMl(VehicleType.MOTOR);
        p.tick(.25,1,0);
        check(p.getState().getFuelMl(VehicleType.MOTOR)==f,"berjalanhemat");
        check(!WorldLayout.canDrive(400,250),"bangunanjalan");
        check(!WorldLayout.canDrive(5,150),"bataspeta");
        check(!WorldLayout.canWalk(Location.PARK,700,180),"kolamhalangan");
        check(!WorldLayout.canWalk(Location.PARK,100,140),"tokohalangan");
        check(!WorldLayout.canWalk(Location.PARK,0,0),"batasjalan");
        check(!WorldLayout.canWalk(Location.MAP,250,340),"rumahhalangan_map");
        check(!WorldLayout.canWalk(Location.MAP,390,280),"pusatkotahalangan_map");
        check(!WorldLayout.canWalk(Location.MAP,680,250),"tamanhalangan_map");
        check(!WorldLayout.canWalk(Location.MAP,680,380),"spbuhalangan_map");
        check(!WorldLayout.canWalk(Location.CITY,150,150),"balaikotahalangan_city");
        check(!WorldLayout.canWalk(Location.CITY,400,150),"kantorkurirhalangan_city");
        check(!WorldLayout.canWalk(Location.CITY,300,380),"kiospakrakahalangan_city");
        check(!WorldLayout.canWalk(Location.CITY,700,375),"kolamplazahalangan_city");
        // Uji Jalur Kiri (Left-Hand Traffic) untuk keempat arah gerak
        GameManager laneGm=drive();
        GameState ls=laneGm.getState();
        ls.setPosition(265,470);
        for(int i=0;i<16;i++)laneGm.tick(.05,1,0);
        check(ls.getY()<462,"mobil/motor mengarah kanan berada di lajur kiri (atas sumbu jalan)");
        ls.setPosition(700,150);
        for(int i=0;i<16;i++)laneGm.tick(.05,-1,0);
        check(ls.getY()>158,"mengarah kiri berada di lajur kiri (bawah sumbu jalan)");
        ls.setPosition(500,350);
        for(int i=0;i<16;i++)laneGm.tick(.05,0,-1);
        check(ls.getX()<492,"mengarah atas berada di lajur kiri (kiri sumbu jalan)");
        ls.setPosition(500,200);
        for(int i=0;i<16;i++)laneGm.tick(.05,0,1);
        check(ls.getX()>508,"mengarah bawah berada di lajur kiri (kanan sumbu jalan)");
        // Uji 9 Kendaraan NPC di Jalan Raya, Suara Ramai Lalu Lintas (Hanya di MAP), & Klakson
        check(laneGm.getTrafficVehicles().size()==9,"9 kendaraan lalu lintas (campuran motor & mobil) di jalan raya");
        double npcBeforeX=laneGm.getTrafficVehicles().get(0).x();
        double npcBeforeY=laneGm.getTrafficVehicles().get(0).y();
        laneGm.tick(.25,0,0);
        var npcAfter=laneGm.getTrafficVehicles().get(0);
        check(npcAfter.x()!=npcBeforeX||npcAfter.y()!=npcBeforeY,"kendaraan NPC bergerak di jalan raya");
        check(laneGm.honkVehicle().success(),"klakson kendaraan berhasil");
        var firstNpc=laneGm.getTrafficVehicles().get(0);
        ls.setPosition(firstNpc.x()-20,firstNpc.y());
        int soundBeforePass=laneGm.getTrafficSoundEventCount();
        laneGm.tick(.1,0,0);
        check(laneGm.isAnyTrafficVehicleNearPlayer()&&laneGm.getTrafficSoundEventCount()>soundBeforePass,"berpapasan dekat kendaraan lain di MAP memicu suara kendaraan NPC");
        int soundBeforeAmbient=laneGm.getTrafficSoundEventCount();
        seconds(laneGm,3.5);
        check(laneGm.getTrafficSoundEventCount()>soundBeforeAmbient,"suara klakson lalu lintas kota aktif berkala di MAP");
        GameManager citySilence=park(); // Di PARK / luar MAP, suara lalu lintas kota tidak aktif
        int parkTrafficBefore=citySilence.getTrafficSoundEventCount();
        seconds(citySilence,3.5);
        check(citySilence.getTrafficSoundEventCount()==parkTrafficBefore&&!citySilence.isAnyTrafficVehicleNearPlayer(),"suara ramai lalu lintas tidak aktif saat keluar dari jalan raya kota");
        System.out.println("OK gerak lurus/diagonal, jalur kiri, multi-kendaraan, collision, BBM idle/pause/dialog/nol, bantuan");
    }
    static void foodAnimals()throws Exception {
        GameManager g=park();
        GameState s=g.getState();
        s.setPosition(140,240);
        check(!g.buyFood(FoodType.DAGING,0).success(),"qty0");
        check(!g.buyFood(FoodType.DAGING,-1).success(),"qtyneg");
        check(!g.buyFood(FoodType.DAGING,100).success(),"qty100");
        check(!g.buyFood(FoodType.DAGING,6).success(),"uangkurang");
        check(s.getMoney()==50000&&s.getInventory().get(FoodType.DAGING)==0,"penolakanatomic");
        check(g.buyFood(FoodType.DAGING,3).success(),"belidaging");
        check(g.buyFood(FoodType.BIJI_BIJIAN,1).success(),"belibiji");
        check(g.buyFood(FoodType.ROTI,1).success(),"beliroti");
        check(s.getMoney()==8000,"saldo42000");
        s.getInventory().set(FoodType.ROTI,999);
        check(!g.buyFood(FoodType.ROTI,1).success(),"kapasitastas");
        s.getInventory().set(FoodType.ROTI,1);
        nearAnimal(g,AnimalType.KUCING);
        s.setFriendship(AnimalType.KUCING,1);
        check(!g.feedAnimal(AnimalType.KUCING,FoodType.ROTI).success(),"salahmakanan");
        check(s.getInventory().get(FoodType.ROTI)==1,"makanankembali");
        check(s.getFriendship(AnimalType.KUCING)==0,"minfr0");
        check(!g.feedAnimal(AnimalType.KUCING,FoodType.DAGING).success(),"cooldown");
        seconds(g,2.1);
        check(g.feedAnimal(AnimalType.KUCING,FoodType.DAGING).success(),"sesuaikucing");
        check(s.getFriendship(AnimalType.KUCING)==10&&s.getInventory().get(FoodType.DAGING)==2,"frstok");
        check(s.getFedAnimals().size()==1,"jenisdifferent1");
        seconds(g,2.1);
        g.feedAnimal(AnimalType.KUCING,FoodType.DAGING);
        seconds(g,2.1);
        g.feedAnimal(AnimalType.KUCING,FoodType.DAGING);
        check(s.getFedAnimals().size()==1,"ulanghewanbukandua");
        check(g.getFollowingAnimal()==AnimalType.KUCING,"followfr30");
        var before=g.getAnimalPosition(AnimalType.KUCING);
        s.setPosition(520,360);
        seconds(g,1);
        check(before.distance(g.getAnimalPosition(AnimalType.KUCING))>0,"followgeraknyata");
        nearAnimal(g,AnimalType.BURUNG);
        check(g.feedAnimal(AnimalType.BURUNG,FoodType.BIJI_BIJIAN).success(),"jeniskedua");
        check(!s.isMissionComplete(),"belumpulang");
        s.setPosition(145,505);
        check(g.leaveLocation().success(),"leavepark");
        s.setPosition(265,470);
        check(g.enterNearbyLocation().success(),"homeenter");
        check(s.isMissionComplete(),"misi2hewandanpulang");
        check(s.getMoney()==8000&&s.getInventory().get(FoodType.ROTI)==1&&s.getFriendship(AnimalType.KUCING)==30,"transisistate");
        g.endDay();
        int f=s.getFuelMl(VehicleType.MOTOR);
        g.nextDay();
        check(s.getMoney()==8000&&s.getInventory().get(FoodType.ROTI)==1&&s.getFriendship(AnimalType.KUCING)==30&&s.getFuelMl(VehicleType.MOTOR)==f,"nextdaypreserve");
        check(s.getFedAnimals().isEmpty()&&!s.isVisitedPark()&&!s.isReturnedHome()&&!s.isDeparted(),"resetprogres");
        for(AnimalType a:AnimalType.values()) {
            GameManager h=park();
            nearAnimal(h,a);
            FoodType correct=h.getAnimal(a).getMakananFavorit();
            h.getState().getInventory().set(correct,1);
            check(h.feedAnimal(a,correct).success(),"overridefood "+a);
            seconds(h,2.1);
            check(!h.feedAnimal(a,correct).success(),"stock0 "+a);
            h.getState().setFriendship(a,99);
            check(h.petAnimal(a).success(),"pet "+a);
            check(h.getState().getFriendship(a)==100,"maxfr "+a);
            seconds(h,2.1);
            h.getState().setMinutes(1080);
            check(!h.petAnimal(a).success(),"nightrest "+a);
        }
        GameManager n=park();
        n.getState().setPosition(140,240);
        n.getState().setMinutes(1080);
        check(!n.buyFood(FoodType.ROTI,1).success(),"tokotutup");
        System.out.println("OK beli/penolakan makanan, lima override, friendship/cooldown/follow, misi & state scene");
    }
    static void cityTime()throws Exception {
        GameManager g=city();
        GameState s=g.getState();
        check(!g.beginCityWork().success(),"npcjauh");
        s.setPosition(285,445);
        check(g.beginCityWork().success(),"kerjamulai");
        check(!g.beginCityWork().success(),"kerjatidakganda");
        check(!g.canPersist(),"kerjabloksave");
        long m=s.getMoney();
        g.setPaused(true);
        seconds(g,5);
        check(s.getMoney()==m&&!s.isCityWorkDone(),"kerjapause");
        g.setPaused(false);
        seconds(g,4.1);
        check(s.getMoney()==m+15000&&s.isCityWorkDone(),"kerjacommit");
        seconds(g,5);
        check(s.getMoney()==m+15000,"kerjasekali");
        check(!g.beginCityWork().success(),"kerjasatuhari");
        s.setPosition(650,440);
        double t=s.getMinutes();
        check(g.exploreCity().success(),"plaza");
        close(s.getMinutes(),t+15,"plazawaktu");
        check(!g.exploreCity().success(),"plazasekali");
        for(double[] pair:new double[][] {
            {
                0,3
            }
            , {
                359,3
            }
            , {
                360,0
            }
            , {
                659,0
            }
            , {
                660,1
            }
            , {
                839,1
            }
            , {
                840,2
            }
            , {
                1079,2
            }
            , {
                1080,3
            }
            , {
                1439,3
            }
        }
        )check(TimePeriod.at(pair[0]).ordinal()==(int)pair[1],"fase "+pair[0]);
        s.setMinutes(1079.9);
        g.tick(.1,0,0);
        check(s.isNightWarned(),"nightwarning");
        int logs=s.getActivityLog().size();
        seconds(g,2);
        check(s.getActivityLog().size()==logs,"nightwarningspam");
        s.setMinutes(1438.9);
        seconds(g,2);
        close(s.getMinutes(),1439,"clockcap");
        System.out.println("OK NPC, aktivitas sekali per hari, periode waktu, malam, batas 23.59");
    }
    static void filling()throws Exception {
        GameManager g=fuel(0);
        GameState s=g.getState();
        s.setFuelMl(VehicleType.MOTOR,9950);
        s.setMoney(1000);
        FuelQuote q=g.previewFuel(50000);
        check(q.milliliters()==50&&q.cost()==500,"hampirpenuhbiaya");
        check(g.beginFuel(q).success(),"fillbegin");
        check(!g.beginFuel(q).success(),"antidouble");
        check(!g.canPersist(),"fillblocksave");
        check(!g.leaveLocation().success(),"fillblockleave");
        seconds(g,2);
        check(s.getMoney()==1000&&s.getFuelMl(VehicleType.MOTOR)==9950,"pendingatomic");
        check(g.cancelFuel().success(),"cancel");
        seconds(g,8);
        check(s.getMoney()==1000&&s.getFuelMl(VehicleType.MOTOR)==9950,"cancelnocharge");
        check(g.beginFuel(g.previewFuel(50000)).success(),"restartfill");
        g.setPaused(true);
        seconds(g,6);
        check(s.getMoney()==1000,"fillpause");
        g.setPaused(false);
        seconds(g,5.1);
        check(s.getMoney()==500&&s.getFuelMl(VehicleType.MOTOR)==10000,"commitonce");
        seconds(g,10);
        check(s.getMoney()==500&&s.getFuelMl(VehicleType.MOTOR)==10000,"nomorecharge");
        rejects(()->g.previewFuel(10000),"fullreject");
        s.setFuelMl(VehicleType.MOTOR,1000);
        s.setMoney(3500);
        for(long n:new long[] {
            0,-1,9
        }
        )rejects(()->g.previewFuel(n),"nominal "+n);
        q=g.previewFuel(50000);
        check(q.milliliters()==350&&q.cost()==3500,"saldoterbatas");
        check(g.previewFuel(Long.MAX_VALUE).cost()==3500,"tanpaoverflow");
        check(!g.beginFuel(new FuelQuote(50000,351,3510,1000,3500,VehicleType.MOTOR)).success(),"forgedquote");
        s.setMoney(3000);
        check(!g.beginFuel(q).success(),"stalequote");
        GameManager queue=fuel(3);
        check(queue.getQueueCount()==3,"queue3");
        check(!queue.canPersist(),"queueblocksave");
        rejects(()->queue.previewFuel(10000),"noturn");
        queue.setPaused(true);
        seconds(queue,5);
        check(queue.getQueueCount()==3,"queuepause");
        queue.setPaused(false);
        seconds(queue,6.1);
        check(queue.getQueueCount()==0&&queue.canPersist(),"queueready");
        check(queue.leaveLocation().success(),"fuelleave");
        System.out.println("OK antrean, nominal/saldo/tangki, preview valid, pembayaran ganda, pause/batal atomik");
    }
    static void events()throws Exception {
        int[] counts=new int[4];
        for(int i=0;i<100;i++)counts[TrafficEvent.choose(i).ordinal()]++;
        check(Arrays.equals(counts,new int[] {
            70,10,10,10
        }
        ),"probabilitas100kesempatan");
        for(int roll:new int[] {
            75,85,95
        }
        ) {
            GameManager g=new GameManager(new GameState("Event"),new ControlledRandom(roll,0),false);
            g.chooseVehicle(VehicleType.MOTOR);
            g.departHome();
            GameState s=g.getState();
            for(int i=0;i<160;i++) {
                s.setPosition(265,470);
                g.tick(.05,1,0);
            }
            TrafficEvent e=TrafficEvent.choose(roll);
            check(g.getTrafficEvent()==e,"eventcooldown8sec "+e);
            check(!g.canPersist(),"eventsaveguard");
            s.setPosition(265,470);
            int fuel=s.getFuelMl(VehicleType.MOTOR);
            g.tick(.05,1,0);
            if(e==TrafficEvent.JAM) {
                close(s.getX()-265,3.5,"macetsetengah");
                check(s.getFuelMl(VehicleType.MOTOR)<fuel,"macetnyata");
            }
            else {
                close(s.getX(),265,"eventberhenti");
                check(s.getFuelMl(VehicleType.MOTOR)==fuel,"eventhemat");
            }
            seconds(g,6);
            check(g.getTrafficEvent()==TrafficEvent.NORMAL&&g.canPersist(),"eventselesai");
        }
        System.out.println("OK peluang event 70/10/10/10, cooldown 8 detik, stop/macet, akhir event");
    }
    static void persistence()throws Exception {
        Path dir=Files.createTempDirectory("city-test-");
        Path path=dir.resolve("save.properties");
        SaveService save=new SaveService(path);
        rejects(save::load,"missingsave");
        GameManager g=new GameManager(PersistenceProbe.sample(),new ControlledRandom(0,0),false);
        save.save(g);
        GameState loaded=save.load();
        check(SaveService.toProperties(g.getState()).equals(SaveService.toProperties(loaded)),"roundtripsemuafields");
        byte[] old=Files.readAllBytes(path);
        rejects(()->save.save(new DemoController().getGame()),"demosaveblock");
        check(Arrays.equals(old,Files.readAllBytes(path)),"demobytesunchanged");
        GameManager busy=fuel(0);
        busy.beginFuel(busy.previewFuel(10000));
        rejects(()->save.save(busy),"busysaveblock");
        check(Arrays.equals(old,Files.readAllBytes(path)),"busyoldintact");
        Properties valid=SaveService.toProperties(g.getState());
        for(String[] bad:new String[][] {
            {
                "money","-1"
            }
            , {
                "minutes","NaN"
            }
            , {
                "minutes","1440"
            }
            , {
                "day","0"
            }
            , {
                "fuel.MOTOR","10001"
            }
            , {
                "remainder.MOTOR","1"
            }
            , {
                "food.ROTI","-3"
            }
            , {
                "friend.KUCING","101"
            }
            , {
                "cooldown.KUCING","9"
            }
            , {
                "location","UNKNOWN"
            }
            , {
                "departed","yes"
            }
            , {
                "fed","SAPI"
            }
            , {
                "mapX","0"
            }
            , {
                "x","1001"
            }
            , {
                "status","ENDED"
            }
            , {
                "version","2"
            }
            , {
                "name",""
            }
            , {
                "eventSeconds","9"
            }
        }
        ) {
            Properties p=new Properties();
            p.putAll(valid);
            p.setProperty(bad[0],bad[1]);
            rejects(()->SaveService.fromProperties(p),"rusak "+bad[0]+"="+bad[1]);
        }
        Properties missing=new Properties();
        missing.putAll(valid);
        missing.remove("money");
        rejects(()->SaveService.fromProperties(missing),"keymissing");
        Files.writeString(path,"bukan save yang valid");
        rejects(save::load,"filegarbage");
        check(g.getState().getMoney()==27000&&g.getState().getLocation()==Location.PARK,"failedloadstateintact");
        Files.write(path,old);
        g.getState().setMoney(-1);
        rejects(()->save.save(g),"invalidwrite");
        check(Arrays.equals(old,Files.readAllBytes(path)),"failedwriteoldintact");
        Path blocker=dir.resolve("file-parent");
        Files.writeString(blocker,"blocked");
        rejects(()->new SaveService(blocker.resolve("save.properties")).save(fresh()),"ioblocked");
        try(var stream=Files.list(dir)) {
            check(stream.noneMatch(p->p.getFileName().toString().endsWith(".tmp")),"tempcleanup");
        }
        String java=Path.of(System.getProperty("java.home"),"bin",System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")?"java.exe":"java").toString();
        String cp=System.getProperty("java.class.path");
        for(String mode:new String[] {
            "write","read"
        }
        ) {
            int code=new ProcessBuilder(java,"-Djava.awt.headless=true","-cp",cp,PersistenceProbe.class.getName(),mode,path.toString()).inheritIO().start().waitFor();
            check(code==0,"separateJVM "+mode);
        }
        System.out.println("OK save/load semua fields, save rusak/IO, write atomik, dua JVM independen");
    }
    static void demo()throws Exception {
        GameManager player=drive();
        player.getState().setMoney(32123);
        Properties snapshot=SaveService.toProperties(player.getState());
        Path p=Files.createTempDirectory("city-demo-").resolve("save.properties");
        SaveService save=new SaveService(p);
        save.save(player);
        byte[] before=Files.readAllBytes(p);
        DemoController demo=new DemoController();
        boolean success=false,mobil=false;
        EnumSet<Location> captured=EnumSet.noneOf(Location.class);
        int steps=0;
        for(;steps<16000&&!demo.isFinished();steps++) {
            demo.update(.05);
            GameManager g=demo.getGame();
            g.tick(.05,demo.getInputX(),demo.getInputY());
            GameState s=g.getState();
            if(s.getDay()==1&&s.getDayStatus()==DayStatus.ENDED)success=s.isMissionComplete();
            if(s.getDay()==2&&s.getVehicleType()==VehicleType.MOBIL)mobil=true;
            if(captured.add(s.getLocation())) {
                GameScreen[] screen= {
                    null
                }
                ;
                SwingUtilities.invokeAndWait(()-> {
                    screen[0]=new GameScreen(()->g,new InputController(),new QuietUi());
                    screen[0].refresh();
                    render(screen[0],1280,850,"demo-"+s.getLocation().name().toLowerCase(Locale.ROOT));
                }
                );
            }
        }
        check(demo.isFinished(),"demo lengkap, stage="+demo.getCaption()+" pos="+demo.getGame().getState().getX()+","+demo.getGame().getState().getY());
        check(success,"demo misi pertama berhasil");
        check(mobil,"demo mobilhari2");
        check(steps*.05>=180&&steps*.05<=300,"demo3-5menit "+steps*.05);
        check(captured.size()==5,"demo5scene");
        check(SaveService.toProperties(player.getState()).equals(snapshot),"demo playerstateutuh");
        check(Arrays.equals(before,Files.readAllBytes(p)),"demo saveutuh");
        check(!demo.getGame().canPersist(),"demosaveforbidden");
        check(!demo.getGame().getState().isMissionComplete(),"demoending2jujur");
        System.out.printf(Locale.ROOT,"OK Auto Demo %.1f detik (%d tahap), misi pertama berhasil, state/save pemain utuh%n",steps*.05,demo.getStageCount());
    }
    static class QuietUi implements UiActions {
        int interactions;
        public void interact() {
            interactions++;
        }
        public void inventory() {
        }
        public void togglePause() {
        }
        public void saveGame() {
        }
        public void loadGame() {
        }
        public void menu() {
        }
        public void help() {
        }
        public void demo() {
        }
        public void emergency() {
        }
        public void leave() {
        }
        public void message(String s) {
        }
        public void animal(AnimalType a) {
        }
        public void shop() {
        }
        public void home() {
        }
        public void cityNpc() {
        }
        public void fuel() {
        }
    }
    static void trigger(JComponent c,String key) {
        Action a=c.getActionMap().get(key);
        if(a==null)throw new AssertionError(key);
        a.actionPerformed(new ActionEvent(c,ActionEvent.ACTION_PERFORMED,key));
    }
    static void layout(Container c) {
        c.doLayout();
        for(Component child:c.getComponents())if(child instanceof Container co)layout(co);
    }
    static void render(JComponent c,int w,int h,String filename) {
        try {
            c.setSize(w,h);
            layout(c);
            BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=im.createGraphics();
            c.paint(g);
            g.dispose();
            ImageIO.write(im,"png",QA.resolve(filename+".png").toFile());
            Path docsShots=Path.of("docs/screenshots");
            if(Files.isDirectory(docsShots)) {
                ImageIO.write(im,"png",docsShots.resolve(filename+".png").toFile());
            }
        }
        catch(IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    static void pixelOverhaulFeatures()throws Exception {
        // 1. Polymorphism Hewan.getSanityBonus(), Suara Sebelum/Sesudah Makan & Kendaraan Audio
        check(new Anjing().getSanityBonus()==20,"sanity anjing 20");
        check(new Kucing().getSanityBonus()==18,"sanity kucing 18");
        check(new Angsa().getSanityBonus()==16,"sanity angsa 16");
        check(new Burung().getSanityBonus()==14,"sanity burung 14");
        check(new Ayam().getSanityBonus()==12,"sanity ayam 12");
        for(AnimalType a:AnimalType.values()) {
            Hewan h=a.create();
            check(!h.getSuaraSebelumMakan().equals(h.getSuaraSesudahMakan()),"suara sebelum/sesudah makan beda "+a);
        }
        check(new Motor().getKapasitasKargo()==1&&new Mobil().getKapasitasKargo()==2,"kargo motor vs mobil");
        check(new Motor().getAudioResource().equals("/audio/motor.wav"),"audio motor");
        check(new Mobil().getAudioResource().equals("/audio/mobil.wav"),"audio mobil");
        check(new Motor().getHornResource().equals("/audio/klakson_motor.wav"),"klakson motor");
        check(new Mobil().getHornResource().equals("/audio/klakson_mobil.wav"),"klakson mobil");
        check(!new Motor().getSuaraMesin().equals(new Mobil().getSuaraMesin()),"suara mesin beda");
        close(new Motor().getMultiplierKurir(),1.0,"kurir motor 1.0x");
        close(new Mobil().getMultiplierKurir(),1.5,"kurir mobil 1.5x");

        // 2. Mount / Dismount Kendaraan di Jalan Kota (Hemat BBM saat jalan kaki)
        GameManager g=drive();
        GameState s=g.getState();
        check(s.isMounted(),"awal berkendara");
        int fuelBefore=s.getFuelMl(VehicleType.MOTOR);
        check(g.toggleMount().success(),"turun dari motor");
        check(!s.isMounted(),"status jalan kaki");
        double walkX=s.getX();
        g.tick(.05,1,0);
        check(s.getX()>walkX,"jalan kaki bergerak di peta");
        check(s.getFuelMl(VehicleType.MOTOR)==fuelBefore,"jalan kaki tidak kuras BBM");
        s.setPosition(800,150);
        check(!g.toggleMount().success(),"terlalu jauh dari parkir motor");
        s.setPosition(s.getParkedX()+10,s.getParkedY());
        check(g.toggleMount().success(),"naik kembali ke motor");
        check(s.isMounted(),"kembali berkendara");

        // 3. Sistem Sanity, Bangku Taman, dan Kafe Kopi Taman
        GameManager pk=park();
        GameState ps=pk.getState();
        ps.setSanity(25);
        check(ps.isLowSanity(),"low sanity < 30");
        check(!pk.acceptQuest(0).success(),"blokir quest kerja saat stres");
        ps.setPosition(455,145);
        check(pk.restAtParkBench().success(),"istirahat bangku taman");
        check(ps.getSanity()==45&&!ps.isLowSanity(),"sanity pulih +20 dari bangku");
        ps.setPosition(140,220);
        long moneyBefore=ps.getMoney();
        check(pk.buySnack(SnackType.KOPI_KOTA).success(),"beli es kopi kota");
        check(ps.getMoney()==moneyBefore-8000&&ps.getSanity()==67,"kopi +22 sanity");

        // 4. Sistem 3 Subclass Quest PBO (DeliveryQuest, ServiceQuest, ParkCareQuest) & Navigasi Quest
        GameManager qm=fresh();
        qm.chooseVehicle(VehicleType.MOBIL);
        qm.departHome();
        GameState qs=qm.getState();
        check(qm.acceptQuest(0).success(),"ambil DeliveryQuest dengan Mobil");
        check(qm.getActiveQuest() instanceof DeliveryQuest,"instance DeliveryQuest");
        var nav1=qm.getQuestNavigation();
        check(nav1.isQuestActive()&&nav1.targetLocation()==Location.CITY,"nav DeliveryQuest step 1 -> CITY");
        check(Math.abs(nav1.targetX()-370)<1&&Math.abs(nav1.targetY()-150)<1,"nav DeliveryQuest di MAP arahkan ke gerbang Pusat Kota (370,150)");
        qs.setPosition(370,150);
        qm.enterNearbyLocation(); // masuk CITY otomatis ambil paket step 2
        check(qs.getQuestStep()==2,"paket diambil di Pusat Kota");
        var nav2City=qm.getQuestNavigation();
        check(nav2City.targetLocation()==Location.PARK&&Math.abs(nav2City.targetX()-WorldLayout.CITY_EXIT.x)<1,"nav DeliveryQuest step 2 di CITY arahkan ke pintu keluar");
        qs.setPosition(140,500);
        qm.leaveLocation();
        var nav2Map=qm.getQuestNavigation();
        check(nav2Map.targetLocation()==Location.PARK&&Math.abs(nav2Map.targetX()-650)<1,"nav DeliveryQuest step 2 di MAP arahkan ke gerbang Taman (650,150)");
        qs.setPosition(650,150);
        qm.enterNearbyLocation(); // masuk PARK
        var nav2Park=qm.getQuestNavigation();
        check(Math.abs(nav2Park.targetX()-WorldLayout.SHOP_DOOR.x)<1,"nav DeliveryQuest step 2 di PARK arahkan ke Kios Taman");
        long beforeDeliver=qs.getMoney();
        check(qm.completeOrAdvanceQuest().success(),"selesaikan DeliveryQuest di Taman");
        check(qs.getMoney()==beforeDeliver+30000,"upah kurir mobil 1.5x = Rp30.000");
        check(qs.getQuestsCompletedToday()==1,"counter quest bertambah");

        // ServiceQuest
        check(qm.acceptQuest(1).success(),"ambil ServiceQuest");
        check(qm.getQuestNavigation().targetLocation()==Location.CITY,"nav ServiceQuest -> CITY");
        qs.setPosition(145,505);
        qm.leaveLocation();
        qs.setPosition(370,150);
        qm.enterNearbyLocation();
        check(Math.abs(qm.getQuestNavigation().targetX()-WorldLayout.CITY_NPC.x)<1,"nav ServiceQuest di CITY arahkan ke Pak Raka");
        long beforeService=qs.getMoney();
        check(qm.completeOrAdvanceQuest().success(),"selesaikan ServiceQuest");
        check(qs.getMoney()==beforeService+25000,"upah ServiceQuest Rp25.000");

        // ParkCareQuest selesai otomatis saat memberi makan hewan favorit di taman
        qs.setSanity(60);
        check(qm.acceptQuest(2).success(),"ambil ParkCareQuest");
        check(qm.getQuestNavigation().targetLocation()==Location.PARK,"nav ParkCareQuest -> PARK");
        qs.setPosition(140,500);
        qm.leaveLocation();
        qs.setPosition(650,150);
        qm.enterNearbyLocation();
        check(Math.abs(qm.getQuestNavigation().targetX()-WorldLayout.SHOP_DOOR.x)<1,"nav ParkCareQuest tanpa pakan arahkan ke Kios Taman");
        qs.getInventory().set(FoodType.DAGING,1);
        check(qm.getQuestNavigation().targetY()>240,"nav ParkCareQuest punya pakan arahkan ke Satwa");
        nearAnimal(qm,AnimalType.ANJING);
        long beforeParkQuest=qs.getMoney();
        check(qm.feedAnimal(AnimalType.ANJING,FoodType.DAGING).success(),"beri makan anjing selesaikan ParkCareQuest");
        check(qs.getMoney()==beforeParkQuest+18000&&qm.getActiveQuest()==null,"upah ParkCareQuest Rp18.000 + bonus sanity");
        check(qs.getSanity()==94,"sanity +20 (Anjing) +14 (ParkCareQuest)");
        // 4b. 15 Hewan di Taman, Filter Makanan Saat Bergrombol & Idle Roaming
        var playerAtFeed=new java.awt.geom.Point2D.Double(qs.getX(),qs.getY());
        var catHome=WorldLayout.animalHome(AnimalType.KUCING);
        var birdHome=WorldLayout.animalHome(AnimalType.BURUNG);
        check(qm.getParkAnimals().size()==15,"taman memiliki 15 satwa (3 ekor per jenis)");
        for(var pa:qm.getParkAnimals()) {
            check(WorldLayout.canWalk(Location.PARK,pa.x(),pa.y()),"satwa "+pa.type()+"#"+pa.id()+" berada di area jalan taman");
        }
        check(qm.isAnimalsGathering(),"hewan berkumpul aktif setelah diberi makan");
        check(qm.getGatherCenter()!=null&&qm.getGatherCenter().distance(playerAtFeed)<1,"pusat berkumpul berada di sekitar player");
        seconds(qm,2.0);
        check(qm.getAnimalPosition(AnimalType.KUCING).distance(playerAtFeed)<catHome.distance(playerAtFeed)-25,"kucing (pemakan DAGING) mendekat saat anjing diberi daging");
        check(qm.getAnimalPosition(AnimalType.BURUNG).distance(birdHome)<40,"burung (tidak makan DAGING) tetap di habitatnya");
        seconds(qm,14.0);
        check(!qm.isAnimalsGathering(),"durasi berkumpul selesai setelah 10 detik");
        // Gerak bebas alami (Idle Roaming) tanpa makanan & hanya hewan yang suka makanan yang bergerombol
        GameManager parkFlow=park();
        var birdStart=parkFlow.getAnimalPosition(AnimalType.BURUNG);
        seconds(parkFlow,3.2);
        var birdRoamed=parkFlow.getAnimalPosition(AnimalType.BURUNG);
        check(birdStart.distance(birdRoamed)>2,"hewan bergerak alami saat santai di taman");
        parkFlow.getState().setPosition(360,290);
        parkFlow.getState().getInventory().set(FoodType.BIJI_BIJIAN,1);
        double birdDistBeforeFood=parkFlow.getAnimalPosition(AnimalType.BURUNG).distance(360,290);
        double henDistBeforeFood=parkFlow.getAnimalPosition(AnimalType.AYAM).distance(360,290);
        seconds(parkFlow,3.0);
        check(parkFlow.getAnimalPosition(AnimalType.BURUNG).distance(360,290)<birdDistBeforeFood-40,"burung menghampiri player saat tas berisi BIJI_BIJIAN");
        check(parkFlow.getAnimalPosition(AnimalType.AYAM).distance(360,290)<henDistBeforeFood-40,"ayam menghampiri player saat tas berisi BIJI_BIJIAN");
        for(var pa:parkFlow.getParkAnimals()) {
            if(pa.type()==AnimalType.KUCING||pa.type()==AnimalType.ANGSA) {
                check(Math.hypot(pa.x()-360,pa.y()-290)>55,"kucing/angsa tidak ikut mengerumuni pemain yang hanya bawa BIJI_BIJIAN");
            }
        }
        check(parkFlow.getActiveSoundZoneAnimals().size()>=2,"banyak hewan masuk zona suara bersamaan saat bergerombol");

        // 4c. Zona Suara Tanpa Delay + Shortcut [R] Langsung Pulang + Tukar Kendaraan di Hari Sama
        GameManager zoneGm=park();
        var catPos=zoneGm.getAnimalPosition(AnimalType.KUCING);
        zoneGm.getState().setPosition(catPos.x-40,catPos.y);
        zoneGm.tick(.1,1,0);
        check(zoneGm.getReactingAnimal()==AnimalType.KUCING,"masuk area zona suara kucing memicu suara otomatis");
        check(zoneGm.getReaction().contains(zoneGm.getAnimal(AnimalType.KUCING).getSuaraSebelumMakan()),"zona suara tampilkan ekspresi sebelum makan");
        zoneGm.getState().setPosition(catPos.x-300,catPos.y);
        zoneGm.tick(.1,0,0);
        var dogPos=zoneGm.getAnimalPosition(AnimalType.ANJING);
        zoneGm.getState().setPosition(dogPos.x-30,dogPos.y);
        zoneGm.tick(.1,0,0);
        check(zoneGm.getReactingAnimal()==AnimalType.ANJING,"tanpa delay global: suara anjing langsung berbunyi sesaat setelah kucing");
        double beforeHomeMin=zoneGm.getState().getMinutes();
        check(zoneGm.quickReturnHome().success(),"shortcut [R] langsung pulang ke rumah");
        check(zoneGm.getState().getLocation()==Location.HOME,"lokasi langsung berpindah ke HOME");
        check(zoneGm.getState().isReturnedHome(),"menandai sudah pulang ke rumah");
        check(zoneGm.getState().getMinutes()>=beforeHomeMin+5,"waktu tempuh pulang bertambah 5 menit");
        check(zoneGm.chooseVehicle(VehicleType.MOBIL).success(),"bisa tukar Motor -> Mobil di Rumah pada hari yang sama");
        check(zoneGm.departHome().success()&&zoneGm.getState().getVehicleType()==VehicleType.MOBIL,"berangkat ulang dengan Mobil di hari yang sama");
        check(zoneGm.quickReturnHome().success()&&zoneGm.chooseVehicle(VehicleType.MOTOR).success(),"bisa tukar kembali Mobil -> Motor di Rumah pada hari yang sama");

        // 5. Orientasi 4-arah Kendaraan (UP, DOWN, LEFT, RIGHT)
        check(PixelArtRenderer.directionOf(-Math.PI/2)==PixelArtRenderer.Direction.UP,"arah UP (-pi/2)");
        check(PixelArtRenderer.directionOf(Math.PI/2)==PixelArtRenderer.Direction.DOWN,"arah DOWN (+pi/2)");
        check(PixelArtRenderer.directionOf(Math.PI)==PixelArtRenderer.Direction.LEFT,"arah LEFT (pi)");
        check(PixelArtRenderer.directionOf(0)==PixelArtRenderer.Direction.RIGHT,"arah RIGHT (0)");
        GameManager dirGm=drive();
        dirGm.getState().setPosition(495,300); // Di jalan vertikal tengah (x=465..535, y=115..505)
        dirGm.tick(.05,0,-1);
        check(PixelArtRenderer.directionOf(dirGm.getState().getFacing())==PixelArtRenderer.Direction.UP,"motor gerak ke atas menghadap UP");
        dirGm.tick(.05,0,1);
        check(PixelArtRenderer.directionOf(dirGm.getState().getFacing())==PixelArtRenderer.Direction.DOWN,"motor gerak ke bawah menghadap DOWN");
        dirGm.getState().setPosition(300,150); // Di jalan horizontal atas
        dirGm.tick(.05,-1,0);
        check(PixelArtRenderer.directionOf(dirGm.getState().getFacing())==PixelArtRenderer.Direction.LEFT,"motor gerak ke kiri menghadap LEFT");
        dirGm.tick(.05,1,0);
        check(PixelArtRenderer.directionOf(dirGm.getState().getFacing())==PixelArtRenderer.Direction.RIGHT,"motor gerak ke kanan menghadap RIGHT");
        System.out.println("OK fitur Pixel Overhaul: Sanity, 5 override bonus hewan, Mount/Dismount, Kafe Taman, 3 subclass Quest PBO, 4-arah kendaraan");
    }
    static void uiAndJar()throws Exception {
        SwingUtilities.invokeAndWait(()-> {
            QuietUi ui=new QuietUi();
            InputController input=new InputController();
            JPanel root=new JPanel();
            input.install(root,ui);
            check(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed W")).equals("press.W"),"global key scope");
            trigger(root,"press.W");
            trigger(root,"press.UP");
            check(input.getY()==-1,"w/up");
            trigger(root,"release.W");
            check(input.getY()==-1,"multikeyrelease");
            trigger(root,"stop");
            check(input.getY()==0,"stop");
            trigger(root,"press.UP");
            check(input.getY()==0,"autorepeat suppressed");
            trigger(root,"release.UP");
            trigger(root,"press.UP");
            check(input.getY()==-1,"release reenables");
            trigger(root,"command.E");
            trigger(root,"command.E");
            check(ui.interactions==1,"E onceperpress");
            JPanel modalRoot=new JPanel();
            input.installReleaseBindings(modalRoot);
            trigger(modalRoot,"release.E");
            trigger(root,"command.E");
            check(ui.interactions==2,"modalreleaseE");
            check(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed H")).equals("command.H"),"shortcut H klakson");
            check(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed R")).equals("command.R"),"shortcut R langsung pulang ke rumah");
            check(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed B")).equals("command.B"),"shortcut B darurat");
            check(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed K")).equals("command.K"),"shortcut K simpan");
            check(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed L")).equals("command.L"),"shortcut L muat");
            check(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed X")).equals("command.X"),"shortcut X peta");
            check(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("pressed F1")).equals("command.F1"),"shortcut F1 petunjuk");
            input.direction(1,0);
            check(input.getX()==1&&input.getY()==0,"GUI direction");
            input.stop();
            check(input.getX()==0,"GUI stop");
            GameManager g=park();
            GameScreen screen=new GameScreen(()->g,new InputController(),ui);
            screen.refresh();
            render(screen,980,720,"park-980x720");
            render(screen,1366,850,"park-1366x850");
            g.getState().setMinutes(1080);
            screen.refresh();
            render(screen,1280,850,"park-night");
            ScenePanel[] scenes= {
                new HomePanel(()->g,ui),new WorldMapPanel(()->g,ui),new ParkPanel(()->g,ui),new CityScenePanel(()->g,ui),new FuelStationPanel(()->g,ui)
            }
            ;
            for(ScenePanel s:scenes)check(s.getMouseListeners().length==1,"onelistener "+s.getClass().getSimpleName());
            MenuPanel menu=new MenuPanel(()-> {
            }
            ,()-> {
            }
            ,()-> {
            }
            ,()-> {
            }
            ,()-> {
            }
            ,()-> {
            }
            );
            menu.refresh(false,false);
            render(menu,980,720,"menu-980x720");
            // Render In-Game Pixel Overlays (Quest Board & Dispenser Digital SPBU)
            PixelOverlayPanel ov=new PixelOverlayPanel(()->{},()->{});
            UiActions overlayUi=new QuietUi() {
                @Override public void paintOverlay(Graphics2D g2,GameManager gm) {
                    ov.paint(g2,gm);
                }
            };
            GameScreen ovScreen=new GameScreen(()->g,new InputController(),overlayUi);
            ov.open(PixelOverlayPanel.Mode.QUEST_BOARD,g);
            ovScreen.refresh();
            render(ovScreen,1280,850,"overlay-quest");
            GameManager fg=fuel(0);
            GameScreen fuelScreen=new GameScreen(()->fg,new InputController(),overlayUi);
            ov.open(PixelOverlayPanel.Mode.FUEL,fg);
            fuelScreen.refresh();
            render(fuelScreen,1280,850,"overlay-fuel");
            ov.close(fg);
            fg.beginFuel(fg.previewFuel(20000));
            fuelScreen.refresh();
            render(fuelScreen,1280,850,"fuel-active");
            GameManager mapUp=drive();
            mapUp.acceptQuest(0);
            mapUp.getState().setPosition(500,295);
            mapUp.tick(.05,0,-1);
            GameScreen mapUpScreen=new GameScreen(()->mapUp,new InputController(),ui);
            mapUpScreen.refresh();
            render(mapUpScreen,1280,850,"map-up");
        }
        );
        Path jar=Path.of(GameManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        check(jar.toString().endsWith(".jar"),"tests use deliveredJAR");
        try(JarFile j=new JarFile(jar.toFile())) {
            check(j.getManifest().getMainAttributes().getValue("Main-Class").equals("com.innoelektrica.polymorphism.Main"),"executablemanifest");
            try(DataInputStream in=new DataInputStream(j.getInputStream(j.getJarEntry("com/innoelektrica/polymorphism/Main.class")))) {
                check(in.readInt()==0xCAFEBABE,"classmagic");
                in.readUnsignedShort();
                check(in.readUnsignedShort()==61,"Java17bytecode");
            }
            check(j.stream().filter(e->e.getName().endsWith(".png")).count()==0,"0hd_png_assets");
            check(j.stream().filter(e->e.getName().endsWith(".wav")).count()==9,"9audio");
        }
        System.out.println("OK key bindings/release modal, satu listener per scene, render 980x720 & 1366x850, JAR Java17");
    }
    public static void main(String[] args)throws Exception {
        Files.createDirectories(QA);
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        AudioPlayer.setErrorHandler(s-> {
        }
        );
        basics();
        movement();
        foodAnimals();
        cityTime();
        filling();
        events();
        persistence();
        pixelOverhaulFeatures();
        demo();
        uiAndJar();
        System.out.println("LULUS: "+assertions+" assertions. GUI interaktif, speaker, launcher Windows perlu cek manual.");
        System.exit(0);
    }
}
