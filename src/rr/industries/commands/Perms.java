package rr.industries.commands;

import rr.industries.util.*;
import rr.industries.util.sql.PermTable;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.util.List;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/6/2016
 */
//todo: clean up perms, generalize sql
@CommandInfo(
        commandName = "perms",
        helpText = "Gets and Sets permissions for users of a server")
public class Perms implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "View and Set permissions for SovietBot", args = {})})
    public void execute(CommContext cont) {
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel())
                .withContent(cont.getMessage().getAuthor().mention() + " is a" + BotUtils.startsWithVowel(cont.getCallerPerms().title, "n **", " **") + "** (Level " + cont.getCallerPerms().level + ")"));
    }

    @SubCommand(name = "all", Syntax = {@Syntax(helpText = "Lists all permissions for the server", args = {})})
    public void all(CommContext cont) {
        if (cont.getArgs().size() >= 2) {
            List<Entry<String, Integer>> list = cont.getActions().getTable(PermTable.class).getAllPerms(cont.getMessage().getGuild());
            MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel()).withContent("```markdown\n");
            message.appendContent("# Permissions for " + cont.getMessage().getGuild().getName() + " #\n");
            for (Entry<String, Integer> entry : list) {
                message.appendContent(String.format("%-15s", "<" + BotUtils.toPerms(entry.getSecond()).title + ">") + cont.getClient().getUserByID(entry.getFirst()).getDisplayName(cont.getMessage().getGuild()) + "\n");
            }
            cont.getActions().sendMessage(message.appendContent("```"));

        } else {
            cont.getActions().missingArgs(cont.getMessage().getChannel());
        }
    }

    @SubCommand(name = "set", Syntax = {
            @Syntax(helpText = "Sets the Permssions of yourself", args = {@ArgSet(arg = Arguments.NUMBER)}),
            @Syntax(helpText = "Sets the Permissions of the user(s) @\u200Bmentioned", args = {@ArgSet(arg = Arguments.MENTION, num = 0), @ArgSet(arg = Arguments.NUMBER)}),
            @Syntax(helpText = "Sets the Permissions of all users with the Role(s) @\u200Bmentioned", args = {@ArgSet(arg = Arguments.MENTIONROLE), @ArgSet(arg = Arguments.NUMBER)})
    })
    public void set(CommContext cont) {

        //make sure there are enough arguments
        if (cont.getArgs().size() < 3) {
            cont.getActions().missingArgs(cont.getMessage().getChannel());
        }

        //check if there is a perm level to change too
        if (!BotUtils.tryInt(cont.getArgs().get(cont.getArgs().size() - 1))) {
            cont.getActions().wrongArgs(cont.getMessage().getChannel());
        } else {

            //store perm to change too
            Permissions setPerm = BotUtils.toPerms(Integer.parseInt(cont.getArgs().get(cont.getArgs().size() - 1)));

            //check if the perm to change too is within the boundries of perm levels
            if (setPerm.level < 0 || setPerm.level > Permissions.values().length - 1) {
                cont.getActions().missingPermissions(cont.getMessage().getChannel(), setPerm);

                //check if the caller is at least as high as the perm he is setting
            } else if (cont.getCallerPerms().level < setPerm.level) {
                cont.getActions().missingPermissions(cont.getMessage().getChannel(), setPerm);

                //check if there are @mentions to set perms of
            } else if (cont.getMessage().getMentions().size() > 0) {
                MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());

                //iterate through all of the @mentions
                for (IUser user : cont.getMessage().getMentions()) {
                    //make sure the @mention is the lower perms than the caller
                    if (cont.getCallerPerms().level <= cont.getActions().getTable(PermTable.class).getPerms(user, cont.getMessage().getGuild()).level) {
                        message.appendContent("Did not change " + user.getDisplayName(cont.getMessage().getGuild()) + "'s perms because your level is not higher than " + user.getDisplayName(cont.getMessage().getGuild()) + "'s\n");
                    } else {
                        //and finally, change their perms
                        cont.getActions().getTable(PermTable.class).setPerms(user, cont.getMessage().getGuild(), setPerm);
                        message.appendContent("Changing " + user.mention() + " to a" + BotUtils.startsWithVowel(setPerm.title, "n **", " **") + "**\n");
                    }
                }
                cont.getActions().sendMessage(message);
            } else if (cont.getMessage().getRoleMentions().size() > 0) {
                MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
                for (IRole role : cont.getMessage().getRoleMentions()) {
                    //iterate through all of the users
                    for (IUser user : cont.getMessage().getGuild().getUsersByRole(role)) {
                        if (cont.getCallerPerms().level <= cont.getActions().getTable(PermTable.class).getPerms(cont.getMessage().getAuthor(), cont.getMessage().getGuild()).level) {
                            message.appendContent("Did not change " + user.getDisplayName(cont.getMessage().getGuild()) + "'s perms because your level is not higher than " + user.getDisplayName(cont.getMessage().getGuild()) + "'s\n");
                        } else {
                            //and finally, change their perms
                            cont.getActions().getTable(PermTable.class).setPerms(user, cont.getMessage().getGuild(), setPerm);
                            message.appendContent("Changing " + user.mention() + " to a" + BotUtils.startsWithVowel(setPerm.title, "n **", " **") + "**\n");
                        }
                    }
                }
                cont.getActions().sendMessage(message);
            } else {
                cont.getActions().missingArgs(cont.getMessage().getChannel());
            }
        }
    }
}
