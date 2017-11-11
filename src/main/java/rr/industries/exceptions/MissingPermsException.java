package rr.industries.exceptions;

import rr.industries.util.BotUtils;
import rr.industries.util.Permissions;

import java.util.Optional;

/**
 * @author Sam
 */
public class MissingPermsException extends BotException {
    private Permissions neededPerm;

    public Permissions getNeededPerm() {
        return neededPerm;
    }

    private String action;

    /**
     * You need to be a(n) (perm.formatted) to (action)!
     */
    public MissingPermsException(String action, Permissions perm) {
        super("You need to be a" + BotUtils.startsWithVowel(perm.title, "n ", " ", false) + perm.formatted + " to " + action + "!");
        this.neededPerm = perm;
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.empty();
    }
}
