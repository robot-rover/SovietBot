package rr.industries.commands;

import reactor.core.publisher.Mono;
import rr.industries.Information;
import rr.industries.exceptions.BotException;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@CommandInfo(
        commandName = "uptime",
        helpText = "Shows you how long the bot has been running."
)
public class Uptime implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Displays the hours, minutes, and seconds since the bot was started", args = {})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        Instant launchTime = Information.launchTime;
        Instant current = Instant.now();
        long days = launchTime.until(current, ChronoUnit.DAYS);
        launchTime = launchTime.plus(days, ChronoUnit.DAYS);
        long hours = launchTime.until(current, ChronoUnit.HOURS);
        launchTime = launchTime.plus(hours, ChronoUnit.HOURS);
        long minutes = launchTime.until(current, ChronoUnit.MINUTES);
        launchTime = launchTime.plus(minutes, ChronoUnit.MINUTES);
        long seconds = launchTime.until(current, ChronoUnit.SECONDS);
        boolean multipleTerms = false;
        StringBuilder messageBuild = new StringBuilder("`SovietBot has been running for ");
        if (days > 0) {
            messageBuild.append(days).append(" days, ");
            multipleTerms = true;
        }
        if (hours > 0) {
            messageBuild.append(hours).append(" hours, ");
            multipleTerms = true;
        }
        if (minutes > 0) {
            messageBuild.append(minutes).append(" minutes, ");
            multipleTerms = true;
        }
        if (multipleTerms)
            messageBuild.append("and ");
        messageBuild.append(seconds).append(" seconds.`");
        return cont.getChannel().createMessage(messageBuild.toString()).then();
    }
}
