package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import com.innoelektrica.polymorphism.model.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.function.Supplier;
public final class FuelStationPanel extends ScenePanel {
    private static final int PUMP1_CX = 420;
    private static final int PUMP2_CX = 656;
    private static final int LANE_Y = 458;
    public FuelStationPanel(Supplier<GameManager> s,UiActions u) {
        super(s,u);
    }
    @Override protected void paintWorld(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        double anim=game.getAnimationSeconds();
        TimePeriod period=s.getPeriod();
        boolean night=period==TimePeriod.MALAM||period==TimePeriod.SORE;
        // Latar Langit & Taman Belakang SPBU Pixel
        PixelArtRenderer.pxRect(g,0,0,1000,330,period==TimePeriod.MALAM?new Color(22,32,64):new Color(126,192,238));
        PixelArtRenderer.drawPixelGrassCanvas(g,1000,124,anim);
        // Pelataran Trotoar & Jalur Drive-Thru Aspal SPBU
        PixelArtRenderer.drawPixelSidewalk(g,0,320,1000,280);
        PixelArtRenderer.drawPixelRoad(g,0,396,1000,140,true);
        // Tiang Penyangga Kanopi SPBU
        PixelArtRenderer.pxRect(g,180,176,24,212,new Color(110,118,128));
        PixelArtRenderer.pxRect(g,796,176,24,212,new Color(110,118,128));
        // Pulau Pompa Bensin (Fuel Island) Sejajar dengan Jalur Drive-Thru
        PixelArtRenderer.pxBorderBox(g,236,344,548,48,new Color(156,162,170),new Color(244,204,68));
        // Marka Kotak Berhenti Kendaraan Tepat di Depan Pompa 01 & Pompa 02
        PixelArtRenderer.pxBorderBox(g,PUMP1_CX-72,LANE_Y-32,144,64,new Color(44,50,60),
            game.getQueueCount()==0?PixelArtRenderer.SANITY_HIGH:PixelArtRenderer.LANE_YELLOW);
        PixelArtRenderer.pxBorderBox(g,PUMP2_CX-72,LANE_Y-32,144,64,new Color(44,50,60),new Color(120,130,142));

        // 2 Mesin Dispenser Digital SPBU (Pompa 01 Utama & Pompa 02)
        int[] pumps={PUMP1_CX,PUMP2_CX};
        for(int i=0;i<pumps.length;i++) {
            int cx=pumps[i],px=cx-44,py=236;
            boolean activePump=(i==0);
            PixelArtRenderer.pxBorderBox(g,px,py,88,136,new Color(214,48,44),Color.WHITE);
            // Header Dispenser
            PixelArtRenderer.pxRect(g,px+8,py+8,72,18,new Color(245,245,240));
            g.setColor(new Color(188,28,28));
            g.setFont(new Font("Monospaced",Font.BOLD,11));
            g.drawString(activePump?"POMPA 01":"POMPA 02",px+18,py+21);
            // Layar Meteran Digital LED
            PixelArtRenderer.pxRect(g,px+10,py+30,68,44,new Color(16,22,28));
            g.setColor(new Color(98,246,154));
            g.setFont(new Font("Monospaced",Font.BOLD,11));
            g.drawString("Rp10k/L",px+16,py+46);
            String pumpStatus=activePump
                ?(game.getPendingFuel()!=null?"FILLING":(game.getQueueCount()>0?"BUSY":"READY"))
                :(game.getQueueCount()>=2?"BUSY":"STANDBY");
            g.setColor(game.getPendingFuel()!=null&&activePump?PixelArtRenderer.HUD_BORDER:new Color(98,246,154));
            g.drawString(pumpStatus,px+16,py+64);
            // Panel Bawah & Nozzle Holder di Sisi Kanan Pompa
            PixelArtRenderer.pxRect(g,px+14,py+82,60,44,new Color(238,238,232));
            PixelArtRenderer.pxRect(g,px+88,py+74,10,26,new Color(42,48,56));
        }

        // Petugas SPBU berseragam merah di Pulau Pompa
        PixelArtRenderer.drawPixelCharacter(g,528,358,new Color(214,48,44),Math.PI/2,false,false,anim);

        // Kanopi SPBU Merah-Putih (Di bawah baris HUD/Radar agar tidak tertutup)
        PixelArtRenderer.pxBorderBox(g,120,132,760,52,new Color(218,52,48),Color.WHITE);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced",Font.BOLD,19));
        String canopyTitle="[SPBU DIGITAL KOTA — PERTALITE & PERTAMAX]";
        int ctw=g.getFontMetrics().stringWidth(canopyTitle);
        g.drawString(canopyTitle,500-ctw/2,164);

        // Penempatan Kendaraan Antrean & Kendaraan Pemain Sejajar Mesin Pompa
        Kendaraan playerVehicle=s.getVehicle()!=null?s.getVehicle():new Motor();
        boolean isMotor=playerVehicle instanceof Motor;
        int qCount=game.getQueueCount();
        Color[] queueColors={new Color(218,164,48),new Color(72,168,118),new Color(168,92,188)};

        if(qCount>0) {
            // Kendaraan antrean berada di Pompa 01, Pompa 02, dan jalur keluar kanan
            int[] slotX={PUMP1_CX,PUMP2_CX,856};
            for(int i=0;i<qCount;i++) {
                int baseSlot=slotX[Math.min(i,slotX.length-1)];
                double qx=baseSlot+(game.getQueueProgress()*28);
                Color qc=queueColors[i%queueColors.length];
                PixelArtRenderer.drawPixelCar(g,(int)Math.round(qx),LANE_Y,1.2,PixelArtRenderer.Direction.RIGHT,true,night,qc,anim);
            }
            // Selang Pompa 01 terhubung ke mobil antrean di depan Pompa 01
            int frontCarX=(int)Math.round(PUMP1_CX+(game.getQueueProgress()*28));
            drawFuelHose(g,PUMP1_CX+48,312,frontCarX-22,LANE_Y-8,true,anim);
            if(qCount>=2) {
                int secCarX=(int)Math.round(PUMP2_CX+(game.getQueueProgress()*28));
                drawFuelHose(g,PUMP2_CX+48,312,secCarX-22,LANE_Y-8,true,anim);
            }
            // Kendaraan pemain menunggu dengan rapi di jalur masuk sebelah kiri Pompa 01
            int playerWaitX=212;
            vehicle(g,playerVehicle,playerWaitX,LANE_Y,isMotor?88:96,0,true);
            pill(g,"Posisi Kita (Antre)",playerWaitX,LANE_Y+44,11);
        } else {
            // Giliran Pemain: Kendaraan berhenti TEPAT di depan Pompa 01!
            int vehicleX=PUMP1_CX;
            vehicle(g,playerVehicle,vehicleX,LANE_Y,isMotor?92:104,0,true);
            int tankX=isMotor?(vehicleX+4):(vehicleX-24);
            int tankY=isMotor?(LANE_Y-14):(LANE_Y-8);
            boolean filling=game.getPendingFuel()!=null;
            drawFuelHose(g,PUMP1_CX+48,312,tankX,tankY,filling,anim);
        }
    }
    private void drawFuelHose(Graphics2D g,int nozzleX,int nozzleY,int tankX,int tankY,boolean filling,double anim) {
        Graphics2D g2=(Graphics2D)g.create();
        g2.setColor(new Color(28,32,38));
        g2.setStroke(new BasicStroke(6,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER));
        int ctrlX=(nozzleX+tankX)/2+26;
        int ctrlY=Math.max(nozzleY,tankY)+34;
        g2.draw(new QuadCurve2D.Double(nozzleX,nozzleY,ctrlX,ctrlY,tankX,tankY));
        // Ujung Nozzle Merah di Tangki Kendaraan
        PixelArtRenderer.pxRect(g2,tankX-4,tankY-6,10,8,new Color(228,52,48));
        if(filling) {
            int pulse=((int)(anim*10))%2;
            g2.setColor(pulse==0?PixelArtRenderer.SANITY_HIGH:PixelArtRenderer.HUD_BORDER);
            g2.fillRect((nozzleX+tankX)/2+6,(nozzleY+tankY)/2+10,8,8);
        }
        g2.dispose();
    }
    @Override protected void paintLabels(Graphics2D g,GameManager game) {
        GameState s=game.getState();
        double cap=s.getVehicle()==null?10.0:s.getVehicle().getKapasitasTangki();
        title(g,"SPBU DIGITAL KOTA","Rp10.000/L • Dispenser Pompa 01 (+1L, +3L, +5L, Full) • [X] Ke Peta");
        if(game.getPendingFuel()!=null) {
            progress(g,"Mengalirkan BBM dari Pompa 01...",500,186,400,game.getFillProgress());
        } else {
            pill(g,game.getQueueCount()>0
                ?"Antrean Pompa 01 ("+game.getQueueCount()+")"
                :"Pompa 01 Siap [E]",500,206,12);
        }
        pill(g,String.format(java.util.Locale.ROOT,"Tangki %s: %.2f/%.0f L (%d%%)",
            s.getVehicleType(),s.getFuelLiters(),cap,s.getFuelPercent()),PUMP1_CX,512,11);
    }
    @Override protected void onClick(double x,double y) {
        if(y>540&&x>580)ui.leave();
        else ui.fuel();
    }
}
