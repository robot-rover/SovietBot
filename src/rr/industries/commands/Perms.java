package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Parsable;
import rr.industries.util.Permissions;
import rr.industries.util.sql.SQLUtils;
import sx.blah.discord.util.MessageBuilder;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/6/2016
 */
@CommandInfo(
        commandName = "perms",
        helpText = "Gets and Sets permissions for users of a server")
public class Perms implements Command {
    @Override
    public void execute(CommContext cont) {
        if (cont.getArgs().size() >= 3 && cont.getArgs().get(1).equals("set")) {
            if (Parsable.tryInt(cont.getArgs().get(2))) {
                int setPerm = Integer.parseInt(cont.getArgs().get(2));
                if (setPerm >= 0 && setPerm <= 4) {
                    SQLUtils.updatePerms(cont.getMessage().getMessage().getAuthor().getID(), cont.getMessage().getMessage().getGuild().getID(), Permissions.values()[setPerm], cont.getActions().getSQL(), cont.getActions());
                    cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel())
                            .withContent("Permissions Changed to " + Permissions.values()[setPerm].title + " *(Level " + Permissions.values()[setPerm].level + ")*"));
                }
            }
        } else {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel()).withContent(cont.getCallerPerms().title + " *(Level " + cont.getCallerPerms().level + ")*"));
        }
    }
}
