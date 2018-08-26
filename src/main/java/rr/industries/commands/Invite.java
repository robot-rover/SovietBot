package rr.industries.commands;

import rr.industries.Information;
import rr.industries.exceptions.BotException;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;

@CommandInfo(
        commandName = "invite",
        helpText = "Sends you an Invite for SovietBot"
)
public class Invite implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Clicking the link will invite the bot to your server", args = {})})
    public void execute(CommContext cont) throws BotException {
        String message = "Invite Me to Your Server:\n " + Information.invite;
        cont.getActions().channels().sendMessage(cont.builder().withContent(message));
    }
}
