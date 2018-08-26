package rr.industries.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.Configuration;
import rr.industries.Information;
import rr.industries.exceptions.BotException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @author Sam
 */
public class ChannelActions {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelActions.class);
    Configuration config;
    private IDiscordClient client;
    private Information info;

    public ChannelActions(IDiscordClient client, Configuration config, Information info) {
        this.client = client;
        this.config = config;
        this.info = info;
    }

    public IDiscordClient getClient() {
        return client;
    }

    public Configuration getConfig() {
        return config;
    }

    /**
     * Method for Handling BotExceptions
     *
     * @param exception The exception to be handled
     * @param builder   MessageBuilder with channel and client already set
     */
    public <T extends BotException> void exception(T exception, MessageBuilder builder) {
        exception(exception);
        try {
            sendMessage(builder.withContent(exception.getMessage()));
        } catch (BotException e) {
            exception(e);
        }
    }

    public <T extends BotException> void exception(T exception) {
        if (exception.criticalMessage().isPresent()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String sStackTrace = sw.toString();
            messageOwner("[Critical Error] - " + exception.criticalMessage().get() + "\n```" + sStackTrace + "```", true);
        }
    }

    public void connectToChannel(IVoiceChannel channel) throws BotException {
        if (!channel.isConnected()) {
            channel.getClient().getConnectedVoiceChannels().stream().filter(v -> v.getGuild().equals(channel.getGuild())).findAny().ifPresent((t) -> channel.leave());
            try {
                channel.join();
            } catch (MissingPermissionsException ex) {
                throw BotException.returnException(ex);
            }
        }
    }

    public void delayDelete(IMessage message, int delay) throws BotException {
        if (delay > 0)
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                LOG.warn("delayDelete - Sleep was interrupted - ", ex);
            }
        BotUtils.bufferRequest(() -> {
            try {
                if (client.isReady() && !message.getChannel().isPrivate()) {
                    message.delete();
                }
            } catch (MissingPermissionsException ex) {
                //fail silently
                LOG.debug("Did not delete message, missing permissions");
            } catch (DiscordException ex) {
                throw BotException.returnException(ex);
            }
        });
    }
    public void messageOwner(String message, boolean notify) {
        MessageBuilder messageBuilder = new MessageBuilder(client).withContent(message);
        if (!notify) {
            messageBuilder.withChannel(client.getGuildByID(Long.parseLong(config.outputServer)).getChannelByID(Long.parseLong(config.outputChannel)));
        } else {
            if (config.operators.length == 0) {
                LOG.error("No Bot Operators present, unable to one");
                return;
            }
            try {
                messageBuilder.withChannel(client.getOrCreatePMChannel(client.getUserByID(Long.parseLong(config.operators[0]))));
            } catch (DiscordException ex) {
                LOG.error("Error messaging bot owner", ex);
            }
        }
        try {
            sendMessage(messageBuilder);
        } catch (BotException e) {
            LOG.error("Error when reporting error?!?", e);
        }
    }

    public IMessage sendMessage(MessageBuilder builder) throws BotException {
        BotUtils.IExRequest<IMessage> request = builder::send;
        return BotUtils.bufferRequest(request);
    }

    public void disconnectFromChannel(IGuild guild) {
        guild.getClient().getConnectedVoiceChannels().stream().filter(v -> v.getGuild().equals(guild)).findAny().ifPresent(IVoiceChannel::leave);
    }

    public void terminate() {
        try {
            client.logout();
        } catch (DiscordException | RateLimitException ex) {
            LOG.warn("Logout Failed, Forcing Shutdown", ex);
        }
        System.exit(0);
    }

    public boolean archiveLog() {
        try {
            Files.copy(new File("events.log").toPath(), new File("events_" + Long.toString(System.currentTimeMillis()) + ".log").toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            Files.copy(new File("debug.log").toPath(), new File("debug_" + Long.toString(System.currentTimeMillis()) + ".log").toPath(), StandardCopyOption.COPY_ATTRIBUTES);

        } catch (IOException ex) {
            LOG.error("Error Archiving Log", ex);
            return false;
        }
        return true;
    }
}
