package rr.industries.commands;

import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.util.*;

@CommandInfo(
        commandName = "echo",
        helpText = "Echoes whatever you say.",
        pmSafe = true
)
public class Echo implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Echos the text that you specify", args = {@Argument(value = Validate.LONGTEXT)})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        return cont.getMessage().getMessage().getChannel().flatMap(v -> v.createMessage(cont.getConcatArgs(1))).then();
    }
}
