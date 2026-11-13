package com.innoelektrica.polymorphism.view;
import javax.swing.*;
import java.awt.*;
public final class MenuPanel extends JPanel {
    private final JLabel saveInfo = UiTheme.label("", 12);
    private final JButton resume;
    private final JTextField playerNameField = new JTextField("Warga Pixel");
    @SuppressWarnings("this-escape")
    public MenuPanel(Runnable start, Runnable load, Runnable continueGame, Runnable help, Runnable demo, Runnable exit) {
        super(new GridLayout(1, 2, 0, 0));
        JPanel cover = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                PixelArtRenderer.enableCrispPixels(g);
                int w = getWidth(), h = getHeight();
                PixelArtRenderer.pxRect(g, 0, 0, w, h, new Color(18, 28, 48));
                // Bintang pixel malam kota
                g.setColor(new Color(255, 244, 180));
                for (int i = 0; i < 24; i++) {
                    int sx = (i * 73) % Math.max(100, w - 20);
                    int sy = 20 + (i * 37) % Math.max(80, h / 3);
                    g.fillRect(sx, sy, 4, 4);
                }
                // Ilustrasi Peta Kota 16-Bit Pixel-Art Murni (Tanpa Gambar HD)
                int mapX = 24, mapY = 96, mapW = Math.max(200, w - 48), mapH = Math.max(180, h - 200);
                PixelArtRenderer.pxBorderBox(g, mapX, mapY, mapW, mapH, new Color(32, 64, 54), UiTheme.GOLD);
                PixelArtRenderer.drawMiniMapPreview(g, mapX + 12, mapY + 12, mapW - 24, mapH - 24, 1.2);
                PixelArtRenderer.drawPixelSignboard(g, "KOTA PIXEL 16-BIT", mapX + mapW / 2, mapY + 18, new Color(24, 36, 52), UiTheme.GOLD);

                PixelArtRenderer.pxBorderBox(g, 24, 22, Math.max(200, w - 48), 60, new Color(24, 36, 52), UiTheme.GOLD);
                g.setColor(UiTheme.GOLD);
                g.setFont(new Font("Monospaced", Font.BOLD, 22));
                g.drawString("A DAY IN MY CITY", 40, 48);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Monospaced", Font.BOLD, 11));
                g.drawString("16-BIT PIXEL URBAN SIMULATION • PBO EDITION", 40, 68);

                PixelArtRenderer.drawPixelSignboard(g, "[SPBU Digital] • [Taman Sanity] • [Quest Kota]", w / 2, h - 72, new Color(24, 36, 52), UiTheme.GOLD);
                g.dispose();
            }
        };
        add(cover);

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setBackground(UiTheme.CREAM);
        actions.setBorder(BorderFactory.createEmptyBorder(32, 32, 28, 32));
        JLabel title = UiTheme.label("SELAMAT DATANG DI KOTA PIXEL", 20);
        title.setForeground(UiTheme.GOLD);
        actions.add(title);
        actions.add(Box.createVerticalStrut(10));
        actions.add(UiTheme.paragraph("Kelilingi kota dengan Motor atau Mobil, ambil Quest untuk mencari uang, isi bensin di Dispenser Digital SPBU, dan jaga Sanity di Taman Kota bersama 5 satwa sahabat!"));
        actions.add(Box.createVerticalStrut(10));

        JLabel nameLabel = UiTheme.label("Nama Pemain (1–30 karakter):", 13);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(nameLabel);
        actions.add(Box.createVerticalStrut(4));
        playerNameField.setFont(new Font("Monospaced", Font.BOLD, 14));
        playerNameField.setBackground(new Color(14, 18, 26));
        playerNameField.setForeground(UiTheme.GOLD);
        playerNameField.setCaretColor(Color.WHITE);
        playerNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.GOLD, 2),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        playerNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        playerNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(playerNameField);
        actions.add(Box.createVerticalStrut(12));

        resume = UiTheme.button(">> Lanjutkan Permainan Aktif", continueGame);
        for (JButton b : new JButton[] {
            UiTheme.button("[+] Mulai Permainan Baru", start),
            UiTheme.button("[L] Muat Simpanan (Load Save)", load),
            resume,
            UiTheme.button("[?] Petunjuk & Aturan Main", help),
            UiTheme.button("[*] Jalankan Auto Demo", demo),
            UiTheme.button("[X] Keluar Aplikasi", exit)
        }) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            actions.add(b);
            actions.add(Box.createVerticalStrut(8));
        }
        actions.add(Box.createVerticalGlue());
        saveInfo.setForeground(new Color(168, 224, 196));
        actions.add(saveInfo);
        add(actions);
    }
    public String getEnteredPlayerName() {
        String raw = playerNameField.getText();
        return raw == null || raw.trim().isEmpty() ? "Warga Pixel" : raw.trim();
    }
    public void setStatusMessage(String msg) {
        saveInfo.setText(msg);
    }
    public void refresh(boolean hasSave, boolean hasGame) {
        resume.setEnabled(hasGame);
        saveInfo.setText(hasSave ? "[OK] File Save tersedia — pilih Muat Simpanan." : "[INFO] Ketik nama lalu klik Mulai Permainan Baru.");
    }
}
