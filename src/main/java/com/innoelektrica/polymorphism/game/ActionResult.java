package com.innoelektrica.polymorphism.game;
public record ActionResult(boolean success,String message) {
    public static ActionResult ok(String m) {
        return new ActionResult(true,m);
    }
    public static ActionResult fail(String m) {
        return new ActionResult(false,m);
    }
}
