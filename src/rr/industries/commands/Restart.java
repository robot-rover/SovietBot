package rr.industries.commands;

import rr.industries.util.*;

@CommandInfo(
        commandName = "restart",
        helpText = "Restarts the bot.",
        permLevel = Permissions.BOTOPERATOR,
        deleteMessage = false
)
public class Restart implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "The process running the bot stops and restarts", args = {})})
    public void execute(CommContext cont) {
        cont.getActions().channels().delayDelete(cont.getMessage(), 0);
        cont.getActions().channels().terminate(true);
    }
}
