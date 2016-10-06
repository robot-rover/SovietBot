package rr.industries.Exceptions;

import java.util.Optional;

/**
 * @author Sam
 */
public class IncorrectArgumentsException extends BotException {

    public IncorrectArgumentsException(String message) {
        super("Your arguments are incorrect: " + message);
    }

    public IncorrectArgumentsException() {
        super("Your arguments are incorrect");
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.empty();
    }
}
