package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import sx.blah.discord.util.MessageBuilder;

import static rr.industries.SovietBot.invite;

/**
 * Created by Sam on 8/28/2016.
 */
public class Invite extends Command {
    public Invite() {
        commandName = "invite";
        helpText = "Sends you an Invite for the bot.";
    }

    @Override
    public void execute(CommContext cont) {
        String message = "Invite Me to Your Server:\n " + invite;
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
