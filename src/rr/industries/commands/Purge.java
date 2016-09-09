package rr.industries.commands;

import rr.industries.util.BotUtils;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.util.*;

@CommandInfo(
        commandName = "purge",
        helpText = "Delets Messages from a text Channel",
        permLevel = Permissions.MOD,
        deleteMessage = false
)
public class Purge implements Command {

    @Override
    public void execute(CommContext cont) {

        MessageList clear;
        if (cont.getArgs().size() < 2) {
            cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
            return;
        }
        if (!BotUtils.tryInt(cont.getArgs().get(1))) {
            cont.getActions().wrongArgs(cont.getMessage().getMessage().getChannel());
            return;
        }
        int number = Integer.parseInt(cont.getArgs().get(1)) + 1;
        if (number > 100) {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Cannot delete more than 99 messages at a time (" + number + ")")
                    .withChannel(cont.getMessage().getMessage().getChannel()));
            return;
        }
        clear = new MessageList(cont.getClient(), cont.getMessage().getMessage().getChannel(), number);
        try {
            clear.bulkDelete(clear);
        } catch (DiscordException ex) {
            cont.getActions().customException("Purge", ex.getErrorMessage(), ex, LOG, true);
        } catch (MissingPermissionsException ex) {
            cont.getActions().missingPermissions(cont.getMessage().getMessage().getChannel(), ex);
        } catch (RateLimitException ex) {
            //todo: implement ratelimit
        }
    }
}
