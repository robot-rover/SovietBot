package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "echo",
        helpText = "Echoes whatever you say."
)
public class Echo implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Sends back the text that you specify", args = {Arguments.TEXT})})
    public void execute(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel())
                .withContent(cont.getConcatArgs());
        if (message.getContent().length() > 0) {
            cont.getActions().sendMessage(message);
        }
    }
}
