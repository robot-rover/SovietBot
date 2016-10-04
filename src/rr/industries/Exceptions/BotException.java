package rr.industries.Exceptions;

/**
 * @author Sam
 */
public abstract class BotException extends Exception {
    protected BotException(String message) {
        super(message);
    }

    public abstract boolean isCritical();

    @Override
    public String toString() {
        return getMessage();
    }
}
