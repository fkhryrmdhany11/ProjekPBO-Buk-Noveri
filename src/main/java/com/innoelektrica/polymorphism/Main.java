package com.innoelektrica.polymorphism;
import com.innoelektrica.polymorphism.game.ResourceVerifier;
import com.innoelektrica.polymorphism.view.MainFrame;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
public final class Main {
    private Main() {
    }
    public static void main(String[] args) {
        if(args.length>0&&args[0].equals("--check-resources")) {
            try {
                ResourceVerifier.verify();
            }
            catch(Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            return;
        }
        if(GraphicsEnvironment.isHeadless()) {
            System.err.println("GUI memerlukan desktop. Jalankan JAR pada Windows/Linux desktop dengan Java 17+.");
            System.exit(1);
        }
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            for(Object key:UIManager.getDefaults().keySet())if(UIManager.get(key) instanceof javax.swing.plaf.FontUIResource)UIManager.put(key,new FontUIResource("SansSerif",Font.PLAIN,13));
        }
        catch(Exception ex) {
            System.err.println("Look and feel default digunakan: "+ex.getMessage());
        }
        SwingUtilities.invokeLater(()-> {
            try {
                new MainFrame().setVisible(true);
            }
            catch(Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,"Aplikasi belum dapat dimulai: "+ex.getMessage(),"A Day in My City",JOptionPane.ERROR_MESSAGE);
            }
        }
        );
    }
}
