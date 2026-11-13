package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import com.innoelektrica.polymorphism.model.AnimalType;
import java.awt.*;
import java.awt.geom.*;
import java.util.function.Supplier;
public final class WorldMapPanel extends ScenePanel {
    public WorldMapPanel(Supplier<GameManager> s,UiActions u) {
        super(s,u);
    }
    @Override protected void paintWorld(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        double anim=game.getAnimationSeconds();
        TimePeriod period=s.getPeriod();
        PixelArtRenderer.drawPixelGrassCanvas(g,1000,600,anim);
        // Trotoar & Jalan Raya Pixel-Art
        for(var r:WorldLayout.ROADS) {
            PixelArtRenderer.drawPixelSidewalk(g,(int)r.x,(int)r.y,(int)r.width,(int)r.height);
        }
        for(var r:WorldLayout.ROADS) {
            boolean horiz=r.width>r.height;
            PixelArtRenderer.drawPixelRoad(g,(int)r.x,(int)r.y,(int)r.width,(int)r.height,horiz);
        }
        // Zebra Cross di persimpangan kota
        PixelArtRenderer.drawCrosswalk(g,180,115,24,70,true);
        PixelArtRenderer.drawCrosswalk(g,430,115,24,70,true);
        PixelArtRenderer.drawCrosswalk(g,550,115,24,70,true);
        PixelArtRenderer.drawCrosswalk(g,180,435,24,70,true);
        PixelArtRenderer.drawCrosswalk(g,550,435,24,70,true);
        // Distrik Barat: Rumah & Pusat Kota
        PixelArtRenderer.drawPixelBuilding(g,195,290,132,120,new Color(204,136,92),new Color(128,62,42),"RUMAH KITA",period);
        PixelArtRenderer.drawPixelBuilding(g,336,204,114,158,new Color(112,148,176),new Color(54,78,102),"PUSAT KOTA",period);
        PixelArtRenderer.drawPixelQuestBoard(g,312,248,game.getActiveQuest()!=null,anim);
        // Distrik Timur Utara: Taman Kota (Sanity Zone)
        PixelArtRenderer.pxBorderBox(g,556,196,248,142,new Color(62,132,64),new Color(120,196,108));
        PixelArtRenderer.drawPixelPondAndFountain(g,668,218,116,74,anim);
        PixelArtRenderer.drawPixelParkBench(g,612,235);
        for(int i=0;i<4;i++) {
            PixelArtRenderer.drawPixelTree(g,582+i*24,306,anim);
        }
        PixelArtRenderer.drawPixelSignboard(g,"TAMAN KOTA",680,192,new Color(28,74,48),PixelArtRenderer.SANITY_HIGH);
        // Distrik Timur Selatan: SPBU Digital Kota
        PixelArtRenderer.drawPixelBuilding(g,588,350,196,68,new Color(218,74,64),new Color(156,36,32),"SPBU DIGITAL",period);
        // Lampu Jalan Kota Pixel
        for(int lx:new int[]{82,250,445,610,795,915}) {
            PixelArtRenderer.drawPixelStreetLamp(g,lx,110,period);
            PixelArtRenderer.drawPixelStreetLamp(g,lx,430,period);
        }
        // Gerbang Interaksi Lokasi Pixel (Kotak Presisi Tengah)
        g.setFont(new Font("Monospaced",Font.BOLD,12));
        FontMetrics fm=g.getFontMetrics();
        for(var e:WorldLayout.ENTRANCES) {
            boolean near=Point2D.distance(s.getX(),s.getY(),e.x(),e.y())<=57;
            int ex=((int)Math.round(e.x())/4)*4,ey=((int)Math.round(e.y())/4)*4;
            PixelArtRenderer.pxBorderBox(g,ex-20,ey-16,40,32,near?new Color(248,212,72,235):new Color(38,96,78,210),Color.WHITE);
            g.setColor(near?Color.BLACK:Color.WHITE);
            String tag="[E]";
            int tw=fm.stringWidth(tag);
            g.drawString(tag,ex-tw/2,ey+(fm.getAscent()-fm.getDescent())/2);
        }
        // 5 Kendaraan NPC di Jalur Kiri Jalan Raya (Multi-Traffic)
        Color[] npcColors={new Color(226,158,44),new Color(44,176,122),new Color(142,88,196),new Color(52,164,212),new Color(204,76,108)};
        for(var npc:game.getTrafficVehicles()) {
            com.innoelektrica.polymorphism.model.Kendaraan nv=npc.type().create();
            Color col=npcColors[Math.floorMod(npc.colorIndex(),npcColors.length)];
            PixelArtRenderer.drawPixelVehicle(g,nv,npc.x(),npc.y(),68,npc.facing(),period,anim,true,col);
            if(npc.yielding()) {
                PixelArtRenderer.drawPixelSignboard(g,"MINGGIR!",(int)Math.round(npc.x()),(int)Math.round(npc.y()-30),new Color(214,142,24),Color.WHITE);
            }
        }
        // Kendaraan 4-Arah + Pengendara (atau Parkir Tanpa Pengendara saat Jalan Kaki)
        if(s.isMounted()) {
            vehicle(g,s.getVehicle(),s.getX(),s.getY(),76,s.getFacing(),true);
        } else {
            vehicle(g,s.getVehicle(),s.getParkedX(),s.getParkedY(),76,0,false);
            pill(g,"Parkir [F]",s.getParkedX(),s.getParkedY()-34,11);
            player(g,s.getX(),s.getY(),new Color(53,137,204));
        }
        if(game.getTrafficEvent()==TrafficEvent.DOG_CROSSING) {
            PixelArtRenderer.drawPixelAnimalSprite(g,AnimalType.ANJING,s.getX()+38,s.getY()+6,46,period,anim);
        }
        if(game.getTrafficEvent()==TrafficEvent.RED_LIGHT) {
            PixelArtRenderer.pxBorderBox(g,(int)s.getX()+28,(int)s.getY()-52,24,24,Color.RED,Color.WHITE);
        }
    }
    @Override protected void paintLabels(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        String dirLabel=switch(PixelArtRenderer.directionOf(s.getFacing())) {
            case UP->"Utara";
            case DOWN->"Selatan";
            case LEFT->"Barat";
            case RIGHT->"Timur";
        };
        String modeInfo=s.isMounted()
            ?"Jalur Kiri ("+s.getVehicleType()+" -> "+dirLabel+") • [H] Klakson • [F] Turun"
            :"Jalan Kaki ("+dirLabel+") • [F] Naik "+s.getVehicleType()+" • [E] Masuk";
        title(g,"KOTA PIXEL — JALAN RAYA",modeInfo);
        for(var e:WorldLayout.ENTRANCES) {
            double dy=e.y()<300?-32:32;
            pill(g,e.location().getLabel(),e.x(),e.y()+dy,11);
        }
        if(game.getTrafficSeconds()>0) {
            pill(g,"[!] "+game.getTrafficEvent().getLabel(),500,300,15);
        }
    }
    @Override protected void onClick(double x,double y) {
        GameManager g=source.get();
        for(var e:WorldLayout.ENTRANCES) {
            if(Point2D.distance(x,y,e.x(),e.y())<46) {
                if(Point2D.distance(g.getState().getX(),g.getState().getY(),e.x(),e.y())<=57)ui.interact();
                else ui.message("Kendarai atau berjalan mendekati gerbang "+e.location().getLabel()+" dahulu.");
                return;
            }
        }
    }
}
