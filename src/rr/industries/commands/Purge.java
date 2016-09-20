package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.*;

@CommandInfo(
        commandName = "purge",
        helpText = "Deletes Messages from a text Channel",
        permLevel = Permissions.MOD,
        deleteMessage = false
)
public class Purge implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Deletes the number off messages you specify", args = {@ArgSet(arg = Arguments.NUMBER)})})
    public void execute(CommContext cont) {

        MessageList clear;
        if (cont.getArgs().size() < 2) {
            cont.getActions().missingArgs(cont.getMessage().getChannel());
            return;
        }
        if (!BotUtils.tryInt(cont.getArgs().get(1))) {
            cont.getActions().wrongArgs(cont.getMessage().getChannel());
            return;
        }
        int number = Integer.parseInt(cont.getArgs().get(1)) + 1;
        if (number > 100) {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Cannot delete more than 99 messages at a time (" + number + ")")
                    .withChannel(cont.getMessage().getChannel()));
            return;
        }
        clear = new MessageList(cont.getClient(), cont.getMessage().getChannel(), number);
        try {
            clear.bulkDelete(clear);
        } catch (DiscordException ex) {
            cont.getActions().customException("Purge", ex.getErrorMessage(), ex, LOG, true);
        } catch (MissingPermissionsException ex) {
            cont.getActions().missingPermissions(cont.getMessage().getChannel(), ex);
        } catch (RateLimitException ex) {
            //todo: implement ratelimit
        }
    }
}
