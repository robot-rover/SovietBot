package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "coin",
        helpText = "Flips a coin."
)
public class Coin implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Says either heads or tails", args = {})})
    public void execute(CommContext cont) {
        String message;
        if (rn.nextBoolean()) {
            message = "Heads";
        } else {
            message = "Tails";
        }
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
