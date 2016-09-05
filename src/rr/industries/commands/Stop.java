package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "stop",
        helpText = "Shuts down SovietBot",
        permLevel = Permissions.BOTOPERATOR,
        deleteMessage = false
)
public class Stop implements Command {
    @Override
    public void execute(CommContext cont) {
        if (cont != null) {
            if (!cont.getMessage().getMessage().getAuthor().getID().equals("141981833951838208")) {
                BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("Communism marches on!").withChannel(cont.getMessage().getMessage().getChannel()));
                return;
            }
            if (!cont.getMessage().getMessage().getChannel().isPrivate()) {
                //todo: stop delete message testing
                /*try {
                    cont.getMessage().getMessage().delete();
                } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
                    LOG.debug("Error while deleting stop command", ex);
                }*/
            }
        }
        BotActions.terminate(false, cont.getClient());
    }
}
