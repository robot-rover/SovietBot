package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.util.*;

@CommandInfo(
        commandName = "restart",
        helpText = "Restarts the bot.",
        permLevel = Permissions.BOTOPERATOR,
        deleteMessage = false,
        pmSafe = true
)
public class Restart implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "The process running the bot stops and restarts", args = {})})
    public void execute(CommContext cont) throws BotException {
        cont.getActions().channels().terminate(true);
    }
}
