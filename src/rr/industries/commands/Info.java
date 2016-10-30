package rr.industries.commands;

import rr.industries.Information;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import rr.industries.util.sql.PrefixTable;

@CommandInfo(
        commandName = "info",
        helpText = "Displays basic bot info."
)
public class Info implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Shows you interesting things such as the bots author and invite link", args = {})})
    public void execute(CommContext cont) {
        Information info = cont.getActions().channels().getInfo();
        String message =
                "# **" + info.botName + "** #\n" +
                        "[Created with] " + info.frameName + " version `" + info.frameVersion + "`\n" +
                        "[For help type] `" + cont.getActions().getTable(PrefixTable.class).getPrefix(cont.getMessage().getGuild()) + info.helpCommand + "`\n" +
                        "[Created By] **@" + info.author + "**\n" +
                        "[Invite Link] " + info.invite + "\n" +
                        "[Website] " + info.website;
        cont.getActions().channels().sendMessage(cont.builder().withContent(message));
    }
}