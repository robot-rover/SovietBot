package rr.industries.exceptions;

import discord4j.core.object.entity.Channel;

/**
 * @author Sam
 */
public class PMNotSupportedException extends BotException {

    /**
     * This Command does not support PMs
     */
    public PMNotSupportedException() {
        super("This Command does not support PMs");
    }

    @Override
    public boolean shouldLog() {
        return false;
    }
}
