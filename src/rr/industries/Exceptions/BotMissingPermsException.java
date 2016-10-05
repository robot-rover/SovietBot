package rr.industries.Exceptions;

/**
 * @author Sam
 */
public class BotMissingPermsException extends BotException {
    public BotMissingPermsException(String message, String neededPerm) {
        super("The bot requires the " + neededPerm + " permission to continue!");
    }

    @Override
    public boolean isCritical() {
        return false;
    }
}
