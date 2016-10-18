package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.InternalError;
import rr.industries.util.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageList;
import sx.blah.discord.util.MissingPermissionsException;

@CommandInfo(
        commandName = "purge",
        helpText = "Deletes Messages from a text Channel",
        permLevel = Permissions.MOD,
        deleteMessage = false
)
public class Purge implements Command {
    //    @SuppressWarnings("CollectionAddedToSelf")
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Deletes the number off messages you specify", args = {Arguments.NUMBER})})
    public void execute(CommContext cont) throws BotException {

        MessageList clear;
        int number = Integer.parseInt(cont.getArgs().get(1)) + 1;
        if (number > 100 || number < 2) {
            throw new IncorrectArgumentsException("Your number must be between 1 and 99");
        }
        clear = new MessageList(cont.getClient(), cont.getMessage().getChannel(), number);
        BotUtils.bufferRequest(() -> {
            try {
                for (int i = 0; i < 5 && clear.size() < number; i++) {
                    int loading = number - clear.size();
                    clear.load(loading);
                    LOG.info("Attempted to load {} messages. MessageList now has {} messages.", loading, clear.size());
                }
                if (clear.size() < number)
                    throw new InternalError("Could not load required messages after trying 5 times (" + number + " required, " + clear.size() + " loaded)");
                clear.bulkDelete(clear);
            } catch (DiscordException | MissingPermissionsException ex) {
                throw BotException.returnException(ex);
            }
        });
    }
}
