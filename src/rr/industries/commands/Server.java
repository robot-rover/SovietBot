package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.util.*;
import rr.industries.util.sql.GreetingTable;
import rr.industries.util.sql.PrefixTable;
import sx.blah.discord.util.MessageBuilder;

import java.util.Optional;

/**
 * @author Sam
 */

@CommandInfo(commandName = "server", helpText = "set various server settings", permLevel = Permissions.ADMIN)
public class Server implements Command {

    @SubCommand(name = "prefix", Syntax = {@Syntax(helpText = "set the Command Character for this server", args = {Arguments.TEXT})})
    public void prefix(CommContext cont) throws BotException {
        String prefix = cont.getArgs().get(2);
        cont.getActions().getTable(PrefixTable.class).setPrefix(cont.getMessage().getGuild(), prefix);
        cont.getActions().channels().sendMessage(cont.builder().withContent("Changed Prefix to `" + prefix + "`"));
    }

    @SubCommand(name = "leave", Syntax = {@Syntax(helpText = "set the message sent when a user leaves. \"%user\" is changed to the user's name. \"clear\" removes it", args = {Arguments.LONGTEXT})})
    public void leave(CommContext cont) throws BotException {
        String message = cont.getConcatArgs(2);
        String output = "Changed Leave Message";
        if (message.equals("clear")) {
            message = null;
            output = "Removed Leave Message";
        }
        cont.getActions().getTable(GreetingTable.class).setLeaveMessage(cont.getMessage().getGuild(), message);
        cont.getActions().channels().sendMessage(cont.builder().withContent(output));
    }

    @SubCommand(name = "join", Syntax = {@Syntax(helpText = "set the message sent when a user joins. \"%user\" is changed to the user's name. \"clear\" removes it", args = {Arguments.LONGTEXT})})
    public void join(CommContext cont) throws BotException {
        String message = cont.getConcatArgs(2);
        String output = "Changed Join Message";
        if (message.equals("clear")) {
            message = null;
            output = "Removed Join Message";
        }
        cont.getActions().getTable(GreetingTable.class).setJoinMessage(cont.getMessage().getGuild(), message);
        cont.getActions().channels().sendMessage(cont.builder().withContent(output));
    }

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Shows Current Settings", args = {})})
    public void execute(CommContext cont) throws BotException {
        MessageBuilder message = cont.builder();
        message.appendContent("Command Character: ").appendContent(cont.getActions().getConfig().commChar).appendContent("\n");
        Optional<String> joinMessage = cont.getActions().getTable(GreetingTable.class).getJoinMessage(cont.getMessage().getGuild());
        message.appendContent("Join Message: ").appendContent((joinMessage.map(s -> s.replace("%user", "`USERNAME`")).orElse("*Not Set*"))).appendContent("\n");
        Optional<String> leaveMessage = cont.getActions().getTable(GreetingTable.class).getLeaveMessage(cont.getMessage().getGuild());
        message.appendContent("Leave Message: ").appendContent((leaveMessage.map(s -> s.replace("%user", "`USERNAME`")).orElse("*Not Set*"))).appendContent("\n");
        cont.getActions().channels().sendMessage(message);
    }
}
