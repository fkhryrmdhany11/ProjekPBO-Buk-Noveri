package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.function.Supplier;
public final class GameScreen extends JPanel {
    private final Supplier<GameManager> source;
    private final InputController input;
    private final CardLayout cards=new CardLayout();
    private final JPanel canvas=new JPanel(cards);
    private final JLabel
        identity=UiTheme.label("",13),
        clock=UiTheme.label("",12),
        wallet=UiTheme.label("",13),
        mission=UiTheme.label("",11),
        hint=UiTheme.label("",12),
        notice=UiTheme.label("",11);
    private final JProgressBar sanityBar=new JProgressBar(0,100);
    private final JProgressBar fuelBar=new JProgressBar(0,100);
    private final JButton interaction,questBtn,mountBtn,honkBtn,inventory,leave,pause,demo,helpBtn,menu;
    private Location shown;
    @SuppressWarnings("this-escape")
    public GameScreen(Supplier<GameManager> source,InputController input,UiActions ui) {
        super(new BorderLayout(6,6));
        this.source=source;
        this.input=input;
        setBackground(UiTheme.CREAM);
        setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
        // PIXEL HUD ATAS (Minimalis & Ringkas 1-Strip)
        JPanel hud=new JPanel(new BorderLayout(8,0));
        hud.setBackground(UiTheme.DARK_CARD);
        hud.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.GOLD,2),
            BorderFactory.createEmptyBorder(5,10,5,10)
        ));
        JPanel row=new JPanel(new GridLayout(1,3,10,0));
        row.setOpaque(false);
        JPanel first=new JPanel(new GridLayout(2,1,0,1));
        first.setOpaque(false);
        identity.setForeground(UiTheme.GOLD);
        first.add(identity);
        first.add(clock);
        row.add(first);

        JPanel second=new JPanel(new GridLayout(2,1,0,3));
        second.setOpaque(false);
        wallet.setForeground(new Color(110,242,158));
        second.add(wallet);
        sanityBar.setStringPainted(true);
        sanityBar.setBackground(new Color(32,38,48));
        sanityBar.setForeground(PixelArtRenderer.SANITY_HIGH);
        sanityBar.setFont(new Font("Monospaced",Font.BOLD,11));
        second.add(sanityBar);
        row.add(second);

        JPanel third=new JPanel(new GridLayout(2,1,0,3));
        third.setOpaque(false);
        fuelBar.setStringPainted(true);
        fuelBar.setBackground(new Color(32,38,48));
        fuelBar.setForeground(PixelArtRenderer.FUEL_COLOR);
        fuelBar.setFont(new Font("Monospaced",Font.BOLD,11));
        third.add(fuelBar);
        mission.setForeground(new Color(196,224,255));
        third.add(mission);
        row.add(third);

        hud.add(row,BorderLayout.CENTER);
        add(hud,BorderLayout.NORTH);

        // KANVAS PIXEL UTAMA (1000x600 adaptif)
        canvas.setBackground(new Color(18,24,32));
        canvas.add(new HomePanel(source,ui),Location.HOME.name());
        canvas.add(new WorldMapPanel(source,ui),Location.MAP.name());
        canvas.add(new ParkPanel(source,ui),Location.PARK.name());
        canvas.add(new CityScenePanel(source,ui),Location.CITY.name());
        canvas.add(new FuelStationPanel(source,ui),Location.FUEL.name());
        add(canvas,BorderLayout.CENTER);

        // PIXEL CONTROL DECK BAWAH (Bersih + Baris Shortcut Lengkap)
        JPanel bottom=new JPanel(new BorderLayout(4,4));
        bottom.setOpaque(false);
        JPanel text=new JPanel(new GridLayout(2,1,0,2));
        text.setBackground(UiTheme.DARK_CARD);
        text.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(74,112,98),1),
            BorderFactory.createEmptyBorder(4,10,4,10)
        ));
        hint.setForeground(UiTheme.GOLD);
        notice.setForeground(new Color(188,242,212));
        text.add(hint);
        text.add(notice);
        bottom.add(text,BorderLayout.NORTH);

        JPanel buttons=new JPanel(new GridLayout(1,10,4,0));
        buttons.setOpaque(false);
        interaction=UiTheme.button("Aksi [E]",ui::interact);
        questBtn=UiTheme.button("Quest [Q]",ui::questBoard);
        mountBtn=UiTheme.button("Turun [F]",ui::toggleMount);
        honkBtn=UiTheme.button("Klakson [H]",ui::honk);
        inventory=UiTheme.button("Tas [I]",ui::inventory);
        leave=UiTheme.button("Peta [X]",ui::leave);
        pause=UiTheme.button("Jeda [P]",ui::togglePause);
        demo=UiTheme.button("Demo",ui::demo);
        helpBtn=UiTheme.button("Bantuan [?]",ui::help);
        menu=UiTheme.button("Menu [Esc]",ui::menu);
        for(JButton b:new JButton[] {
            interaction,questBtn,mountBtn,honkBtn,inventory,leave,pause,demo,helpBtn,menu
        })buttons.add(b);
        bottom.add(buttons,BorderLayout.CENTER);
        add(bottom,BorderLayout.SOUTH);
    }
    public void refresh() {
        GameManager g=source.get();
        if(g==null)return;
        GameState s=g.getState();
        if(shown!=s.getLocation()) {
            shown=s.getLocation();
            cards.show(canvas,shown.name());
            input.stop();
        }
        identity.setText(s.getPlayerName()+" • HARI "+s.getDay()+(g.isDemo()?" [DEMO]":""));
        clock.setText(s.getClock()+" ("+s.getPeriod().getLabel()+") • "+s.getLocation().getLabel()+(s.getDayStatus()==DayStatus.ENDED?" [SELESAI]":""));
        wallet.setText(GameManager.rupiah(s.getMoney())+" | Quest selesai: "+s.getQuestsCompletedToday());

        int san=s.getSanity();
        sanityBar.setValue(san);
        sanityBar.setForeground(san>=60?PixelArtRenderer.SANITY_HIGH:(san>=30?PixelArtRenderer.SANITY_MED:PixelArtRenderer.SANITY_LOW));
        sanityBar.setString("SANITY: "+san+"% ("+s.getSanityMood()+")");

        fuelBar.setValue(s.getFuelPercent());
        fuelBar.setString(s.getVehicleType()==null?"BBM: Pilih Kendaraan [E]":"BBM "+s.getVehicleType()+String.format(Locale.ROOT,": %.1f/%.0fL (%d%%)",s.getFuelLiters(),s.getVehicle().getKapasitasTangki(),s.getFuelPercent()));
        mission.setText("Misi: Taman "+(s.isVisitedPark()?"1":"0")+"/1 | Hewan "+s.getFedAnimals().size()+"/2 | Pulang "+(s.isReturnedHome()?"1":"0")+"/1");
        hint.setText(g.getNearbyTarget().label());
        String shortcuts="[Shortcut] [WASD] Gerak | [R] Langsung Pulang | [H] Klakson | [B] Darurat | [K] Simpan | [L] Muat";
        notice.setText(g.getNotice().isEmpty()?shortcuts:g.getNotice()+"  •  "+shortcuts);

        boolean available=!g.isPaused()&&!g.isUiBlocked()&&!g.isDemo();
        interaction.setEnabled(available);
        questBtn.setEnabled(!g.isPaused()&&!g.isDemo());
        mountBtn.setEnabled(available&&!g.isBusy()&&shown==Location.MAP);
        mountBtn.setText(s.isMounted()?"Turun [F]":"Naik [F]");
        honkBtn.setEnabled(available&&shown==Location.MAP&&s.isMounted());
        inventory.setEnabled(!g.isDemo());
        pause.setEnabled(!g.isUiBlocked()&&s.getDayStatus()==DayStatus.ACTIVE);
        pause.setText(g.isPaused()?"Lanjut [P]":"Jeda [P]");
        demo.setText(g.isDemo()?"Stop Demo":"Demo");
        demo.setEnabled(!g.isUiBlocked()&&(g.isDemo()||!g.isBusy()));
        helpBtn.setEnabled(!g.isPaused()&&!g.isDemo());
        menu.setEnabled(g.isDemo()||!g.isBusy());
        leave.setEnabled(available&&g.getPendingFuel()==null&&!g.isCityActivity()&&(shown==Location.PARK||shown==Location.CITY||shown==Location.FUEL));
        canvas.repaint();
    }
}
