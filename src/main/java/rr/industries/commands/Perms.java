package rr.industries.commands;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.jooq.Record2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.MissingPermsException;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.util.*;
import rr.industries.util.sql.PermTable;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author robot_rover
 */

@CommandInfo(
        commandName = "perm",
        helpText = "Gets and Sets permissions for users of a server")
public class Perms implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Shows your permission level", args = {})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        return Mono.justOrEmpty(cont.getMessage().getMember())
                .map(User::getMention)
                .flatMap(v -> cont.getChannel().createMessage(v + " is a" + BotUtils.startsWithVowel(cont.getCallerPerms().title, "n ", " ", false) + cont.getCallerPerms().formatted))
                .then();
    }

    @SubCommand(name = "all", Syntax = {@Syntax(helpText = "Lists all permissions for the server", args = {})})
    public Mono<Void> all(CommContext cont) throws PMNotSupportedException {
        Snowflake guildId = cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new);
        List<Record2<Long, Integer>> list = cont.getActions().getTable(PermTable.class).getAllPerms(guildId);
        StringBuilder message = new StringBuilder("```markdown\n");
        Mono<StringBuilder> content = cont.getMessage().getGuild().map(Guild::getName).map(v -> message.append("# Permissions for ").append(v).append(" #\n"));

        for (Record2<Long, Integer> entry : list) {
            content = content.then(cont.getClient().getUserById(Snowflake.of(entry.component1()))
                    .flatMap(v -> v.asMember(guildId))
                    .map(Member::getDisplayName)
                    .map(v -> message.append(String.format("%-15s", "<" + BotUtils.toPerms(entry.component2()).title + ">")).append(v).append("\n")));
        }
        content = content.map(v -> v.append("```"));
        return content.flatMap(v -> cont.getChannel().createMessage(v.toString())).then();
    }

    @SubCommand(name = "set", Syntax = {
            @Syntax(helpText = "Sets the Permissions of the user(s) @\u200Bmentioned", args = {@Argument(Validate.MENTION), @Argument(description = "Permission Level", value = Validate.NUMBER, options = {"0 - Normal", "1 - Regular", "2 - Moderator", "3 - Admin"})})
    })
    public Mono<Void> set(CommContext cont) throws BotException {
        Snowflake guildId = cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new);
        StringBuilder message = new StringBuilder();
        Permissions setPerm = BotUtils.toPerms(Integer.parseInt(cont.getArgs().get(cont.getArgs().size() - 1)));
            //check if the caller is at least as high as the perm he is setting
        if (cont.getCallerPerms().level < setPerm.level) {
            throw new MissingPermsException("Set Perms to level " + setPerm.level, setPerm);

            //check if there are @mentions to set perms of
        } else if (cont.getMessage().getMessage().getUserMentionIds().size() > 0) {
            Flux<User> mentions = cont.getMessage().getMessage().getUserMentions();
             return mentions.flatMap(v -> {
                //make sure the @mention is the lower perms than the caller
                return cont.getActions().getTable(PermTable.class).getPerms(v, cont.getMessage().getMessage()).map(u -> {
                    if (cont.getCallerPerms().level <= u.level) {
                        return "Did not change " + v.getMention() + "'s perms because your level is not higher than theirs (" + u.formatted + ")\n";
                    } else {
                        //and finally, change their perms
                        cont.getActions().getTable(PermTable.class).setPerms(guildId, v.getId(), setPerm);
                        return "Changing " + v.getMention() + " to a" + BotUtils.startsWithVowel(setPerm.title, "n ", " ", false) + setPerm.formatted + "\n";
                    }
                });
            }).collect(Collectors.joining()).flatMap(v -> cont.getChannel().createMessage(v)).then();
        } else {
            throw new IncorrectArgumentsException("You didn't mention any users or roles!");
        }

    }

    @Override
    public Predicate<List<String>> getValiddityOverride() {
        return (v) -> {
            if (v.size() >= 3 && v.get(1).equals("set")) {
                for (int i = 2; i < v.size() - 1; i++) {
                    if (!Validate.MENTION.isValid.test(v.get(i)) && !Validate.MENTIONROLE.isValid.test(v.get(i))) {
                        return false;
                    }
                }
                return Validate.NUMBER.isValid.test(v.get(v.size() - 1));
            } else if (v.size() == 2 && v.get(1).equals("all")) {
                return true;
            } else if (v.size() == 1) {
                return true;
            }
            return false;
        };
    }
}
