package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

/**
 * @author Sam
 */

@CommandInfo(commandName = "role", helpText = "Lets the Awesome BotOperator screw around", permLevel = Permissions.BOTOPERATOR)
public class Role implements Command {
    @SubCommand(name = "add", Syntax = @Syntax(helpText = "Adds a command named thsi", args = {Arguments.TEXT}))
    public void execute(CommContext cont) throws BotException {
        List<IRole> role = cont.getMessage().getGuild().getRolesByName(cont.getArgs().get(2));
        if (role.size() == 0)
            throw new IncorrectArgumentsException("The role \"" + cont.getArgs().get(2) + "\" was not found");
        try {
            cont.getMessage().getAuthor().addRole(role.get(0));
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            throw BotException.returnException(ex);
        }
    }
}
