package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;

/**
 * Created by Sam on 8/28/2016.
 */
public class Weather extends Command {
    public Weather() {
        permLevel = Permissions.BOTOPERATOR;
        commandName = "weather";
        helpText = "[Coming Soon] Interface for getting the weather in your area.";
    }

    @Override
    public void execute(CommContext cont) {
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("Coming Soon!").withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
