package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;


@CommandInfo(
        commandName = "weather",
        helpText = "[Coming Soon] Interface for getting the weather in your area.",
        permLevel = Permissions.BOTOPERATOR
)
public class Weather implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "[Coming Soon]", args = {})})
    public void execute(CommContext cont) {
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Coming Soon!").withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
