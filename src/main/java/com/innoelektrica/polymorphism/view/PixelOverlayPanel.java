package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import com.innoelektrica.polymorphism.model.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
/** Panel In-Game Pixel Overlay (100% 16-Bit Pixel-Art & Auto-Fit Grid) untuk seluruh transaksi & dialog RPG. */
public final class PixelOverlayPanel {
    public enum Mode {
        NONE, HOME, SUMMARY, ANIMAL, SHOP, FUEL, QUEST_BOARD, NPC, INVENTORY, EMERGENCY, HELP
    }
    private record ClickButton(Rectangle2D.Double bounds, String label, Runnable action, Color bg, Color border) {
    }
    private Mode mode = Mode.NONE;
    private AnimalType activeAnimal = AnimalType.KUCING;
    private VehicleType selectedVehicle = VehicleType.MOTOR;
    private String statusText = "";
    private final List<ClickButton> buttons = new ArrayList<>();
    private final Runnable onSave;
    private final Runnable onStateChanged;
    public PixelOverlayPanel(Runnable onSave, Runnable onStateChanged) {
        this.onSave = onSave;
        this.onStateChanged = onStateChanged;
    }
    public Mode getMode() {
        return mode;
    }
    public boolean isOpen() {
        return mode != Mode.NONE;
    }
    public void open(Mode m, GameManager game) {
        this.mode = m;
        if (game != null) {
            if (game.getState().getVehicleType() != null) {
                selectedVehicle = game.getState().getVehicleType();
            }
            statusText = switch (m) {
                case HOME -> "Pilih kendaraan untuk hari ini, berangkat ke kota, atau akhiri hari.";
                case SUMMARY -> game.getState().isMissionComplete() ? "Selamat! Misi harian berhasil diselesaikan." : "Hari berakhir. Misi harian belum lengkap.";
                case ANIMAL -> game.getAnimal(activeAnimal).dialog(game.getState().getFriendship(activeAnimal), game.getState().getPeriod());
                case SHOP -> {
                    Quest q = game.getActiveQuest();
                    if (q instanceof DeliveryQuest && game.getState().getQuestStep() == 2) {
                        yield game.completeOrAdvanceQuest().message();
                    }
                    yield "Pilih pakan hewan atau kopi/camilan untuk memulihkan Sanity!";
                }
                case FUEL -> game.getQueueCount() > 0 ? "Antrean SPBU masih " + game.getQueueCount() + " kendaraan. Tutup panel agar antrean maju." : "Pilih takaran liter/rupiah pada Dispenser Digital Pompa 01.";
                case QUEST_BOARD -> game.getActiveQuestStatusText();
                case NPC -> {
                    Quest q = game.getActiveQuest();
                    if (q instanceof ServiceQuest || (q instanceof DeliveryQuest && game.getState().getQuestStep() == 1)) {
                        yield game.completeOrAdvanceQuest().message();
                    }
                    yield "Pak Raka: 'Halo! Mau bantu merapikan kios atau mengurus paket quest kota?'";
                }
                case INVENTORY -> "Isi tas pakan, status Sanity, dan tingkat persahabatan 5 satwa taman.";
                case EMERGENCY -> "Layanan darurat mengisi +20% tangki ketika BBM di bawah 20%.";
                case HELP -> "Kontrol & aturan A Day in My City — 16-Bit Pixel Edition.";
                default -> "";
            };
            game.setUiBlocked(m != Mode.NONE);
        }
        onStateChanged.run();
    }
    public void openAnimal(AnimalType a, GameManager game) {
        this.activeAnimal = a != null ? a : AnimalType.KUCING;
        if (game != null && game.getState().getPeriod() != TimePeriod.MALAM) {
            game.getAnimal(this.activeAnimal).bersuara(); // Suara hewan otomatis sebelum diberi makan
        }
        open(Mode.ANIMAL, game);
    }
    public void close(GameManager game) {
        this.mode = Mode.NONE;
        this.buttons.clear();
        if (game != null) game.setUiBlocked(false);
        onStateChanged.run();
    }
    public boolean handleClick(double x, double y, GameManager game) {
        if (!isOpen() || game == null) return false;
        for (ClickButton b : List.copyOf(buttons)) {
            if (b.bounds.contains(x, y)) {
                b.action.run();
                onStateChanged.run();
                return true;
            }
        }
        if (x < 84 || x > 916 || y < 48 || y > 552) {
            close(game);
        }
        return true;
    }
    public void handleKeyChoice(int number, GameManager game) {
        if (!isOpen() || game == null) return;
        int idx = number - 1;
        if (idx >= 0 && idx < buttons.size()) {
            buttons.get(idx).action.run();
            onStateChanged.run();
        }
    }
    private void addBtn(int x, int y, int w, int h, String text, Color bg, Runnable action) {
        addBtn(x, y, w, h, text, bg, Color.WHITE, action);
    }
    private void addBtn(int x, int y, int w, int h, String text, Color bg, Color border, Runnable action) {
        int rx = (x / 4) * 4;
        int ry = (y / 4) * 4;
        int rw = ((w + 3) / 4) * 4;
        int rh = ((h + 3) / 4) * 4;
        int index = buttons.size() + 1;
        String label = index <= 9 ? "[" + index + "] " + text : text;
        buttons.add(new ClickButton(new Rectangle2D.Double(rx, ry, rw, rh), label, action, bg, border));
    }
    private static String fitText(Graphics2D g, String text, int maxWidth) {
        if (text == null) return "";
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int ellW = fm.stringWidth(ellipsis);
        int len = text.length();
        while (len > 0 && fm.stringWidth(text.substring(0, len)) + ellW > maxWidth) {
            len--;
        }
        return text.substring(0, Math.max(0, len)) + ellipsis;
    }
    public void paint(Graphics2D g, GameManager game) {
        if (!isOpen() || game == null) return;
        buttons.clear();
        GameState s = game.getState();
        // Latar gelap transparan di belakang panel pixel
        g.setColor(new Color(8, 12, 18, 175));
        g.fillRect(0, 0, 1000, 600);
        int bx = 88, by = 52, bw = 824, bh = 496;
        PixelArtRenderer.pxBorderBox(g, bx, by, bw, bh, new Color(22, 28, 40, 250), PixelArtRenderer.HUD_BORDER);
        // Header bar pixel
        PixelArtRenderer.pxRect(g, bx + 4, by + 4, bw - 8, 40, new Color(36, 84, 72));
        g.setColor(PixelArtRenderer.HUD_BORDER);
        g.setFont(new Font("Monospaced", Font.BOLD, 17));
        String title = switch (mode) {
            case HOME -> "[RUMAH] GARASI & RENCANA PERJALANAN";
            case SUMMARY -> s.isMissionComplete() ? "[SELESAI] RINGKASAN HARI — MISI BERHASIL!" : "[RINGKASAN] HARI BERAKHIR — MISI BELUM LENGKAP";
            case ANIMAL -> "[TAMAN] INTERAKSI SATWA: " + activeAnimal.name();
            case SHOP -> "[KIOS TAMAN] PAKAN SATWA & KAFE SANITY";
            case FUEL -> "[SPBU DIGITAL] DISPENSER POMPA 01 (Rp10.000 / L)";
            case QUEST_BOARD -> "[QUEST KOTA] PAPAN MISI UANG & PBO";
            case NPC -> "[PUSAT KOTA] KIOS PAK RAKA & LOGISTIK";
            case INVENTORY -> "[INVENTORI] TAS PAKAN, SANITY & PERSAHABATAN";
            case EMERGENCY -> "[DARURAT] LAYANAN BANTUAN BBM KOTA";
            case HELP -> "[PANDUAN] CARA BERMAIN PIXEL EDITION";
            default -> "";
        };
        g.drawString(title, bx + 16, by + 30);
        g.setColor(new Color(218, 238, 228));
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.drawString("[Esc] Tutup", bx + bw - 96, by + 29);

        switch (mode) {
            case HOME -> buildHome(g, game, bx, by, bw, bh);
            case SUMMARY -> buildSummary(g, game, bx, by, bw, bh);
            case ANIMAL -> buildAnimal(g, game, bx, by, bw, bh);
            case SHOP -> buildShop(g, game, bx, by, bw, bh);
            case FUEL -> buildFuel(g, game, bx, by, bw, bh);
            case QUEST_BOARD -> buildQuestBoard(g, game, bx, by, bw, bh);
            case NPC -> buildNpc(g, game, bx, by, bw, bh);
            case INVENTORY -> buildInventory(g, game, bx, by, bw, bh);
            case EMERGENCY -> buildEmergency(g, game, bx, by, bw, bh);
            case HELP -> buildHelp(g, game, bx, by, bw, bh);
            default -> {}
        }

        // Status notice bar presisi di bagian bawah modal
        int noticeY = by + bh - 52;
        PixelArtRenderer.pxBorderBox(g, bx + 20, noticeY, bw - 40, 36, new Color(14, 18, 26), new Color(92, 134, 122));
        g.setColor(new Color(255, 232, 126));
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        String msg = statusText == null || statusText.isEmpty() ? game.getNotice() : statusText;
        g.drawString(fitText(g, "> " + msg, bw - 68), bx + 32, noticeY + 23);

        // Gambar semua tombol pilihan dengan auto-fit font & perataan tengah presisi
        Font btnFont12 = new Font("Monospaced", Font.BOLD, 12);
        Font btnFont11 = new Font("Monospaced", Font.BOLD, 11);
        for (ClickButton b : buttons) {
            int x = (int) b.bounds.x, y = (int) b.bounds.y, w = (int) b.bounds.width, h = (int) b.bounds.height;
            PixelArtRenderer.pxBorderBox(g, x, y, w, h, b.bg, b.border);
            // Garis highlight atas 16-bit button
            g.setColor(new Color(255, 255, 255, 45));
            g.fillRect(x + 4, y + 4, w - 8, 2);

            g.setFont(btnFont12);
            FontMetrics fm = g.getFontMetrics();
            if (fm.stringWidth(b.label) > w - 16) {
                g.setFont(btnFont11);
                fm = g.getFontMetrics();
            }
            String drawnLabel = fitText(g, b.label, w - 12);
            int tx = x + (w - fm.stringWidth(drawnLabel)) / 2;
            int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
            g.setColor(Color.WHITE);
            g.drawString(drawnLabel, tx, ty);
        }
    }
    private void buildHome(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        GameState s = game.getState();
        double anim = game.getAnimationSeconds();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.drawString("Hari ke-" + s.getDay() + " • Jam " + s.getClock() + " • Saldo: " + GameManager.rupiah(s.getMoney()) + " • Sanity: " + s.getSanity() + "% (" + s.getSanityMood() + ")", bx + 24, by + 68);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("Target Harian: " + s.getMissionText(), bx + 24, by + 88);

        // Kartu Motor Sport (Kiri: lebar 376) vs Mobil Sedan (Kanan: lebar 376)
        boolean isMotor = selectedVehicle == VehicleType.MOTOR;
        boolean isMobil = selectedVehicle == VehicleType.MOBIL;
        int cardW = 376, cardH = 172, cardY = by + 104;
        int leftX = bx + 24, rightX = bx + 424;

        PixelArtRenderer.pxBorderBox(g, leftX, cardY, cardW, cardH,
            isMotor ? new Color(32, 74, 60) : new Color(28, 34, 48),
            isMotor ? PixelArtRenderer.SANITY_HIGH : PixelArtRenderer.HUD_BORDER);
        // Kotak Preview Sprite Pixel Motor + Rider
        PixelArtRenderer.pxBorderBox(g, leftX + 12, cardY + 16, 132, 112, new Color(44, 52, 64), new Color(96, 112, 128));
        PixelArtRenderer.drawPixelMotorcycle(g, leftX + 78, cardY + 84, 1.45, PixelArtRenderer.Direction.RIGHT, true, false, anim);
        if (isMotor) {
            PixelArtRenderer.drawPixelSignboard(g, "DIPILIH", leftX + 78, cardY + 132, new Color(28, 96, 68), PixelArtRenderer.SANITY_HIGH);
        }
        g.setColor(PixelArtRenderer.HUD_BORDER);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("MOTOR SPORT (10.0 L)", leftX + 156, cardY + 32);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("• Kecepatan : 7 px/tick (Cepat)", leftX + 156, cardY + 58);
        g.drawString("• Konsumsi  : Hemat (6 ml/tick)", leftX + 156, cardY + 80);
        g.drawString("• Isi Tangki: " + String.format(Locale.ROOT, "%.2f / 10 L", s.getFuelMl(VehicleType.MOTOR) / 1000.0), leftX + 156, cardY + 102);
        g.drawString("• Upah Kurir: 1.0x Normal", leftX + 156, cardY + 124);
        g.setColor(new Color(172, 232, 198));
        g.drawString("• Pengendara: Helm & Jaket Pixel", leftX + 156, cardY + 146);

        PixelArtRenderer.pxBorderBox(g, rightX, cardY, cardW, cardH,
            isMobil ? new Color(32, 74, 60) : new Color(28, 34, 48),
            isMobil ? PixelArtRenderer.SANITY_HIGH : PixelArtRenderer.HUD_BORDER);
        // Kotak Preview Sprite Pixel Mobil + Pengemudi
        PixelArtRenderer.pxBorderBox(g, rightX + 12, cardY + 16, 132, 112, new Color(44, 52, 64), new Color(96, 112, 128));
        PixelArtRenderer.drawPixelCar(g, rightX + 78, cardY + 80, 1.35, PixelArtRenderer.Direction.RIGHT, true, false, new Color(48, 118, 204), anim);
        if (isMobil) {
            PixelArtRenderer.drawPixelSignboard(g, "DIPILIH", rightX + 78, cardY + 132, new Color(28, 96, 68), PixelArtRenderer.SANITY_HIGH);
        }
        g.setColor(PixelArtRenderer.HUD_BORDER);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("MOBIL SEDAN (40.0 L)", rightX + 156, cardY + 32);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("• Kecepatan : 5 px/tick (Stabil)", rightX + 156, cardY + 58);
        g.drawString("• Kapasitas : Kargo Besar 2x", rightX + 156, cardY + 80);
        g.drawString("• Isi Tangki: " + String.format(Locale.ROOT, "%.2f / 40 L", s.getFuelMl(VehicleType.MOBIL) / 1000.0), rightX + 156, cardY + 102);
        g.drawString("• Upah Kurir: 1.5x Bonus Kargo!", rightX + 156, cardY + 124);
        g.setColor(new Color(172, 232, 198));
        g.drawString("• Kabin     : Nyaman & Tangki Besar", rightX + 156, cardY + 146);

        int yBtn = by + 292;
        addBtn(bx + 24, yBtn, 244, 40, isMotor ? "Pilih MOTOR (Aktif)" : "Pilih MOTOR",
            isMotor ? new Color(46, 136, 96) : new Color(42, 98, 84),
            isMotor ? PixelArtRenderer.SANITY_HIGH : Color.WHITE, () -> {
                selectedVehicle = VehicleType.MOTOR;
                statusText = game.chooseVehicle(VehicleType.MOTOR).message();
            });
        addBtn(bx + 284, yBtn, 244, 40, isMobil ? "Pilih MOBIL (Aktif)" : "Pilih MOBIL",
            isMobil ? new Color(46, 136, 96) : new Color(42, 98, 84),
            isMobil ? PixelArtRenderer.SANITY_HIGH : Color.WHITE, () -> {
                selectedVehicle = VehicleType.MOBIL;
                statusText = game.chooseVehicle(VehicleType.MOBIL).message();
            });
        addBtn(bx + 544, yBtn, 256, 40, "BERANGKAT KE KOTA!", new Color(214, 134, 36), PixelArtRenderer.HUD_BORDER, () -> {
            if (!s.isDeparted() && s.getVehicleType() == null) game.chooseVehicle(selectedVehicle);
            ActionResult r = game.departHome();
            statusText = r.message();
            if (r.success()) close(game);
        });
        addBtn(bx + 24, yBtn + 52, 376, 40, "Akhiri Hari & Ringkasan", new Color(168, 64, 64), () -> {
            ActionResult r = game.endDay();
            statusText = r.message();
            if (r.success()) open(Mode.SUMMARY, game);
        });
        addBtn(bx + 424, yBtn + 52, 376, 40, "Tutup Panel [Esc]", new Color(68, 76, 92), () -> close(game));
    }
    private void buildSummary(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        GameState s = game.getState();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("Ringkasan Hari ke-" + s.getDay() + " (" + s.getClock() + ") — Pemain: " + s.getPlayerName(), bx + 28, by + 72);
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g.drawString("• Status Misi Utama   : " + (s.isMissionComplete() ? "BERHASIL (Taman + 2 Satwa + Pulang)" : "BELUM LENGKAP"), bx + 28, by + 100);
        g.drawString("• Satwa Diberi Pakan  : " + s.getFedAnimals() + " (" + s.getFoodGiven() + " pakan diberikan)", bx + 28, by + 124);
        g.drawString("• Quest Kota Selesai  : " + s.getQuestsCompletedToday() + " misi hari ini (Total Upah Quest: " + GameManager.rupiah(s.getQuestEarningsToday()) + ")", bx + 28, by + 148);
        g.drawString("• Saldo Akhir & Sanity: " + GameManager.rupiah(s.getMoney()) + "  |  Sanity: " + s.getSanity() + "% (" + s.getSanityMood() + ")", bx + 28, by + 172);
        g.drawString("• Eksplorasi & Darurat: Plaza Kota: " + (s.isCityExplored() ? "Ya" : "Belum") + "  |  Bantuan BBM: " + s.getEmergencyCount() + "x", bx + 28, by + 196);
        PixelArtRenderer.pxBorderBox(g, bx + 28, by + 212, bw - 56, 116, new Color(16, 22, 30), new Color(76, 98, 112));
        g.setColor(new Color(188, 220, 206));
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        List<String> logs = s.getActivityLog();
        int start = Math.max(0, logs.size() - 5);
        for (int i = start; i < logs.size(); i++) {
            g.drawString(fitText(g, logs.get(i), bw - 84), bx + 40, by + 234 + (i - start) * 19);
        }
        int yBtn = by + 344;
        addBtn(bx + 28, yBtn, 248, 40, "Simpan Game (Save)", new Color(42, 118, 92), onSave);
        addBtn(bx + 292, yBtn, 256, 40, "Mulai Hari Berikutnya", new Color(214, 134, 36), () -> {
            ActionResult r = game.nextDay();
            statusText = r.message();
            if (r.success()) open(Mode.HOME, game);
        });
        addBtn(bx + 564, yBtn, 232, 40, "Tutup [Esc]", new Color(68, 76, 92), () -> close(game));
    }
    private void buildAnimal(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        GameState s = game.getState();
        Hewan h = game.getAnimal(activeAnimal);
        // Bingkai Potret Satwa 16-Bit Pixel-Art Murni (Tanpa Gambar HD)
        PixelArtRenderer.pxBorderBox(g, bx + 24, by + 64, 152, 140, new Color(36, 68, 52), PixelArtRenderer.HUD_BORDER);
        PixelArtRenderer.drawPixelAnimalSprite(g, activeAnimal, bx + 100, by + 158, 92, s.getPeriod(), game.getAnimationSeconds());

        int infoX = bx + 196, maxInfoW = bw - 220;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString(h.getNama().toUpperCase(Locale.ROOT) + " (" + h.getNamaIlmiah() + ")", infoX, by + 84);
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g.setColor(new Color(180, 236, 200));
        g.drawString("Persahabatan: " + s.getFriendship(activeAnimal) + " / 100   |   Pakan Favorit: " + h.getMakananFavorit(), infoX, by + 110);
        g.setColor(PixelArtRenderer.SANITY_HIGH);
        g.drawString("Efek Terapi Taman: Makan Sesuai +" + h.getSanityBonus() + "% Sanity  |  Elus +" + h.getPetSanityBonus() + "% Sanity", infoX, by + 134);
        g.setColor(Color.WHITE);
        g.drawString(fitText(g, "Habitat: " + h.getHabitat(), maxInfoW), infoX, by + 158);
        g.drawString(fitText(g, "Dialog : \"" + h.dialog(s.getFriendship(activeAnimal), s.getPeriod()) + "\"", maxInfoW), infoX, by + 184);

        // Grid 3 Kolom Simetris untuk Pilihan Makanan & Interaksi
        int yBtn = by + 228;
        int colW = 248, gap = 16;
        FoodType[] foods = FoodType.values();
        for (int i = 0; i < foods.length; i++) {
            FoodType f = foods[i];
            int stock = s.getInventory().get(f);
            boolean fav = f == h.getMakananFavorit();
            addBtn(bx + 24 + i * (colW + gap), yBtn, colW, 44,
                "Beri " + f + " (" + stock + ")" + (fav ? " *" : ""),
                fav ? new Color(42, 128, 86) : new Color(58, 76, 96),
                fav ? PixelArtRenderer.SANITY_HIGH : Color.WHITE, () -> {
                    ActionResult r = game.feedAnimal(activeAnimal, f);
                    statusText = r.message();
                });
        }
        addBtn(bx + 24, yBtn + 60, colW, 44, "Elus (+" + h.getPetSanityBonus() + "% Sanity)", new Color(188, 112, 42), () -> {
            ActionResult r = game.petAnimal(activeAnimal);
            statusText = r.message();
        });
        addBtn(bx + 24 + colW + gap, yBtn + 60, colW, 44, "Suara (" + h.getSuara() + ")", new Color(72, 98, 148), () -> {
            h.bersuara();
            statusText = h.getNama() + " bersuara: \"" + h.getSuara() + "!\"";
        });
        addBtn(bx + 24 + (colW + gap) * 2, yBtn + 60, colW, 44, "Selesai [Esc]", new Color(68, 76, 92), () -> close(game));
    }
    private void buildShop(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        GameState s = game.getState();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.drawString("Saldo: " + GameManager.rupiah(s.getMoney()) + "  |  Sanity: " + s.getSanity() + "% (" + s.getSanityMood() + ")  |  Jam Buka: 07.00–18.00", bx + 24, by + 70);

        int colW = 376, colH = 304, leftX = bx + 24, rightX = bx + 424;
        // Kolom Kiri: Pakan Satwa
        PixelArtRenderer.pxBorderBox(g, leftX, by + 84, colW, colH, new Color(28, 42, 38), new Color(88, 148, 118));
        g.setColor(PixelArtRenderer.HUD_BORDER);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.drawString("[PAKAN SATWA TAMAN — STOK TAS]", leftX + 16, by + 110);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("Daging: " + s.getInventory().get(FoodType.DAGING) + " | Biji: " + s.getInventory().get(FoodType.BIJI_BIJIAN) + " | Roti: " + s.getInventory().get(FoodType.ROTI), leftX + 16, by + 132);

        int btnW = colW - 32;
        addBtn(leftX + 16, by + 148, btnW, 40, "1 Daging (Rp10.000 • Kucing/Anjing)", new Color(42, 114, 86), () -> {
            statusText = game.buyFood(FoodType.DAGING, 1).message();
        });
        addBtn(leftX + 16, by + 198, btnW, 40, "1 Biji-bijian (Rp5.000 • Burung/Ayam)", new Color(42, 114, 86), () -> {
            statusText = game.buyFood(FoodType.BIJI_BIJIAN, 1).message();
        });
        addBtn(leftX + 16, by + 248, btnW, 40, "1 Roti (Rp7.000 • Angsa)", new Color(42, 114, 86), () -> {
            statusText = game.buyFood(FoodType.ROTI, 1).message();
        });
        addBtn(leftX + 16, by + 298, btnW, 40, "Paket 3 Daging (Rp30.000)", new Color(56, 94, 126), () -> {
            statusText = game.buyFood(FoodType.DAGING, 3).message();
        });

        // Kolom Kanan: Kafe Kopi & Camilan Sanity
        PixelArtRenderer.pxBorderBox(g, rightX, by + 84, colW, colH, new Color(38, 34, 48), PixelArtRenderer.SANITY_HIGH);
        g.setColor(PixelArtRenderer.SANITY_HIGH);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.drawString("[KAFE TAMAN — PEMULIH SANITY]", rightX + 16, by + 110);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("Nikmati sajian taman untuk atasi lelah:", rightX + 16, by + 132);

        SnackType[] snacks = SnackType.values();
        for (int i = 0; i < snacks.length; i++) {
            SnackType sn = snacks[i];
            addBtn(rightX + 16, by + 148 + i * 50, btnW, 40,
                sn.getLabel() + " (" + GameManager.rupiah(sn.getPrice()) + " | +" + sn.getSanityRestore() + "% SAN)",
                new Color(148, 88, 42), () -> {
                    statusText = game.buySnack(sn).message();
                });
        }
        addBtn(rightX + 16, by + 298, btnW, 40, "Tutup Kios Taman [Esc]", new Color(68, 76, 92), () -> close(game));
    }
    private void buildFuel(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        GameState s = game.getState();
        double cap = s.getVehicle() == null ? 10.0 : s.getVehicle().getKapasitasTangki();
        // Layar LED Dispenser Digital Pixel (Kiri)
        int leftX = bx + 24, rightX = bx + 424, colW = 376;
        PixelArtRenderer.pxBorderBox(g, leftX, by + 68, colW, 288, new Color(14, 18, 22), new Color(228, 68, 58));
        g.setColor(new Color(238, 78, 64));
        g.setFont(new Font("Monospaced", Font.BOLD, 15));
        g.drawString("DISPENSER POMPA 01 — LED METER", leftX + 20, by + 98);
        g.setColor(new Color(120, 245, 160));
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString("KENDARAAN : " + (s.getVehicleType() == null ? "-" : s.getVehicleType()), leftX + 20, by + 132);
        g.drawString(String.format(Locale.ROOT, "ISI TANGKI: %.3f / %.0f L (%d%%)", s.getFuelLiters(), cap, s.getFuelPercent()), leftX + 20, by + 162);
        g.drawString("SALDO KITA: " + GameManager.rupiah(s.getMoney()), leftX + 20, by + 192);
        g.drawString("ANTREAN   : " + game.getQueueCount() + " Kendaraan", leftX + 20, by + 222);
        String fillState = game.getPendingFuel() != null
            ? String.format(Locale.ROOT, "MENGALIR... %.0f%%", game.getFillProgress() * 100)
            : (game.getQueueCount() > 0 ? "MENUNGGU ANTREAN" : "SIAP ISI (PILIH LITER)");
        g.setColor(PixelArtRenderer.HUD_BORDER);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("STATUS: " + fillState, leftX + 20, by + 258);
        // Bar Tangki Visual Presisi
        int barW = colW - 40;
        PixelArtRenderer.pxBorderBox(g, leftX + 20, by + 276, barW, 28, new Color(32, 38, 46), Color.WHITE);
        int fillW = (int) Math.round((barW - 8) * (s.getFuelPercent() / 100.0));
        if (fillW > 0) {
            PixelArtRenderer.pxRect(g, leftX + 24, by + 280, fillW, 20, PixelArtRenderer.FUEL_COLOR);
        }

        int ry = by + 68;
        if (game.getPendingFuel() != null) {
            addBtn(rightX, ry, colW, 48, "Lanjutkan Pengisian (Tutup Panel)", new Color(42, 118, 92), () -> close(game));
            addBtn(rightX, ry + 64, colW, 48, "Batalkan Pengisian (Uang Utuh)", new Color(178, 58, 58), () -> {
                statusText = game.cancelFuel().message();
            });
        } else if (game.getQueueCount() > 0) {
            addBtn(rightX, ry, colW, 48, "Tunggu Antrean Maju (Tutup Panel)", new Color(42, 118, 92), () -> close(game));
            addBtn(rightX, ry + 64, colW, 48, "Keluar SPBU ke Peta Jalan", new Color(148, 82, 46), () -> {
                if (game.leaveLocation().success()) close(game);
            });
        } else {
            long[] nominals = { 10000L, 30000L, 50000L, 500000L, 5000L };
            String[] labels = {
                "Isi +1.0 Liter (Rp10.000)",
                "Isi +3.0 Liter (Rp30.000)",
                "Isi +5.0 Liter (Rp50.000)",
                "ISI PENUH / FULL TANK",
                "Isi Eceran +0.5 Liter (Rp5.000)"
            };
            for (int i = 0; i < nominals.length; i++) {
                long nom = nominals[i];
                addBtn(rightX, ry + i * 48, colW, 40, labels[i],
                    i == 3 ? new Color(214, 134, 36) : new Color(42, 118, 92),
                    i == 3 ? PixelArtRenderer.HUD_BORDER : Color.WHITE, () -> {
                        try {
                            FuelQuote q = game.previewFuel(nom);
                            ActionResult r = game.beginFuel(q);
                            statusText = r.message();
                            if (r.success()) close(game);
                        } catch (Exception ex) {
                            statusText = ex.getMessage();
                        }
                    });
            }
            addBtn(rightX, ry + 244, colW, 40, "Keluar SPBU ke Peta Jalan", new Color(68, 76, 92), () -> {
                if (game.leaveLocation().success()) close(game);
            });
        }
    }
    private void buildQuestBoard(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        GameState s = game.getState();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.drawString("Sanity: " + s.getSanity() + "% (" + s.getSanityMood() + ")  |  Quest Selesai: " + s.getQuestsCompletedToday() + "  |  Kendaraan: " + (s.getVehicleType() == null ? "Belum dipilih" : s.getVehicleType()), bx + 24, by + 68);

        List<Quest> quests = game.getAvailableQuests();
        int cardX = bx + 24, cardW = bw - 48, btnW = 176, btnX = cardX + cardW - btnW - 16;
        int maxTextW = btnX - (cardX + 16) - 16;

        for (int i = 0; i < quests.size(); i++) {
            Quest q = quests.get(i);
            int cardY = by + 84 + i * 76;
            boolean isActive = q.getId().equals(s.getActiveQuestId());
            PixelArtRenderer.pxBorderBox(g, cardX, cardY, cardW, 68,
                isActive ? new Color(36, 76, 62) : new Color(28, 36, 52),
                isActive ? PixelArtRenderer.SANITY_HIGH : PixelArtRenderer.HUD_BORDER);

            g.setColor(PixelArtRenderer.HUD_BORDER);
            g.setFont(new Font("Monospaced", Font.BOLD, 13));
            int reward = q.hitungUpah(s.getVehicle());
            String sanStr = q.getSanityDelta() >= 0 ? "+" + q.getSanityDelta() + "% SAN" : q.getSanityDelta() + "% SAN";
            String headerLine = "[" + q.getKategori() + "] " + q.getTitle() + " — " + GameManager.rupiah(reward) + " (" + sanStr + ")";
            g.drawString(fitText(g, headerLine, maxTextW), cardX + 16, cardY + 25);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 11));
            String shortTarget = q instanceof ParkCareQuest ? "Taman Kota" : (q instanceof ServiceQuest ? "Pusat Kota" : "Pusat Kota -> Taman");
            String descLine = q.getDescription() + " [Lokasi: " + shortTarget + "]";
            g.drawString(fitText(g, descLine, maxTextW), cardX + 16, cardY + 48);

