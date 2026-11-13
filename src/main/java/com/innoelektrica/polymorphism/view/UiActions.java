package com.innoelektrica.polymorphism.view;
import com.innoelektrica.polymorphism.game.GameManager;
import com.innoelektrica.polymorphism.model.AnimalType;
import java.awt.Graphics2D;
public interface UiActions {
    void interact();
    void inventory();
    void togglePause();
    void saveGame();
    void loadGame();
    void menu();
    void help();
    void demo();
    void emergency();
    void leave();
    void message(String s);
    void animal(AnimalType a);
    void shop();
    void home();
    void cityNpc();
    void fuel();
    default void toggleMount() {
    }
    default void questBoard() {
    }
    default void restBench() {
    }
    default void honk() {
    }
    default void goHome() {
    }
    default boolean handleOverlayClick(double x, double y) {
        return false;
    }
    default void paintOverlay(Graphics2D g, GameManager game) {
    }
    default void handleKeyChoice(int choice) {
    }
}
