package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

@CommandInfo(
        commandName = "restart",
        helpText = "Restarts the bot.",
        permLevel = Permissions.BOTOPERATOR,
        deleteMessage = false
)
public class Restart implements Command {

    @Override
    public void execute(CommContext cont) {

        if (!cont.getMessage().getMessage().getAuthor().getID().equals("141981833951838208")) {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("Communism marches on!").withChannel(cont.getMessage().getMessage().getChannel()));
            return;
        }
        if (!cont.getMessage().getMessage().getChannel().isPrivate()) {
            try {
                cont.getMessage().getMessage().delete();
            } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
                LOG.debug("Error while deleting restart command", ex);
            }
        }
        BotActions.terminate(true, cont.getClient());
    }
}
