package rr.industries.commands;

import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;

@CommandInfo(
        commandName = "coin",
        helpText = "Flips a coin.",
        pmSafe = true
)
public class Coin implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Says either heads or tails", args = {})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        return cont.getMessage().getMessage().getChannel().flatMap(v -> v.createMessage(rn.nextBoolean() ? "Heads" : "Tails")).then();
    }
}
