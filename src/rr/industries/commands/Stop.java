package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

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
                cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Communism marches on!").withChannel(cont.getMessage().getMessage().getChannel()));
                return;
            }
            if (!cont.getMessage().getMessage().getChannel().isPrivate()) {
                //todo: stop delete message testing
                try {
                    cont.getMessage().getMessage().delete();
                } catch (MissingPermissionsException ex) {
                    cont.getActions().missingPermissions(cont.getMessage().getMessage().getChannel(), ex);
                } catch (RateLimitException ex) {
                    //todo: implement ratelimit
                } catch (DiscordException ex) {
                    cont.getActions().customException("Stop", ex.getErrorMessage(), ex, LOG, true);
                }
            }
        }
        cont.getActions().terminate(false);
    }
}
