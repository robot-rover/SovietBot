package rr.industries.Exceptions;

import java.util.Optional;

/**
 * @author Sam
 */
public class InternalError extends BotException {
    private String reasonForFailure;

    public InternalError(String reasonForFailure) {
        super("There was an internal error. The bot Operator has been notified.");
        LOG.error("The bot is Misconfigured: " + reasonForFailure);
    }

    public InternalError(String reasonForFailure, Throwable cause) {
        super("There was an internal error. The bot Operator has been notified.");
        LOG.error("The bot is Misconfigured: " + reasonForFailure, cause);
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.of("Bot is Misconfigured: " + reasonForFailure);
    }
}
