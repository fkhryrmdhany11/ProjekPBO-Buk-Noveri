package com.innoelektrica.polymorphism.game;
import com.innoelektrica.polymorphism.model.AnimalType;
import java.awt.geom.*;
import java.util.*;
public final class WorldLayout {
    public static final int WIDTH=1000,HEIGHT=600;
    public static final double LANE_OFFSET=14.0;
    public static final double ANIMAL_SOUND_RADIUS=68.0;
    public static final List<Rectangle2D.Double> ROADS=List.of(
        new Rectangle2D.Double(40,115,920,70),
        new Rectangle2D.Double(40,435,920,70),
        new Rectangle2D.Double(100,115,70,390),
        new Rectangle2D.Double(465,115,70,390),
        new Rectangle2D.Double(825,115,70,390)
    );
    private static final Area ROAD_AREA=new Area();
    static {
        for(var r:ROADS)ROAD_AREA.add(new Area(r));
    }
    public record Entrance(Location location,double x,double y) {
    }
    public static final List<Entrance> ENTRANCES=List.of(
        new Entrance(Location.HOME,265,470),
        new Entrance(Location.CITY,370,150),
        new Entrance(Location.PARK,650,150),
        new Entrance(Location.FUEL,695,470)
    );
    public static final Point2D.Double
        PARK_EXIT=new Point2D.Double(145,505),
        SHOP_DOOR=new Point2D.Double(140,220),
        PARK_BENCH=new Point2D.Double(455,145),
        PARK_FOUNTAIN=new Point2D.Double(725,195),
        CITY_NPC=new Point2D.Double(300,405),
        CITY_PLAZA=new Point2D.Double(680,440),
        CITY_QUEST_BOARD=new Point2D.Double(490,395),
        CITY_EXIT=new Point2D.Double(140,500);
    public static final Ellipse2D.Double POND=new Ellipse2D.Double(608,128,260,132);
    public static final Rectangle2D.Double SHOP=new Rectangle2D.Double(55,90,172,114);
    public static final List<Rectangle2D.Double> MAP_OBSTACLES=List.of(
        new Rectangle2D.Double(195,286,132,124), // Rumah Kita
        new Rectangle2D.Double(336,200,114,162), // Pusat Kota
        new Rectangle2D.Double(290,224,44,36),   // Papan Quest Peta
        new Rectangle2D.Double(556,196,248,142), // Pagar & Area Taman Kota di Peta
        new Rectangle2D.Double(588,346,196,72)   // Gedung & Kanopi SPBU Digital
    );
    public static final List<Rectangle2D.Double> CITY_OBSTACLES=List.of(
        new Rectangle2D.Double(64,88,216,134),   // Gedung Balai Kota
        new Rectangle2D.Double(316,88,204,134),  // Kantor Kurir Kota
        new Rectangle2D.Double(208,348,184,68),  // Kios Pak Raka
        new Rectangle2D.Double(468,360,44,34),   // Papan Quest Kota
        new Rectangle2D.Double(640,340,140,74),  // Kolam Air Mancur Plaza Kota
        new Rectangle2D.Double(96,526,76,36)     // Kendaraan Parkir Pusat Kota
    );
    public static final List<Rectangle2D.Double> PARK_OBSTACLES=List.of(
        SHOP,                                    // Kios & Kopi Taman
        new Rectangle2D.Double(604,124,268,140), // Kolam Air Mancur Taman
        new Rectangle2D.Double(104,526,76,36)    // Kendaraan Parkir Taman
    );
    private WorldLayout() {
    }
    public static List<Rectangle2D.Double> obstaclesFor(Location loc) {
        if(loc==null)return List.of();
        return switch(loc) {
            case MAP->MAP_OBSTACLES;
            case CITY->CITY_OBSTACLES;
            case PARK->PARK_OBSTACLES;
            default->List.of();
        };
    }
    public static boolean canDrive(double x,double y) {
        return ROAD_AREA.contains(x-20,y-20,40,40);
    }
    /** Menghitung koordinat lajur kiri (Left-Hand Traffic) sesuai arah gerak di jalan kota. */
    public static Point2D.Double leftLaneTarget(double x,double y,int dirX,int dirY) {
        if(dirX!=0&&dirY==0) {
            for(var r:ROADS) {
                if(r.width>r.height&&x>=r.x&&x<=r.x+r.width&&y>=r.y&&y<=r.y+r.height) {
                    double cy=r.y+r.height/2.0;
                    double targetY=dirX>0?(cy-LANE_OFFSET):(cy+LANE_OFFSET);
                    return new Point2D.Double(x,targetY);
                }
            }
        }
        else if(dirY!=0&&dirX==0) {
            for(var r:ROADS) {
                if(r.height>r.width&&x>=r.x&&x<=r.x+r.width&&y>=r.y&&y<=r.y+r.height) {
                    double cx=r.x+r.width/2.0;
                    double targetX=dirY>0?(cx+LANE_OFFSET):(cx-LANE_OFFSET);
                    return new Point2D.Double(targetX,y);
                }
            }
        }
        return null;
    }
    public static boolean canWalk(Location loc,double x,double y) {
        if(x<28||x>972||y<28||y>572)return false;
        double hx=x-10,hy=y-8,hw=20,hh=16;
        if(loc==Location.PARK&&POND.intersects(hx,hy,hw,hh))return false;
        for(var obs:obstaclesFor(loc)) {
            if(obs.intersects(hx,hy,hw,hh))return false;
        }
        return true;
    }
    public static Entrance nearEntrance(double x,double y) {
        return ENTRANCES.stream().filter(e->Point2D.distance(x,y,e.x,e.y)<=57).findFirst().orElse(null);
    }
    public static Point2D.Double animalHome(AnimalType a) {
        return switch(a) {
            case KUCING->new Point2D.Double(615,390);
            case ANJING->new Point2D.Double(400,250);
            case BURUNG->new Point2D.Double(275,420);
            case AYAM->new Point2D.Double(470,410);
            case ANGSA->new Point2D.Double(685,288);
        };
    }
}
