package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import com.innoelektrica.polymorphism.model.*;
import java.awt.*;
import java.util.function.Supplier;
public final class HomePanel extends ScenePanel {
    public HomePanel(Supplier<GameManager> s,UiActions u) {
        super(s,u);
    }
    @Override protected void paintWorld(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        double anim=game.getAnimationSeconds();
        // Dinding & Lantai Kayu Pixel-Art Rumah
        PixelArtRenderer.pxRect(g,0,0,1000,380,new Color(228,216,184));
        PixelArtRenderer.pxRect(g,0,372,1000,12,new Color(118,82,52));
        PixelArtRenderer.pxRect(g,0,384,1000,216,new Color(168,118,78));
        g.setColor(new Color(124,84,52));
        for(int y=404;y<600;y+=32)g.fillRect(0,y,1000,4);
        // Bingkai Peta Kota 16-Bit Pixel-Art Murni di Dinding
        PixelArtRenderer.pxBorderBox(g,48,112,376,232,new Color(78,54,36),PixelArtRenderer.HUD_BORDER);
        PixelArtRenderer.drawMiniMapPreview(g,60,124,352,208,anim);
        PixelArtRenderer.drawPixelSignboard(g,"PETA KOTA PIXEL",236,104,new Color(28,36,48),PixelArtRenderer.HUD_BORDER);
        // Sofa Hijau Pixel untuk Istirahat
        PixelArtRenderer.pxBorderBox(g,52,388,308,96,new Color(62,118,86),new Color(38,78,56));
        PixelArtRenderer.pxRect(g,72,368,268,54,new Color(88,148,112));
        // Area Garasi Pixel (Motor & Mobil Parkir Tanpa Pengendara)
        PixelArtRenderer.pxBorderBox(g,504,132,456,316,new Color(44,52,64),PixelArtRenderer.HUD_BORDER);
        PixelArtRenderer.drawPixelSignboard(g,"GARASI KENDARAAN PRIBADI",732,124,new Color(28,36,48),PixelArtRenderer.HUD_BORDER);
        // Slot Parkir Motor (Kiri) & Mobil (Kanan) dengan Highlight Pilihan Presisi
        boolean motorSel=s.getVehicleType()==VehicleType.MOTOR;
        boolean mobilSel=s.getVehicleType()==VehicleType.MOBIL;
        PixelArtRenderer.pxBorderBox(g,524,160,200,176,motorSel?new Color(38,78,64):new Color(58,68,82),motorSel?PixelArtRenderer.SANITY_HIGH:new Color(110,122,138));
        PixelArtRenderer.pxBorderBox(g,740,160,200,176,mobilSel?new Color(38,78,64):new Color(58,68,82),mobilSel?PixelArtRenderer.SANITY_HIGH:new Color(110,122,138));
        vehicle(g,new Motor(),624,252,108,0,false);
        vehicle(g,new Mobil(),840,252,108,0,false);
        player(g,448,456,new Color(53,137,204));
    }
    @Override protected void paintLabels(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        title(g,"RUMAH & GARASI","Pilih kendaraan [E], cek Quest [Q], atau akhiri hari.");
        pill(g,"Motor • 10 L (Cepat)",624,358,11);
        pill(g,"Mobil • 40 L (Kurir 1.5x)",840,358,11);
        pill(g,s.getVehicleType()==null?"Pilih Kendaraan [E]":"Aktif: "+s.getVehicleType()+" [E]",732,406,13);
        pill(g,s.getDayStatus()==DayStatus.ENDED?"[E] Ringkasan Hari / Save / Hari Berikutnya":"[E] Pilih Kendaraan / Berangkat  |  [Q] Quest",500,532,12);
    }
    @Override protected void onClick(double x,double y) {
        ui.home();
    }
}
