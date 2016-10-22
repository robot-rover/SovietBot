package rr.industries.exceptions;

import java.util.Optional;

/**
 * @author Sam
 */
public class PMNotSupportedException extends BotException {

    /**
     * This Command does not support PMs
     */
    public PMNotSupportedException() {
        super("This Command does not support PMs");
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.empty();
    }
}
