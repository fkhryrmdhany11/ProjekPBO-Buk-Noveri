package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.*;
import com.innoelektrica.polymorphism.util.AudioPlayer;
import java.util.*;
/** Rute presentasi menjalankan gerak dan transaksi yang sama dengan pemain. */
public final class DemoController {
    private record Stage(String label,double x,double y,double dwell,Runnable action) {
    }
    private final GameManager game=new GameManager(new GameState("Pengunjung Demo"),new Random(4),true);
    private final List<Stage> stages=new ArrayList<>();
    private int index,inputX,inputY;
    private boolean started;
    private double remaining;
    public DemoController() {
        step("Satu hari di kotamu",5,()-> {
        }
        );
        step("Pilih Motor 10 L",5,()->game.chooseVehicle(VehicleType.MOTOR));
        step("Berangkat dari rumah",3,game::departHome);
        move("Simpang jalan",500,470,2);
        move("Jalan utara",500,150,2);
        move("Pintu taman",650,150,3);
        step("Masuk taman",4,game::enterNearbyLocation);
        move("Toko makanan",140,240,3);
        step("Beli makanan dari saldo pemain",7,()-> {
            game.buyFood(FoodType.DAGING,3);
            game.buyFood(FoodType.BIJI_BIJIAN,1);
            game.buyFood(FoodType.ROTI,1);
        }
        );
        move("Dekati kucing",585,380,3);
        step("Dengarkan kucing",4,()->game.getAnimal(AnimalType.KUCING).bersuara());
        step("Makanan salah dikembalikan",5,()->game.feedAnimal(AnimalType.KUCING,FoodType.ROTI));
        step("Makanan sesuai +10",5,()->game.feedAnimal(AnimalType.KUCING,FoodType.DAGING));
        step("Friendship bertambah",5,()->game.feedAnimal(AnimalType.KUCING,FoodType.DAGING));
        step("Friendship 30: mengikuti",3,()->game.feedAnimal(AnimalType.KUCING,FoodType.DAGING));
        move("Kucing mengikuti sebentar",520,360,3);
        move("Dekati burung",290,410,3);
        step("Suara dan makanan jenis kedua",5,()-> {
            game.getAnimal(AnimalType.BURUNG).bersuara();
            game.feedAnimal(AnimalType.BURUNG,FoodType.BIJI_BIJIAN);
        }
        );
        move("Pintu keluar taman",160,485,3);
        step("Kembali ke kendaraan",3,game::leaveLocation);
        move("Jalan ke pusat kota",500,150,2);
        move("Pintu pusat kota",370,150,2);
        step("Masuk pusat kota",3,game::enterNearbyLocation);
        move("Pak Raka di kios",285,445,3);
        step("Membantu NPC mendapat upah",7,game::beginCityWork);
        move("Plaza kota",650,440,3);
        step("Eksplorasi memengaruhi waktu",5,game::exploreCity);
        move("Pintu keluar kota",160,490,2);
        step("Kembali ke jalan",2,game::leaveLocation);
        move("Simpang utara",500,150,1);
        move("Simpang selatan",500,470,1);
        move("Pintu SPBU",695,470,2);
        step("Antrean SPBU",8,game::enterNearbyLocation);
        step("Isi BBM sesuai saldo dan ruang tangki",8,()->game.beginFuel(game.previewFuel(5000)));
        step("Kembali dari SPBU",4,game::leaveLocation);
        move("Pulang ke rumah",265,470,3);
        step("Masuk rumah",4,game::enterNearbyLocation);
        step("Hari pertama: misi berhasil",7,game::endDay);
        step("Hari berikutnya: progres harian direset",5,game::nextDay);
        step("Pilihan Mobil 40 L",4,()->game.chooseVehicle(VehicleType.MOBIL));
        step("Mobil lebih lambat dan boros",2,game::departHome);
        move("Mencoba Mobil",500,470,3);
        move("Kembali ke rumah",265,470,2);
        step("Ending sesuai progres: hari kedua belum lengkap",5,()-> {
            game.enterNearbyLocation();
            game.endDay();
        }
        );
    }
    private void step(String s,double t,Runnable a) {
        stages.add(new Stage(s,Double.NaN,Double.NaN,t,a));
    }
    private void move(String s,double x,double y,double t) {
        stages.add(new Stage(s,x,y,t,()-> {
        }
        ));
    }
    public GameManager getGame() {
        return game;
    }
    public int getInputX() {
        return inputX;
    }
    public int getInputY() {
        return inputY;
    }
    public int getStageCount() {
        return stages.size();
    }
    public boolean isFinished() {
        return index>=stages.size();
    }
    public String getCaption() {
        return isFinished()?"Demo selesai":(index+1)+"/"+stages.size()+" — "+stages.get(index).label;
    }
    public void update(double dt) {
        inputX=inputY=0;
        if(isFinished()||game.isPaused()||game.isUiBlocked())return;
        Stage s=stages.get(index);
        if(!started) {
            started=true;
            remaining=s.dwell;
            s.action.run();
            game.notifyPlayer("Auto Demo "+getCaption());
        }
        if(Double.isFinite(s.x)) {
            double tx=s.x,ty=s.y;
            String lowLabel=s.label.toLowerCase(Locale.ROOT);
            if(lowLabel.contains("burung")) {
                var p=game.getAnimalPosition(AnimalType.BURUNG);
                tx=p.x;
                ty=p.y;
            }
            else if(lowLabel.contains("kucing")) {
                var p=game.getAnimalPosition(AnimalType.KUCING);
                tx=p.x;
                ty=p.y;
            }
            double dx=tx-game.getState().getX(),dy=ty-game.getState().getY();
            if(game.getState().getLocation()==Location.MAP) {
                if(Math.abs(dx)>5) {
                    inputX=dx>0?1:-1;
                    return;
                }
                if(Math.abs(dy)>5) {
                    inputY=dy>0?1:-1;
                    return;
                }
            }
            else {
                if(Math.abs(dx)>5)inputX=dx>0?1:-1;
                if(Math.abs(dy)>5)inputY=dy>0?1:-1;
                if(inputX!=0||inputY!=0)return;
            }
        }
        remaining-=dt;
        if(remaining<=1e-9) {
            index++;
            started=false;
        }
    }
    public void stop() {
        AudioPlayer.stopAll();
    }
}
