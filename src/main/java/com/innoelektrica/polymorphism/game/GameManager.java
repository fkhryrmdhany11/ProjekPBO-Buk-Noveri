package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.*;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.*;
/** Logika dunia, Sanity, Quest PBO, dan transaksi; tidak bergantung pada Swing. */
public final class GameManager {
    private final GameState state;
    private final Random random;
    private final boolean demo;
    private final EnumMap<AnimalType,Hewan> animals=new EnumMap<>(AnimalType.class);
    private final List<Quest> quests=List.of(
        new DeliveryQuest("Q_DELIVERY","Kurir Logistik Taman","Ambil paket di Pusat Kota lalu antarkan ke Taman Kota.",20000,"PARK"),
        new ServiceQuest("Q_SERVICE","Bantu Logistik Pasar Kota","Bantu Pak Raka & rapikan area Pusat Kota.",25000),
        new ParkCareQuest("Q_PARKCARE","Sahabat Satwa Taman","Beri makan satwa di Taman Kota untuk menjaga ekosistem.",18000,AnimalType.KUCING)
    );
    private boolean paused,uiBlocked;
    private double animationSeconds,noticeSeconds;
    private String notice="Selamat datang! Pilih kendaraan di rumah [E].";
    private TrafficEvent trafficEvent=TrafficEvent.NORMAL;
    private double trafficSeconds;
    private int queueCount;
    private double queueSeconds;
    private FuelQuote pendingFuel;
    private double fillingSeconds;
    private boolean cityActivity;
    private double activitySeconds;
    private AnimalType followingAnimal,reactingAnimal,gatherFedType;
    private FoodType gatherFood;
    private Point2D.Double followerPosition,gatherCenter;
    private double followSeconds,reactionSeconds,gatherSeconds,npcYieldSeconds,trafficWarnCooldown;
    private double ambientTrafficTimer=1.4;
    private int trafficSoundEvents,ambientTrafficIndex;
    private String reaction="";
    private final EnumSet<AnimalType> lastAttractTypes=EnumSet.noneOf(AnimalType.class);
    public record ParkAnimal(int id,AnimalType type,Hewan model,double x,double y,boolean primary,boolean inSoundZone) {
    }
    private static final class ParkAnimalEntry {
        final int id;
        final AnimalType type;
        final Hewan model;
        final Point2D.Double home;
        final boolean primary;
        double x,y,roamX,roamY,roamTimer;
        boolean inSoundZone;
        ParkAnimalEntry(int id,AnimalType type,Point2D.Double home,boolean primary) {
            this.id=id;
            this.type=type;
            this.model=type.create();
            this.home=home;
            this.primary=primary;
            this.x=this.roamX=home.x;
            this.y=this.roamY=home.y;
            this.roamTimer=0.4+(id%5)*0.35;
        }
    }
    private final List<ParkAnimalEntry> parkAnimals=new ArrayList<>();
    public record TrafficVehicle(VehicleType type,double x,double y,double facing,int colorIndex,boolean yielding) {
    }
    private static final class NpcCar {
        final VehicleType type;
        final Point2D.Double[] waypoints;
        final double speed;
        final int colorIndex;
        int wpIndex;
        double x,y,facing;
        boolean nearPlayer;
        NpcCar(VehicleType type,Point2D.Double[] waypoints,int startWp,double startX,double startY,double speed,int colorIndex) {
            this.type=type;
            this.waypoints=waypoints;
            this.wpIndex=startWp;
            this.x=startX;
            this.y=startY;
            this.speed=speed;
            this.colorIndex=colorIndex;
            Point2D.Double tgt=waypoints[wpIndex];
            this.facing=Math.atan2(tgt.y-y,tgt.x-x);
        }
        void step(double dt) {
            Point2D.Double tgt=waypoints[wpIndex];
            double dx=tgt.x-x,dy=tgt.y-y,d=Math.hypot(dx,dy);
            if(d<3.0) {
                x=tgt.x;
                y=tgt.y;
                wpIndex=(wpIndex+1)%waypoints.length;
                tgt=waypoints[wpIndex];
                dx=tgt.x-x;
                dy=tgt.y-y;
                d=Math.hypot(dx,dy);
            }
            if(d>1e-6) {
                double move=Math.min(d,speed*dt);
                x+=dx/d*move;
                y+=dy/d*move;
                facing=Math.atan2(dy,dx);
            }
        }
    }
    private final List<NpcCar> npcCars=new ArrayList<>();
    public GameManager(GameState s) {
        this(s,new Random(),false);
    }
    public GameManager(GameState s,Random r,boolean d) {
        state=Objects.requireNonNull(s);
        random=Objects.requireNonNull(r);
        demo=d;
        for(AnimalType a:AnimalType.values()) {
            animals.put(a,a.create());
        }
        initParkAnimals();
        initTrafficVehicles();
    }
    private void initParkAnimals() {
        parkAnimals.clear();
        int nextId=0;
        for(AnimalType a:AnimalType.values()) {
            parkAnimals.add(new ParkAnimalEntry(nextId++,a,WorldLayout.animalHome(a),true));
        }
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.KUCING,new Point2D.Double(550,315),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.KUCING,new Point2D.Double(735,420),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.ANJING,new Point2D.Double(335,305),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.ANJING,new Point2D.Double(515,290),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.BURUNG,new Point2D.Double(235,330),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.BURUNG,new Point2D.Double(355,370),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.AYAM,new Point2D.Double(415,485),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.AYAM,new Point2D.Double(565,485),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.ANGSA,new Point2D.Double(595,292),false));
        parkAnimals.add(new ParkAnimalEntry(nextId++,AnimalType.ANGSA,new Point2D.Double(785,292),false));
    }
    private void resetAllAnimalsHome() {
        for(ParkAnimalEntry e:parkAnimals) {
            e.x=e.roamX=e.home.x;
            e.y=e.roamY=e.home.y;
            e.roamTimer=0.4+(e.id%5)*0.35;
            e.inSoundZone=false;
        }
        gatherCenter=null;
        gatherSeconds=0;
        gatherFood=null;
        gatherFedType=null;
        lastAttractTypes.clear();
    }
    public List<ParkAnimal> getParkAnimals() {
        List<ParkAnimal> out=new ArrayList<>(parkAnimals.size());
        for(ParkAnimalEntry e:parkAnimals) {
            out.add(new ParkAnimal(e.id,e.type,e.model,e.x,e.y,e.primary,e.inSoundZone));
        }
        return Collections.unmodifiableList(out);
    }
    private void initTrafficVehicles() {
        Point2D.Double[] outerCW= {
            new Point2D.Double(874,136),new Point2D.Double(874,484),new Point2D.Double(121,484),new Point2D.Double(121,136)
        };
        Point2D.Double[] westCCW= {
            new Point2D.Double(149,164),new Point2D.Double(149,456),new Point2D.Double(486,456),new Point2D.Double(486,164)
        };
        Point2D.Double[] eastCCW= {
            new Point2D.Double(514,164),new Point2D.Double(514,456),new Point2D.Double(846,456),new Point2D.Double(846,164)
        };
        npcCars.add(new NpcCar(VehicleType.MOBIL,outerCW,0,720,136,68,0));
        npcCars.add(new NpcCar(VehicleType.MOTOR,outerCW,1,874,300,92,5));
        npcCars.add(new NpcCar(VehicleType.MOTOR,outerCW,2,340,484,84,1));
        npcCars.add(new NpcCar(VehicleType.MOBIL,outerCW,3,121,310,70,6));
        npcCars.add(new NpcCar(VehicleType.MOBIL,westCCW,1,149,280,64,2));
        npcCars.add(new NpcCar(VehicleType.MOTOR,westCCW,2,320,456,86,7));
        npcCars.add(new NpcCar(VehicleType.MOBIL,westCCW,3,486,260,72,8));
        npcCars.add(new NpcCar(VehicleType.MOTOR,eastCCW,2,680,456,88,3));
        npcCars.add(new NpcCar(VehicleType.MOBIL,eastCCW,3,846,290,72,4));
    }
    public List<TrafficVehicle> getTrafficVehicles() {
        List<TrafficVehicle> out=new ArrayList<>(npcCars.size());
        for(NpcCar c:npcCars) {
            out.add(new TrafficVehicle(c.type,c.x,c.y,c.facing,c.colorIndex,npcYieldSeconds>0));
        }
        return Collections.unmodifiableList(out);
    }
    public int getTrafficSoundEventCount() {
        return trafficSoundEvents;
    }
    public boolean isAnyTrafficVehicleNearPlayer() {
        for(NpcCar c:npcCars)if(c.nearPlayer)return true;
        return false;
    }
    public boolean isAnimalsGathering() {
        return gatherSeconds>0;
    }
    public Point2D.Double getGatherCenter() {
        return gatherCenter==null?null:new Point2D.Double(gatherCenter.x,gatherCenter.y);
    }
    public GameState getState() {
        return state;
    }
    public Hewan getAnimal(AnimalType a) {
        return animals.get(a);
    }
    public List<Quest> getAvailableQuests() {
        return quests;
    }
    public Quest getActiveQuest() {
        String id=state.getActiveQuestId();
        if(id==null||id.equals("NONE"))return null;
        for(Quest q:quests)if(q.getId().equals(id))return q;
        return null;
    }
    public record QuestNav(
        Location targetLocation,
        double targetX,
        double targetY,
        String badgeText,
        String stepInstruction,
        boolean isQuestActive
    ) {
    }
    public QuestNav getQuestNavigation() {
        Quest q=getActiveQuest();
        Location cur=state.getLocation();
        if(q instanceof DeliveryQuest) {
            int reward=q.hitungUpah(state.getVehicle());
            if(state.getQuestStep()<=1) {
                if(cur==Location.CITY) {
                    return new QuestNav(Location.CITY,WorldLayout.CITY_NPC.x,WorldLayout.CITY_NPC.y,
                        "★ AMBIL PAKET [E]","STEP 1/2: Dekati Pak Raka [E] di Kios Kota untuk ambil paket ("+rupiah(reward)+")",true);
                }
                if(cur==Location.PARK) {
                    return new QuestNav(Location.CITY,WorldLayout.PARK_EXIT.x,WorldLayout.PARK_EXIT.y,
                        "★ KELUAR TAMAN [E]","STEP 1/2: Keluar Taman [E] lalu menuju Pusat Kota untuk ambil paket",true);
                }
                return new QuestNav(Location.CITY,370,150,
                    "★ AMBIL PAKET [E]","STEP 1/2: Menuju Gerbang PUSAT KOTA [E] (Utara) untuk ambil paket -> "+rupiah(reward),true);
            }
            else {
                if(cur==Location.PARK) {
                    return new QuestNav(Location.PARK,WorldLayout.SHOP_DOOR.x,WorldLayout.SHOP_DOOR.y,
                        "★ SERAHKAN PAKET [E]","STEP 2/2: Dekati Kios Taman [E] untuk serahkan paket & klaim "+rupiah(reward)+"!",true);
                }
                if(cur==Location.CITY) {
                    return new QuestNav(Location.PARK,WorldLayout.CITY_EXIT.x,WorldLayout.CITY_EXIT.y,
                        "★ KELUAR KOTA [E]","STEP 2/2: Paket diambil! Keluar Pusat Kota [E] lalu antar ke Taman Kota",true);
                }
                return new QuestNav(Location.PARK,650,150,
                    "★ ANTAR PAKET [E]","STEP 2/2: Antarkan paket ke Gerbang TAMAN KOTA [E] (Timur Utara) -> "+rupiah(reward),true);
            }
        }
        if(q instanceof ServiceQuest) {
            int reward=q.hitungUpah(state.getVehicle());
            if(cur==Location.CITY) {
                return new QuestNav(Location.CITY,WorldLayout.CITY_NPC.x,WorldLayout.CITY_NPC.y,
                    "★ BANTU PAK RAKA [E]","TARGET QUEST: Dekati Pak Raka [E] di Kios Kota untuk klaim "+rupiah(reward)+"!",true);
            }
            if(cur==Location.PARK) {
                return new QuestNav(Location.CITY,WorldLayout.PARK_EXIT.x,WorldLayout.PARK_EXIT.y,
                    "★ KELUAR TAMAN [E]","TARGET QUEST: Keluar Taman [E] lalu menuju Pusat Kota ("+rupiah(reward)+")",true);
            }
            return new QuestNav(Location.CITY,370,150,
                "★ KERJA LOGISTIK [E]","TARGET QUEST: Menuju Gerbang PUSAT KOTA [E] (Utara) -> "+rupiah(reward),true);
        }
        if(q instanceof ParkCareQuest pcq) {
            int reward=q.hitungUpah(state.getVehicle());
            if(cur==Location.PARK) {
                int totalFood=0;
                for(FoodType f:FoodType.values())totalFood+=state.getInventory().get(f);
                if(totalFood==0) {
                    return new QuestNav(Location.PARK,WorldLayout.SHOP_DOOR.x,WorldLayout.SHOP_DOOR.y,
                        "★ BELI PAKAN [E]","STEP 1/2: Beli Pakan Satwa di Kios Taman [E] terlebih dahulu!",true);
                }
                AnimalType targetAnimal=pcq.getTargetAnimal();
                if(state.getInventory().get(animals.get(targetAnimal).getMakananFavorit())==0) {
                    for(AnimalType a:AnimalType.values()) {
                        if(state.getInventory().get(animals.get(a).getMakananFavorit())>0) {
                            targetAnimal=a;
                            break;
                        }
                    }
                }
                Point2D.Double pos=getAnimalPosition(targetAnimal);
                return new QuestNav(Location.PARK,pos.x,pos.y,
                    "★ BERI PAKAN "+targetAnimal+" [E]","STEP 2/2: Dekati "+targetAnimal+" [E] & beri pakan favorit -> "+rupiah(reward),true);
            }
            if(cur==Location.CITY) {
                return new QuestNav(Location.PARK,WorldLayout.CITY_EXIT.x,WorldLayout.CITY_EXIT.y,
                    "★ KELUAR KOTA [E]","TARGET QUEST: Keluar Pusat Kota [E] lalu menuju Taman Kota ("+rupiah(reward)+")",true);
            }
            return new QuestNav(Location.PARK,650,150,
                "★ RAWAT SATWA [E]","TARGET QUEST: Menuju Gerbang TAMAN KOTA [E] (Timur Utara) -> "+rupiah(reward),true);
        }
        // Fallback: Panduan Misi Harian
        if(!state.isVisitedPark()||state.getFedAnimals().size()<2) {
            if(cur==Location.PARK) {
                int totalFood=0;
                for(FoodType f:FoodType.values())totalFood+=state.getInventory().get(f);
                if(totalFood==0) {
                    return new QuestNav(Location.PARK,WorldLayout.SHOP_DOOR.x,WorldLayout.SHOP_DOOR.y,
                        "MISI: KIOS TAMAN [E]","MISI HARIAN: Beli pakan di Kios Taman [E] atau tekan [Q] ambil Quest",false);
                }
                AnimalType nextA=AnimalType.KUCING;
                for(AnimalType a:AnimalType.values()) {
                    if(!state.getFedAnimals().contains(a)&&state.getInventory().get(animals.get(a).getMakananFavorit())>0) {
                        nextA=a;
                        break;
                    }
                }
                Point2D.Double pos=getAnimalPosition(nextA);
                return new QuestNav(Location.PARK,pos.x,pos.y,
                    "MISI: "+nextA+" [E]","MISI HARIAN: Beri pakan favorit ke "+nextA+" ("+state.getFedAnimals().size()+"/2) | [Q] Quest",false);
            }
            if(cur==Location.CITY) {
                return new QuestNav(Location.PARK,WorldLayout.CITY_EXIT.x,WorldLayout.CITY_EXIT.y,
                    "MISI: KE TAMAN [E]","MISI HARIAN: Keluar Kota [E] lalu kunjungi Taman Kota | [Q] Papan Quest",false);
            }
            return new QuestNav(Location.PARK,650,150,
                "MISI: TAMAN KOTA [E]","MISI HARIAN: Kunjungi Gerbang TAMAN KOTA [E] (Timur Utara) | [Q] Papan Quest",false);
        }
        if(!state.isReturnedHome()) {
            if(cur==Location.PARK) {
                return new QuestNav(Location.HOME,WorldLayout.PARK_EXIT.x,WorldLayout.PARK_EXIT.y,
                    "MISI: PULANG [E]","MISI HARIAN: Keluar Taman [E] lalu pulang ke Rumah Kita",false);
            }
            if(cur==Location.CITY) {
                return new QuestNav(Location.HOME,WorldLayout.CITY_EXIT.x,WorldLayout.CITY_EXIT.y,
                    "MISI: PULANG [E]","MISI HARIAN: Keluar Kota [E] lalu pulang ke Rumah Kita",false);
            }
            return new QuestNav(Location.HOME,265,470,
                "MISI: RUMAH KITA [E]","MISI HARIAN: Kembali ke Gerbang RUMAH KITA [E] untuk selesaikan hari!",false);
        }
        return new QuestNav(Location.CITY,370,150,
            "PAPAN QUEST [Q]","Misi Harian Selesai! Tekan [Q] ambil Quest Uang atau Akhiri Hari di Rumah [E]",false);
    }
    public String getActiveQuestStatusText() {
        QuestNav nav=getQuestNavigation();
        return nav.stepInstruction();
    }
    public boolean isDemo() {
        return demo;
    }
    public boolean isPaused() {
        return paused;
    }
    public void setPaused(boolean v) {
        paused=v;
    }
    public boolean isUiBlocked() {
        return uiBlocked;
    }
    public void setUiBlocked(boolean v) {
        uiBlocked=v;
    }
    public double getAnimationSeconds() {
        return animationSeconds;
    }
    public String getNotice() {
        return noticeSeconds>0||animationSeconds==0?notice:"";
    }
    public void notifyPlayer(String s) {
        notice=s;
        noticeSeconds=9;
    }
    private ActionResult result(boolean ok,String s) {
        notifyPlayer(s);
        return new ActionResult(ok,s);
    }
    public static String rupiah(long n) {
        return "Rp"+NumberFormat.getIntegerInstance(Locale.forLanguageTag("id-ID")).format(n);
    }
    public TrafficEvent getTrafficEvent() {
        return trafficEvent;
    }
    public double getTrafficSeconds() {
        return trafficSeconds;
    }
    public int getQueueCount() {
        return queueCount;
    }
    public double getQueueProgress() {
        return queueCount>0?1-queueSeconds/2:1;
    }
    public FuelQuote getPendingFuel() {
        return pendingFuel;
    }
    public double getFillProgress() {
        return pendingFuel==null?0:1-fillingSeconds/5;
    }
    public boolean isCityActivity() {
        return cityActivity;
    }
    public double getActivityProgress() {
        return cityActivity?1-activitySeconds/4:0;
    }
    public boolean isBusy() {
        return pendingFuel!=null||cityActivity||queueCount>0;
    }
    public boolean canPersist() {
        return !demo&&!isBusy()&&trafficSeconds<=0;
    }
    private boolean active() {
        return state.getDayStatus()==DayStatus.ACTIVE&&!paused;
    }
    public boolean isShopOpen() {
        return state.getMinutes()>=420&&state.getMinutes()<1080;
    }
    public void modifySanity(int delta) {
        int before=state.getSanity();
        int after=Math.max(0,Math.min(100,before+delta));
        state.setSanity(after);
        if(after<30&&!state.isLowSanityWarned()) {
            state.setLowSanityWarned(true);
            notifyPlayer("Sanity di bawah 30%! Kamu kelelahan, kunjungi Taman Kota untuk bersantai.");
            state.log("Peringatan Sanity rendah (<30%)");
        }
        else if(after>=30) {
            state.setLowSanityWarned(false);
        }
    }
    public ActionResult toggleMount() {
        if(!active()||isBusy())return result(false,"Selesaikan aktivitas terlebih dahulu.");
        if(state.getLocation()!=Location.MAP) {
            return result(false,"Gunakan pintu keluar/parkir untuk kembali ke kendaraan di jalan kota.");
        }
        if(state.isMounted()) {
            com.innoelektrica.polymorphism.util.AudioPlayer.stopEngineLoop();
            state.setParkedPosition(state.getX(),state.getY());
            state.setMounted(false);
            state.log("Turun dari "+state.getVehicleType()+" di jalan kota");
            return result(true,"Turun dari "+state.getVehicleType()+". Sekarang berjalan kaki (hemat BBM). Tekan [F] di dekat kendaraan untuk naik lagi.");
        }
        else {
            double dist=Point2D.distance(state.getX(),state.getY(),state.getParkedX(),state.getParkedY());
            if(dist>85)return result(false,"Dekati "+state.getVehicleType()+" yang diparkir dahulu (jarak "+Math.round(dist)+" px).");
            state.setMounted(true);
            state.setPosition(state.getParkedX(),state.getParkedY());
            state.log("Naik kembali ke "+state.getVehicleType());
            return result(true,"Naik kembali ke "+state.getVehicleType()+". Siap mengemudi!");
        }
    }
    public ActionResult acceptQuest(int index) {
        if(!active()||isBusy())return result(false,"Tidak dapat mengambil quest saat ini.");
        if(index<0||index>=quests.size())return result(false,"Pilihan quest tidak valid.");
        if(getActiveQuest()!=null)return result(false,"Selesaikan atau batalkan quest aktif terlebih dahulu.");
        Quest q=quests.get(index);
        if(q.butuhKendaraan()&&state.getVehicle()==null) {
            return result(false,"Quest ini membutuhkan kendaraan. Pilih Motor atau Mobil di Rumah dahulu!");
        }
        if(!q.bisaDiambil(state.getSanity(),state.getVehicle())) {
            return result(false,"Sanity terlalu rendah ("+state.getSanity()+"%). Pulihkan diri di Taman Kota (minimal 30%) sebelum mengambil pekerjaan ini!");
        }
        state.setActiveQuestId(q.getId());
        state.setQuestStep(1);
        int upah=q.hitungUpah(state.getVehicle());
        state.log("Mengambil quest: "+q.getTitle()+" ("+rupiah(upah)+")");
        return result(true,"Quest dimulai: "+q.getTitle()+"! Target: "+q.getTargetLokasiLabel()+" (Upah "+rupiah(upah)+").");
    }
    public ActionResult cancelQuest() {
        Quest q=getActiveQuest();
        if(q==null)return result(false,"Tidak ada quest aktif.");
        state.setActiveQuestId("NONE");
        state.setQuestStep(0);
        state.log("Membatalkan quest: "+q.getTitle());
        return result(true,"Quest '"+q.getTitle()+"' dibatalkan.");
    }
    public ActionResult completeOrAdvanceQuest() {
        if(!active()||isBusy())return result(false,"Selesaikan aktivitas terlebih dahulu.");
        Quest q=getActiveQuest();
        if(q==null)return result(false,"Belum ada quest aktif. Buka Papan Quest [Q] untuk mengambil misi!");
        Location loc=state.getLocation();
        if(q instanceof DeliveryQuest) {
            if(state.getQuestStep()==1) {
                boolean atCity=loc==Location.CITY||(loc==Location.MAP&&Point2D.distance(state.getX(),state.getY(),370,150)<=85);
                if(!atCity)return result(false,"Pergi ke Pusat Kota dahulu untuk mengambil paket kurir!");
                state.setQuestStep(2);
                state.addMinutes(5);
                state.log("Paket kurir diambil di Pusat Kota");
                return result(true,"Paket berhasil diambil! Sekarang antarkan ke Taman Kota.");
            }
            else {
                boolean atPark=loc==Location.PARK||(loc==Location.MAP&&Point2D.distance(state.getX(),state.getY(),650,150)<=85);
                if(!atPark)return result(false,"Antarkan paket ini ke Taman Kota untuk menyelesaikan pengantaran!");
                return finishActiveQuest(q);
            }
        }
        else if(q instanceof ServiceQuest) {
            boolean atCity=loc==Location.CITY||(loc==Location.MAP&&Point2D.distance(state.getX(),state.getY(),370,150)<=85);
            if(!atCity)return result(false,"Pergi ke Pusat Kota untuk menyelesaikan pekerjaan logistik!");
            return finishActiveQuest(q);
        }
        else if(q instanceof ParkCareQuest) {
            if(loc!=Location.PARK)return result(false,"Pergi ke Taman Kota dan rawat satwa taman!");
            return finishActiveQuest(q);
        }
        return result(false,"Kondisi quest belum terpenuhi.");
    }
    private ActionResult finishActiveQuest(Quest q) {
        int reward=q.hitungUpah(state.getVehicle());
        state.setMoney(Math.min(GameState.MAX_MONEY,state.getMoney()+reward));
        state.addMinutes(q.getDurationMinutes());
        modifySanity(q.getSanityDelta());
        state.setActiveQuestId("NONE");
        state.setQuestStep(0);
        state.setQuestsCompletedToday(state.getQuestsCompletedToday()+1);
        state.setTotalQuestsCompleted(state.getTotalQuestsCompleted()+1);
        state.setQuestEarningsToday(state.getQuestEarningsToday()+reward);
        String sanityInfo=q.getSanityDelta()>=0?"+"+q.getSanityDelta()+"%":q.getSanityDelta()+"%";
        state.log("Quest selesai: "+q.getTitle()+" (+"+rupiah(reward)+", Sanity "+sanityInfo+")");
        return result(true,"Quest '"+q.getTitle()+"' SELESAI! Upah +"+rupiah(reward)+" diterima (Sanity "+sanityInfo+").");
    }
    public ActionResult restAtParkBench() {
        if(!active()||isBusy())return result(false,"Selesaikan aktivitas terlebih dahulu.");
        boolean inPark=state.getLocation()==Location.PARK||(state.getLocation()==Location.MAP&&Point2D.distance(state.getX(),state.getY(),650,150)<=95);
        if(!inPark)return result(false,"Kunjungi Taman Kota untuk bersantai di bangku taman.");
        if(state.getSanity()>=100)return result(false,"Sanity sudah penuh (100% — Segar)!");
        int before=state.getSanity();
        modifySanity(20);
        state.addMinutes(10);
        int gained=state.getSanity()-before;
        state.log("Bersantai di bangku Taman Kota (Sanity +"+gained+"%)");
        return result(true,"Duduk menikmati udara segar & gemericik air mancur taman. Sanity +"+gained+"% (kini "+state.getSanity()+"%).");
    }
    public ActionResult buySnack(SnackType snack) {
        if(!active()||isBusy())return result(false,"Selesaikan aktivitas terlebih dahulu.");
        if(state.getLocation()!=Location.PARK||!nearShop())return result(false,"Dekati Kios Taman dahulu.");
        if(!isShopOpen())return result(false,"Kios Taman buka pukul 07.00–18.00.");
        if(snack==null)return result(false,"Pilih menu camilan/kopi terlebih dahulu.");
        if(state.getMoney()<snack.getPrice())return result(false,"Saldo tidak cukup untuk membeli "+snack.getLabel()+".");
        state.setMoney(state.getMoney()-snack.getPrice());
        int before=state.getSanity();
        modifySanity(snack.getSanityRestore());
        state.addMinutes(5);
        int gained=state.getSanity()-before;
        state.log("Menikmati "+snack.getLabel()+" di Taman (Sanity +"+gained+"%)");
        return result(true,"Menikmati "+snack.getLabel()+"! Sanity +"+gained+"% (Saldo -"+rupiah(snack.getPrice())+").");
    }
    public ActionResult chooseVehicle(VehicleType v) {
        if(!active()||state.getLocation()!=Location.HOME||isBusy()||v==null)return result(false,"Kendaraan dapat ditukar atau dipilih saat berada di Rumah.");
        state.setVehicleType(v);
        return result(true,v+" dipilih (Sisa BBM: "+String.format(Locale.ROOT,"%.2f / %.0f L).",state.getFuelLiters(),v.create().getKapasitasTangki()));
    }
    public ActionResult quickReturnHome() {
        if(!active()||isBusy())return result(false,"Selesaikan aktivitas atau lanjutkan game dahulu sebelum pulang.");
        com.innoelektrica.polymorphism.util.AudioPlayer.stopEngineLoop();
        if(state.getLocation()==Location.HOME)return result(true,"Sudah berada di Rumah. Tekan [E] untuk pilih/tukar kendaraan atau akhiri hari.");
        state.setMapPosition(265,470);
        state.addMinutes(5);
        changeLocation(Location.HOME);
        return result(true,"Langsung pulang ke Rumah (+5 menit). Tekan [E] untuk tukar Motor <-> Mobil atau akhiri hari.");
    }
    public ActionResult departHome() {
        if(!active()||state.getLocation()!=Location.HOME||isBusy())return result(false,"Belum dapat berangkat.");
        if(state.getVehicleType()==null)return result(false,"Pilih Motor atau Mobil dahulu.");
        state.setDeparted(true);
        state.setMounted(true);
        changeLocation(Location.MAP);
        return result(true,"Kendarai jalan ke pintu lokasi. Tekan E untuk masuk atau F untuk turun jalan kaki.");
    }
    public ActionResult enterNearbyLocation() {
        if(!active()||state.getLocation()!=Location.MAP||isBusy())return result(false,"Masuk lokasi dari jalan kota.");
        var e=WorldLayout.nearEntrance(state.getX(),state.getY());
        if(e==null)return result(false,"Dekati pintu lokasi dahulu.");
        changeLocation(e.location());
        return result(true,"Tiba di "+e.location().getLabel()+".");
    }
    private void changeLocation(Location loc) {
        if(state.getLocation()==Location.MAP&&loc!=Location.MAP) {
            com.innoelektrica.polymorphism.util.AudioPlayer.stopAll();
        }
        else if(loc!=Location.MAP)com.innoelektrica.polymorphism.util.AudioPlayer.stopEngineLoop();
        for(NpcCar c:npcCars)c.nearPlayer=false;
        ambientTrafficTimer=1.4;
        state.setLocation(loc);
        switch(loc) {
            case MAP-> {
                state.setMounted(true);
                state.setPosition(state.getMapX(),state.getMapY());
            }
            case HOME-> {
                state.setPosition(500,440);
                state.setReturnedHome(state.isDeparted());
            }
            case PARK-> {
                state.setPosition(200,470);
                state.setVisitedPark(true);
                resetAllAnimalsHome();
            }
            case CITY-> {
                state.setPosition(230,470);
                Quest q=getActiveQuest();
                if(q instanceof DeliveryQuest&&state.getQuestStep()==1) {
                    state.setQuestStep(2);
                    state.log("Paket kurir diambil di Pusat Kota");
                }
            }
            case FUEL-> {
                state.setPosition(550,390);
                queueCount=random.nextInt(4);
                queueSeconds=2;
            }
        }
        trafficEvent=TrafficEvent.NORMAL;
        trafficSeconds=0;
        followingAnimal=null;
        followSeconds=0;
        state.log("Mengunjungi "+loc.getLabel());
    }
    public ActionResult honkVehicle() {
        if(!active())return result(false,"Game sedang dijeda.");
        Kendaraan v=state.getVehicle();
        if(v==null||state.getLocation()!=Location.MAP||!state.isMounted()) {
            return result(false,"Naik kendaraan di jalan kota untuk membunyikan klakson [H].");
        }
        v.klakson();
        npcYieldSeconds=3.0;
        state.log("Membunyikan klakson "+v.getNama());
        return result(true,v.getNama()+": \""+v.getSuaraMesin()+"\" (Klakson [H])");
    }
    public ActionResult leaveLocation() {
        if(!active()||pendingFuel!=null||cityActivity)return result(false,"Selesaikan atau batalkan aktivitas terlebih dahulu.");
        Location l=state.getLocation();
        boolean close=l==Location.FUEL||(l==Location.PARK&&distance(WorldLayout.PARK_EXIT)<=78)||(l==Location.CITY&&distance(WorldLayout.CITY_EXIT)<=78);
        if(!close)return result(false,"Dekati pintu keluar terlebih dahulu.");
        queueCount=0;
        changeLocation(Location.MAP);
        return result(true,"Kembali ke jalan kota.");
    }
    private double distance(Point2D p) {
        return p.distance(state.getX(),state.getY());
    }
    public boolean nearShop() {
        return state.getLocation()==Location.PARK&&distance(WorldLayout.SHOP_DOOR)<=85;
    }
    public boolean nearParkBench() {
        return state.getLocation()==Location.PARK&&(distance(WorldLayout.PARK_BENCH)<=85||distance(WorldLayout.PARK_FOUNTAIN)<=95);
    }
    public Point2D.Double getAnimalPosition(AnimalType a) {
        if(a==followingAnimal&&followSeconds>0&&followerPosition!=null) {
            return new Point2D.Double(followerPosition.x,followerPosition.y);
        }
        for(ParkAnimalEntry e:parkAnimals) {
            if(e.type==a&&e.primary)return new Point2D.Double(e.x,e.y);
        }
        return WorldLayout.animalHome(a);
    }
    public AnimalType getFollowingAnimal() {
        return followSeconds>0?followingAnimal:null;
    }
    public AnimalType getReactingAnimal() {
        return reactionSeconds>0?reactingAnimal:null;
    }
    public Set<AnimalType> getActiveSoundZoneAnimals() {
        EnumSet<AnimalType> out=EnumSet.noneOf(AnimalType.class);
        for(ParkAnimalEntry e:parkAnimals)if(e.inSoundZone)out.add(e.type);
        return out;
    }
    public AnimalType getActiveSoundZoneAnimal() {
        for(ParkAnimalEntry e:parkAnimals)if(e.inSoundZone)return e.type;
        return null;
    }
    public String getReaction() {
        return reactionSeconds>0?reaction:"";
    }
    public void tick(double seconds,int inputX,int inputY) {
        if(paused||uiBlocked||state.getDayStatus()!=DayStatus.ACTIVE) {
            com.innoelektrica.polymorphism.util.AudioPlayer.stopEngineLoop();
            return;
        }
        double dt=Math.max(0,Math.min(.25,seconds));
        if(!Double.isFinite(dt)||dt==0)return;
        animationSeconds+=dt;
        state.addMinutes(dt*2);
        noticeSeconds=Math.max(0,noticeSeconds-dt);
        reactionSeconds=Math.max(0,reactionSeconds-dt);
        npcYieldSeconds=Math.max(0,npcYieldSeconds-dt);
        trafficWarnCooldown=Math.max(0,trafficWarnCooldown-dt);
        for(AnimalType a:AnimalType.values()) {
            state.setCooldown(a,Math.max(0,state.getCooldown(a)-dt));
        }
        for(ParkAnimalEntry e:parkAnimals) {
            e.roamTimer=Math.max(0,e.roamTimer-dt);
        }
        // Gerakkan 9 kendaraan NPC di jalur kiri jalan raya kota
        for(NpcCar c:npcCars) {
            boolean nearPlayer=false;
            if(state.getLocation()==Location.MAP&&state.isMounted()&&npcYieldSeconds<=0) {
                double rx=state.getX()-c.x,ry=state.getY()-c.y;
                double cf=Math.cos(c.facing),sf=Math.sin(c.facing);
                double fwd=rx*cf+ry*sf,lat=Math.abs(-rx*sf+ry*cf);
                nearPlayer=fwd>0&&fwd<36&&lat<12;
            }
            if(!nearPlayer)c.step(dt);
        }
        // Suara ramai lalu lintas kota (hanya aktif di Jalan Raya Kota / Location.MAP)
        if(state.getLocation()==Location.MAP) {
            for(NpcCar c:npcCars) {
                double d=Point2D.distance(c.x,c.y,state.getX(),state.getY());
                if(d<=95.0) {
                    if(!c.nearPlayer) {
                        c.nearPlayer=true;
                        trafficSoundEvents++;
                        Kendaraan kv=c.type.create();
                        com.innoelektrica.polymorphism.util.AudioPlayer.playShort(kv.getAudioResource(),1400,-9.0f);
                        if(d<=60.0||(c.colorIndex%2==0)) {
                            com.innoelektrica.polymorphism.util.AudioPlayer.playShort(kv.getHornResource(),950,-6.5f);
                        }
                    }
                }
                else if(d>110.0) {
                    c.nearPlayer=false;
                }
            }
            ambientTrafficTimer-=dt;
            if(ambientTrafficTimer<=0&&!npcCars.isEmpty()) {
                int idx=Math.floorMod(ambientTrafficIndex++,npcCars.size());
                ambientTrafficTimer=1.4+(idx%3)*0.55;
                NpcCar pick=npcCars.get(idx);
                trafficSoundEvents++;
                com.innoelektrica.polymorphism.util.AudioPlayer.playShort(pick.type.create().getHornResource(),1050,-6.5f);
            }
        }
        if(state.getLocation()==Location.PARK&&state.getSanity()<100) {
            double rem=state.getSanityRemainder()+dt*0.4;
            if(rem>=1.0) {
                int gain=(int)rem;
                modifySanity(gain);
                rem-=gain;
            }
            state.setSanityRemainder(rem);
        }
        else if(state.getLocation()!=Location.HOME&&state.getMinutes()>=1200&&state.getSanity()>15) {
            double rem=state.getSanityRemainder()-dt*0.2;
            if(rem<=-1.0) {
                int loss=(int)(-rem);
                modifySanity(-loss);
                rem+=loss;
            }
            state.setSanityRemainder(rem);
        }
        if(gatherSeconds>0) {
            gatherSeconds=Math.max(0,gatherSeconds-dt);
            if(gatherSeconds==0) {
                gatherCenter=null;
                gatherFood=null;
                gatherFedType=null;
            }
        }
        if(followSeconds>0) {
            followSeconds=Math.max(0,followSeconds-dt);
            double dx=state.getX()-followerPosition.x,dy=state.getY()-followerPosition.y,d=Math.hypot(dx,dy);
            if(d>58) {
                double step=Math.min(d-58,70*dt),nx=followerPosition.x+dx/d*step,ny=followerPosition.y+dy/d*step;
                if(WorldLayout.canWalk(Location.PARK,nx,ny)) {
                    followerPosition.setLocation(nx,ny);
                    for(ParkAnimalEntry e:parkAnimals) {
                        if(e.type==followingAnimal&&e.primary) {
                            e.x=nx;
                            e.y=ny;
                            break;
                        }
                    }
                }
            }
        }
        // Gerak hewan: hanya hewan yang suka makanan di tas (atau baru diberi makan) yang menghampiri player
        if(state.getLocation()==Location.PARK&&state.getPeriod()!=TimePeriod.MALAM) {
            List<ParkAnimalEntry> attracted=new ArrayList<>();
            EnumSet<AnimalType> attractedTypes=EnumSet.noneOf(AnimalType.class);
            for(ParkAnimalEntry e:parkAnimals) {
                boolean match=gatherSeconds>0
                    ?(e.type==gatherFedType||(gatherFood!=null&&e.model.menerima(gatherFood))||likesFoodInBag(e.type))
                    :likesFoodInBag(e.type);
                if(match) {
                    attracted.add(e);
                    attractedTypes.add(e.type);
                }
            }
            if(gatherSeconds>0) {
                gatherCenter=new Point2D.Double(state.getX(),state.getY());
            }
            else if(!attractedTypes.equals(lastAttractTypes)) {
                EnumSet<AnimalType> newlyAttracted=EnumSet.copyOf(attractedTypes);
                newlyAttracted.removeAll(lastAttractTypes);
                if(!newlyAttracted.isEmpty()) {
                    StringBuilder sb=new StringBuilder();
                    for(AnimalType t:newlyAttracted) {
                        if(sb.length()>0)sb.append(", ");
                        sb.append(animals.get(t).getNama());
                        animals.get(t).bersuara(); // Bersahut-sahutan langsung tanpa delay
                    }
                    notifyPlayer(sb+" mencium aroma makanan kesukaannya dan menghampiri!");
                }
            }
            lastAttractTypes.clear();
            lastAttractTypes.addAll(attractedTypes);

            for(ParkAnimalEntry e:parkAnimals) {
                if(e.primary&&e.type==followingAnimal&&followSeconds>0)continue;
                int attractIdx=attracted.indexOf(e);
                double tx,ty,spd;
                boolean flocking=(attractIdx>=0);
                if(flocking) {
                    double baseRad=gatherSeconds>0?44.0:58.0;
                    double ringRad=attractIdx<6?baseRad:baseRad+28.0;
                    int ringCount=attractIdx<6?Math.min(6,attracted.size()):Math.max(1,attracted.size()-6);
                    int ringSlot=attractIdx<6?attractIdx:(attractIdx-6);
                    Point2D.Double slot=ringSlotAroundPlayer(ringSlot,ringCount,ringRad);
                    tx=slot.x;
                    ty=slot.y;
                    spd=gatherSeconds>0?88.0:72.0;
                }
                else {
                    if(Point2D.distance(e.roamX,e.roamY,e.x,e.y)<=2.0&&e.roamTimer<=0) {
                        Point2D.Double rt=pickRoamTarget(e.home);
                        e.roamX=rt.x;
                        e.roamY=rt.y;
                        e.roamTimer=1.6+random.nextDouble()*2.0;
                    }
                    tx=e.roamX;
                    ty=e.roamY;
                    spd=26.0;
                }
                double dx=tx-e.x,dy=ty-e.y,d=Math.hypot(dx,dy);
                if(d>1.5) {
                    double step=Math.min(d,spd*dt);
                    double nx=e.x+dx/d*step,ny=e.y+dy/d*step;
                    if(WorldLayout.canWalk(Location.PARK,nx,ny)) {
                        e.x=nx;
                        e.y=ny;
                    }
                    else {
                        if(WorldLayout.canWalk(Location.PARK,nx,e.y))e.x=nx;
                        if(WorldLayout.canWalk(Location.PARK,e.x,ny))e.y=ny;
                        if(!flocking) {
                            e.roamX=e.x;
                            e.roamY=e.y;
                            e.roamTimer=1.2+random.nextDouble()*1.5;
                        }
                    }
                }
            }
            // Zona suara ramai tanpa delay: tiap hewan yang masuk radius langsung bersuara secara paralel
            for(ParkAnimalEntry e:parkAnimals) {
                double d=Point2D.distance(e.x,e.y,state.getX(),state.getY());
                if(d<=WorldLayout.ANIMAL_SOUND_RADIUS) {
                    if(!e.inSoundZone) {
                        e.inSoundZone=true;
                        e.model.bersuara();
                        reactingAnimal=e.type;
                        reactionSeconds=4.5;
                        reaction=e.model.getNama()+": \""+e.model.getSuaraSebelumMakan()+"\"";
                    }
                }
                else if(d>WorldLayout.ANIMAL_SOUND_RADIUS+6.0) {
                    e.inSoundZone=false;
                }
            }
        }
        else {
            for(ParkAnimalEntry e:parkAnimals)e.inSoundZone=false;
            lastAttractTypes.clear();
        }
        if(state.getPeriod()==TimePeriod.MALAM&&!state.isNightWarned()) {
            state.setNightWarned(true);
            notifyPlayer("Malam tiba. Toko tutup; hewan beristirahat. Saatnya pulang.");
            state.log("Pengingat pulang malam");
        }
        if(queueCount>0) {
            queueSeconds-=dt;
            if(queueSeconds<=1e-9) {
                queueCount--;
                queueSeconds=2;
                if(queueCount==0)notifyPlayer("Giliranmu mengisi BBM. Tekan E.");
            }
            return;
        }
        if(pendingFuel!=null) {
            fillingSeconds-=dt;
            if(fillingSeconds<=1e-9) {
                FuelQuote q=pendingFuel;
                pendingFuel=null;
                state.setMoney(state.getMoney()-q.cost());
                state.setFuelMl(q.vehicleType(),state.getFuelMl(q.vehicleType())+q.milliliters());
                state.addMinutes(5);
                state.log("Isi BBM "+String.format(Locale.ROOT,"%.3f L",q.liters())+" / "+rupiah(q.cost()));
                notifyPlayer("Pengisian selesai. Saldo dan BBM diperbarui.");
            }
            return;
        }
        if(cityActivity) {
            activitySeconds-=dt;
            if(activitySeconds<=1e-9) {
                cityActivity=false;
                state.setCityWorkDone(true);
                state.setMoney(Math.min(GameState.MAX_MONEY,state.getMoney()+15000));
                state.addMinutes(25);
                modifySanity(-12);
                state.log("Membantu Pak Raka; upah Rp15.000");
                notifyPlayer("Pekerjaan selesai. Upah Rp15.000 diterima (Sanity -12%).");
            }
            return;
        }
        trafficSeconds=Math.max(0,trafficSeconds-dt);
        if(trafficSeconds==0)trafficEvent=TrafficEvent.NORMAL;
        double dx=Integer.signum(inputX),dy=Integer.signum(inputY),norm=Math.hypot(dx,dy);
        if(norm==0) {
            com.innoelektrica.polymorphism.util.AudioPlayer.stopEngineLoop();
            return;
        }
        dx/=norm;
        dy/=norm;
        state.setFacing(Math.atan2(dy,dx));
        boolean drive=state.getLocation()==Location.MAP&&state.isMounted();
        if(!drive&&state.getLocation()!=Location.MAP&&state.getLocation()!=Location.PARK&&state.getLocation()!=Location.CITY) {
            com.innoelektrica.polymorphism.util.AudioPlayer.stopEngineLoop();
            return;
        }
        Kendaraan vehicle=state.getVehicle();
        if(drive&&(vehicle==null||state.getFuelMl(state.getVehicleType())==0||trafficEvent==TrafficEvent.RED_LIGHT||trafficEvent==TrafficEvent.DOG_CROSSING)) {
            com.innoelektrica.polymorphism.util.AudioPlayer.stopEngineLoop();
            return;
        }
        double walkSpeed=state.isLowSanity()?75:110;
        double speed=drive?vehicle.bergerak()/.05:walkSpeed;
        if(trafficEvent==TrafficEvent.JAM&&drive)speed*=.5;
        if(drive&&npcYieldSeconds<=0) {
            for(NpcCar c:npcCars) {
                double rx=c.x-state.getX(),ry=c.y-state.getY();
                double fwd=rx*dx+ry*dy,lat=Math.abs(-rx*dy+ry*dx);
                if((fwd>0&&fwd<40&&lat<12)||Math.hypot(rx,ry)<18) {
                    speed*=0.45;
                    if(trafficWarnCooldown<=0) {
                        notifyPlayer("Hati-hati kendaraan di jalur ini! Tetap di jalur kiri atau tekan [H] klakson.");
                        trafficWarnCooldown=4.0;
                    }
                    break;
                }
            }
        }
        double amount=speed*dt;
        if(drive) {
            double available=state.getFuelMl(state.getVehicleType())-state.getFuelRemainder(state.getVehicleType());
            amount=Math.min(amount,Math.max(0,available)*vehicle.bergerak()/(vehicle.getKonsumsiPerTick()*1000));
        }
        double sx=state.getX(),sy=state.getY(),x=sx,y=sy;
        int steps=Math.max(1,(int)Math.ceil(amount/4));
        double step=amount/steps;
        for(int i=0;i<steps;i++) {
            double nx=x+dx*step,ny=y+dy*step;
            if(canMove(drive,nx,ny)) {
                x=nx;
                y=ny;
            }
            else {
                if(canMove(drive,nx,y))x=nx;
                if(canMove(drive,x,ny))y=ny;
            }
        }
        double travelled=Math.hypot(x-sx,y-sy);
        if(travelled<1e-9) {
            com.innoelektrica.polymorphism.util.AudioPlayer.stopEngineLoop();
            return;
        }
        double moveFacing=Math.atan2(y-sy,x-sx);
        // Auto-bimbing kendaraan ke jalur kiri (Left-Hand Traffic) pada arah gerak lurus
        if(drive&&((inputX!=0)^(inputY!=0))) {
            Point2D.Double lane=WorldLayout.leftLaneTarget(x,y,inputX,inputY);
            if(lane!=null) {
                double steer=36.0*dt;
                if(inputX!=0) {
                    double diff=lane.y-y;
                    double ny=y+Math.signum(diff)*Math.min(Math.abs(diff),steer);
                    if(canMove(true,x,ny))y=ny;
                }
                else {
                    double diff=lane.x-x;
                    double nx=x+Math.signum(diff)*Math.min(Math.abs(diff),steer);
                    if(canMove(true,nx,y))x=nx;
                }
            }
        }
        state.setPosition(x,y);
        state.setFacing(moveFacing);
        if(drive) {
            vehicle.bersuara();
            VehicleType v=state.getVehicleType();
            double use=travelled/vehicle.bergerak()*vehicle.getKonsumsiPerTick()*1000+state.getFuelRemainder(v);
            int whole=(int)Math.floor(use+1e-9);
            int fuel=Math.max(0,state.getFuelMl(v)-whole);
            state.setFuelMl(v,fuel);
            state.setFuelRemainder(v,fuel==0?0:Math.max(0,Math.min(.999999999,use-whole)));
            if(state.getFuelPercent()<10&&!state.isLowFuelWarned()) {
                state.setLowFuelWarned(true);
                notifyPlayer("BBM di bawah 10%. Kunjungi SPBU atau pilih bantuan darurat.");
                state.log("Peringatan BBM rendah");
            }
            state.setDrivingEventSeconds(state.getDrivingEventSeconds()+dt);
            if(state.getDrivingEventSeconds()>=8-1e-9&&trafficSeconds<=0) {
                state.setDrivingEventSeconds(0);
                trafficEvent=TrafficEvent.choose(random.nextInt(100));
                trafficSeconds=trafficEvent.getSeconds();
                state.addMinutes(trafficEvent.getMinutes());
                if(trafficEvent==TrafficEvent.JAM)modifySanity(-4);
                notifyPlayer(trafficEvent.getLabel());
                state.log(trafficEvent.getLabel());
            }
        }
    }
    private boolean canMove(boolean drive,double x,double y) {
        return drive?WorldLayout.canDrive(x,y):WorldLayout.canWalk(state.getLocation(),x,y);
    }
    public ActionResult emergencyFuel() {
        if(!active()||state.getVehicleType()==null||isBusy()||state.getFuelPercent()>=20)return result(false,"Bantuan tersedia ketika BBM kurang dari 20% dan tidak ada aktivitas.");
        VehicleType v=state.getVehicleType();
        int capacity=(int)(v.create().getKapasitasTangki()*1000);
        boolean paid=state.getMoney()>=5000;
        if(paid)state.setMoney(state.getMoney()-5000);
        state.setFuelMl(v,Math.min(capacity,state.getFuelMl(v)+capacity/5));
        state.addMinutes(paid?60:90);
        state.setEmergencyCount(state.getEmergencyCount()+1);
        state.log(paid?"Bantuan BBM Rp5.000 (+60 menit)":"Bantuan BBM subsidi (+90 menit)");
        return result(true,"BBM darurat +20% tangki. "+(paid?"Biaya Rp5.000, waktu +60 menit.":"Subsidi gratis, waktu +90 menit."));
    }
    public ActionResult buyFood(FoodType food,int quantity) {
        if(!active()||isBusy()||!nearShop())return result(false,"Dekati toko di taman dahulu.");
        if(!isShopOpen())return result(false,"Toko buka pukul 07.00–18.00.");
        if(food==null||quantity<1||quantity>99)return result(false,"Jumlah harus bilangan bulat 1–99.");
        long cost=(long)food.getPrice()*quantity;
        if(state.getInventory().get(food)+quantity>Inventory.MAX_ITEMS)return result(false,"Tas menampung maksimum 999 per makanan.");
        if(state.getMoney()<cost)return result(false,"Saldo tidak cukup. Tidak ada perubahan.");
        state.setMoney(state.getMoney()-cost);
        state.getInventory().add(food,quantity);
        state.setFoodBought(state.getFoodBought()+quantity);
        state.addMinutes(2);
        state.log("Membeli "+quantity+" "+food);
        return result(true,"Pembelian berhasil: "+quantity+" "+food+" / "+rupiah(cost));
    }
    private boolean likesFoodInBag(AnimalType a) {
        Hewan h=animals.get(a);
        if(h==null)return false;
        for(FoodType f:FoodType.values()) {
            if(state.getInventory().get(f)>0&&h.menerima(f))return true;
        }
        return false;
    }
    private String animalError(AnimalType a) {
        if(a==null||!active()||isBusy()||state.getLocation()!=Location.PARK)return "Dekati hewan dahulu.";
        boolean close=false;
        for(ParkAnimalEntry e:parkAnimals) {
            if(e.type==a&&Point2D.distance(e.x,e.y,state.getX(),state.getY())<=85) {
                close=true;
                break;
            }
        }
        if(!close)return "Dekati hewan dahulu.";
        if(state.getCooldown(a)>0)return "Tunggu sebentar sebelum berinteraksi lagi.";
        return null;
    }
    public ActionResult feedAnimal(AnimalType a,FoodType food) {
        String error=animalError(a);
        if(error!=null)return result(false,error);
        if(food==null||state.getInventory().get(food)==0)return result(false,"Stok makanan tersebut kosong.");
        Hewan animal=animals.get(a);
        boolean accepted=animal.menerima(food);
        state.setCooldown(a,2);
        state.addMinutes(2);
        if(accepted) {
            state.getInventory().removeOne(food);
            state.setFriendship(a,Math.min(100,state.getFriendship(a)+10));
            modifySanity(animal.getSanityBonus());
            state.addFed(a);
            state.setFoodGiven(state.getFoodGiven()+1);
            state.log("Memberi "+food+" kepada "+a+" (Sanity +"+animal.getSanityBonus()+"%)");
            animal.bersuara(); // Suara hewan langsung tanpa delay sesudah diberi makan
            startGathering(a,food);
            follow(a);
            Quest q=getActiveQuest();
            if(q instanceof ParkCareQuest) {
                finishActiveQuest(q);
            }
        }
        else {
            state.setFriendship(a,Math.max(0,state.getFriendship(a)-2));
            modifySanity(-2);
            state.log(a+" menolak "+food);
        }
        reactingAnimal=a;
        reactionSeconds=6;
        reaction=animal.reaksiMakanan(accepted);
        return result(accepted,reaction);
    }
    public ActionResult petAnimal(AnimalType a) {
        String error=animalError(a);
        if(error!=null)return result(false,error);
        if(state.getPeriod()==TimePeriod.MALAM)return result(false,"Hewan sedang beristirahat. Datang pagi lagi.");
        Hewan animal=animals.get(a);
        state.setFriendship(a,Math.min(100,state.getFriendship(a)+2));
        modifySanity(animal.getPetSanityBonus());
        state.setCooldown(a,2);
        state.addMinutes(1);
        follow(a);
        reactingAnimal=a;
        reactionSeconds=6;
        reaction=a+" senang dielus. Friendship +2, Sanity +"+animal.getPetSanityBonus()+"%.";
        return result(true,reaction);
    }
    private Point2D.Double pickRoamTarget(Point2D.Double home) {
        for(int i=0;i<6;i++) {
            double ang=random.nextDouble()*Math.PI*2.0;
            double rad=18.0+random.nextDouble()*42.0;
            double nx=home.x+Math.cos(ang)*rad,ny=home.y+Math.sin(ang)*rad;
            if(WorldLayout.canWalk(Location.PARK,nx,ny))return new Point2D.Double(nx,ny);
        }
        return new Point2D.Double(home.x,home.y);
    }
    private Point2D.Double ringSlotAroundPlayer(int index,int total,double radius) {
        double angle=(Math.PI*2.0*index)/Math.max(1,total)+0.35;
        for(double mult:new double[]{1.0,0.68,0.35}) {
            double nx=state.getX()+Math.cos(angle)*radius*mult;
            double ny=state.getY()+Math.sin(angle)*radius*mult;
            if(WorldLayout.canWalk(Location.PARK,nx,ny))return new Point2D.Double(nx,ny);
        }
        return new Point2D.Double(state.getX(),state.getY());
    }
    private void startGathering(AnimalType fed,FoodType food) {
        if(state.getPeriod()==TimePeriod.MALAM)return;
        gatherFedType=fed;
        gatherFood=food;
        gatherCenter=new Point2D.Double(state.getX(),state.getY());
        gatherSeconds=10.0;
    }
    private void follow(AnimalType a) {
        if(state.getFriendship(a)>=30&&state.getPeriod()!=TimePeriod.MALAM) {
            followerPosition=getAnimalPosition(a);
            followingAnimal=a;
            followSeconds=8;
        }
    }
    public ActionResult beginCityWork() {
        if(!active()||isBusy()||state.getLocation()!=Location.CITY||distance(WorldLayout.CITY_NPC)>85)return result(false,"Dekati Pak Raka dahulu.");
        if(state.isCityWorkDone())return result(false,"Pekerjaan hari ini sudah selesai.");
        if(state.isLowSanity())return result(false,"Sanity terlalu rendah (<30%). Istirahat di Taman Kota dahulu!");
        cityActivity=true;
        activitySeconds=4;
        return result(true,"Bantu merapikan kios selama 4 detik. Upah Rp15.000, waktu +25 menit.");
    }
    public ActionResult exploreCity() {
        if(!active()||isBusy()||state.getLocation()!=Location.CITY||distance(WorldLayout.CITY_PLAZA)>85)return result(false,"Dekati plaza dahulu.");
        if(state.isCityExplored())return result(false,"Plaza sudah dikunjungi hari ini.");
        state.setCityExplored(true);
        state.addMinutes(15);
        modifySanity(5);
        state.log("Menjelajah plaza kota");
        return result(true,"Kota telah dijelajahi. Waktu +15 menit, Sanity +5%.");
    }
    public FuelQuote previewFuel(long nominal) {
        if(!active()||state.getLocation()!=Location.FUEL||isBusy()||state.getVehicleType()==null)throw new IllegalStateException("Tunggu giliran dan selesaikan aktivitas terlebih dahulu.");
        if(nominal<=0)throw new IllegalArgumentException("Nominal harus positif.");
        VehicleType v=state.getVehicleType();
        int room=(int)(v.create().getKapasitasTangki()*1000)-state.getFuelMl(v);
        int ml=(int)Math.min(room,Math.min(nominal,state.getMoney())/10);
        if(ml<=0)throw new IllegalArgumentException(room<=0?"Tangki sudah penuh.":"Saldo/nominal minimal Rp10 untuk 1 ml.");
        return new FuelQuote(nominal,ml,ml*10L,state.getFuelMl(v),state.getMoney(),v);
    }
    public ActionResult beginFuel(FuelQuote q) {
        try {
            if(q==null||!q.equals(previewFuel(q.requestedRupiah())))return result(false,"Rincian transaksi berubah. Hitung kembali.");
            pendingFuel=q;
            fillingSeconds=5;
            return result(true,"Pengisian berlangsung 5 detik. Pembayaran saat selesai.");
        }
        catch(IllegalArgumentException|IllegalStateException ex) {
            return result(false,ex.getMessage());
        }
    }
    public ActionResult cancelFuel() {
        if(pendingFuel==null)return result(false,"Tidak ada pengisian aktif.");
        pendingFuel=null;
        fillingSeconds=0;
        return result(true,"Pengisian dibatalkan; uang dan BBM tidak berubah.");
    }
    public ActionResult endDay() {
        if(!active()||state.getLocation()!=Location.HOME||isBusy())return result(false,"Akhiri hari di rumah setelah aktivitas selesai.");
        state.setDayStatus(DayStatus.ENDED);
        state.log(state.isMissionComplete()?"Hari selesai: misi berhasil":"Hari selesai: misi belum lengkap");
        return result(true,state.isMissionComplete()?"Misi hari ini berhasil!":"Hari selesai. Misi belum lengkap.");
    }
    public ActionResult nextDay() {
        if(state.getLocation()!=Location.HOME||state.getDayStatus()!=DayStatus.ENDED||isBusy()||state.getDay()>=9999)return result(false,"Hari berikutnya tersedia setelah mengakhiri hari di rumah.");
        state.setDay(state.getDay()+1);
        state.resetDailyProgress();
        paused=false;
        trafficSeconds=0;
        trafficEvent=TrafficEvent.NORMAL;
        followingAnimal=reactingAnimal=null;
        followSeconds=reactionSeconds=0;
        for(NpcCar c:npcCars)c.nearPlayer=false;
        ambientTrafficTimer=1.4;
        resetAllAnimalsHome();
        return result(true,"Hari "+state.getDay()+" dimulai pukul 07.00. Uang, tas, friendship dan BBM tetap.");
    }
    public enum TargetKind {
        NONE,HOME,ENTRANCE,EXIT,SHOP,ANIMAL,NPC,PLAZA,FUEL,BENCH,QUEST_BOARD
    }
    public record NearbyTarget(TargetKind kind,String label,AnimalType animal,Location location) {
    }
    private NearbyTarget target(TargetKind k,String s,AnimalType a,Location l) {
        return new NearbyTarget(k,s,a,l);
    }
    public NearbyTarget getNearbyTarget() {
        Location l=state.getLocation();
        if(l==Location.HOME)return target(TargetKind.HOME,"[E] Garasi & Rumah (Pilih Kendaraan / Berangkat / Akhiri Hari) | [Q] Quest",null,l);
        if(l==Location.FUEL)return target(TargetKind.FUEL,queueCount>0?"Antrean SPBU: "+queueCount+" kendaraan":pendingFuel!=null?"[E] Dispenser SPBU / Batalkan":"[E] Dispenser Digital SPBU | [Ke Peta] Keluar",null,l);
        if(l==Location.MAP) {
            var e=WorldLayout.nearEntrance(state.getX(),state.getY());
            if(e!=null)return target(TargetKind.ENTRANCE,"[E] Masuk "+e.location().getLabel()+" | [F] "+(state.isMounted()?"Turun Kendaraan":"Naik Kendaraan"),null,e.location());
            String mountHint=state.isMounted()?"[F] Turun & Jalan Kaki":"[F] Naik "+state.getVehicleType()+" (dekat parkir)";
            return target(TargetKind.NONE,state.getFuelPercent()==0&&state.isMounted()?"BBM habis! Klik [Bantuan] atau tekan [F] untuk jalan kaki.":"Ikuti jalan kota • Dekati gerbang lokasi [E] • "+mountHint+" • [Q] Quest",null,l);
        }
        if(l==Location.PARK) {
            if(nearShop())return target(TargetKind.SHOP,"[E] Kios Taman (Beli Pakan Satwa & Kopi Sanity 07.00–18.00)",null,l);
            AnimalType nearest=null;
            double best=85;
            for(ParkAnimalEntry e:parkAnimals) {
                double d=Point2D.distance(e.x,e.y,state.getX(),state.getY());
                if(d<=best) {
                    best=d;
                    nearest=e.type;
                }
            }
            if(nearest!=null)return target(TargetKind.ANIMAL,"[E] Interaksi "+nearest+" (+Sanity & Friendship)",nearest,l);
            if(nearParkBench())return target(TargetKind.BENCH,"[E] Duduk Santai di Bangku / Air Mancur Taman (+20% Sanity)",null,l);
            if(distance(WorldLayout.PARK_EXIT)<=78)return target(TargetKind.EXIT,"[E] Keluar Taman ke Kendaraan",null,l);
            return target(TargetKind.NONE,"ZONA TAMAN (+Sanity Pasif) • Dekati Hewan, Kios, atau Bangku Taman [E]",null,l);
        }
        if(l==Location.CITY) {
            if(distance(WorldLayout.CITY_NPC)<=85)return target(TargetKind.NPC,"[E] Pak Raka (Kerja Kios +Rp15.000 / Quest Kota)",null,l);
            if(distance(WorldLayout.CITY_QUEST_BOARD)<=85)return target(TargetKind.QUEST_BOARD,"[E] Papan Quest Kota (Ambil / Klaim Misi Uang)",null,l);
            if(distance(WorldLayout.CITY_PLAZA)<=85)return target(TargetKind.PLAZA,"[E] Jelajahi Plaza Air Mancur Kota (+15 menit, +5% Sanity)",null,l);
            if(distance(WorldLayout.CITY_EXIT)<=78)return target(TargetKind.EXIT,"[E] Kembali ke Jalan Kota",null,l);
        }
        return target(TargetKind.NONE,"WASD/Panah: Gerak | Dekati objek interaksi [E] | [Q] Papan Quest",null,l);
    }
}
