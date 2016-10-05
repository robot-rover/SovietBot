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
    @SuppressWarnings("CollectionAddedToSelf")
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Deletes the number off messages you specify", args = {Arguments.NUMBER})})
    public void execute(CommContext cont) {

        MessageList clear;
        int number = Integer.parseInt(cont.getArgs().get(1)) + 1;
        if (number > 100) {
            cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withContent("Cannot delete more than 99 messages at a time (" + number + ")")
                    .withChannel(cont.getMessage().getChannel()));
            return;
        }
        clear = new MessageList(cont.getClient(), cont.getMessage().getChannel(), number);
        RequestBuffer.request(() -> {
            try {
                clear.bulkDelete(clear);
            } catch (DiscordException ex) {
                cont.getActions().channels().customException("Purge", ex.getErrorMessage(), ex, LOG, true);
            } catch (MissingPermissionsException ex) {
                cont.getActions().channels().missingPermissions(cont.getMessage().getChannel(), ex);
            }
        });
    }
}
