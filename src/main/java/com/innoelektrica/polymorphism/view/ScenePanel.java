package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import com.innoelektrica.polymorphism.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Supplier;
public abstract class ScenePanel extends JPanel {
    protected final Supplier<GameManager> source;
    protected final UiActions ui;
    @SuppressWarnings("this-escape")
    protected ScenePanel(Supplier<GameManager> source,UiActions ui) {
        this.source=source;
        this.ui=ui;
        setBackground(new Color(18,24,32));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                GameManager g=source.get();
                if(g==null||g.isDemo())return;
                double k=scale(),ox=(getWidth()-1000*k)/2,oy=(getHeight()-600*k)/2;
                double x=(e.getX()-ox)/k,y=(e.getY()-oy)/k;
                if(x<0||x>1000||y<0||y>600)return;
                if(ui.handleOverlayClick(x,y))return;
                if(g.isPaused()||g.isUiBlocked())return;
                if(x>=740&&x<=984&&y>=14&&y<=126) {
                    ui.questBoard();
                    return;
                }
                onClick(x,y);
            }
        });
    }
    private double scale() {
        return Math.max(.01,Math.min(getWidth()/1000.0,getHeight()/600.0));
    }
    @Override protected final void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        GameManager game=source.get();
        if(game==null)return;
        Graphics2D g=(Graphics2D)graphics.create();
        double k=scale();
        g.translate((getWidth()-1000*k)/2,(getHeight()-600*k)/2);
        g.scale(k,k);
        PixelArtRenderer.enableCrispPixels(g);
        paintWorld(g,game);
        Color tint=switch(game.getState().getPeriod()) {
            case PAGI->new Color(255,228,148,14);
            case SIANG->new Color(255,255,210,8);
            case SORE->new Color(225,124,58,48);
            case MALAM->new Color(12,22,62,96);
        };
        g.setColor(tint);
        g.fillRect(0,0,1000,600);
        paintLabels(g,game);
        PixelArtRenderer.drawQuestGuidance(g,game);
        PixelArtRenderer.drawMiniMapAndTracker(g,game);
        ui.paintOverlay(g,game);
        if(game.isPaused()) {
            g.setColor(new Color(8,14,22,160));
            g.fillRect(0,0,1000,600);
            pill(g,"[PAUSE] Tekan [P] atau tombol Lanjut",500,290,18);
        }
        g.dispose();
    }
    protected abstract void paintWorld(Graphics2D g,GameManager game);
    protected abstract void paintLabels(Graphics2D g,GameManager game);
    protected void onClick(double x,double y) {
        ui.interact();
    }
    protected void title(Graphics2D g,String heading,String subtitle) {
        Font fHead=new Font("Monospaced",Font.BOLD,15);
        Font fSub=new Font("Monospaced",Font.PLAIN,11);
        int subW=g.getFontMetrics(fSub).stringWidth(subtitle);
        int headW=g.getFontMetrics(fHead).stringWidth(heading);
        int boxW=Math.max(320,Math.min(540,Math.max(headW,subW)+28));
        PixelArtRenderer.pxBorderBox(g,18,14,boxW,50,PixelArtRenderer.HUD_BG,PixelArtRenderer.HUD_BORDER);
        g.setColor(PixelArtRenderer.HUD_BORDER);
        g.setFont(fHead);
        g.drawString(heading,30,34);
        g.setColor(new Color(224,236,230));
        g.setFont(fSub);
        g.drawString(subtitle,30,54);
    }
    protected void pill(Graphics2D g,String text,double cx,double cy,int size) {
        g.setFont(new Font("Monospaced",Font.BOLD,Math.max(11,size-1)));
        FontMetrics fm=g.getFontMetrics();
        int textW=fm.stringWidth(text);
        int w=((textW+24+3)/4)*4;
        int h=((fm.getHeight()+10+3)/4)*4;
        int rx=((int)Math.round(cx-w/2.0)/4)*4;
        int ry=((int)Math.round(cy-h/2.0)/4)*4;
        PixelArtRenderer.pxBorderBox(g,rx,ry,w,h,new Color(22,28,38,236),PixelArtRenderer.HUD_BORDER);
        g.setColor(Color.WHITE);
        int tx=rx+(w-textW)/2;
        int ty=ry+(h+fm.getAscent()-fm.getDescent())/2;
        g.drawString(text,tx,ty);
    }
    protected void player(Graphics2D g,double x,double y,Color shirt) {
        GameManager game=source.get();
        boolean lowSanity=game!=null&&game.getState().isLowSanity();
        double anim=game!=null?game.getAnimationSeconds():0;
        double facing=game!=null?game.getState().getFacing():0;
        PixelArtRenderer.drawPixelCharacter(g,x,y,shirt,facing,true,lowSanity,anim);
    }
    protected void vehicle(Graphics2D g,Kendaraan v,double x,double y,double width,double angle) {
        vehicle(g,v,x,y,width,angle,true);
    }
    protected void vehicle(Graphics2D g,Kendaraan v,double x,double y,double width,double angle,boolean hasRider) {
        GameManager game=source.get();
        TimePeriod period=game!=null?game.getState().getPeriod():TimePeriod.PAGI;
        double anim=game!=null?game.getAnimationSeconds():0;
        PixelArtRenderer.drawPixelVehicle(g,v,x,y,width,angle,period,anim,hasRider);
    }
    protected void progress(Graphics2D g,String text,double cx,double cy,int width,double ratio) {
        g.setFont(new Font("Monospaced",Font.BOLD,12));
        int textW=g.getFontMetrics().stringWidth(text);
        int w=Math.max(width,((textW+36+3)/4)*4);
        int x=((int)Math.round(cx-w/2.0)/4)*4,y=((int)Math.round(cy)/4)*4;
        PixelArtRenderer.pxBorderBox(g,x,y,w,48,PixelArtRenderer.HUD_BG,PixelArtRenderer.HUD_BORDER);
        g.setColor(Color.WHITE);
        g.drawString(text,x+(w-textW)/2,y+20);
        PixelArtRenderer.pxRect(g,x+14,y+26,w-28,12,new Color(45,55,68));
        int fill=(int)Math.round((w-28)*Math.max(0,Math.min(1,ratio)));
        PixelArtRenderer.pxRect(g,x+14,y+26,fill,12,PixelArtRenderer.SANITY_HIGH);
    }
    protected void building(Graphics2D g,int x,int y,int w,int h,Color wall) {
        GameManager game=source.get();
        TimePeriod p=game!=null?game.getState().getPeriod():TimePeriod.PAGI;
        PixelArtRenderer.drawPixelBuilding(g,x,y,w,h,wall,wall.darker().darker(),"",p);
    }
}
