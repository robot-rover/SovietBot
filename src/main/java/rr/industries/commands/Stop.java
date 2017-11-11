package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.util.*;

@CommandInfo(
        commandName = "stop",
        helpText = "Shuts down SovietBot",
        permLevel = Permissions.BOTOPERATOR,
        pmSafe = true
)
public class Stop implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Stops the process running the bot", args = {})})
    public void execute(CommContext cont) throws BotException {
        cont.getActions().channels().terminate(false);
    }
}
