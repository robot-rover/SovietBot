package rr.industries.Exceptions;

import sx.blah.discord.util.DiscordException;

import java.util.Optional;

/**
 * @author Sam
 */
public class DiscordError extends BotException {
    public DiscordError(DiscordException ex) {
        super("An internal Error has occurred: " + ex.getErrorMessage());
        LOG.error("Discord Error", ex);
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.empty();
    }
}
