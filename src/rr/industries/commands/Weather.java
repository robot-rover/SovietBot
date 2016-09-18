package rr.industries.commands;

import rr.industries.CommandList;
import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;


@CommandInfo(
        commandName = "weather",
        helpText = "[Coming Soon] Interface for getting the weather in your area.",
        permLevel = Permissions.BOTOPERATOR
)
public class Weather implements Command {
    static {
        CommandList.defaultCommandList.add(Weather.class);
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Displays the current weather in your area", args = {})
    })
    public void execute(CommContext cont) {
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Coming Soon!").withChannel(cont.getMessage().getMessage().getChannel()));
    }

    @SubCommand(name = "set", Syntax = {@Syntax(helpText = "Sets your zip code", args = {Arguments.NUMBER})})
    public void set(CommContext cont) {
        //OpenWeatherMap map = new OpenWeatherMap()
    }
}
