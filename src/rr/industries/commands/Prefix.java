package rr.industries.commands;

import rr.industries.util.*;

@CommandInfo(
        commandName = "prefix",
        helpText = "Changes the character(s) of the bot",
        permLevel = Permissions.BOTOPERATOR,
        pmSafe = true
)
//todo: Guild specific command character
public class Prefix implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "The command character is changed to the value you specify", args = {Arguments.TEXT})})
    public void execute(CommContext cont) {
        cont.getActions().getConfig().commChar = cont.getArgs().get(1);
        cont.getActions().channels().sendMessage(cont.builder().withContent("Command Prefix changed to `" + cont.getArgs().get(1) + "`"));
    }
}
