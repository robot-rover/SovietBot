package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import sx.blah.discord.util.MessageBuilder;

import static rr.industries.SovietBot.invite;

@CommandInfo(
        commandName = "invite",
        helpText = "Sends you an Invite for SovietBot"
)
public class Invite implements Command {
    public void execute(CommContext cont) {
        String message = "Invite Me to Your Server:\n " + invite;
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
