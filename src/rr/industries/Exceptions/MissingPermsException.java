package rr.industries.Exceptions;

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

    public MissingPermsException(String action, Permissions perm) {
        super("You need to be a" + BotUtils.startsWithVowel(perm.title, "n **", " **") + "** (" + perm.level + ") to " + action + "!");
        this.neededPerm = perm;
    }

    @Override
    public Optional<String> criticalMessage() {
        return Optional.empty();
    }
}
