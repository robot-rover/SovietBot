package rr.industries.commands;

import net.aksingh.owmjapis.OpenWeatherMap;
import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;


@CommandInfo(
        commandName = "weather",
        helpText = "[Coming Soon] Interface for getting the weather in your area.",
        permLevel = Permissions.BOTOPERATOR
)
public class Weather implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Displays the current weather in your area", args = {})
    })
    public void execute(CommContext cont) {
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Coming Soon!").withChannel(cont.getMessage().getMessage().getChannel()));
    }

    @SubCommand(name = "set", Syntax = {@Syntax(helpText = "Sets your zip code", args = {Arguments.NUMBER})})
    public void set(CommContext cont) {
        OpenWeatherMap map = new OpenWeatherMap(OpenWeatherMap.Units.IMPERIAL, cont.getActions().getConfig().owmKey);
    }
}
