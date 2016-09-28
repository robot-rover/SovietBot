package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "stop",
        helpText = "Shuts down SovietBot",
        permLevel = Permissions.BOTOPERATOR,
        deleteMessage = false
)
public class Stop implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Stops the process running the bot", args = {})})
    public void execute(CommContext cont) {
        if (cont != null) {
            if (!cont.getMessage().getAuthor().getID().equals("141981833951838208")) {
                cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withContent("Communism marches on!").withChannel(cont.getMessage().getChannel()));
                return;
            }
            if (!cont.getMessage().getChannel().isPrivate()) {
                cont.getActions().channels().delayDelete(cont.getMessage(), 0);
            }
        }
        cont.getActions().channels().terminate(false);
    }
}
