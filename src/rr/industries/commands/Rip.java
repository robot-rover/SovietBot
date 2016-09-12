package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

import java.util.regex.Pattern;

@CommandInfo(
        commandName = "rip",
        helpText = "Gives you a link to a place where everything is Rip",
        permLevel = Permissions.REGULAR
)
public class Rip implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Specify the thing(s) that are rip", args = {Arguments.TEXT})})
    public void execute(CommContext cont) {
        String channelName;
        Pattern p = Pattern.compile("<@!?[0-9]{18}>");
        Pattern start = Pattern.compile(".*<@!?");
        Pattern end = Pattern.compile(">.*");
        Pattern space = Pattern.compile(" ");
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel()).withContent("http://www.ripme.xyz/");
        if (cont.getArgs().size() >= 2) {
            for (int i = 1; i < cont.getArgs().size(); i++) {
                if (p.matcher(cont.getArgs().get(i)).find()) {
                    String id = cont.getArgs().get(i);
                    id = start.matcher(id).replaceFirst("");
                    id = end.matcher(id).replaceFirst("");
                    message.appendContent(cont.getClient().getUserByID(id).getDisplayName(cont.getMessage().getMessage().getGuild()));
                    message.withContent(space.matcher(message.getContent()).replaceAll("%20"));
                } else {
                    message.appendContent(cont.getArgs().get(i));
                }
                if (i < cont.getArgs().size() - 1)
                    message.appendContent("%20");
            }
        }
        cont.getActions().sendMessage(message);
    }
}
