package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.*;
import java.nio.file.*;
/** Dipanggil dalam dua JVM independen untuk menguji save setelah proses ditutup. */
public final class PersistenceProbe {
    static GameState sample() {
        GameState s=new GameState("Fakhry Uji");
        s.setDay(4);
        s.setMinutes(775.25);
        s.setMoney(27000);
        s.setSanity(84);
        s.setMounted(true);
        s.setParkedPosition(650,150);
        s.setActiveQuestId("Q_DELIVERY");
        s.setQuestStep(2);
        s.setQuestsCompletedToday(2);
        s.setTotalQuestsCompleted(5);
        s.setQuestEarningsToday(45000);
        s.setVehicleType(VehicleType.MOTOR);
        s.setLocation(Location.PARK);
        s.setPosition(585,380);
        s.setMapPosition(650,150);
        s.setFacing(.4);
        s.setDeparted(true);
        s.setVisitedPark(true);
        s.setFuelMl(VehicleType.MOTOR,3567);
        s.setFuelMl(VehicleType.MOBIL,28123);
        s.setFuelRemainder(VehicleType.MOTOR,.25);
        s.getInventory().set(FoodType.DAGING,2);
        s.getInventory().set(FoodType.BIJI_BIJIAN,3);
        s.getInventory().set(FoodType.ROTI,4);
        for(AnimalType a:AnimalType.values())s.setFriendship(a,20+a.ordinal()*10);
        s.setCooldown(AnimalType.KUCING,1.2);
        s.addFed(AnimalType.KUCING);
        s.addFed(AnimalType.BURUNG);
        s.setCityExplored(true);
        s.setCityWorkDone(true);
        s.setFoodBought(5);
        s.setFoodGiven(2);
        s.setEmergencyCount(1);
        s.setDrivingEventSeconds(3.5);
        s.setLowFuelWarned(true);
        s.setNightWarned(true);
        s.log("State lintas proses");
        return s;
    }
    static void require(boolean ok,String label) {
        if(!ok)throw new AssertionError(label);
    }
    public static void main(String[] args)throws Exception {
        SaveService save=new SaveService(Path.of(args[1]));
        if(args[0].equals("write")) {
            save.save(new GameManager(sample()));
            System.out.println("WRITE_OK");
            return;
        }
        GameState s=save.load();
        require(s.getPlayerName().equals("Fakhry Uji")&&s.getDay()==4&&s.getMinutes()==775.25,"identitas/jam");
        require(s.getMoney()==27000&&s.getSanity()==84&&s.getLocation()==Location.PARK&&s.getX()==585&&s.getY()==380&&s.getMapX()==650&&s.getMapY()==150&&s.getFacing()==.4,"saldo/sanity/posisi");
        require(s.getActiveQuestId().equals("Q_DELIVERY")&&s.getQuestStep()==2&&s.getQuestsCompletedToday()==2&&s.getTotalQuestsCompleted()==5&&s.getQuestEarningsToday()==45000,"queststate");
        require(s.getVehicleType()==VehicleType.MOTOR&&s.getFuelMl(VehicleType.MOTOR)==3567&&s.getFuelMl(VehicleType.MOBIL)==28123&&s.getFuelRemainder(VehicleType.MOTOR)==.25,"kedua tangki");
        require(s.getInventory().get(FoodType.DAGING)==2&&s.getInventory().get(FoodType.BIJI_BIJIAN)==3&&s.getInventory().get(FoodType.ROTI)==4,"inventory");
        for(AnimalType a:AnimalType.values())require(s.getFriendship(a)==20+a.ordinal()*10,"friendship "+a);
        require(s.getCooldown(AnimalType.KUCING)==1.2&&s.getFedAnimals().size()==2&&s.getFedAnimals().contains(AnimalType.KUCING)&&s.getFedAnimals().contains(AnimalType.BURUNG),"cooldown/misi");
        require(s.isDeparted()&&s.isVisitedPark()&&!s.isReturnedHome()&&s.isCityExplored()&&s.isCityWorkDone()&&s.isLowFuelWarned()&&s.isNightWarned(),"flags");
        require(s.getFoodBought()==5&&s.getFoodGiven()==2&&s.getEmergencyCount()==1&&s.getDrivingEventSeconds()==3.5&&s.getDayStatus()==DayStatus.ACTIVE&&s.getActivityLog().size()==1,"progres/log");
        System.out.println("READ_OK");
    }
}
