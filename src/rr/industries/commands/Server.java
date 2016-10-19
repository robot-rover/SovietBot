package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.util.*;
import rr.industries.util.sql.PrefixTable;

/**
 * @author Sam
 */

@CommandInfo(commandName = "server", helpText = "set various server settings", permLevel = Permissions.ADMIN)
public class Server implements Command {

    @SubCommand(name = "prefix", Syntax = {@Syntax(helpText = "set the Command Character for this server", args = {Arguments.TEXT})})
    public void prefix(CommContext cont) throws BotException {
        String prefix = cont.getArgs().get(2);
        cont.getActions().getTable(PrefixTable.class).setPrefix(cont.getMessage().getGuild(), prefix);
        cont.getActions().channels().sendMessage(cont.builder().withContent("Changed Prefix to " + prefix));
    }
}
