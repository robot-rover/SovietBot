package rr.industries.exceptions;

import sx.blah.discord.util.DiscordException;

import java.util.Optional;

/**
 * @author Sam
 */
public class DiscordError extends BotException {
    DiscordException ex;
    public DiscordError(DiscordException ex) {
        super("An internal Error has occurred... ");
        LOG.error("Discord Error", ex);
        this.ex = ex;
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.of("Discord Server Error: " + ex.getMessage());
    }
}
