package net.monsterdev.automosreg.events;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.stage.Window;

public class LoginEvent extends Event {
    /**
     * Event additional message
     */
    private String message;
    /**
     * Common supertype for all window event types.
     */
    private static final EventType<LoginEvent> ANY = new EventType<LoginEvent>(Event.ANY, "LOGIN");
    /**
     * This event occurs on successful login.
     */
    public static final EventType<LoginEvent> LOGIN_SUCCESS = new EventType<LoginEvent>(ANY, "LOGIN_SUCCESS");

    /**
     * This event occurs on login failed.
     */
    public static final EventType<LoginEvent> LOGIN_FAILED = new EventType<LoginEvent>(ANY, "LOGIN_FAILED");

    public LoginEvent(final @NamedArg("source") Window source, final @NamedArg("eventType") EventType<? extends Event> eventType, final @NamedArg("message") String message) {
        super(source, source, eventType);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
