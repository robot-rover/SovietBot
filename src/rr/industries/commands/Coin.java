package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import sx.blah.discord.util.MessageBuilder;

/**
 * Created by Sam on 8/28/2016.
 */
public class Coin extends Command {
    public Coin() {
        commandName = "coin";
        helpText = "Flips a coin.";
    }

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
