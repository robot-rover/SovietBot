package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "prefix",
        helpText = "Changes the character(s) of the bot",
        permLevel = Permissions.BOTOPERATOR
)
//todo: Guild specific command character
public class Prefix implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "The command character is changed to the value you specify", args = {@ArgSet(arg = Arguments.TEXT)})})
    public void execute(CommContext cont) {
        if (cont.getArgs().size() >= 2) {
            cont.getActions().getConfig().commChar = cont.getArgs().get(1);
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel())
                    .withContent("Command Prefix changed to `" + cont.getArgs().get(1) + "`"));
        } else {
            cont.getActions().missingArgs(cont.getMessage().getChannel());
        }
    }
}