            int idx = i;
            addBtn(btnX, cardY + 14, btnW, 40,
                isActive ? "SEDANG AKTIF" : "Ambil Misi #" + (i + 1),
                isActive ? new Color(46, 138, 92) : new Color(42, 114, 142),
                isActive ? PixelArtRenderer.SANITY_HIGH : Color.WHITE, () -> {
                    statusText = game.acceptQuest(idx).message();
                });
        }
        int yBtn = by + 320;
        addBtn(bx + 24, yBtn, 336, 40, "Klaim / Lanjutkan Quest di Sini", new Color(214, 134, 36), PixelArtRenderer.HUD_BORDER, () -> {
            statusText = game.completeOrAdvanceQuest().message();
        });
        addBtn(bx + 376, yBtn, 212, 40, "Batalkan Quest", new Color(168, 64, 64), () -> {
            statusText = game.cancelQuest().message();
        });
        addBtn(bx + 604, yBtn, 196, 40, "Tutup [Esc]", new Color(68, 76, 92), () -> close(game));
    }
    private void buildNpc(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        GameState s = game.getState();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 15));
        g.drawString("PAK RAKA — KOORDINATOR KIOS & LOGISTIK PUSAT KOTA", bx + 28, by + 84);
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g.drawString("\"Selamat " + s.getPeriod().getLabel().toLowerCase(Locale.ROOT) + "! Di Pusat Kota kamu bisa bekerja merapikan kios harian", bx + 28, by + 116);
        g.drawString("atau mengambil Misi Kurir & Logistik dari Papan Quest Perkotaan.\"", bx + 28, by + 138);
        g.setColor(PixelArtRenderer.SANITY_HIGH);
        g.drawString("• Kerja Kios Harian: Upah Rp15.000 | Durasi 4 detik | Waktu +25 mnt | Sanity -12%", bx + 28, by + 170);
        g.drawString("• Status Kerja Hari Ini: " + (s.isCityWorkDone() ? "SUDAH SELESAI" : "TERSEDIA") + "  |  Sanity Kamu: " + s.getSanity() + "%", bx + 28, by + 194);
        int yBtn = by + 232;
        addBtn(bx + 24, yBtn, 376, 44, "Bantu Kios Pak Raka (+Rp15.000)", new Color(42, 118, 92), () -> {
            ActionResult r = game.beginCityWork();
            statusText = r.message();
            if (r.success()) close(game);
        });
        addBtn(bx + 424, yBtn, 376, 44, "Ambil / Selesaikan Quest Kota", new Color(214, 134, 36), () -> {
            statusText = game.completeOrAdvanceQuest().message();
        });
        addBtn(bx + 24, yBtn + 60, 376, 44, "Buka Papan Quest Perkotaan [Q]", new Color(64, 102, 148), () -> open(Mode.QUEST_BOARD, game));
        addBtn(bx + 424, yBtn + 60, 376, 44, "Tinggalkan Kios [Esc]", new Color(68, 76, 92), () -> close(game));
    }
    private void buildInventory(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        GameState s = game.getState();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("Saldo: " + GameManager.rupiah(s.getMoney()) + "  |  Sanity: " + s.getSanity() + "% (" + s.getSanityMood() + ")  |  Quest Selesai: " + s.getQuestsCompletedToday(), bx + 24, by + 74);
        // Isi Tas (Kiri)
        int leftX = bx + 24, rightX = bx + 424, colW = 376;
        PixelArtRenderer.pxBorderBox(g, leftX, by + 92, colW, 224, new Color(28, 38, 48), PixelArtRenderer.HUD_BORDER);
        g.setColor(PixelArtRenderer.HUD_BORDER);
        g.drawString("[ISI TAS PAKAN SATWA]", leftX + 16, by + 118);
        g.setColor(Color.WHITE);
        int fy = by + 148;
        for (FoodType f : FoodType.values()) {
            g.drawString("• " + f + " : " + s.getInventory().get(f) + " unit (" + GameManager.rupiah(f.getPrice()) + ")", leftX + 16, fy);
            fy += 32;
        }
        g.setColor(PixelArtRenderer.SANITY_HIGH);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.drawString("Tips: Kunjungi Taman Kota untuk pulihkan", leftX + 16, fy + 10);
        g.drawString("Sanity lewat Hewan, Bangku, atau Kopi!", leftX + 16, fy + 28);

        // Persahabatan 5 Hewan (Kanan)
        PixelArtRenderer.pxBorderBox(g, rightX, by + 92, colW, 224, new Color(28, 38, 48), PixelArtRenderer.SANITY_HIGH);
        g.setColor(PixelArtRenderer.SANITY_HIGH);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("[PERSAHABATAN & BONUS SANITY]", rightX + 16, by + 118);
        int ay = by + 144;
        for (AnimalType a : AnimalType.values()) {
            int fr = s.getFriendship(a);
            Hewan h = game.getAnimal(a);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.drawString(String.format(Locale.ROOT, "%-7s: %3d/100 (+%d%% SAN)", a.name(), fr, h.getSanityBonus()), rightX + 16, ay);
            PixelArtRenderer.pxRect(g, rightX + 228, ay - 11, 128, 12, new Color(45, 55, 65));
            if (fr > 0) {
                PixelArtRenderer.pxRect(g, rightX + 228, ay - 11, Math.max(4, (int) Math.round(1.28 * fr)), 12, PixelArtRenderer.SANITY_HIGH);
            }
            ay += 32;
        }
        addBtn(leftX, by + 336, colW, 42, "Buka Papan Quest Kota [Q]", new Color(64, 102, 148), () -> open(Mode.QUEST_BOARD, game));
        addBtn(rightX, by + 336, colW, 42, "Tutup Tas [Esc]", new Color(68, 76, 92), () -> close(game));
    }
    private void buildEmergency(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.drawString("Unit Derek & Bantuan BBM Darurat Kota siap membantu ketika BBM < 20%.", bx + 28, by + 92);
        g.drawString("• Menambah +20% kapasitas tangki kendaraan aktif.", bx + 28, by + 124);
        g.drawString("• Jika saldo >= Rp5.000 : Biaya Rp5.000 dan waktu +60 menit.", bx + 28, by + 152);
        g.drawString("• Jika saldo <  Rp5.000 : Subsidi GRATIS dari kota dan waktu +90 menit.", bx + 28, by + 180);
        addBtn(bx + 24, by + 236, 376, 48, "Panggil Bantuan BBM Sekarang", new Color(214, 134, 36), () -> {
            ActionResult r = game.emergencyFuel();
            statusText = r.message();
            if (r.success()) close(game);
        });
        addBtn(bx + 424, by + 236, 376, 48, "Kembali [Esc]", new Color(68, 76, 92), () -> close(game));
    }
    private void buildHelp(Graphics2D g, GameManager game, int bx, int by, int bw, int bh) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        String[] lines = {
            "1. TUJUAN HARIAN: Kunjungi Taman Kota, beri makan sesuai ke >= 2 hewan, lalu pulang ke Rumah.",
            "2. SHORTCUT LENGKAP: [WASD/Panah] Gerak | [E] Aksi | [F] Naik/Turun | [H] Klakson | [R] Pulang ke Rumah",
            "   [Q] Quest | [I] Tas | [X] Ke Peta | [B] BBM Darurat | [K] Simpan | [L] Muat | [P] Jeda | [F1/?] Bantuan",
            "3. TUKAR KENDARAAN & JALUR KIRI: Pulang ke Rumah [R] kapan saja untuk bertukar Motor <-> Mobil di hari sama.",
            "4. TAMAN & PERILAKU HEWAN: Hewan berjalan santai di habitatnya dan bergerombol mendekat jika kamu bawa",
            "   makanan! Masuki zona hijau untuk suara singkat berjeda (maks 2.2 dtk), beri makan agar berkumpul bersama.",
            "5. SISTEM QUEST PBO (Tekan [Q]): Kurir Logistik (Mobil bonus 1.5x), Kerja Kota (Pak Raka), Penjaga Taman.",
            "6. SPBU DRIVE-THRU: Berhenti sejajar Pompa 01 untuk isi BBM (+1L, +3L, +5L, Penuh)."
        };
        for (int i = 0; i < lines.length; i++) {
            g.drawString(fitText(g, lines[i], bw - 56), bx + 28, by + 76 + i * 24);
        }
        addBtn(bx + 264, by + 336, 296, 42, "Mengerti & Lanjut Main [Esc]", new Color(42, 118, 92), () -> close(game));
    }
}
