package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import com.innoelektrica.polymorphism.model.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.function.Supplier;
public final class ParkPanel extends ScenePanel {
    public ParkPanel(Supplier<GameManager> s,UiActions u) {
        super(s,u);
    }
    @Override protected void paintWorld(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        double anim=game.getAnimationSeconds();
        TimePeriod period=s.getPeriod();
        PixelArtRenderer.drawPixelGrassCanvas(g,1000,600,anim);
        // Jalur Setapak Taman Pixel
        PixelArtRenderer.drawPixelSidewalk(g,120,210,560,36);
        PixelArtRenderer.drawPixelSidewalk(g,160,210,36,310);
        PixelArtRenderer.drawPixelSidewalk(g,160,440,560,36);
        PixelArtRenderer.drawPixelSidewalk(g,430,150,36,310);
        // Kolam & Air Mancur Pixel (Sanity Oasis)
        PixelArtRenderer.drawPixelPondAndFountain(g,608,128,260,132,anim);
        // Bangku Taman Santai (+20% Sanity)
        PixelArtRenderer.drawPixelParkBench(g,455,145);
        PixelArtRenderer.drawPixelParkBench(g,555,215);
        PixelArtRenderer.drawPixelParkBench(g,760,320);
        // Kios Taman (Pakan Satwa & Kafe Kopi Sanity)
        PixelArtRenderer.drawPixelBuilding(g,55,96,172,110,new Color(236,204,142),new Color(42,118,84),"KIOS & KOPI TAMAN",period);
        // Pepohonan & Lampu Taman Pixel
        for(int[] tree:new int[][]{{88,330},{310,135},{895,180},{880,430},{735,495},{345,525}}) {
            PixelArtRenderer.drawPixelTree(g,tree[0],tree[1],anim);
        }
        PixelArtRenderer.drawPixelStreetLamp(g,245,205,period);
        PixelArtRenderer.drawPixelStreetLamp(g,575,435,period);
        // Area Parkir Kendaraan di Gerbang Taman (Tanpa Pengendara karena Pemain Jalan Kaki)
        PixelArtRenderer.drawPixelSidewalk(g,64,484,156,92);
        vehicle(g,s.getVehicle(),142,542,88,0,false);
        // Zona Suara Kecil di sekitar setiap hewan + Hewan 16-Bit Proporsional
        AnimalType activeZone=game.getActiveSoundZoneAnimal();
        for(AnimalType a:AnimalType.values()) {
            Point2D.Double p=game.getAnimalPosition(a);
            PixelArtRenderer.drawAnimalSoundZone(g,p.x,p.y,WorldLayout.ANIMAL_SOUND_RADIUS,activeZone==a,anim);
        }
        if(game.isAnimalsGathering()&&game.getGatherCenter()!=null) {
            Point2D.Double gc=game.getGatherCenter();
            PixelArtRenderer.drawAnimalSoundZone(g,gc.x,gc.y,56,true,anim);
        }
        for(AnimalType a:AnimalType.values()) {
            Point2D.Double p=game.getAnimalPosition(a);
            PixelArtRenderer.drawPixelAnimal(g,a,game.getAnimal(a),p.x,p.y,s.getFriendship(a),period,anim);
        }
        player(g,s.getX(),s.getY(),new Color(53,137,204));
    }
    @Override protected void paintLabels(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        title(g,"TAMAN KOTA (+SANITY)","Sanity "+s.getSanity()+"% • Dekati zona suara hewan, beri makan agar berkumpul!");
        pill(g,game.isShopOpen()?"Kios & Kopi [E]":"Kios Tutup",142,222,11);
        pill(g,"Bangku [E]",455,112,11);
        pill(g,"Kolam [E]",628,144,11);
        pill(g,"Keluar [E]",142,492,11);
        for(AnimalType a:AnimalType.values()) {
            var p=game.getAnimalPosition(a);
            String rest=s.getPeriod()==TimePeriod.MALAM?" • Zzz":"";
            pill(g,a+" ("+s.getFriendship(a)+")"+rest,p.x,p.y+20,11);
        }
        if(game.isAnimalsGathering()) {
            pill(g,"Hewan berkumpul menikmati makanan!",520,86,12);
        }
        if(!game.getReaction().isEmpty())pill(g,game.getReaction(),560,542,12);
    }
    @Override protected void onClick(double x,double y) {
        GameManager g=source.get();
        for(AnimalType a:AnimalType.values()) {
            if(g.getAnimalPosition(a).distance(x,y)<55) {
                ui.animal(a);
                return;
            }
        }
        if(WorldLayout.SHOP.contains(x,y)||WorldLayout.SHOP_DOOR.distance(x,y)<55) {
            ui.shop();
            return;
        }
        if(WorldLayout.PARK_BENCH.distance(x,y)<65||WorldLayout.PARK_FOUNTAIN.distance(x,y)<85) {
            ui.restBench();
            return;
        }
        if(WorldLayout.PARK_EXIT.distance(x,y)<65)ui.leave();
    }
}
