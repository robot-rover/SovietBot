package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.MessageBuilder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by Sam on 8/28/2016.
 */
public class Uptime extends Command {

    public Uptime() {
        commandName = "uptime";
        helpText = "Shows you how long the bot has been running.";
    }

    @Override
    public void execute(CommContext cont) {

        LocalDateTime launchTime = Discord4J.getLaunchTime();
        LocalDateTime current = LocalDateTime.now();
        long hours = launchTime.until(current, ChronoUnit.HOURS);
        launchTime = launchTime.plusHours(hours);
        long minutes = launchTime.until(current, ChronoUnit.MINUTES);
        launchTime = launchTime.plusMinutes(minutes);
        long seconds = launchTime.until(current, ChronoUnit.SECONDS);
        String message = "`SovietBot has been running for " + Long.toString(hours) + " hours, " + Long.toString(minutes) + " minutes, and " + Long.toString(seconds) + " seconds.`";
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
