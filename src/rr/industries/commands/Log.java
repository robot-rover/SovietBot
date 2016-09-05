package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Logging;
import rr.industries.util.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.IOException;

@CommandInfo(
        commandName = "log",
        helpText = "Uploads the bots log to Chat",
        permLevel = Permissions.BOTOPERATOR
)
public class Log implements Command {
    @Override
    public void execute(CommContext cont) {
        String path;
        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).equals("full")) {
            path = "debug.log";
        } else {
            path = "events.log";
        }
        File file = new File(path);
        try {
            cont.getMessage().getMessage().getChannel().sendFile(file);
        } catch (IOException ex) {
            LOG.warn("Log file not found", ex);
        } catch (MissingPermissionsException ex) {
            Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "Log", ex, LOG);
        } catch (RateLimitException ex) {
            Logging.rateLimit(ex, this::execute, cont, LOG);
        } catch (DiscordException ex) {
            Logging.error(cont.getMessage().getMessage().getGuild(), "Log", ex, LOG);
        }
    }
}
