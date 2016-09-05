package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import sx.blah.discord.util.MessageBuilder;

import static rr.industries.SovietBot.*;

@CommandInfo(
        commandName = "info",
        helpText = "Displays basic bot info."
)
public class Info implements Command {
    @Override
    public void execute(CommContext cont) {
        String message = "```markdown\n" +
                "# " + botName + " version " + version + " #\n" +
                "Created with " + frameName + " version " + frameVersion + "\n" +
                "[For help type](" + cont.getCommChar() + helpCommand + ")\n" + "This bot was created by <" + author + ">\n" +
                "[Invite Link](" + SovietBot.invite + ")" +
                "```";
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
    }
}