package rr.industries.Exceptions;

import rr.industries.util.BotUtils;
import rr.industries.util.Permissions;

/**
 * @author Sam
 */
public class AlreadyExistsException extends BotException {
    private Permissions neededPerm;

    public AlreadyExistsException(String name, String type, String reason) {
        super("A" + BotUtils.startsWithVowel(type, "n ", " ") + " named `" + name + "` already exists, and could not be overwritten because " + reason);
    }

    public AlreadyExistsException(String name, String type, Permissions neededPerm) {
        super("A" + BotUtils.startsWithVowel(type, "n ", " ") + " named `" + name + "` already exists, and was not overwritten because you are not a " + neededPerm.formatted);
        this.neededPerm = neededPerm;
    }

    public AlreadyExistsException(String name, String type) {
        super("A" + BotUtils.startsWithVowel(type, "n ", " ") + " named `" + name + "` already exists");
    }

    /**
     * The perm needed to complete the action. May be null
     */
    public Permissions getNeededPerm() {
        return neededPerm;
    }

    @Override
    public boolean isCritical() {
        return false;
    }
}
