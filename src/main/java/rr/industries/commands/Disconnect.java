package rr.industries.commands;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.VoiceChannel;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.util.*;

@CommandInfo(
        commandName = "disconnect",
        helpText = "Disconnects a user from a voice channel",
        permLevel = Permissions.ADMIN
)
//todo: disconnect multiple users
public class Disconnect implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Disconnects the mentioned user", args = {@Argument(value = Validate.MENTION)})})
    public Mono<Void> execute(CommContext cont) throws BotException {

        Mono<User> user = cont.getMessage().getMessage().getUserMentions().next();

        Mono<VoiceChannel> createChannel = cont.getMessage().getGuild().flatMap(v -> v.createVoiceChannel(u -> u.setName("Disconnect"))).cache();
        Mono<Void> moveUser = user
                .zipWith(Mono.justOrEmpty(cont.getMessage().getGuildId()))
                .flatMap(v -> v.getT1().asMember(v.getT2()))
                .zipWith(createChannel)
                .flatMap(v -> v.getT1().edit(u -> u.setNewVoiceChannel(v.getT2().getId())));
        Mono<Void> deleteChannel = moveUser.then(createChannel.flatMap(Channel::delete));
        return deleteChannel;
    }
}
