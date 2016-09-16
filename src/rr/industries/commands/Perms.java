package rr.industries.commands;

import rr.industries.CommandList;
import rr.industries.util.*;
import rr.industries.util.sql.SQLUtils;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

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
    static {
        CommandList.defaultCommandList.add(Perms.class);
    }

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "View and Set permissions for SovietBot", args = {})})
    public void execute(CommContext cont) {
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel())
                .withContent(cont.getMessage().getMessage().getAuthor().mention() + " is a" + BotUtils.startsWithVowel(cont.getCallerPerms().title, "n **", " **") + cont.getCallerPerms().title + "** (Level " + cont.getCallerPerms().level + ")"));
    }

    @SubCommand(name = "all", Syntax = {@Syntax(helpText = "Lists all permissions for the server", args = {})})
    public void all(CommContext cont) {
        if (cont.getArgs().size() >= 2) {
            try {
                ResultSet rs = cont.getActions().getSQL().executeQuery("SELECT userid, perm FROM perms where guildid=" + cont.getMessage().getMessage().getGuild().getID() + " ORDER BY perm DESC;");
                MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel()).withContent("```markdown\n");
                message.appendContent("# Permissions for " + cont.getMessage().getMessage().getGuild().getName() + " #\n");
                while (rs.next()) {
                    message.appendContent(String.format("%-15s", "<" + BotUtils.toPerms(rs.getInt("perm")).title + ">") + cont.getClient().getUserByID(rs.getString("userid")).getDisplayName(cont.getMessage().getMessage().getGuild()) + "\n");
                }
                cont.getActions().sendMessage(message.appendContent("```"));
            } catch (SQLException ex) {
                cont.getActions().sqlError(ex, "perms", LOG);
            }
        } else {
            cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
        }
    }

    @SubCommand(name = "set", Syntax = {
            @Syntax(helpText = "Sets the Permssions of yourself", args = {Arguments.NUMBER}),
            @Syntax(helpText = "Sets the Permissions of the user(s) @\u200Bmentioned", args = {Arguments.MENTION, Arguments.NUMBER}),
            @Syntax(helpText = "Sets the Permissions of all users with the Role(s) @\u200Bmentioned", args = {Arguments.MENTIONROLE, Arguments.NUMBER})
    })
    public void set(CommContext cont) {

        //make sure there are enough arguments
        if (cont.getArgs().size() < 3) {
            cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
        }

        //check if there is a perm level to change too
        if (!BotUtils.tryInt(cont.getArgs().get(cont.getArgs().size() - 1))) {
            cont.getActions().wrongArgs(cont.getMessage().getMessage().getChannel());
        } else {

            //store perm to change too
            int setPerm = Integer.parseInt(cont.getArgs().get(cont.getArgs().size() - 1));

            //check if the perm to change too is within the boundries of perm levels
            if (setPerm < 0 || setPerm > Permissions.values().length - 1) {
                cont.getActions().missingPermissions(cont.getMessage().getMessage().getChannel(), BotUtils.toPerms(setPerm));

                //check if the caller is at least as high as the perm he is setting
            } else if (cont.getCallerPerms().level < setPerm) {
                cont.getActions().missingPermissions(cont.getMessage().getMessage().getChannel(), BotUtils.toPerms(setPerm));

                //check if there are @mentions to set perms of
            } else if (cont.getMessage().getMessage().getMentions().size() > 0) {
                MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel());

                //iterate through all of the @mentions
                for (IUser user : cont.getMessage().getMessage().getMentions()) {
                    //make sure the @mention is the lower perms than the caller
                    if (cont.getCallerPerms().level <= SQLUtils.getPerms(user.getID(), cont.getMessage().getMessage().getGuild().getID(), cont.getActions().getSQL(), cont.getActions()).level) {
                        message.appendContent("Did not change " + user.getDisplayName(cont.getMessage().getMessage().getGuild()) + "'s perms because your level is not higher than " + user.getDisplayName(cont.getMessage().getMessage().getGuild()) + "'s\n");
                    } else {
                        //and finally, change their perms
                        SQLUtils.updatePerms(user.getID(), cont.getMessage().getMessage().getGuild().getID(), BotUtils.toPerms(setPerm), cont.getActions().getSQL(), cont.getActions());
                        message.appendContent("Changing " + user.mention() + " to a" + BotUtils.startsWithVowel(BotUtils.toPerms(setPerm).title, "n **", " **") + BotUtils.toPerms(setPerm).title + "**\n");
                    }
                }
                cont.getActions().sendMessage(message);
            } else if (cont.getMessage().getMessage().getRoleMentions().size() > 0) {
                MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel());
                for (IRole role : cont.getMessage().getMessage().getRoleMentions()) {
                    //iterate through all of the users
                    for (IUser user : cont.getMessage().getMessage().getGuild().getUsers()) {
                        if (user.getRolesForGuild(cont.getMessage().getMessage().getGuild()).contains(role)) {
                            //make sure the @mention is the lower perms than the caller
                            if (cont.getCallerPerms().level <= SQLUtils.getPerms(user.getID(), cont.getMessage().getMessage().getGuild().getID(), cont.getActions().getSQL(), cont.getActions()).level) {
                                message.appendContent("Did not change " + user.getDisplayName(cont.getMessage().getMessage().getGuild()) + "'s perms because your level is not higher than " + user.getDisplayName(cont.getMessage().getMessage().getGuild()) + "'s\n");
                            } else {
                                //and finally, change their perms
                                SQLUtils.updatePerms(user.getID(), cont.getMessage().getMessage().getGuild().getID(), BotUtils.toPerms(setPerm), cont.getActions().getSQL(), cont.getActions());
                                message.appendContent("Changing " + user.mention() + " to a" + BotUtils.startsWithVowel(BotUtils.toPerms(setPerm).title, "n **", " **") + BotUtils.toPerms(setPerm).title + "**\n");
                            }
                        }
                    }
                }
                cont.getActions().sendMessage(message);
            } else {
                cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
            }
        }
    }
}
