package rr.industries.commands;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.util.*;

@CommandInfo(
        commandName = "purge",
        helpText = "Deletes Messages from a text Channel",
        permLevel = Permissions.MOD
)
public class Purge implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Deletes the number off messages you specify", args = {@Argument(description = "# of Messages", value = Validate.NUMBER)})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        int number = Integer.parseInt(cont.getArgs().get(1));
        if (number > 99 || number < 1) {
            throw new IncorrectArgumentsException("Your number must be between 1 and 99");
        }
        if(TextChannel.class.isAssignableFrom(cont.getChannel().getClass())) {
            TextChannel channel = (TextChannel) cont.getChannel();
            Flux<Snowflake> toDelete = channel.getMessagesBefore(cont.getMessage().getMessage().getId()).take(number).map(Message::getId);
            LOG.debug("Trying to delete {} messages", number);
            return channel.bulkDelete(toDelete).count().flatMap(v -> {
                cont.getChannel().createMessage(v > 0 ? "Deleted " + number + " messages" : "Failed to delete " + v + " messages");
                return Mono.empty();
            });
        } else {
            throw new PMNotSupportedException();
        }
    }
}
