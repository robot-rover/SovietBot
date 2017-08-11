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
        int number = Integer.parseInt(cont.getArgs().get(1)) + 1;
        if (number > 100 || number < 2) {
            throw new IncorrectArgumentsException("Your number must be between 1 and 99");
        }
        MessageHistory clear = cont.getMessage().getChannel().getMessageHistory(number + 1);
        BotUtils.bufferRequest(() -> {
            try {
                cont.getMessage().getChannel().bulkDelete(clear);
            } catch (DiscordException | MissingPermissionsException ex) {
                throw BotException.returnException(ex);
            }
        });
    }
}
