package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import sx.blah.discord.Discord4J;

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
        long days = launchTime.until(current, ChronoUnit.DAYS);
        launchTime = launchTime.plusDays(days);
        long hours = launchTime.until(current, ChronoUnit.HOURS);
        launchTime = launchTime.plusHours(hours);
        long minutes = launchTime.until(current, ChronoUnit.MINUTES);
        launchTime = launchTime.plusMinutes(minutes);
        long seconds = launchTime.until(current, ChronoUnit.SECONDS);
        boolean multipleTerms = false;
        StringBuilder messageBuild = new StringBuilder("`SovietBot has been running for ");
        if (days > 0) {
            messageBuild.append(Long.toString(days)).append(" days, ");
            multipleTerms = true;
        }
        if (hours > 0) {
            messageBuild.append(Long.toString(hours)).append(" hours, ");
            multipleTerms = true;
        }
        if (minutes > 0) {
            messageBuild.append(Long.toString(minutes)).append(" minutes, ");
            multipleTerms = true;
        }
        if (multipleTerms)
            messageBuild.append("and ");
        messageBuild.append(Long.toString(seconds)).append(" seconds.`");
        cont.getActions().channels().sendMessage(cont.builder().withContent(messageBuild.toString()));
    }
}
