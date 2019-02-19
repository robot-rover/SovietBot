package rr.industries.exceptions;

/**
 * @author Sam
 */
public class ServerError extends BotException {
    public ServerError(String reasonForFailure) {
        super("There was an internal error. The bot Operator has been notified.");
        LOG.error("The bot is Misconfigured: " + reasonForFailure);
    }

    @Override
    public boolean shouldLog() {
        return true;
    }

    public ServerError(String reasonForFailure, Throwable cause) {
        super("There was an internal error. The bot Operator has been notified.");
        LOG.error("The bot threw an Internal Error: " + reasonForFailure, cause);
    }
}
