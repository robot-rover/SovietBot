package rr.industries.exceptions;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.sql.SQLException;
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

    @Deprecated
    public static void translateException(Exception ex) throws BotException {
        throw returnException(ex);
    }

    public static BotException returnException(Exception ex) {
        if (ex instanceof DiscordException) {
            return new DiscordError((DiscordException) ex);
        } else if (ex instanceof RateLimitException) {
            return new InternalError("A RateLimitException was not handled!", ex);
        } else if (ex instanceof MissingPermissionsException) {
            return new BotMissingPermsException(((MissingPermissionsException) ex).getErrorMessage());
        } else if (ex instanceof UnirestException) {
            return new InternalError("Unirest Exception", ex);
        } else if (ex instanceof SQLException) {
            return new InternalError("SQLError", ex);
        } else {
            throw new UnsupportedOperationException(ex.getClass().getName() + " is not a supported exception", ex);
        }
    }
}
