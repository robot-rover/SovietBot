package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

@CommandInfo(
        commandName = "restart",
        helpText = "Restarts the bot.",
        permLevel = Permissions.BOTOPERATOR,
        deleteMessage = false
)
public class Restart implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "The process running the bot stops and restarts", args = {})})
    public void execute(CommContext cont) {
        if (!cont.getMessage().getMessage().getChannel().isPrivate()) {
            try {
                cont.getMessage().getMessage().delete();
            } catch (RateLimitException | DiscordException ex) {
                LOG.debug("Error while deleting restart command", ex);
            } catch (MissingPermissionsException ex) {
                //fail Silently
            }
        }
        cont.getActions().terminate(true);
    }
}
