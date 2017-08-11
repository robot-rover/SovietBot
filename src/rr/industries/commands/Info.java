package rr.industries.commands;

import rr.industries.Information;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import rr.industries.util.sql.PrefixTable;
import sx.blah.discord.util.EmbedBuilder;

@CommandInfo(
        commandName = "info",
        helpText = "Displays basic bot info."
)
public class Info implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Shows you interesting things such as the bots author and invite link", args = {})})
    public void execute(CommContext cont) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.withAuthorIcon(cont.getActions().getConfig().url + "/avatar");
        embed.withAuthorName(Information.botName);
        embed.appendField("Created with", Information.frameName + " version `" + Information.frameVersion + "`", false);
        embed.appendField("For help type", "`" + cont.getActions().getTable(PrefixTable.class).getPrefix(cont.getMessage()) + Information.helpCommand + "`", false);
        embed.appendField("Created By", "**@" + Information.author + "**", false);
        embed.appendField("Website", cont.getActions().getConfig().url, false);
        embed.appendField("Invite Link", Information.invite, false);
        cont.getActions().channels().sendMessage(cont.builder().withEmbed(embed.build()));
    }
}