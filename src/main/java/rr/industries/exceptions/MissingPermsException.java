package rr.industries.exceptions;

import rr.industries.util.BotUtils;
import rr.industries.util.Permissions;

/**
 * @author Sam
 */
public class MissingPermsException extends BotException {

    /**
     * You need to be a(n) (perm.formatted) to (action)!
     */
    public MissingPermsException(String action, Permissions perm) {
        super("You need to be a" + BotUtils.startsWithVowel(perm.title, "n ", " ", false) + perm.formatted + " to " + action + "!");
    }

    @Override
    public boolean shouldLog() {
        return false;
    }
}
