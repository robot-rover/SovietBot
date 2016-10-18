package rr.industries.commands;

import rr.industries.util.*;

/**
 * @author Sam
 */

@CommandInfo(commandName = "server", helpText = "set various server settings")
public class Server implements Command {

    @SubCommand(name = "prefix", Syntax = {@Syntax(helpText = "set the Command Character for this server", args = {Arguments.TEXT})})
    public void prefix(CommContext cont) {

    }
}
