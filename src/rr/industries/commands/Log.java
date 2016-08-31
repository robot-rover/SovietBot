package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.Logging;
import rr.industries.util.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.IOException;

/**
 * Created by Sam on 8/28/2016.
 */
public class Log extends Command {
    public Log() {
        permLevel = Permissions.BOTOPERATOR;
        commandName = "log";
        helpText = "Brings all current users of a server to you";
    }

    @Override
    public void execute(CommContext cont) {
        String path;
        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).equals("full")) {
            path = "debug.LOG";
        } else {
            path = "events.LOG";
        }
        File file = new File(path);
        try {
            cont.getMessage().getMessage().getChannel().sendFile(file);
        } catch (IOException ex) {
            LOG.warn("Log file not found", ex);
        } catch (MissingPermissionsException ex) {
            Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "log", ex, LOG);
        } catch (RateLimitException ex) {
            Logging.rateLimit(ex, this::execute, cont, LOG);
        } catch (DiscordException ex) {
            Logging.error(cont.getMessage().getMessage().getGuild(), "LOG", ex, LOG);
        }
    }
}
