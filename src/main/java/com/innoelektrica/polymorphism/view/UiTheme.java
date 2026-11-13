package com.innoelektrica.polymorphism.view;
import javax.swing.*;
import java.awt.*;
final class UiTheme {
    static final Color
        CREAM = new Color(24, 30, 42),
        TEAL = new Color(38, 108, 86),
        INK = new Color(238, 242, 236),
        GOLD = new Color(242, 198, 76),
        DARK_CARD = new Color(16, 20, 28);
    private UiTheme() {
    }
    static JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.BOLD, 12));
        b.setBackground(TEAL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GOLD, 2),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }
    static JTextArea paragraph(String s) {
        JTextArea t = new JTextArea() {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
            @Override public void setText(String text) {
                super.setText(text);
                int rows = 0;
                for (String line : text.split("\\n", -1)) rows += Math.max(1, (line.length() + 44) / 45);
                setRows(Math.max(2, rows));
                revalidate();
            }
        };
        t.setColumns(40);
        t.setText(s);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setEditable(false);
        t.setOpaque(false);
        t.setFont(new Font("Monospaced", Font.PLAIN, 13));
        t.setForeground(INK);
        t.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        return t;
    }
    static JLabel label(String s, int size) {
        JLabel l = new JLabel(s);
        l.setForeground(INK);
        l.setFont(new Font("Monospaced", Font.BOLD, size));
        return l;
    }
}
