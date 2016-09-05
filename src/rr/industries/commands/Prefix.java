package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "prefix",
        helpText = "Changes the character(s) you put in front of commands",
        permLevel = Permissions.BOTOPERATOR
)
public class Prefix implements Command {

    @Override
    public void execute(CommContext cont) {
        if (cont.getArgs().size() >= 2) {
            SovietBot.getBot().config.commChar = cont.getArgs().get(1);
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel())
                    .withContent("Command Prefix changed to `" + cont.getArgs().get(1) + "`"));
        } else {
            Logging.missingArgs(cont.getMessage(), "prefix", cont.getArgs(), LOG);
        }
    }
}
