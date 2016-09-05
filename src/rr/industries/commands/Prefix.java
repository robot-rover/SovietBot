package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.Logging;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/4/2016
 */
public class Prefix extends Command {
    public Prefix() {
        permLevel = Permissions.ADMIN;
        commandName = "prefix";
        helpText = "Changes the character(s) you put in front of commands";
        deleteMessage = true;
    }

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
