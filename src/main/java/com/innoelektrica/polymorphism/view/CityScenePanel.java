package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import java.awt.*;
import java.util.function.Supplier;
public final class CityScenePanel extends ScenePanel {
    public CityScenePanel(Supplier<GameManager> s,UiActions u) {
        super(s,u);
    }
    @Override protected void paintWorld(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        double anim=game.getAnimationSeconds();
        TimePeriod period=s.getPeriod();
        PixelArtRenderer.drawPixelGrassCanvas(g,1000,600,anim);
        // Jalan Raya Utama & Trotoar Pusat Kota
        PixelArtRenderer.drawPixelSidewalk(g,0,225,1000,260);
        PixelArtRenderer.drawPixelRoad(g,0,255,1000,110,true);
        PixelArtRenderer.drawCrosswalk(g,465,255,36,110,true);
        // Gedung Balai Kota, Kantor Kurir Kota & Plaza Air Mancur
        PixelArtRenderer.drawPixelBuilding(g,64,92,216,130,new Color(122,148,176),new Color(52,74,98),"GEDUNG BALAI KOTA",period);
        PixelArtRenderer.drawPixelBuilding(g,316,92,204,130,new Color(198,142,102),new Color(118,64,42),"KANTOR KURIR KOTA",period);
        PixelArtRenderer.drawPixelPondAndFountain(g,640,340,140,74,anim);
        // Kios Pak Raka & Papan Quest Kota
        PixelArtRenderer.pxBorderBox(g,212,368,176,52,new Color(212,162,92),new Color(45,104,82));
        PixelArtRenderer.pxRect(g,208,348,184,22,new Color(42,112,86));
        PixelArtRenderer.drawPixelSignboard(g,"KIOS PAK RAKA",300,342,new Color(24,36,48),PixelArtRenderer.HUD_BORDER);
        PixelArtRenderer.drawPixelCharacter(g,300,408,new Color(204,118,52),0,false,false,anim);
        PixelArtRenderer.drawPixelQuestBoard(g,(int)WorldLayout.CITY_QUEST_BOARD.x,(int)WorldLayout.CITY_QUEST_BOARD.y,game.getActiveQuest()!=null,anim);
        // Pepohonan & Lampu Jalan Kota
        PixelArtRenderer.drawPixelTree(g,865,155,anim);
        PixelArtRenderer.drawPixelTree(g,890,460,anim);
        PixelArtRenderer.drawPixelStreetLamp(g,190,245,period);
        PixelArtRenderer.drawPixelStreetLamp(g,565,245,period);
        PixelArtRenderer.drawPixelStreetLamp(g,820,385,period);
        // Parkir Kendaraan Pemain (Tanpa Pengendara) & Karakter Pemain
        PixelArtRenderer.drawPixelSidewalk(g,56,492,156,84);
        vehicle(g,s.getVehicle(),134,544,88,0,false);
        player(g,s.getX(),s.getY(),new Color(53,137,204));
    }
    @Override protected void paintLabels(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        title(g,"PUSAT KOTA","Kios Pak Raka [E], Papan Quest [Q], atau Plaza Air Mancur [E].");
        pill(g,"Pak Raka [E]",300,318,11);
        pill(g,s.isCityWorkDone()?"Upah Kios Selesai":"Kerja +Rp15.000",300,446,11);
        pill(g,"Quest [E/Q]",490,318,11);
        pill(g,s.isCityExplored()?"Plaza Selesai":"Plaza (+5% SAN) [E]",690,486,11);
        pill(g,"Keluar [E]",134,492,11);
        if(game.isCityActivity())progress(g,"Merapikan logistik kios Pak Raka...",500,524,360,game.getActivityProgress());
    }
    @Override protected void onClick(double x,double y) {
        if(WorldLayout.CITY_NPC.distance(x,y)<68)ui.cityNpc();
        else if(WorldLayout.CITY_QUEST_BOARD.distance(x,y)<68)ui.questBoard();
        else if(WorldLayout.CITY_PLAZA.distance(x,y)<78) {
            if(WorldLayout.CITY_PLAZA.distance(source.get().getState().getX(),source.get().getState().getY())<=85)ui.interact();
            else ui.message("Dekati Plaza Air Mancur dahulu.");
        }
        else if(WorldLayout.CITY_EXIT.distance(x,y)<65)ui.leave();
    }
}
