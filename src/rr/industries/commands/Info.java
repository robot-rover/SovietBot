package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import sx.blah.discord.util.MessageBuilder;

import static rr.industries.SovietBot.*;

/**
 * Created by Sam on 8/28/2016.
 */
public class Info extends Command {
    public Info() {
        commandName = "info";
        helpText = "Displays basic bot info.";
    }

    @Override
    public void execute(CommContext cont) {
        String message = "```" + botName + " version " + version + "\n" + "Created with " + frameName + " version " + frameVersion + "\n" + "For help type: " + cont.getCommChar() + helpCommand + "\n" + "This bot was created by " + author + "```";
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
    }
}