package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
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
            cont.getConfig().commChar = cont.getArgs().get(1);
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel())
                    .withContent("Command Prefix changed to `" + cont.getArgs().get(1) + "`"));
        } else {
            cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
        }
    }
}
