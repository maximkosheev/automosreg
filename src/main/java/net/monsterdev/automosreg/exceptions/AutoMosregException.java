package net.monsterdev.automosreg.exceptions;

/**
 * Исключение, порождаемое приложением, как правило связанное с действиями пользователя
 */
public class AutoMosregException extends Exception {
    public AutoMosregException(String message) {
        super(message);
    }
}
