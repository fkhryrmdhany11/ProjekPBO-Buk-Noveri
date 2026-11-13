package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import com.innoelektrica.polymorphism.model.*;
import java.awt.*;
/** Mesin perender 16-bit Pixel-Art murni (prosedural & 4-arah) untuk A Day in My City. */
public final class PixelArtRenderer {
    public static final int P = 4; // Ukuran 1 blok piksel art pada kanvas 1000x600
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }
    public static final Color
        GRASS_BASE = new Color(76, 148, 72),
        GRASS_DARK = new Color(58, 122, 56),
        GRASS_LIGHT = new Color(102, 174, 88),
        ASPHALT = new Color(54, 58, 68),
        ASPHALT_DARK = new Color(42, 46, 54),
        SIDEWALK = new Color(184, 180, 168),
        SIDEWALK_EDGE = new Color(132, 128, 118),
        LANE_YELLOW = new Color(244, 208, 76),
        WATER_DEEP = new Color(44, 118, 184),
        WATER_MID = new Color(68, 152, 214),
        WATER_FOAM = new Color(186, 232, 252),
        WOOD_MAIN = new Color(142, 92, 52),
        WOOD_DARK = new Color(96, 58, 30),
        HUD_BG = new Color(22, 26, 36, 236),
        HUD_BORDER = new Color(236, 198, 78),
        SANITY_HIGH = new Color(68, 214, 138),
        SANITY_MED = new Color(242, 196, 62),
        SANITY_LOW = new Color(232, 72, 72),
        FUEL_COLOR = new Color(245, 142, 48);
    private PixelArtRenderer() {
    }
    public static Direction directionOf(double angle) {
        double sin = Math.sin(angle), cos = Math.cos(angle);
        if (sin < -0.5) return Direction.UP;
        if (sin > 0.5) return Direction.DOWN;
        if (cos < 0) return Direction.LEFT;
        return Direction.RIGHT;
    }
    public static void enableCrispPixels(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    public static void pxRect(Graphics2D g, int x, int y, int w, int h, Color c) {
        g.setColor(c);
        g.fillRect((x / P) * P, (y / P) * P, Math.max(P, (w / P) * P), Math.max(P, (h / P) * P));
    }
    public static void pxBorderBox(Graphics2D g, int x, int y, int w, int h, Color bg, Color border) {
        int rx = (x / P) * P;
        int ry = (y / P) * P;
        int rw = Math.max(P * 2, ((w + P - 1) / P) * P);
        int rh = Math.max(P * 2, ((h + P - 1) / P) * P);
        g.setColor(new Color(10, 12, 18, 140));
        g.fillRect(rx + P, ry + P, rw, rh);
        g.setColor(bg);
        g.fillRect(rx, ry, rw, rh);
        g.setColor(border);
        g.fillRect(rx, ry, rw, P);
        g.fillRect(rx, ry + rh - P, rw, P);
        g.fillRect(rx, ry, P, rh);
        g.fillRect(rx + rw - P, ry, P, rh);
    }
    /** Menggambar plang nama gedung/lokasi dengan lebar kotak otomatis mengikuti panjang teks. */
    public static void drawPixelSignboard(Graphics2D g, String label, int cx, int y, Color bg, Color border) {
        if (label == null || label.isEmpty()) return;
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(label);
        int bw = ((textW + 24 + P - 1) / P) * P;
        int bh = 24;
        int rx = ((cx - bw / 2) / P) * P;
        int ry = (y / P) * P;
        pxBorderBox(g, rx, ry, bw, bh, bg, border);
        g.setColor(Color.WHITE);
        int tx = rx + (bw - textW) / 2;
        int ty = ry + (bh + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(label, tx, ty);
    }
    public static void drawPixelGrassCanvas(Graphics2D g, int w, int h, double anim) {
        g.setColor(GRASS_BASE);
        g.fillRect(0, 0, w, h);
        int step = 32;
        int phase = ((int) (anim * 2)) % 2;
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                int hash = ((x * 31) ^ (y * 17)) & 7;
                g.setColor(hash < 3 ? GRASS_DARK : GRASS_LIGHT);
                g.fillRect(x + 4, y + 8, P, P * 2);
                g.fillRect(x + 8, y + 12, P, P);
                if (hash == 0) {
                    g.setColor(new Color(246, 224, 92));
                    g.fillRect(x + 16 + phase * 2, y + 16, P, P);
                } else if (hash == 4) {
                    g.setColor(new Color(238, 118, 142));
                    g.fillRect(x + 20, y + 6, P, P);
                }
            }
        }
    }
    public static void drawPixelSidewalk(Graphics2D g, int x, int y, int w, int h) {
        pxRect(g, x - 8, y - 8, w + 16, h + 16, SIDEWALK_EDGE);
        pxRect(g, x - 4, y - 4, w + 8, h + 8, SIDEWALK);
        g.setColor(SIDEWALK_EDGE);
        for (int tx = x; tx < x + w; tx += 24) {
            g.fillRect(tx, y - 4, 2, h + 8);
        }
        for (int ty = y; ty < y + h; ty += 24) {
            g.fillRect(x - 4, ty, w + 8, 2);
        }
    }
    public static void drawPixelRoad(Graphics2D g, int x, int y, int w, int h, boolean horizontal) {
        pxRect(g, x, y, w, h, ASPHALT);
        g.setColor(ASPHALT_DARK);
        for (int py = y + 4; py < y + h - 4; py += 16) {
            for (int px = x + 4; px < x + w - 4; px += 24) {
                if (((px + py) & 16) == 0) g.fillRect(px, py, P, P);
            }
        }
        g.setColor(LANE_YELLOW);
        if (horizontal) {
            int midY = y + h / 2 - 2;
            for (int lx = x + 12; lx < x + w - 20; lx += 32) {
                g.fillRect(lx, midY, 16, P);
            }
            if (h >= 48) {
                g.setColor(new Color(210, 218, 224, 95));
                int topY = y + h / 4, botY = y + (3 * h) / 4;
                for (int ax = x + 56; ax < x + w - 56; ax += 112) {
                    // Jalur kiri LHT: atas -> Timur (>), bawah -> Barat (<)
                    g.fillRect(ax - 6, topY - 1, 10, 2);
                    g.fillRect(ax + 2, topY - 3, 2, 6);
                    g.fillRect(ax + 4, topY - 1, 2, 2);
                    g.fillRect(ax - 4, botY - 1, 10, 2);
                    g.fillRect(ax - 4, botY - 3, 2, 6);
                    g.fillRect(ax - 6, botY - 1, 2, 2);
                }
            }
        } else {
            int midX = x + w / 2 - 2;
            for (int ly = y + 12; ly < y + h - 20; ly += 32) {
                g.fillRect(midX, ly, P, 16);
            }
            if (w >= 48) {
                g.setColor(new Color(210, 218, 224, 95));
                int leftX = x + w / 4, rightX = x + (3 * w) / 4;
                for (int ay = y + 56; ay < y + h - 56; ay += 112) {
                    // Jalur kiri LHT: kiri -> Utara (^), kanan -> Selatan (v)
                    g.fillRect(leftX - 1, ay - 4, 2, 10);
                    g.fillRect(leftX - 3, ay - 4, 6, 2);
                    g.fillRect(leftX - 1, ay - 6, 2, 2);
                    g.fillRect(rightX - 1, ay - 6, 2, 10);
                    g.fillRect(rightX - 3, ay + 2, 6, 2);
                    g.fillRect(rightX - 1, ay + 4, 2, 2);
                }
            }
        }
    }
    public static void drawCrosswalk(Graphics2D g, int x, int y, int w, int h, boolean horizontalRoad) {
        g.setColor(new Color(235, 235, 228));
        if (horizontalRoad) {
            for (int ry = y + 6; ry < y + h - 6; ry += 10) {
                g.fillRect(x, ry, w, 6);
            }
        } else {
            for (int rx = x + 6; rx < x + w - 6; rx += 10) {
                g.fillRect(rx, y, 6, h);
            }
        }
    }
    public static void drawPixelTree(Graphics2D g, int cx, int cy, double anim) {
        int sway = ((int) Math.round(Math.sin(anim * 2 + cx * 0.1) * 1)) * 2;
        g.setColor(new Color(20, 40, 20, 95));
        g.fillRect(cx - 16, cy + 12, 32, 8);
        pxRect(g, cx - 6, cy - 4, 12, 20, WOOD_DARK);
        pxRect(g, cx - 2, cy - 4, 4, 18, WOOD_MAIN);
        pxRect(g, cx - 20 + sway, cy - 28, 40, 24, new Color(36, 98, 48));
        pxRect(g, cx - 16 + sway, cy - 36, 32, 24, new Color(52, 134, 62));
        pxRect(g, cx - 12 + sway, cy - 40, 24, 12, new Color(86, 172, 82));
    }
    public static void drawPixelBuilding(Graphics2D g, int x, int y, int w, int h, Color wall, Color roof, String label, TimePeriod period) {
        g.setColor(new Color(18, 24, 22, 100));
        g.fillRect(x + 6, y + 6, w, h);
        pxRect(g, x, y, w, h, wall);
        pxRect(g, x - 4, y - 6, w + 8, 24, roof);
        pxRect(g, x - 4, y + 14, w + 8, 4, roof.darker());
        boolean night = period == TimePeriod.MALAM || period == TimePeriod.SORE;
        Color winColor = night ? new Color(255, 228, 118) : new Color(138, 198, 232);
        for (int wy = y + 28; wy < y + h - 38; wy += 28) {
            for (int wx = x + 14; wx < x + w - 22; wx += 28) {
                pxRect(g, wx - 2, wy - 2, 18, 18, roof.darker());
                pxRect(g, wx, wy, 14, 14, winColor);
                pxRect(g, wx, wy, 6, 6, night ? new Color(255, 248, 190) : new Color(200, 238, 255));
            }
        }
        int doorX = x + w / 2 - 12;
        pxRect(g, doorX, y + h - 28, 24, 28, WOOD_DARK);
        pxRect(g, doorX + 4, y + h - 24, 16, 24, WOOD_MAIN);
        if (label != null && !label.isEmpty()) {
            drawPixelSignboard(g, label, x + w / 2, y - 4, new Color(24, 32, 44), HUD_BORDER);
        }
    }
    public static void drawPixelPondAndFountain(Graphics2D g, int x, int y, int w, int h, double anim) {
        pxRect(g, x - 6, y - 6, w + 12, h + 12, SIDEWALK_EDGE);
        pxRect(g, x, y, w, h, WATER_DEEP);
        pxRect(g, x + 8, y + 8, w - 16, h - 16, WATER_MID);
        int waveOffset = ((int) (anim * 8)) % 16;
        g.setColor(WATER_FOAM);
        for (int wy = y + 16; wy < y + h - 16; wy += 20) {
            for (int wx = x + 16 + waveOffset; wx < x + w - 24; wx += 36) {
                g.fillRect(wx, wy, 12, P);
            }
        }
        int fx = x + w / 2, fy = y + h / 2;
        pxRect(g, fx - 24, fy - 16, 48, 32, SIDEWALK);
        pxRect(g, fx - 16, fy - 12, 32, 24, WATER_FOAM);
        int spoutH = 12 + ((int) (anim * 10) % 8);
        pxRect(g, fx - 4, fy - 12 - spoutH, 8, spoutH + 8, WATER_FOAM);
        pxRect(g, fx - 12, fy - 8 - (spoutH / 2), 24, P, Color.WHITE);
    }
    public static void drawPixelParkBench(Graphics2D g, int x, int y) {
        pxRect(g, x - 24, y + 8, 48, 6, new Color(24, 38, 24, 110));
        pxRect(g, x - 22, y - 6, 44, 8, WOOD_MAIN);
        pxRect(g, x - 22, y + 2, 44, 6, WOOD_DARK);
        pxRect(g, x - 18, y - 12, 6, 22, new Color(48, 52, 58));
        pxRect(g, x + 12, y - 12, 6, 22, new Color(48, 52, 58));
    }
    public static void drawPixelStreetLamp(Graphics2D g, int x, int y, TimePeriod period) {
        boolean lit = period == TimePeriod.SORE || period == TimePeriod.MALAM;
        if (lit) {
            g.setColor(new Color(255, 224, 112, 55));
            g.fillOval(x - 36, y - 48, 72, 64);
            g.setColor(new Color(255, 236, 152, 95));
            g.fillOval(x - 18, y - 40, 36, 32);
        }
        pxRect(g, x - 2, y - 32, 4, 34, new Color(45, 52, 60));
        pxRect(g, x - 8, y - 36, 16, 8, new Color(68, 76, 86));
        pxRect(g, x - 4, y - 32, 8, 6, lit ? new Color(255, 240, 130) : new Color(190, 210, 220));
    }
    public static void drawPixelQuestBoard(Graphics2D g, int x, int y, boolean hasActiveQuest, double anim) {
        pxRect(g, x - 24, y - 28, 48, 32, WOOD_DARK);
        pxRect(g, x - 20, y - 24, 40, 24, WOOD_MAIN);
        pxRect(g, x - 16, y + 4, 6, 14, WOOD_DARK);
        pxRect(g, x + 10, y + 4, 6, 14, WOOD_DARK);
        pxRect(g, x - 16, y - 20, 10, 12, new Color(248, 240, 200));
        pxRect(g, x - 2, y - 18, 10, 14, new Color(248, 240, 200));
        pxRect(g, x + 10, y - 20, 8, 10, new Color(252, 214, 110));
        int bob = (int) Math.round(Math.sin(anim * 5) * 3);
        pxBorderBox(g, x - 12, y - 54 + bob, 24, 24, hasActiveQuest ? SANITY_HIGH : HUD_BORDER, Color.BLACK);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString(hasActiveQuest ? "?" : "!", x - 4, y - 37 + bob);
    }
    public static void drawPixelCharacter(Graphics2D g, double x, double y, Color shirt, double facing, boolean moving, boolean lowSanity, double anim) {
        int cx = (int) Math.round(x), cy = (int) Math.round(y);
        int step = moving ? (((int) (anim * 10)) % 2 == 0 ? -3 : 3) : 0;
        Direction dir = directionOf(facing);
        g.setColor(new Color(14, 22, 18, 115));
        g.fillRect(cx - 12, cy + 12, 24, 6);
        // Kaki pixel
        pxRect(g, cx - 8, cy + 2 + step, 6, 12, new Color(36, 48, 72));
        pxRect(g, cx + 2, cy + 2 - step, 6, 12, new Color(36, 48, 72));
        // Badan pixel
        pxRect(g, cx - 10, cy - 12, 20, 16, shirt.darker());
        pxRect(g, cx - 8, cy - 12, 16, 14, shirt);
        // Lengan pixel
        pxRect(g, cx - 14, cy - 10 - step, 4, 12, shirt);
        pxRect(g, cx + 10, cy - 10 + step, 4, 12, shirt);
        // Kepala & Rambut pixel
        pxRect(g, cx - 8, cy - 26, 16, 14, new Color(242, 198, 158));
        pxRect(g, cx - 10, cy - 30, 20, 8, new Color(56, 38, 28));
        if (dir == Direction.UP) {
            // Tampak belakang kepala (rambut menutup bagian belakang)
            pxRect(g, cx - 8, cy - 26, 16, 10, new Color(56, 38, 28));
        } else {
            int eyeOffset = dir == Direction.RIGHT ? 2 : (dir == Direction.LEFT ? -2 : 0);
            g.setColor(new Color(24, 24, 32));
            g.fillRect(cx - 5 + eyeOffset, cy - 20, 3, 3);
            g.fillRect(cx + 2 + eyeOffset, cy - 20, 3, 3);
        }
        if (lowSanity) {
            int dropY = ((int) (anim * 8)) % 6;
            pxRect(g, cx + 12, cy - 28 + dropY, 4, 8, new Color(92, 204, 255));
            drawPixelSignboard(g, "LELAH!", cx, cy - 54, SANITY_LOW, Color.BLACK);
        }
    }
    /** Overload standar untuk menggambar kendaraan pixel (default hasRider = true saat di jalan). */
    public static void drawPixelVehicle(Graphics2D g, Kendaraan v, double x, double y, double width, double angle, TimePeriod period, double anim) {
        drawPixelVehicle(g, v, x, y, width, angle, period, anim, true);
    }
    /** Menggambar kendaraan 100% 16-bit Pixel-Art dalam 4 arah (UP, DOWN, LEFT, RIGHT) beserta pengendara/pengemudi. */
    public static void drawPixelVehicle(Graphics2D g, Kendaraan v, double x, double y, double width, double angle, TimePeriod period, double anim, boolean hasRider) {
        drawPixelVehicle(g, v, x, y, width, angle, period, anim, hasRider, null);
    }
    /** Menggambar kendaraan pixel dengan aksen warna khusus (mis. untuk variasi kendaraan NPC). */
    public static void drawPixelVehicle(Graphics2D g, Kendaraan v, double x, double y, double width, double angle, TimePeriod period, double anim, boolean hasRider, Color accentColor) {
        if (v == null) return;
        int cx = (int) Math.round(x), cy = (int) Math.round(y);
        Direction dir = directionOf(angle);
        boolean night = period == TimePeriod.SORE || period == TimePeriod.MALAM;
        double scale = Math.max(0.65, width / 76.0);
        if (v instanceof Motor) {
            Color motoColor = accentColor != null ? accentColor : new Color(220, 52, 46);
            drawPixelMotorcycle(g, cx, cy, scale, dir, hasRider, night, anim, motoColor);
        } else {
            Color carColor = accentColor != null ? accentColor : new Color(48, 118, 204);
            drawPixelCar(g, cx, cy, scale, dir, hasRider, night, carColor, anim);
        }
    }
    /** Menggambar Motor Sport 16-bit Pixel-Art 4-arah + karakter pengendara berhelm saat hasRider == true. */
    public static void drawPixelMotorcycle(Graphics2D g, int cx, int cy, double scale, Direction dir, boolean hasRider, boolean night, double anim) {
        drawPixelMotorcycle(g, cx, cy, scale, dir, hasRider, night, anim, new Color(220, 52, 46));
    }
    public static void drawPixelMotorcycle(Graphics2D g, int cx, int cy, double scale, Direction dir, boolean hasRider, boolean night, double anim, Color baseColor) {
        Graphics2D g2 = (Graphics2D) g.create();
        enableCrispPixels(g2);
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        int bob = hasRider ? (((int) (anim * 12)) % 2) : 0;
        Color tireDark = new Color(26, 30, 36);
        Color rimSilver = new Color(188, 198, 210);
        Color bodyRed = baseColor != null ? baseColor : new Color(220, 52, 46);
        Color bodyShadow = bodyRed.darker();
        Color bodyLight = bodyRed.brighter();
        Color engineGray = new Color(76, 84, 96);
        Color seatBlack = new Color(36, 40, 48);
        Color jacketBlue = new Color(53, 137, 204);
        Color jacketDark = new Color(32, 92, 148);
        Color pantsNavy = new Color(34, 46, 72);
        Color helmetWhite = new Color(242, 246, 250);
        Color visorDark = new Color(32, 44, 58);

        if (dir == Direction.UP || dir == Direction.DOWN) {
            // Sorot lampu depan vertikal saat malam/sore
            if (night) {
                g2.setColor(new Color(255, 242, 148, 85));
                if (dir == Direction.UP) g2.fillOval(-22, -62, 44, 38);
                else g2.fillOval(-22, 20, 44, 38);
            }
            // Bayangan aspal vertikal
            g2.setColor(new Color(14, 18, 22, 120));
            g2.fillRect(-14, -24, 28, 52);

            if (dir == Direction.UP) {
                // === MOTOR MENGHADAP ATAS (UTARA / TAMPAK BELAKANG-ATAS) ===
                // Ban depan (atas) & lampu depan
                g2.setColor( new Color(255, 238, 112) );
                g2.fillRect(-5, -34, 10, 4);
                g2.setColor(tireDark);
                g2.fillRect(-5, -30, 10, 14);
                // Stang kiri-kanan & spion
                g2.setColor(engineGray);
                g2.fillRect(-20, -20 + bob, 40, 4);
                g2.setColor(seatBlack);
                g2.fillRect(-22, -21 + bob, 6, 6);
                g2.fillRect(16, -21 + bob, 6, 6);
                // Tangki & bodi tengah merah
                g2.setColor(bodyShadow);
                g2.fillRect(-10, -18 + bob, 20, 32);
                g2.setColor(bodyRed);
                g2.fillRect(-8, -16 + bob, 16, 26);
                g2.setColor(bodyLight);
                g2.fillRect(-4, -14 + bob, 8, 10);
                // Knalpot samping kanan
                g2.setColor(rimSilver);
                g2.fillRect(10, 2, 5, 16);

                if (hasRider) {
                    // Kaki kiri & kanan mengapit motor
                    g2.setColor(pantsNavy);
                    g2.fillRect(-14, -6 + bob, 6, 16);
                    g2.fillRect(8, -6 + bob, 6, 16);
                    // Lengan kiri & kanan memegang stang
                    g2.setColor(jacketBlue);
                    g2.fillRect(-18, -18 + bob, 8, 12);
                    g2.fillRect(10, -18 + bob, 8, 12);
                    // Punggung jaket biru pengendara
                    g2.setColor(jacketDark);
                    g2.fillRect(-11, -14 + bob, 22, 18);
                    g2.setColor(jacketBlue);
                    g2.fillRect(-9, -12 + bob, 18, 14);
                    // Helm putih strip merah tampak belakang
                    g2.setColor(helmetWhite);
                    g2.fillRect(-9, -28 + bob, 18, 15);
                    g2.setColor(bodyRed);
                    g2.fillRect(-3, -28 + bob, 6, 15);
                } else {
                    // Jok hitam tampak atas saat tanpa pengendara
                    g2.setColor(seatBlack);
                    g2.fillRect(-7, -6, 14, 16);
                }
                // Ban belakang & lampu rem merah menyala
                g2.setColor(tireDark);
                g2.fillRect(-6, 12, 12, 16);
                g2.setColor(new Color(255, 56, 48));
                g2.fillRect(-5, 12 + bob, 10, 4);
            } else {
                // === MOTOR MENGHADAP BAWAH (SELATAN / TAMPAK DEPAN-ATAS) ===
                // Ban belakang (atas)
                g2.setColor(tireDark);
                g2.fillRect(-6, -28, 12, 14);
                // Bodi belakang & tengah
                g2.setColor(bodyShadow);
                g2.fillRect(-10, -16 + bob, 20, 30);
                g2.setColor(bodyRed);
                g2.fillRect(-8, -14 + bob, 16, 26);

                if (hasRider) {
                    // Kaki mengapit bodi
                    g2.setColor(pantsNavy);
                    g2.fillRect(-14, -8 + bob, 6, 16);
                    g2.fillRect(8, -8 + bob, 6, 16);
                    // Badan & bahu jaket biru menghadap depan
                    g2.setColor(jacketDark);
                    g2.fillRect(-11, -16 + bob, 22, 16);
                    g2.setColor(jacketBlue);
                    g2.fillRect(-9, -14 + bob, 18, 14);
                    // Lengan kiri & kanan memegang stang depan
                    g2.fillRect(-18, -6 + bob, 8, 12);
                    g2.fillRect(10, -6 + bob, 8, 12);
                    // Helm putih & kaca visor gelap menghadap depan
                    g2.setColor(helmetWhite);
                    g2.fillRect(-9, -28 + bob, 18, 15);
                    g2.setColor(bodyRed);
                    g2.fillRect(-3, -28 + bob, 6, 6);
                    g2.setColor(visorDark);
                    g2.fillRect(-7, -21 + bob, 14, 6);
                } else {
                    g2.setColor(seatBlack);
                    g2.fillRect(-7, -12, 14, 16);
                }
                // Stang depan & spion
                g2.setColor(engineGray);
                g2.fillRect(-20, 4 + bob, 40, 4);
                g2.setColor(seatBlack);
                g2.fillRect(-22, 3 + bob, 6, 6);
                g2.fillRect(16, 3 + bob, 6, 6);
                // Fairing depan merah & lampu utama kuning terang
                g2.setColor(bodyRed);
                g2.fillRect(-9, 6 + bob, 18, 12);
                g2.setColor(new Color(255, 240, 118));
                g2.fillRect(-6, 12 + bob, 12, 6);
                // Garpu & ban depan (bawah)
                g2.setColor(rimSilver);
                g2.fillRect(-7, 16, 3, 12);
                g2.fillRect(4, 16, 3, 12);
                g2.setColor(tireDark);
                g2.fillRect(-5, 16, 10, 14);
            }
            g2.dispose();
            return;
        }

        // === MOTOR MENGHADAP KANAN / KIRI (TAMPAK SAMPING) ===
        if (dir == Direction.LEFT) {
            g2.scale(-1, 1);
        }
        if (night) {
            g2.setColor(new Color(255, 242, 148, 85));
            g2.fillOval(18, -22, 48, 34);
        }
        // Bayangan bawah
        g2.setColor(new Color(14, 18, 22, 120));
        g2.fillRect(-28, 12, 56, 7);
        // Standar samping jika sedang parkir tanpa pengendara
        if (!hasRider) {
            g2.setColor(rimSilver);
            g2.fillRect(-6, 6, 4, 11);
        }
        // Roda Belakang & Roda Depan Pixel
        for (int wx : new int[]{ -20, 18 }) {
            g2.setColor(tireDark);
            g2.fillRect(wx - 9, -2, 18, 18);
            g2.setColor(rimSilver);
            g2.fillRect(wx - 5, 2, 10, 10);
            g2.setColor(tireDark);
            g2.fillRect(wx - 2, 5, 4, 4);
        }
        // Knalpot & Blok Mesin
        g2.setColor(rimSilver);
        g2.fillRect(-28, 3 + bob, 18, 4);
        g2.setColor(engineGray);
        g2.fillRect(-8, -2 + bob, 16, 10);
        // Garpu depan & Stang
        g2.setColor(rimSilver);
        g2.fillRect(12, -14 + bob, 5, 18);
        g2.setColor(seatBlack);
        g2.fillRect(8, -20 + bob, 8, 4);
        // Rangka Bodi Sport Merah & Tangki
        g2.setColor(bodyShadow);
        g2.fillRect(-24, -10 + bob, 44, 10);
        g2.setColor(bodyRed);
        g2.fillRect(-22, -12 + bob, 20, 8);
        g2.fillRect(-2, -14 + bob, 22, 12);
        g2.setColor(bodyLight);
        g2.fillRect(2, -13 + bob, 14, 4);
        // Jok Hitam
        g2.setColor(seatBlack);
        g2.fillRect(-16, -14 + bob, 16, 6);
        // Lampu Depan Kuning & Lampu Rem Merah
        g2.setColor(new Color(255, 240, 118));
        g2.fillRect(19, -12 + bob, 5, 6);
        g2.setColor(new Color(255, 56, 48));
        g2.fillRect(-25, -11 + bob, 4, 5);

        // Karakter Pengendara di atas Motor (Jika hasRider == true)
        if (hasRider) {
            // Kaki & Sepatu menginjak footstep
            g2.setColor(pantsNavy);
            g2.fillRect(-10, -10 + bob, 10, 10);
            g2.fillRect(-4, -3 + bob, 8, 8);
            g2.setColor(seatBlack);
            g2.fillRect(0, 3 + bob, 7, 4);
            // Badan Jaket Biru condong ke stang
            g2.setColor(jacketDark);
            g2.fillRect(-12, -26 + bob, 14, 16);
            g2.setColor(jacketBlue);
            g2.fillRect(-10, -25 + bob, 12, 14);
            // Lengan memegang stang depan
            g2.setColor(jacketBlue);
            g2.fillRect(-2, -22 + bob, 14, 5);
            g2.setColor(new Color(242, 198, 158));
            g2.fillRect(10, -21 + bob, 4, 4);
            // Helm Pelindung Putih-Merah + Visor menghadap depan
            g2.setColor(helmetWhite);
            g2.fillRect(-10, -38 + bob, 16, 13);
            g2.setColor(bodyRed);
            g2.fillRect(-10, -33 + bob, 12, 3);
            g2.setColor(visorDark);
            g2.fillRect(0, -35 + bob, 7, 6);
        }
        g2.dispose();
    }
    /** Menggambar Mobil Sedan 16-bit Pixel-Art 4-arah + pengemudi di balik kaca. */
    public static void drawPixelCar(Graphics2D g, int cx, int cy, double scale, Direction dir, boolean hasDriver, boolean night, Color bodyColor, double anim) {
        Graphics2D g2 = (Graphics2D) g.create();
        enableCrispPixels(g2);
        g2.translate(cx, cy);
        g2.scale(scale, scale);
        int bob = hasDriver ? (((int) (anim * 10)) % 2) : 0;
        Color tireDark = new Color(24, 28, 34);
        Color rimSilver = new Color(186, 196, 208);
        Color bodyMain = bodyColor != null ? bodyColor : new Color(48, 118, 204);
        Color bodyDark = bodyMain.darker();
        Color roofLight = new Color(Math.min(255, bodyMain.getRed() + 36), Math.min(255, bodyMain.getGreen() + 36), Math.min(255, bodyMain.getBlue() + 36));
        Color glassBlue = new Color(136, 204, 238);
        Color glassGlare = new Color(212, 244, 255);

        if (dir == Direction.UP || dir == Direction.DOWN) {
            if (night) {
                g2.setColor(new Color(255, 242, 148, 85));
                if (dir == Direction.UP) g2.fillOval(-18, -48, 36, 28);
                else g2.fillOval(-18, 18, 36, 28);
            }
            // Bayangan bawah vertikal (proporsi ramping ~28x44 px agar pas di lajur kiri 35 px)
            g2.setColor(new Color(14, 18, 22, 125));
            g2.fillRect(-13, -21, 26, 44);
            // 4 Roda di sisi kiri & kanan
            g2.setColor(tireDark);
            g2.fillRect(-15, -16, 4, 10);
            g2.fillRect(11, -16, 4, 10);
            g2.fillRect(-15, 6, 4, 10);
            g2.fillRect(11, 6, 4, 10);
            // Bodi Utama Vertikal
            g2.setColor(bodyDark);
            g2.fillRect(-12, -22 + bob, 24, 44);
            g2.setColor(bodyMain);
            g2.fillRect(-10, -20 + bob, 20, 40);

            if (dir == Direction.UP) {
                // Kap mesin di atas & lampu depan kembar
                g2.setColor(new Color(255, 240, 118));
                g2.fillRect(-9, -22 + bob, 5, 3);
                g2.fillRect(4, -22 + bob, 5, 3);
                // Kaca depan (atas) & kaca belakang (bawah)
                g2.setColor(glassBlue);
                g2.fillRect(-8, -12 + bob, 16, 7);
                g2.fillRect(-8, 7 + bob, 16, 6);
                if (hasDriver) {
                    g2.setColor(new Color(56, 38, 28));
                    g2.fillRect(-6, -10 + bob, 5, 4);
                }
                // Atap tengah
                g2.setColor(roofLight);
                g2.fillRect(-8, -5 + bob, 16, 12);
                // Lampu rem belakang merah di bawah
                g2.setColor(new Color(255, 56, 48));
                g2.fillRect(-9, 19 + bob, 5, 3);
                g2.fillRect(4, 19 + bob, 5, 3);
            } else {
                // Bagasi di atas & lampu rem merah
                g2.setColor(new Color(255, 56, 48));
                g2.fillRect(-9, -22 + bob, 5, 3);
                g2.fillRect(4, -22 + bob, 5, 3);
                // Kaca belakang (atas) & Atap tengah
                g2.setColor(glassBlue);
                g2.fillRect(-8, -14 + bob, 16, 6);
                g2.setColor(roofLight);
                g2.fillRect(-8, -8 + bob, 16, 12);
                // Kaca depan lebar (bawah) + pengemudi terlihat
                g2.setColor(glassBlue);
                g2.fillRect(-8, 4 + bob, 16, 8);
                g2.setColor(glassGlare);
                g2.fillRect(-7, 5 + bob, 4, 3);
                if (hasDriver) {
                    g2.setColor(new Color(242, 198, 158));
                    g2.fillRect(-6, 5 + bob, 5, 5);
                    g2.setColor(new Color(56, 38, 28));
                    g2.fillRect(-6, 4 + bob, 5, 2);
                }
                // Lampu depan kembar di bawah
                g2.setColor(new Color(255, 240, 118));
                g2.fillRect(-9, 19 + bob, 5, 3);
                g2.fillRect(4, 19 + bob, 5, 3);
            }
            g2.dispose();
            return;
        }

        // === MOBIL MENGHADAP KANAN / KIRI (TAMPAK SAMPING) ===
        if (dir == Direction.LEFT) {
            g2.scale(-1, 1);
        }
        if (night) {
            g2.setColor(new Color(255, 242, 148, 85));
            g2.fillOval(26, -20, 52, 36);
        }
        // Bayangan aspal
        g2.setColor(new Color(14, 18, 22, 125));
        g2.fillRect(-34, 12, 68, 8);
        // Bodi bawah & atas sedan
        g2.setColor(bodyDark);
        g2.fillRect(-34, -8 + bob, 68, 18);
        g2.setColor(bodyMain);
        g2.fillRect(-32, -10 + bob, 64, 16);
        g2.fillRect(-20, -22 + bob, 38, 14);
        g2.setColor(roofLight);
        g2.fillRect(-18, -22 + bob, 34, 4);
        // Jendela kaca samping kiri-kanan
        g2.setColor(glassBlue);
        g2.fillRect(-16, -18 + bob, 14, 9);
        g2.fillRect(1, -18 + bob, 14, 9);
        g2.setColor(glassGlare);
        g2.fillRect(3, -17 + bob, 4, 4);
        // Pengemudi di balik jendela depan
        if (hasDriver) {
            g2.setColor(new Color(242, 198, 158));
            g2.fillRect(4, -17 + bob, 6, 6);
            g2.setColor(new Color(56, 38, 28));
            g2.fillRect(3, -18 + bob, 7, 3);
            g2.setColor(new Color(53, 137, 204));
            g2.fillRect(3, -11 + bob, 8, 3);
        }
        // Lampu depan & belakang
        g2.setColor(new Color(255, 240, 118));
        g2.fillRect(29, -6 + bob, 5, 6);
        g2.setColor(new Color(255, 56, 48));
        g2.fillRect(-34, -6 + bob, 5, 6);
        // Roda depan & belakang
        for (int wx : new int[]{ -20, 20 }) {
            g2.setColor(tireDark);
            g2.fillRect(wx - 9, 0, 18, 18);
            g2.setColor(rimSilver);
            g2.fillRect(wx - 5, 4, 10, 10);
            g2.setColor(tireDark);
            g2.fillRect(wx - 2, 7, 4, 4);
        }
        g2.dispose();
    }
    /** Menggambar sprite 16-bit Pixel-Art murni untuk ke-5 subclass Hewan (Tanpa gambar HD). */
    public static void drawPixelAnimalSprite(Graphics2D g, AnimalType type, double x, double y, double size, TimePeriod period, double anim) {
        Graphics2D g2 = (Graphics2D) g.create();
        enableCrispPixels(g2);
        double scale = Math.max(0.35, size / 64.0);
        int bob = period == TimePeriod.MALAM ? 0 : (((int) (anim * 4 + type.ordinal())) % 2) * 2;
        g2.translate(x, y + bob);
        g2.scale(scale, scale);

        switch (type) {
            case KUCING -> {
                // Kucing Oranye Tabby 16-Bit Pixel
                Color orange = new Color(236, 138, 52);
                Color stripe = new Color(178, 86, 24);
                Color cream = new Color(252, 240, 216);
                // Ekor melengkung
                g2.setColor(orange);
                g2.fillRect(-22, -22, 6, 16);
                g2.fillRect(-26, -26, 6, 8);
                // Badan & belang
                g2.fillRect(-16, -16, 28, 16);
                g2.setColor(stripe);
                g2.fillRect(-10, -16, 4, 10);
                g2.fillRect(-2, -16, 4, 10);
                // Kaki & dada putih krem
                g2.setColor(cream);
                g2.fillRect(4, -12, 8, 10);
                g2.fillRect(-14, 0, 6, 6);
                g2.fillRect(6, 0, 6, 6);
                // Kepala & telinga lancip
                g2.setColor(orange);
                g2.fillRect(2, -28, 18, 16);
                g2.fillRect(2, -34, 6, 6);
                g2.fillRect(14, -34, 6, 6);
                g2.setColor(new Color(244, 156, 168));
                g2.fillRect(4, -32, 2, 4);
                g2.fillRect(16, -32, 2, 4);
                // Mata & hidung pink
                g2.setColor(new Color(32, 44, 32));
                g2.fillRect(6, -22, 3, period == TimePeriod.MALAM ? 1 : 3);
                g2.fillRect(14, -22, 3, period == TimePeriod.MALAM ? 1 : 3);
                g2.setColor(new Color(244, 136, 148));
                g2.fillRect(10, -18, 3, 2);
            }
            case ANJING -> {
                // Anjing Golden/Coklat Sahabat Taman 16-Bit Pixel
                Color brown = new Color(188, 122, 58);
                Color darkEar = new Color(114, 66, 28);
                Color snout = new Color(238, 204, 162);
                // Ekor goyang
                int wag = ((int) (anim * 10)) % 2 == 0 ? -2 : 2;
                g2.setColor(brown);
                g2.fillRect(-24, -22 + wag, 8, 12);
                // Badan & kaki
                g2.fillRect(-18, -18, 32, 18);
                g2.setColor(darkEar);
                g2.fillRect(-8, -18, 10, 8);
                g2.setColor(brown);
                g2.fillRect(-16, 0, 7, 8);
                g2.fillRect(6, 0, 7, 8);
                // Kalung merah
                g2.setColor(new Color(220, 48, 48));
                g2.fillRect(4, -18, 12, 4);
                g2.setColor(HUD_BORDER);
                g2.fillRect(12, -14, 4, 4);
                // Kepala, telinga turun & moncong
                g2.setColor(brown);
                g2.fillRect(4, -32, 18, 16);
                g2.setColor(darkEar);
                g2.fillRect(2, -30, 6, 12);
                g2.setColor(snout);
                g2.fillRect(16, -26, 10, 10);
                // Mata, hidung hitam & lidah
                g2.setColor(Color.BLACK);
                g2.fillRect(12, -26, 3, period == TimePeriod.MALAM ? 1 : 3);
                g2.fillRect(23, -26, 4, 4);
                if (period != TimePeriod.MALAM) {
                    g2.setColor(new Color(242, 102, 122));
                    g2.fillRect(20, -16, 4, 5);
                }
            }
            case BURUNG -> {
                // Burung Biru Langit 16-Bit Pixel
                Color skyBlue = new Color(68, 164, 232);
                Color wingBlue = new Color(38, 108, 178);
                Color breast = new Color(212, 240, 255);
                int flap = period == TimePeriod.MALAM ? 0 : (((int) (anim * 12)) % 2) * 4;
                // Ekor & Badan bulat
                g2.setColor(wingBlue);
                g2.fillRect(-20, -16, 8, 6);
                g2.setColor(skyBlue);
                g2.fillRect(-12, -22, 24, 18);
                g2.setColor(breast);
                g2.fillRect(0, -16, 12, 12);
                // Sayap mengepak
                g2.setColor(wingBlue);
                g2.fillRect(-8, -18 - flap, 14, 10);
                // Jambul, Mata & Paruh Kuning
                g2.setColor(skyBlue);
                g2.fillRect(2, -28, 6, 6);
                g2.setColor(Color.BLACK);
                g2.fillRect(6, -18, 3, period == TimePeriod.MALAM ? 1 : 3);
                g2.setColor(new Color(252, 202, 56));
                g2.fillRect(12, -16, 8, 5);
                // Kaki mungil
                g2.setColor(new Color(232, 148, 48));
                g2.fillRect(-4, -4, 3, 8);
                g2.fillRect(4, -4, 3, 8);
            }
            case AYAM -> {
                // Ayam Putih Jengger Merah 16-Bit Pixel
                Color feather = new Color(246, 242, 232);
                Color wingCream = new Color(218, 208, 192);
                Color combRed = new Color(224, 48, 48);
                // Ekor & Badan
                g2.setColor(feather);
                g2.fillRect(-18, -24, 8, 12);
                g2.fillRect(-14, -20, 26, 18);
                g2.setColor(wingCream);
                g2.fillRect(-8, -16, 14, 10);
                // Kepala & Leher
                g2.setColor(feather);
                g2.fillRect(2, -30, 14, 14);
                // Jengger merah di atas & pial di bawah paruh
                g2.setColor(combRed);
                g2.fillRect(4, -36, 10, 6);
                g2.fillRect(12, -18, 4, 5);
                // Mata & Paruh Oranye
                g2.setColor(Color.BLACK);
                g2.fillRect(10, -26, 3, period == TimePeriod.MALAM ? 1 : 3);
                g2.setColor(new Color(246, 158, 42));
                g2.fillRect(16, -24, 7, 5);
                // Kaki kuning
                g2.setColor(new Color(238, 178, 48));
                g2.fillRect(-4, -2, 3, 8);
                g2.fillRect(4, -2, 3, 8);
            }
            case ANGSA -> {
                // Angsa Putih Anggun 16-Bit Pixel
                Color white = new Color(248, 250, 252);
                Color wingShade = new Color(214, 226, 238);
                // Riak air di bawah angsa
                g2.setColor(WATER_FOAM);
                g2.fillRect(-22, 0, 44, 4);
                // Badan & sayap berlipat
                g2.setColor(white);
                g2.fillRect(-20, -16, 34, 16);
                g2.setColor(wingShade);
                g2.fillRect(-14, -14, 20, 10);
                // Leher panjang melengkung & kepala
                g2.setColor(white);
                g2.fillRect(8, -36, 7, 24);
                g2.fillRect(8, -40, 14, 9);
                // Mata & Paruh Oranye
                g2.setColor(Color.BLACK);
                g2.fillRect(15, -38, 3, period == TimePeriod.MALAM ? 1 : 3);
                g2.setColor(new Color(248, 138, 42));
                g2.fillRect(22, -37, 8, 5);
            }
        }
        g2.dispose();
    }
    /** Proporsi ukuran pixel tiap hewan disesuaikan dengan skala karakter manusia (~44 px). */
    public static int animalPixelSize(AnimalType type) {
        return switch (type) {
            case ANJING -> 46;
            case ANGSA -> 44;
            case KUCING -> 38;
            case AYAM -> 32;
            case BURUNG -> 26;
        };
    }
    /** Menggambar area zona suara kecil di sekitar hewan (player mendengar suara saat memasukinya). */
    public static void drawAnimalSoundZone(Graphics2D g, double x, double y, double radius, boolean active, double anim) {
        Graphics2D g2 = (Graphics2D) g.create();
        int ix = (int) Math.round(x), iy = (int) Math.round(y), r = (int) Math.round(radius);
        int pulse = active ? (((int) (anim * 6)) % 2) * 3 : 0;
        g2.setColor(active ? new Color(102, 232, 168, 52) : new Color(146, 224, 178, 26));
        g2.fillOval(ix - r, iy - r, r * 2, r * 2);
        g2.setColor(active ? new Color(122, 245, 182, 195) : new Color(168, 228, 190, 105));
        g2.setStroke(new java.awt.BasicStroke(active ? 2.5f : 1.5f, java.awt.BasicStroke.CAP_SQUARE, java.awt.BasicStroke.JOIN_MITER, 10.0f, new float[]{8f, 6f}, (float) ((anim * 16) % 14)));
        g2.drawOval(ix - r - pulse, iy - r - pulse, (r + pulse) * 2, (r + pulse) * 2);
        g2.dispose();
    }
    public static void drawPixelAnimal(Graphics2D g, AnimalType type, Hewan animal, double x, double y, int friendship, TimePeriod period, double anim) {
        int ix = (int) Math.round(x), iy = (int) Math.round(y);
        int bob = period == TimePeriod.MALAM ? 0 : (((int) (anim * 4 + type.ordinal())) % 2) * 2;
        int size = animalPixelSize(type);
        int shadowW = Math.max(22, (int) Math.round(size * 0.65));
        g.setColor(new Color(18, 32, 18, 95));
        g.fillRect(ix - shadowW / 2, iy - 3, shadowW, 6);
        drawPixelAnimalSprite(g, type, ix, iy - 4, size, period, anim);
        // Emote bubble ringkas di atas kepala hewan tanpa menutupi sprite hewan
        double spriteScale = Math.max(0.35, size / 64.0);
        int badgeY = iy - (int) Math.round(44 * spriteScale) - 18 + bob;
        String emote = period == TimePeriod.MALAM ? "Zzz" : (friendship >= 30 ? "<3 +" + animal.getSanityBonus() + "%" : "+" + animal.getSanityBonus() + "%");
        Color badgeBg = period == TimePeriod.MALAM ? new Color(46, 58, 98) : (friendship >= 30 ? new Color(188, 48, 72) : new Color(28, 86, 62));
        drawPixelSignboard(g, emote, ix, badgeY, badgeBg, HUD_BORDER);
    }
    /** Menggambar ilustrasi Peta Kota 16-bit Pixel-Art murni untuk bingkai Rumah & Cover Menu Utama. */
    public static void drawMiniMapPreview(Graphics2D g, int x, int y, int w, int h, double anim) {
        Graphics2D g2 = (Graphics2D) g.create();
        enableCrispPixels(g2);
        g2.setClip(x, y, w, h);
        pxRect(g2, x, y, w, h, GRASS_BASE);
        // Jaringan jalan raya kota mini
        int rTop = y + (int) (h * 0.22), rBot = y + (int) (h * 0.72), rH = Math.max(16, (int) (h * 0.14));
        int cLeft = x + (int) (w * 0.14), cMid = x + (int) (w * 0.48), cRight = x + (int) (w * 0.82), rW = Math.max(16, (int) (w * 0.08));
        drawPixelRoad(g2, x + 8, rTop, w - 16, rH, true);
        drawPixelRoad(g2, x + 8, rBot, w - 16, rH, true);
        drawPixelRoad(g2, cLeft, rTop, rW, rBot - rTop + rH, false);
        drawPixelRoad(g2, cMid, rTop, rW, rBot - rTop + rH, false);
        drawPixelRoad(g2, cRight, rTop, rW, rBot - rTop + rH, false);
        // Miniatur Rumah, Pusat Kota, Kolam Taman, dan SPBU
        pxBorderBox(g2, x + (int) (w * 0.24), y + (int) (h * 0.44), (int) (w * 0.18), (int) (h * 0.22), new Color(204, 136, 92), new Color(128, 62, 42));
        pxBorderBox(g2, x + (int) (w * 0.58), y + (int) (h * 0.40), (int) (w * 0.20), (int) (h * 0.20), WATER_MID, SIDEWALK);
        pxBorderBox(g2, x + (int) (w * 0.58), y + (int) (h * 0.62), (int) (w * 0.20), (int) (h * 0.10), new Color(218, 52, 48), Color.WHITE);
        // Miniatur Motor & Mobil Pixel di jalan
        drawPixelMotorcycle(g2, x + (int) (w * 0.35), rBot + rH / 2, 0.55, Direction.RIGHT, true, false, anim);
        drawPixelCar(g2, x + (int) (w * 0.66), rTop + rH / 2, 0.55, Direction.LEFT, true, false, new Color(48, 118, 204), anim);
        g2.dispose();
    }
    /** Menggambar Pin Target Quest & Panah Kompas Pixel di sekitar pemain agar arah tujuan selalu jelas. */
    public static void drawQuestGuidance(Graphics2D g, GameManager game) {
        GameState s = game.getState();
        Location loc = s.getLocation();
        if (loc != Location.MAP && loc != Location.CITY && loc != Location.PARK) return;
        GameManager.QuestNav nav = game.getQuestNavigation();
        double anim = game.getAnimationSeconds();
        double px = s.getX(), py = s.getY();
        double tx = nav.targetX(), ty = nav.targetY();
        int bounce = (((int) (anim * 5)) % 2) * 4;
        boolean blink = (((int) (anim * 4)) % 2) == 0;

        Color pinBg = nav.isQuestActive()
            ? (blink ? new Color(214, 118, 24) : new Color(168, 82, 18))
            : new Color(26, 88, 68);
        Color pinBorder = nav.isQuestActive() ? HUD_BORDER : SANITY_HIGH;

        // 1. Pin Target Berkedip + Panah Pixel Menunjuk Objek/Gerbang Target (Anti-Overlap dengan Label Lokasi)
        int baseOffset = 64;
        if (loc == Location.MAP && ty < 300) {
            baseOffset = 82; // Tepat di atas pill gerbang Utara ("Pusat Kota" / "Taman")
        } else if (loc == Location.CITY && Math.abs(tx - WorldLayout.CITY_NPC.x) < 20 && Math.abs(ty - WorldLayout.CITY_NPC.y) < 20) {
            baseOffset = 140; // Tepat di atas pill "Pak Raka [E]"
        } else if (loc == Location.PARK && Math.abs(tx - WorldLayout.SHOP_DOOR.x) < 20 && Math.abs(ty - WorldLayout.SHOP_DOOR.y) < 20) {
            baseOffset = 52; // Tepat di atas pill "[KIOS] Pakan & Kopi [E]"
        }
        int pinX = ((int) Math.round(tx) / 4) * 4;
        int pinY = ((int) Math.round(ty - baseOffset - bounce) / 4) * 4;
        pinY = Math.max(60, Math.min(520, pinY));
        drawPixelSignboard(g, nav.badgeText(), pinX, pinY, pinBg, pinBorder);
        // Segitiga panah pixel menunjuk ke bawah tepat di bawah bingkai signboard (bh = 24)
        pxRect(g, pinX - 6, pinY + 24, 12, 4, pinBorder);
        pxRect(g, pinX - 4, pinY + 28, 8, 4, pinBorder);
        pxRect(g, pinX - 2, pinY + 32, 4, 4, pinBorder);

        // 2. Kompas Panah Arah & Jarak di Dekat Pemain (ketika jarak > 75 px)
        double dist = Math.hypot(tx - px, ty - py);
        if (dist > 75) {
            double ux = (tx - px) / dist;
            double uy = (ty - py) / dist;
            int cx = ((int) Math.round(px + ux * 52) / 4) * 4;
            int cy = ((int) Math.round(py + uy * 44) / 4) * 4;
            cx = Math.max(56, Math.min(944, cx));
            cy = Math.max(92, Math.min(540, cy));
            String arrow;
            if (Math.abs(ux) > Math.abs(uy) * 1.6) {
                arrow = ux > 0 ? ">>>" : "<<<";
            } else if (Math.abs(uy) > Math.abs(ux) * 1.6) {
                arrow = uy > 0 ? "vvv" : "^^^";
            } else {
                arrow = ux > 0 ? (uy > 0 ? ">>v" : ">>^") : (uy > 0 ? "v<<" : "^<<");
            }
            String compassTag = arrow + " " + Math.max(1, Math.round(dist / 8.0)) + "m";
            drawPixelSignboard(g, compassTag, cx, cy, new Color(18, 26, 38, 235), pinBorder);
        }
    }
    public static void drawMiniMapAndTracker(Graphics2D g, GameManager game) {
        GameState s = game.getState();
        GameManager.QuestNav nav = game.getQuestNavigation();
        double anim = game.getAnimationSeconds();
        boolean blink = (((int) (anim * 5)) % 2) == 0;
        // Quest & Sanity Status Banner di Kanan Atas
        int rx = 740, ry = 14, rw = 244, rh = 112;
        pxBorderBox(g, rx, ry, rw, rh, HUD_BG, HUD_BORDER);
        g.setColor(HUD_BORDER);
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.drawString("RADAR KOTA & QUEST [Q]", rx + 10, ry + 18);
        // Minimap Radar (Kiri dalam kotak)
        int mx = rx + 10, my = ry + 26, mw = 96, mh = 58;
        pxRect(g, mx, my, mw, mh, new Color(42, 82, 48));
        g.setColor(new Color(90, 96, 108));
        g.fillRect(mx + 4, my + 10, mw - 8, 6);
        g.fillRect(mx + 4, my + 42, mw - 8, 6);
        g.fillRect(mx + 10, my + 10, 6, 38);
        g.fillRect(mx + 45, my + 10, 6, 38);
        g.fillRect(mx + 80, my + 10, 6, 38);
        // Titik lokasi penting di minimap
        g.setColor( new Color(236, 168, 96));
        g.fillRect(mx + 23, my + 43, 6, 6); // Rumah
        g.setColor(SANITY_HIGH);
        g.fillRect(mx + 62, my + 11, 6, 6); // Taman
        g.setColor(FUEL_COLOR);
        g.fillRect(mx + 66, my + 43, 6, 6); // SPBU
        g.setColor(new Color(118, 192, 255));
        g.fillRect(mx + 35, my + 11, 6, 6); // Kota
        // Beacon berkedip pada lokasi target Quest/Misi di Radar
        if (blink && nav.targetLocation() != null) {
            int bx = switch (nav.targetLocation()) {
                case HOME -> mx + 23;
                case CITY -> mx + 35;
                case PARK -> mx + 62;
                case FUEL -> mx + 66;
                case MAP -> mx + 45;
            };
            int by = switch (nav.targetLocation()) {
                case CITY, PARK -> my + 11;
                default -> my + 43;
            };
            g.setColor(HUD_BORDER);
            g.drawRect(bx - 2, by - 2, 9, 9);
        }
        // Posisi pemain di minimap
        int dotX = mx + (int) Math.round(s.getMapX() / 1000.0 * mw);
        int dotY = my + (int) Math.round(s.getMapY() / 600.0 * mh);
        g.setColor(Color.WHITE);
        g.fillRect(dotX - 3, dotY - 3, 6, 6);
        // Info Sanity & Tujuan di sebelah kanan minimap
        int tx = mx + mw + 10;
        g.setColor(s.isLowSanity() ? SANITY_LOW : SANITY_HIGH);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.drawString("SANITY: " + s.getSanity() + "%", tx, my + 14);
        g.setColor(Color.WHITE);
        g.drawString("TUJUAN: " + nav.targetLocation().getLabel().toUpperCase(java.util.Locale.ROOT), tx, my + 30);
        String modeStr = s.getLocation() == Location.MAP
            ? (s.isMounted() ? "KEMUDI [F]" : "JALAN [F]")
            : s.getLocation().getLabel().toUpperCase(java.util.Locale.ROOT);
        g.setColor(new Color(180, 224, 255));
        g.drawString("POSISI: " + modeStr, tx, my + 46);
        // Baris Instruksi Langkah Quest/Misi di bawah minimap
        Quest q = game.getActiveQuest();
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(q != null ? new Color(255, 230, 115) : new Color(190, 232, 210));
        String qText = nav.badgeText() + " -> " + nav.targetLocation().getLabel();
        if (qText.length() > 36) qText = qText.substring(0, 36);
        g.drawString(qText, rx + 10, ry + 100);
    }
}
