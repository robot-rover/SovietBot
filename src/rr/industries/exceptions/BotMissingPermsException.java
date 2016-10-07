package rr.industries.exceptions;

import java.util.Optional;

/**
 * @author Sam
 */
public class BotMissingPermsException extends BotException {
    public BotMissingPermsException(String neededPerm) {
        super("The bot requires the " + neededPerm + " permission to continue!");
    }

    public BotMissingPermsException(String action, String neededPerm) {
        super("The bot requires the " + neededPerm + " permission to " + action);
    }

    public Optional<String> criticalMessage() {
        return Optional.empty();
    }
}
