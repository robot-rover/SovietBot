package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "coin",
        helpText = "Flips a coin."
)
public class Coin implements Command {
    @Override
    public void execute(CommContext cont) {
        String message;
        if (rn.nextBoolean()) {
            message = "Heads";
        } else {
            message = "Tails";
        }
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
