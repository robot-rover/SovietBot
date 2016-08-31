package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.Logging;
import rr.industries.util.Parsable;
import rr.industries.util.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageList;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Created by Sam on 8/28/2016.
 */
public class Purge extends Command {
    public Purge() {
        permLevel = Permissions.MOD;
        commandName = "purge";
        helpText = "Deletes Messages from a text Channel.";
        deleteMessage = false;
    }

    @Override
    public void execute(CommContext cont) {

        MessageList clear;
        if (cont.getArgs().size() < 2) {
            Logging.missingArgs(cont.getMessage(), "purge", cont.getArgs(), LOG);
            return;
        }
        if (!Parsable.tryInt(cont.getArgs().get(1))) {
            Logging.wrongArgs(cont.getMessage(), "purge", cont.getArgs(), LOG);
            return;
        }
        int number = Integer.parseInt(cont.getArgs().get(1)) + 1;
        if (number > 100) {
            Logging.customException(cont.getMessage(), "purge", "You cannont delete more than 100 messages at a time (" + number + ")", null, LOG);
            return;
        }
        clear = new MessageList(cont.getClient(), cont.getMessage().getMessage().getChannel(), number);
        try {
            clear.bulkDelete(clear);
        } catch (DiscordException ex) {
            Logging.error(cont.getMessage().getMessage().getGuild(), "purge", ex, LOG);
        } catch (MissingPermissionsException ex) {
            Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "purge", ex, LOG);
        } catch (RateLimitException ex) {
            Logging.rateLimit(ex, this::execute, cont, LOG);
        }
    }
}
