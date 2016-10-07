package rr.industries.Exceptions;

import rr.industries.util.BotUtils;

import java.util.Optional;

/**
 * @author Sam
 */
public class NotFoundException extends BotException {
    public NotFoundException(String message) {
        super("Could not find " + message);
    }

    public NotFoundException(String type, String name) {
        super("Could not find a" + BotUtils.startsWithVowel(type, "n ", " ") + " called `" + name + "`");
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.empty();
    }
}
