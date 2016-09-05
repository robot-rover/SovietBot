package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;


@CommandInfo(
        commandName = "weather",
        helpText = "[Coming Soon] Interface for getting the weather in your area.",
        permLevel = Permissions.BOTOPERATOR
)
public class Weather implements Command {
    @Override
    public void execute(CommContext cont) {
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("Coming Soon!").withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
