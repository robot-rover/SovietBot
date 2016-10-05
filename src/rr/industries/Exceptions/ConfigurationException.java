package rr.industries.Exceptions;

/**
 * @author Sam
 */
public class ConfigurationException extends BotException {
    private String reasonForFailure;

    public ConfigurationException(String reasonForFailure) {
        super("Bot is Misconfigured: " + reasonForFailure);
        this.reasonForFailure = reasonForFailure;
    }

    @Override
    public boolean isCritical() {
        return true;
    }
}
