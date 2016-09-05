package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "echo",
        helpText = "Echoes whatever you say."
)
public class Echo implements Command {
    @Override
    public void execute(CommContext cont) {
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel())
                .withContent(cont.getConcatArgs()));
    }
}
