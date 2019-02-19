package rr.industries.commands;

import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import org.jooq.Record4;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.util.*;
import rr.industries.util.sql.GreetingTable;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sam
 */

@CommandInfo(commandName = "server", helpText = "set various server settings", permLevel = Permissions.ADMIN)
public class Server implements Command {
    @SubCommand(name = "channel", Syntax = {@Syntax(helpText = "sets the Greeting Channel for this server", args = {@Argument(Validate.TEXTCHANNEL)})})
    public Mono<Void> channel(CommContext cont) throws BotException {
        Snowflake guildId = cont.getGuildId();
        Pattern textChannelPattern = Pattern.compile("<#([0-9]{18})>");
        Matcher m = textChannelPattern.matcher(cont.getMessage().getMessage().getContent().orElse(""));
        if(!m.find()) {
            throw new IncorrectArgumentsException("There is no text channel specified");
        }
        Snowflake channelId = Snowflake.of(m.group(1));
        Mono<String> content = cont.getMessage().getGuild().flatMap(v -> v.getChannelById(channelId)).cast(TextChannel.class).map(v -> {
            cont.getActions().getTable(GreetingTable.class).setChannel(guildId, channelId);
            return v;
        }).map(v -> "Greeting Channel set to " + v.getName()).defaultIfEmpty("Could not find channel");
        return content.flatMap(v -> cont.getChannel().createMessage(v)).then();
    }

    @SubCommand(name = "prefix", Syntax = {@Syntax(helpText = "set the Command Character for this server", args = {@Argument(description = "Command Character", value = Validate.TEXT)})})
    public Mono<Void> prefix(CommContext cont) throws BotException {
        Snowflake guildId = cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new);
        String prefix = cont.getArgs().get(2);
        cont.getActions().getTable(GreetingTable.class).setPrefix(guildId, prefix);
        return cont.getChannel().createMessage("Changed Prefix to `" + prefix + "`").then();
    }

    @SubCommand(name = "leave", Syntax = {@Syntax(helpText = "set the message sent when a user leaves. \"%user\" is changed to the user's name. \"clear\" removes it", args = {@Argument(description = "Leave Message", value = Validate.LONGTEXT)})})
    public Mono<Void> leave(CommContext cont) throws BotException {
        String message = cont.getConcatArgs(2);
        String output = "Changed Leave Message";
        if (message.equals("clear")) {
            message = null;
            output = "Removed Leave Message";
        }
        cont.getActions().getTable(GreetingTable.class).setLeaveMessage(cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new), message);
        return cont.getChannel().createMessage(output).then();
    }

    @SubCommand(name = "join", Syntax = {@Syntax(helpText = "set the message sent when a user joins. \"%user\" is changed to the user's name. \"clear\" removes it", args = {@Argument(description = "Join Message", value = Validate.LONGTEXT)})})
    public Mono<Void> join(CommContext cont) throws BotException {
        String message = cont.getConcatArgs(2);
        String output = "Changed Join Message";
        if (message.equals("clear")) {
            message = null;
            output = "Removed Join Message";
        }
        cont.getActions().getTable(GreetingTable.class).setJoinMessage(cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new), message);
        return cont.getChannel().createMessage(output).then();
    }

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Shows Current Settings", args = {})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        StringBuilder message = new StringBuilder();
        Snowflake guildId = cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new);
        Optional<Record4<String, Long, String, String>> info = cont.getActions().getTable(GreetingTable.class).getServerInfo(guildId);
        String commChar = info.map(Record4::value1).orElse(cont.getActions().getConfig().commChar);
        Mono<String> greetingChannel = Mono.zip(cont.getMessage().getGuild(), Mono.justOrEmpty(info).filter(v -> v.value2() != null)    .map(Record4::value2))
                .flatMap(v -> v.getT1().getChannelById(Snowflake.of(v.getT2())))
                .cast(TextChannel.class)
                .map(TextChannel::getName)
                .defaultIfEmpty("*Not Set*");
        String joinMessage = info.map(Record4::value3).map(s -> s.replace("%user", "`USERNAME`")).orElse("*Not Set*");
        String leaveMessage = info.map(Record4::value4).map(s -> s.replace("%user", "`USERNAME`")).orElse("*Not Set*");
        return greetingChannel.map(v -> {
            message.append("Command Character: ")
                    .append(commChar)
                    .append("\n");
            message.append("Greeting Channel: ")
                    .append(v)
                    .append("\n");
            message.append("Join Message: ")
                    .append(joinMessage)
                    .append("\n");
            message.append("Leave Message: ")
                    .append(leaveMessage);
            return message;})
            .flatMap(v -> cont.getChannel().createMessage(v.toString()))
            .then();
    }
}
