package rr.industries.commands;

import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.util.*;

@CommandInfo(
        commandName = "stop",
        helpText = "Shuts down SovietBot",
        permLevel = Permissions.BOTOPERATOR,
        pmSafe = true
)
public class Stop implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Stops the process running the bot", args = {})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        cont.getActions().terminate();
        return Mono.empty();
    }
}
