package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.util.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageHistory;
import sx.blah.discord.util.MissingPermissionsException;

@CommandInfo(
        commandName = "purge",
        helpText = "Deletes Messages from a text Channel",
        permLevel = Permissions.MOD
)
public class Purge implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Deletes the number off messages you specify", args = {@Argument(description = "# of Messages", value = Validate.NUMBER)})})
    public void execute(CommContext cont) throws BotException {
        int number = Integer.parseInt(cont.getArgs().get(1));
        if (number > 99 || number < 1) {
            throw new IncorrectArgumentsException("Your number must be between 1 and 99");
        }
        MessageHistory clear = cont.getMessage().getChannel().getMessageHistory(number + 1);
        int initSize = clear.size();
        BotUtils.bufferRequest(() -> {
            try {
                int messagesDeleted = cont.getMessage().getChannel().bulkDelete(clear).size();
                LOG.debug("Messages attempted to delete: {}, initial list size: {}, final list size: {}, messages Deleted: {}", number, initSize, clear.size(), messagesDeleted);
            } catch (DiscordException | MissingPermissionsException ex) {
                throw BotException.returnException(ex);
            }
        });
    }
}
