package rr.industries.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;


/**
 * @author Sam
 */
public abstract class BotException extends Exception {
    protected Logger LOG = LoggerFactory.getLogger(BotException.class);
    protected BotException(String message) {
        super(message);
    }

    public abstract boolean isCritical();

    @Override
    public String toString() {
        return getMessage();
    }

    public static void translateException(Exception ex) {
        if (ex instanceof DiscordException) {

        } else if (ex instanceof RateLimitException) {

        } else if (ex instanceof MissingPermsException) {

        } else {
            throw new UnsupportedOperationException(ex.getClass().getName() + " is not a supported exception", ex);
        }
    }
}
