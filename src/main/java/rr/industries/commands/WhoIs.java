package rr.industries.commands;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import org.apache.commons.text.WordUtils;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.util.*;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

/**
 * @author Sam
 */

@CommandInfo(commandName = "whois", helpText = "Tells you information about users.")
public class WhoIs implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Tells you information about yourself", args = {}),
            @Syntax(helpText = "Tells you information about the mentioned user", args = {@Argument(Validate.MENTION)})
    })
    public Mono<Void> execute(CommContext cont) throws BotException {
        Snowflake guildId = cont.getGuildId();
        Member author = cont.getMember();
        Mono<Member> examine = cont.getMessage().getMessage().getUserMentions().next()
                .flatMap(v -> v.asMember(guildId))
                .defaultIfEmpty(author).cache();
        return Mono.zip(examine, getRoles(examine), formatPresence(examine)).flatMap(v -> {
            Member target = v.getT1();
            String roles = v.getT2();
            String presence = v.getT3();

            StringBuilder message = new StringBuilder();
            message.append("`" + target.getUsername() + "#" + target.getDiscriminator() + "`");
            target.getNickname().ifPresent(u -> message.append(" aka *").append(u).append("*"));
            if (target.isBot()) {
                message.append(" - `|BOT|`");
            }
            message.append("\n**--------------**\n");
            message.append("ID: " + target.getId().asString() + "\n");
            message.append("Joined: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneOffset.UTC).format(target.getJoinTime()) + "\n");
            message.append("Roles: " + roles + "\n");
            message.append("Status: " + presence + "\n");
            message.append("Avatar: " + target.getAvatarUrl() + "\n");
            return cont.getChannel().createMessage(message.toString()).then();
        });
    }

    private Mono<String> getRoles(Mono<Member> target) {
        return target
                .flatMapMany(Member::getRoles)
                .filter(u -> !u.isEveryone())
                .map(Role::getName)
                .collect(Collectors.joining(", "));
    }

    private Mono<String> formatPresence(Mono<Member> target) {
        return target
                .flatMap(Member::getPresence)
                .map(p -> WordUtils.capitalizeFully(p.getStatus().getValue()) + p.getActivity().map(a -> " - " + WordUtils.capitalizeFully(a.getType().name()) + " " + a.getName()).orElse(""));
    }
}
