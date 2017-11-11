package rr.industries.commands;

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
    public void execute(CommContext cont) {
        cont.getActions().channels().sendMessage(cont.builder().withContent((rn.nextBoolean() ? "Heads" : "Tails")));
    }
}
