package rr.industries.exceptions;

import java.util.Optional;

/**
 * @author Sam
 */
public class IncorrectArgumentsException extends BotException {

    /**
     * Your arguments are incorrect: (message)
     *
     * @param message
     */
    public IncorrectArgumentsException(String message) {
        super("Your arguments are incorrect: " + message);
    }

    /**
     * Your arguments are incorrect
     */
    public IncorrectArgumentsException() {
        super("Your arguments are incorrect");
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.empty();
    }
}
