package rr.industries.exceptions;

import rr.industries.util.BotUtils;

/**
 * @author Sam
 */
public class NotFoundException extends BotException {
    public NotFoundException(String message) {
        super("Could not find " + message);
    }

    public NotFoundException(String type, String name) {
        super("Could not find a" + BotUtils.startsWithVowel(type, "n ", " ", true) + " called `" + name + "`");
    }

    @Override
    public boolean shouldLog() {
        return false;
    }
}
