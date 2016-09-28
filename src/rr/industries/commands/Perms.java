package rr.industries.commands;

import rr.industries.util.*;
import rr.industries.util.sql.PermTable;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/6/2016
 */

@CommandInfo(
        commandName = "perms",
        helpText = "Gets and Sets permissions for users of a server")
public class Perms implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "View and Set permissions for SovietBot", args = {})})
    public void execute(CommContext cont) {
        cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel())
                .withContent(cont.getMessage().getAuthor().mention() + " is a" + BotUtils.startsWithVowel(cont.getCallerPerms().title, "n **", " **") + "** (Level " + cont.getCallerPerms().level + ")"));
    }

    @SubCommand(name = "all", Syntax = {@Syntax(helpText = "Lists all permissions for the server", args = {})})
    public void all(CommContext cont) {
            List<Entry<String, Integer>> list = cont.getActions().getTable(PermTable.class).getAllPerms(cont.getMessage().getGuild());
            MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel()).withContent("```markdown\n");
            message.appendContent("# Permissions for " + cont.getMessage().getGuild().getName() + " #\n");
            for (Entry<String, Integer> entry : list) {
                message.appendContent(String.format("%-15s", "<" + BotUtils.toPerms(entry.second()).title + ">") + cont.getClient().getUserByID(entry.first()).getDisplayName(cont.getMessage().getGuild()) + "\n");
            }
        cont.getActions().channels().sendMessage(message.appendContent("```"));
    }

    @SubCommand(name = "set", Syntax = {
            @Syntax(helpText = "Sets the Permssions of yourself", args = {Arguments.NUMBER}),
            @Syntax(helpText = "Sets the Permissions of the user(s) @\u200Bmentioned", args = {Arguments.MENTION, Arguments.NUMBER}),
            @Syntax(helpText = "Sets the Permissions of all users with the Role(s) @\u200Bmentioned", args = {Arguments.MENTIONROLE, Arguments.NUMBER})
    })
    public void set(CommContext cont) {


        //store perm to change too
        Permissions setPerm = BotUtils.toPerms(Integer.parseInt(cont.getArgs().get(cont.getArgs().size() - 1)));

        //check if the perm to change too is within the boundries of perm levels
        if (setPerm.level < 0 || setPerm.level > Permissions.values().length - 1) {
            cont.getActions().channels().missingPermissions(cont.getMessage().getChannel(), setPerm);

            //check if the caller is at least as high as the perm he is setting
        } else if (cont.getCallerPerms().level < setPerm.level) {
            cont.getActions().channels().missingPermissions(cont.getMessage().getChannel(), setPerm);

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
            cont.getActions().channels().sendMessage(message);
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
            cont.getActions().channels().sendMessage(message);
        } else {
            cont.getActions().channels().missingArgs(cont.getMessage().getChannel());
        }

    }

    @Override
    public Predicate<List<String>> getValiddityOverride() {
        return (v) -> {
            if (v.size() >= 3 && v.get(1).equals("set")) {
                for (int i = 2; i < v.size() - 1; i++) {
                    if (!Arguments.MENTION.isValid.test(v.get(i)) && !Arguments.MENTIONROLE.isValid.test(v.get(i))) {
                        return false;
                    }
                }
                return Arguments.NUMBER.isValid.test(v.get(v.size() - 1));
            } else if (v.size() == 2 && v.get(1).equals("all")) {
                return true;
            } else if (v.size() == 1) {
                return true;
            }
            return false;
        };
    }
}
