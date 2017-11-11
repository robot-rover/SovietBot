package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "echo",
        helpText = "Echoes whatever you say.",
        pmSafe = true
)
public class Echo implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Echos the text that you specify", args = {@Argument(value = Validate.LONGTEXT)})})
    public void execute(CommContext cont) {
        MessageBuilder message = cont.builder().withContent(cont.getConcatArgs(1));
        if (message.getContent().length() > 0) {
            cont.getActions().channels().sendMessage(message);
        }
    }
}
