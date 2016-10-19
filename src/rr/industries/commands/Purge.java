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
    final static int tries = 2;
    //    @SuppressWarnings("CollectionAddedToSelf")
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Deletes the number off messages you specify", args = {Arguments.NUMBER})})
    public void execute(CommContext cont) throws BotException {
        int number = Integer.parseInt(cont.getArgs().get(1)) + 1;
        if (number > 100 || number < 2) {
            throw new IncorrectArgumentsException("Your number must be between 1 and 99");
        }
        MessageList clear = cont.getMessage().getChannel().getMessages();
        clear.setCacheCapacity(number);
        BotUtils.bufferRequest(() -> {
            try {
                boolean successful = clear.size() == number;
                for (int i = 0; i < tries && !successful; i++) {
                    int loading = number - clear.size();
                    successful = clear.load(loading);
                    LOG.info((successful ? "Succeeded " : "Failed") + " to load/unload {} messages. MessageList now has {} messages.", loading, clear.size());
                }
                if (clear.size() != number)
                    throw new InternalError("Could not load required messages after trying " + tries + " times (" + number + " required, " + clear.size() + " loaded)");
                clear.bulkDelete(clear);
            } catch (DiscordException | MissingPermissionsException ex) {
                throw BotException.returnException(ex);
            }
        });
    }
}
