package rr.industries.exceptions;

import java.util.Optional;

/**
 * @author Sam
 */
public class ServerError extends BotException {
    private String reasonForFailure;

    public ServerError(String reasonForFailure) {
        super("There was an internal error. The bot Operator has been notified.");
        LOG.error("The bot is Misconfigured: " + reasonForFailure);
    }

    public ServerError(String reasonForFailure, Throwable cause) {
        super("There was an internal error. The bot Operator has been notified.");
        LOG.error("The bot threw an Internal Error: " + reasonForFailure, cause);
        this.reasonForFailure = reasonForFailure;
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.of("The bot threw an Internal Error: " + reasonForFailure);
    }
}
