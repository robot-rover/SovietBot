package rr.industries.commands;

import rr.industries.util.*;
import rr.industries.util.sql.TimeTable;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;

/**
 * @author robot_rover
 */
@CommandInfo(
        commandName = "time",
        helpText = "Used to see users local time"
)
public class Time implements Command {
    @SubCommand(name = "all", Syntax = {@Syntax(helpText = "Sends you all of the time information for the Guild", args = {})})
    public void all(CommContext cont) {
        MessageBuilder message = cont.builder().withContent("**Times:**");
        for (IUser user : cont.getMessage().getGuild().getUsers()) {
            Optional<String> zone = cont.getActions().getTable(TimeTable.class).getTimeZone(user);
            if (zone.isPresent())
                message.appendContent("\n").appendContent(user.getDisplayName(cont.getMessage().getGuild())).appendContent(" - ")
                        .appendContent(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(ZonedDateTime.now(ZoneId.of(zone.get()))));
        }
        cont.getActions().channels().sendMessage(message);
    }

    @SubCommand(name = "set", Syntax = {@Syntax(helpText = "Set your own timezone", args = {Arguments.TIMEZONE})})
    public void set(CommContext cont) {
            MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        cont.getActions().getTable(TimeTable.class).setTimeZone(cont.getMessage().getAuthor(), ZoneId.of(cont.getArgs().get(2)).normalized().getId());
        message.withContent("Setting your timezone to: \n**" + ZoneId.of(cont.getArgs().get(2)).normalized().getDisplayName(TextStyle.FULL_STANDALONE, Locale.US) + "**");
        cont.getActions().channels().sendMessage(message);
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Show your own time and timezone", args = {}),
            @Syntax(helpText = "Show the mentioned person's current time and timezone", args = {Arguments.MENTION})
    })
    public void execute(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        if (cont.getArgs().size() >= 2 && cont.getMessage().getMentions().size() >= 1) {
            Optional<String> timezone = cont.getActions().getTable(TimeTable.class).getTimeZone(cont.getMessage().getMentions().get(0));
            if (timezone.isPresent()) {
                ZonedDateTime time = ZonedDateTime.now(ZoneId.of(timezone.get()));
                message.withContent(cont.getMessage().getMentions().get(0).getDisplayName(cont.getMessage().getGuild()) + "'s timezone: " + ZoneId.of(timezone.get()).getDisplayName(TextStyle.FULL_STANDALONE, Locale.US) + "\n" +
                        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time));
            } else {
                message.withContent("They have not set your timezone yet.");
            }
        } else {
            Optional<String> timezone = cont.getActions().getTable(TimeTable.class).getTimeZone(cont.getMessage().getAuthor());
            if (timezone.isPresent()) {
                ZonedDateTime time = ZonedDateTime.now(ZoneId.of(timezone.get()));
                message.withContent("Your timezone: " + ZoneId.of(timezone.get()).getDisplayName(TextStyle.FULL_STANDALONE, Locale.US) + "\n" + DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time));
            } else {
                message.withContent("You have not set your timezone yet. Use " + cont.getActions().getConfig().commChar + "time set GMT+(Your Timezone)");
            }
        }
        cont.getActions().channels().sendMessage(message);
    }
}
