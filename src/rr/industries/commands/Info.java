package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import sx.blah.discord.util.MessageBuilder;

import static rr.industries.SovietBot.*;

@CommandInfo(
        commandName = "info",
        helpText = "Displays basic bot info."
)
public class Info implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Shows you interesting things such as the bots author and invite link", args = {})})
    public void execute(CommContext cont) {
        String message = "```markdown\n" +
                "# " + botName + " #\n" +
                "Created with " + frameName + " version " + frameVersion + "\n" +
                "[For help type](" + cont.getCommChar() + helpCommand + ")\n" + "This bot was created by <" + author + ">\n" +
                "[Invite Link](" + SovietBot.invite + ")\n" +
                "[Website](" + SovietBot.website + ")\n" +
                "```";
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getChannel()));
    }
}