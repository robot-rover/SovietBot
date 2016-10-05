package rr.industries.Exceptions;

import sx.blah.discord.util.DiscordException;

/**
 * @author Sam
 */
public class DiscordError extends BotException {
    public DiscordError(DiscordException ex) {
        super("An internal Error has occurred: " + ex.getErrorMessage());
        LOG.error("Discord Error", ex);
    }

    @Override
    public boolean isCritical() {
        return true;
    }
}
