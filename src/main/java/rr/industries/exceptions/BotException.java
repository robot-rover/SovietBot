package rr.industries.exceptions;

import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;


/**
 * @author Sam
 */
public abstract class BotException extends Exception {
    protected Logger LOG = LoggerFactory.getLogger(BotException.class);
    protected Channel channel;
    protected BotException(String message) {
        super(message);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public abstract boolean shouldLog();

    public Mono<Void> handle() {
        if(MessageChannel.class.isAssignableFrom(channel.getClass())) {
            MessageChannel mc = (MessageChannel) channel;
            return mc.createMessage(this.getMessage()).then();
        }
        LOG.warn("Exception created on Non-Message Channel {}", channel);
        return Mono.empty();
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
