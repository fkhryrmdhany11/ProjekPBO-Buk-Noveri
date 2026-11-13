package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.*;
import com.innoelektrica.polymorphism.model.*;
import com.innoelektrica.polymorphism.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public final class MainFrame extends JFrame implements UiActions {
    private final SaveService saves = new SaveService();
    private final InputController input = new InputController();
    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final GameScreen screen;
    private final MenuPanel mainMenu;
    private final PixelOverlayPanel overlay;
    private final Timer timer;
    private GameManager game, backupGame;
    private DemoController demoController;
    private boolean menuVisible = true, backupMenu;
    private long lastNano = System.nanoTime();
    private Location lastLocation;
    @SuppressWarnings("this-escape")
    public MainFrame() {
        super("A Day in My City — Pixel Edition");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(980, 720));
        Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1280, desktop.width - 40), Math.min(850, desktop.height - 60));
        setLocationRelativeTo(null);
        overlay = new PixelOverlayPanel(this::saveGame, this::refreshUi);
        screen = new GameScreen(() -> game, input, this);
        mainMenu = new MenuPanel(this::startNew, this::loadGame, this::showGame, this::help, this::demo, this::exit);
        root.add(mainMenu, "MENU");
        root.add(screen, "GAME");
        setContentPane(root);
        input.install(getRootPane(), this);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) {
                if (game != null && game.isUiBlocked()) {
                    input.stop();
                    return;
                }
                input.clear();
                if (game != null && !menuVisible && game.getState().getDayStatus() == DayStatus.ACTIVE) {
                    game.setPaused(true);
                    screen.refresh();
                }
            }
        });
        AudioPlayer.setErrorHandler(this::message);
        timer = new Timer(50, e -> tick());
        timer.setCoalesce(true);
        timer.start();
        refreshMenu();
    }
    private void refreshUi() {
        if (!menuVisible) screen.refresh();
    }
    private void refreshMenu() {
        mainMenu.refresh(saves.exists(), game != null);
    }
    private void tick() {
        long now = System.nanoTime();
        double dt = Math.min(.25, (now - lastNano) / 1_000_000_000.0);
        lastNano = now;
        if (menuVisible || game == null) return;
        if (demoController != null) {
            demoController.update(dt);
            game.tick(dt, demoController.getInputX(), demoController.getInputY());
            if (demoController.isFinished()) {
                stopDemo();
                return;
            }
        } else {
            game.tick(dt, input.getX(), input.getY());
        }
        if (lastLocation != game.getState().getLocation()) {
            lastLocation = game.getState().getLocation();
            input.stop();
            AudioPlayer.stopAll();
        }
        screen.refresh();
    }
    private void startNew() {
        if (demoController != null) stopDemo();
        String name = mainMenu.getEnteredPlayerName();
        try {
            overlay.close(game);
            game = new GameManager(new GameState(name));
            showGame();
            overlay.open(PixelOverlayPanel.Mode.HOME, game);
        } catch (IllegalArgumentException ex) {
            mainMenu.setStatusMessage("⚠ " + ex.getMessage());
        }
    }
    private void showGame() {
        if (game == null) return;
        menuVisible = false;
        rootCards.show(root, "GAME");
        input.clear();
        lastNano = System.nanoTime();
        lastLocation = game.getState().getLocation();
        screen.refresh();
    }
    private boolean manual() {
        return game != null && !menuVisible && !game.isDemo() && !game.isPaused() && !game.isUiBlocked();
    }
    @Override public void interact() {
        if (overlay.isOpen()) {
            overlay.handleKeyChoice(1, game);
            return;
        }
        if (!manual()) return;
        var t = game.getNearbyTarget();
        switch (t.kind()) {
            case HOME -> home();
            case ENTRANCE -> result(game.enterNearbyLocation());
            case EXIT -> leave();
            case SHOP -> shop();
            case ANIMAL -> animal(t.animal());
            case BENCH -> restBench();
            case NPC -> cityNpc();
            case QUEST_BOARD -> questBoard();
            case PLAZA -> result(game.exploreCity());
            case FUEL -> fuel();
            case NONE -> message(t.label());
        }
    }
    private void result(ActionResult r) {
        message(r.message());
        screen.refresh();
    }
    @Override public void message(String s) {
        if (game != null && !menuVisible) {
            game.notifyPlayer(s);
            screen.refresh();
        } else {
            mainMenu.setStatusMessage(s);
        }
    }
    @Override public void togglePause() {
        if (game == null || menuVisible || game.isUiBlocked() || game.getState().getDayStatus() == DayStatus.ENDED) return;
        input.clear();
        game.setPaused(!game.isPaused());
        if (game.isPaused()) AudioPlayer.stopEngineLoop();
        screen.refresh();
    }
    @Override public void toggleMount() {
        if (!manual()) return;
        result(game.toggleMount());
    }
    @Override public void honk() {
        if (!manual()) return;
        result(game.honkVehicle());
    }
    @Override public void goHome() {
        if (!manual()) return;
        result(game.quickReturnHome());
    }
    @Override public void questBoard() {
        if (game == null || menuVisible || game.isDemo() || game.isPaused()) return;
        if (overlay.getMode() == PixelOverlayPanel.Mode.QUEST_BOARD) {
            overlay.close(game);
        } else {
            input.stop();
            overlay.open(PixelOverlayPanel.Mode.QUEST_BOARD, game);
        }
    }
    @Override public void restBench() {
        if (!manual()) return;
        result(game.restAtParkBench());
    }
    @Override public void menu() {
        if (overlay.isOpen()) {
            overlay.close(game);
            AudioPlayer.stopAll();
            return;
        }
        if (demoController != null) stopDemo();
        if (game != null && game.isBusy()) {
            message("Selesaikan atau batalkan aktivitas sebelum membuka menu.");
            return;
        }
        input.clear();
        AudioPlayer.stopAll();
        menuVisible = true;
        rootCards.show(root, "MENU");
        refreshMenu();
    }
    @Override public void saveGame() {
        if (game == null) {
            message("Mulai permainan dahulu.");
            return;
        }
        try {
            saves.save(game);
            message("Permainan tersimpan. Save bisa dimuat setelah aplikasi dibuka kembali.");
            refreshMenu();
        } catch (Exception ex) {
            message("Save gagal: " + ex.getMessage());
        }
    }
    @Override public void loadGame() {
        if (game != null && !game.canPersist()) {
            message("Load tidak tersedia selama demo atau aktivitas/event aktif.");
            return;
        }
        try {
            GameState loaded = saves.load();
            AudioPlayer.stopAll();
            overlay.close(game);
            game = new GameManager(loaded);
            showGame();
            message("Save berhasil dimuat.");
        } catch (Exception ex) {
            message("Load gagal: " + ex.getMessage());
        }
    }
    @Override public void leave() {
        if (!manual()) return;
        result(game.leaveLocation());
    }
    @Override public void home() {
        if (!manual() || game.getState().getLocation() != Location.HOME) return;
        input.stop();
        if (game.getState().getDayStatus() == DayStatus.ENDED) {
            overlay.open(PixelOverlayPanel.Mode.SUMMARY, game);
        } else {
            overlay.open(PixelOverlayPanel.Mode.HOME, game);
        }
    }
    @Override public void inventory() {
        if (game == null || menuVisible || game.isDemo()) return;
        if (overlay.getMode() == PixelOverlayPanel.Mode.INVENTORY) {
            overlay.close(game);
        } else {
            input.stop();
            overlay.open(PixelOverlayPanel.Mode.INVENTORY, game);
        }
    }
    @Override public void animal(AnimalType a) {
        if (!manual() || a == null || game.getState().getLocation() != Location.PARK) return;
        if (game.getAnimalPosition(a).distance(game.getState().getX(), game.getState().getY()) > 85) {
            message("Dekati " + a + " dahulu.");
            return;
        }
        input.stop();
        overlay.openAnimal(a, game);
    }
    @Override public void shop() {
        if (!manual()) return;
        if (!game.nearShop()) {
            message("Dekati Kios Taman dahulu.");
            return;
        }
        input.stop();
        overlay.open(PixelOverlayPanel.Mode.SHOP, game);
    }
    @Override public void cityNpc() {
        if (!manual() || game.getState().getLocation() != Location.CITY) return;
        if (WorldLayout.CITY_NPC.distance(game.getState().getX(), game.getState().getY()) > 85) {
            message("Dekati Pak Raka dahulu.");
            return;
        }
        input.stop();
        overlay.open(PixelOverlayPanel.Mode.NPC, game);
    }
    @Override public void fuel() {
        if (!manual() || game.getState().getLocation() != Location.FUEL) return;
        input.stop();
        overlay.open(PixelOverlayPanel.Mode.FUEL, game);
    }
    @Override public void emergency() {
        if (!manual()) return;
        input.stop();
        overlay.open(PixelOverlayPanel.Mode.EMERGENCY, game);
    }
    @Override public void help() {
        if (game == null) {
            game = new GameManager(new GameState(mainMenu.getEnteredPlayerName()));
            showGame();
        }
        input.stop();
        overlay.open(PixelOverlayPanel.Mode.HELP, game);
    }
    @Override public boolean handleOverlayClick(double x, double y) {
        return overlay.handleClick(x, y, game);
    }
    @Override public void paintOverlay(Graphics2D g, GameManager gm) {
        overlay.paint(g, gm);
    }
    @Override public void handleKeyChoice(int choice) {
        overlay.handleKeyChoice(choice, game);
    }
    @Override public void demo() {
        if (game != null && game.isUiBlocked()) overlay.close(game);
        if (demoController != null) {
            stopDemo();
            return;
        }
        if (game != null && game.isBusy()) {
            message("Selesaikan aktivitas dahulu sebelum Auto Demo.");
            return;
        }
        backupGame = game;
        backupMenu = menuVisible;
        AudioPlayer.stopAll();
        demoController = new DemoController();
        game = demoController.getGame();
        showGame();
    }
    private void stopDemo() {
        if (demoController == null) return;
        demoController.stop();
        demoController = null;
        game = backupGame;
        backupGame = null;
        input.clear();
        if (backupMenu || game == null) {
            menuVisible = true;
            rootCards.show(root, "MENU");
            refreshMenu();
        } else {
            showGame();
        }
    }
    private void exit() {
        if (game != null && game.getPendingFuel() != null) game.cancelFuel();
        timer.stop();
        AudioPlayer.stopAll();
        dispose();
    }
}
