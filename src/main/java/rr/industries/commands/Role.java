package rr.industries.commands;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.util.*;

/**
 * @author Sam
 */

@CommandInfo(commandName = "role", helpText = "Lets the Awesome BotOperator screw around", permLevel = Permissions.BOTOPERATOR)
public class Role implements Command {
    @SubCommand(name = "add", Syntax = @Syntax(helpText = "Adds a role named this", args = {@Argument(description = "Role Name", value = Validate.TEXT)}))
    public Mono<Void> execute(CommContext cont) throws BotException {
        Mono<discord4j.core.object.entity.Role> role = cont.getMessage().getGuild().flatMapMany(Guild::getRoles).filter(v -> v.getName().equals(cont.getArgs().get(2))).next();
        Member member = cont.getMember();
        return role.flatMap(v -> member.addRole(v.getId()));
    }
}
