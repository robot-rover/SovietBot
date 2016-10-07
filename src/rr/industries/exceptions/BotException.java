package rr.industries.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.Optional;


/**
 * @author Sam
 */
public abstract class BotException extends Exception {
    protected Logger LOG = LoggerFactory.getLogger(BotException.class);
    protected BotException(String message) {
        super(message);
    }

    public abstract Optional<String> criticalMessage();

    @Override
    public String toString() {
        return getMessage();
    }

    public static void translateException(Exception ex) throws BotException {
        throw returnException(ex);
    }

    public static BotException returnException(Exception ex) {
        if (ex instanceof DiscordException) {
            return new DiscordError((DiscordException) ex);
        } else if (ex instanceof RateLimitException) {
            return new InternalError("A RateLimitException was not handled!", ex);
        } else if (ex instanceof MissingPermsException) {
            return new BotMissingPermsException(((MissingPermissionsException) ex).getErrorMessage());
        } else {
            throw new UnsupportedOperationException(ex.getClass().getName() + " is not a supported exception", ex);
        }
    }
}
