package rr.industries.exceptions;

/**
 * @author Sam
 */
public class IncorrectArgumentsException extends BotException {

    /**
     * Your arguments are incorrect: (message)
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
    public boolean shouldLog() {
        return false;
    }
}
