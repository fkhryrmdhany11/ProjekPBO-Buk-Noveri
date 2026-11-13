package com.innoelektrica.polymorphism.view;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
public final class InputController {
    private final Set<String> pressed=new HashSet<>(),suppressed=new HashSet<>(),commands=new HashSet<>();
    private int guiX,guiY;
    private static final String[] DIRECTIONS= {
        "W","A","S","D","UP","LEFT","DOWN","RIGHT"
    };
    public int getX() {
        int x=guiX+(pressed.contains("D")||pressed.contains("RIGHT")?1:0)-(pressed.contains("A")||pressed.contains("LEFT")?1:0);
        return Integer.signum(x);
    }
    public int getY() {
        int y=guiY+(pressed.contains("S")||pressed.contains("DOWN")?1:0)-(pressed.contains("W")||pressed.contains("UP")?1:0);
        return Integer.signum(y);
    }
    public void stop() {
        suppressed.addAll(pressed);
        pressed.clear();
        guiX=guiY=0;
    }
    public void clear() {
        pressed.clear();
        suppressed.clear();
        commands.clear();
        guiX=guiY=0;
    }
    public void direction(int x,int y) {
        stop();
        guiX=x;
        guiY=y;
    }
    private void bind(JComponent c,String stroke,String id,Runnable r) {
        c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(stroke),id);
        c.getActionMap().put(id,new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                r.run();
            }
        });
    }
    public void install(JComponent c,UiActions ui) {
        for(String k:DIRECTIONS) {
            bind(c,"pressed "+k,"press."+k,()-> {
                if(!suppressed.contains(k)) {
                    pressed.add(k);
                    guiX=guiY=0;
                }
            });
            bind(c,"released "+k,"release."+k,()->release(k));
        }
        command(c,"E",ui::interact);
        command(c,"I",ui::inventory);
        command(c,"P",ui::togglePause);
        command(c,"F",ui::toggleMount);
        command(c,"Q",ui::questBoard);
        command(c,"H",ui::honk);
        command(c,"R",ui::goHome);
        command(c,"B",ui::emergency);
        command(c,"K",ui::saveGame);
        command(c,"L",ui::loadGame);
        command(c,"X",ui::leave);
        command(c,"F1",ui::help);
        command(c,"SLASH",ui::help);
        command(c,"ESCAPE",ui::menu);
        for(int n=1;n<=7;n++) {
            int choice=n;
            command(c,String.valueOf(n),()->ui.handleKeyChoice(choice));
        }
        bind(c,"pressed SPACE","stop",this::stop);
    }
    private void command(JComponent c,String k,Runnable r) {
        bind(c,"pressed "+k,"command."+k,()-> {
            if(commands.add(k))r.run();
        });
        bind(c,"released "+k,"release."+k,()->release(k));
    }
    private void release(String k) {
        pressed.remove(k);
        suppressed.remove(k);
        commands.remove(k);
    }
    public void installReleaseBindings(JComponent c) {
        for(String k:DIRECTIONS)bind(c,"released "+k,"release."+k,()->release(k));
        for(String k:new String[] {
            "E","I","P","F","Q","H","R","B","K","L","X","F1","SLASH","ESCAPE","1","2","3","4","5","6","7"
        })bind(c,"released "+k,"release."+k,()->release(k));
    }
}
