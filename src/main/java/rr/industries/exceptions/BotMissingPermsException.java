package rr.industries.exceptions;

import org.apache.commons.text.WordUtils;

import java.util.Optional;

/**
 * @author Sam
 */
public class BotMissingPermsException extends BotException {
    public static String formatDiscordPerms(String perm) {
        return WordUtils.capitalize(perm.replace("_", " ").toLowerCase());
    }

    public BotMissingPermsException(String neededPerm) {
        super(formatDiscordPerms(neededPerm));
    }

    public Optional<String> criticalMessage() {
        return Optional.empty();
    }

    @Override
    public boolean shouldLog() {
        return false;
    }
}
