package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.MessageBuilder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@CommandInfo(
        commandName = "uptime",
        helpText = "Shows you how long the bot has been running."
)
public class Uptime implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Displays the hours, minutes, and seconds since the bot was started", args = {})})
    public void execute(CommContext cont) {

        LocalDateTime launchTime = Discord4J.getLaunchTime();
        LocalDateTime current = LocalDateTime.now();
        long hours = launchTime.until(current, ChronoUnit.HOURS);
        launchTime = launchTime.plusHours(hours);
        long minutes = launchTime.until(current, ChronoUnit.MINUTES);
        launchTime = launchTime.plusMinutes(minutes);
        long seconds = launchTime.until(current, ChronoUnit.SECONDS);
        String message = "`SovietBot has been running for " + Long.toString(hours) + " hours, " + Long.toString(minutes) + " minutes, and " + Long.toString(seconds) + " seconds.`";
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getChannel()));
    }
}
