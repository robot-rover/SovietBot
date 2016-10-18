package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.exceptions.BotException;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import rr.industries.util.sql.PrefixTable;

import static rr.industries.SovietBot.*;

@CommandInfo(
        commandName = "info",
        helpText = "Displays basic bot info."
)
public class Info implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Shows you interesting things such as the bots author and invite link", args = {})})
    public void execute(CommContext cont) throws BotException {
        String message =
                "# **" + botName + "** #\n" +
                        "[Created with] " + frameName + " version `" + frameVersion + "`\n" +
                        "[For help type] `" + cont.getActions().getTable(PrefixTable.class).getPrefix(cont.getMessage().getGuild()) + helpCommand + "`\n" +
                        "[Created By] **@" + author + "**\n" +
                        "[Invite Link] " + SovietBot.invite + "\n" +
                        "[Website] " + SovietBot.website;
        cont.getActions().channels().sendMessage(cont.builder().withContent(message));
    }
}