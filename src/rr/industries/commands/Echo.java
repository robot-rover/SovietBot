package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "echo",
        helpText = "Echoes whatever you say."
)
public class Echo implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Sends back the text that you specify", args = {Arguments.LONGTEXT})})
    public void execute(CommContext cont) {
        MessageBuilder message = cont.builder().withContent(cont.getConcatArgs(1));
        if (message.getContent().length() > 0) {
            cont.getActions().channels().sendMessage(message);
        }
    }
}
