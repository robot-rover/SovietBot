package rr.industries.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.Configuration;
import rr.industries.SovietBot;
import rr.industries.exceptions.BotException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * @author Sam
 */
public class ChannelActions {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelActions.class);
    Configuration config;
    private IDiscordClient client;

    public ChannelActions(IDiscordClient client, Configuration config) {
        this.client = client;
        this.config = config;
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
        sendMessage(builder.withContent(exception.getMessage()));
    }

    public <T extends BotException> void exception(T exception) {
        if (exception.criticalMessage().isPresent()) {
            messageOwner("[Critical Error] - " + exception.criticalMessage().get(), true);
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
                if (SovietBot.loggedIn && !message.getChannel().isPrivate()) {
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
            for (IGuild guild : client.getGuilds()) {
                if (guild.getID().equals("161155978199302144"))
                    messageBuilder.withChannel("161155978199302144");
                if (guild.getID().equals("141313424880566272"))
                    messageBuilder.withChannel("170685308273164288");
            }
        }
        if (messageBuilder.getChannel() == null) {
            RequestBuffer.request(() -> {
                try {
                    messageBuilder.withChannel(client.getOrCreatePMChannel(client.getUserByID("141981833951838208")));
                } catch (DiscordException ex) {
                    LOG.error("Error messaging bot owner", ex);
                }
            });
        }
        sendMessage(messageBuilder);
    }

    public Optional<IMessage> sendMessage(MessageBuilder builder) {
        RequestBuffer.IRequest<Optional<IMessage>> request = () -> {
            try {
                return Optional.of(builder.send());
            } catch (DiscordException | MissingPermissionsException ex) {
                exception(BotException.returnException(ex));
            }
            return Optional.empty();
        };
        return RequestBuffer.request(request).get();
    }

    public void disconnectFromChannel(IGuild guild) {
        guild.getClient().getConnectedVoiceChannels().stream().filter(v -> v.getGuild().equals(guild)).findAny().ifPresent(IVoiceChannel::leave);

    }

    public void terminate(Boolean restart) {
        LOG.info("Writing Config");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("configuration.json", false)) {
            writer.write(gson.toJson(config));
        } catch (FileNotFoundException ex) {
            LOG.error("Config file not found upon exit, but was saved", ex);
        } catch (IOException ex) {
            LOG.error("Config file was not saved upon exit", ex);
        }
        BotActions.getActions(client).disableModules();
        try {
            client.logout();
        } catch (RateLimitException | DiscordException ex) {
            LOG.warn("Logout Failed, Forcing Shutdown", ex);
        }
        LOG.info("\n------------------------------------------------------------------------\n"
                + "Terminated\n"
                + "------------------------------------------------------------------------");
        if (restart) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (new File("sovietBot-update.jar").exists()) {
                        LOG.info("Updated Jar exists, copying...");
                        try {
                            Files.copy(new File("sovietBot-update.jar").toPath(), new File("sovietBot-master.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
                            Files.delete(new File("sovietBot-update.jar").toPath());
                        } catch (IOException ex) {
                            saveLog();
                            LOG.error("Failed to Update SovietBot", ex);
                        }
                    }
                    try {
                        File launcher = new File("launch");
                        if (launcher.exists()) {
                            new ProcessBuilder("/bin/bash", launcher.getAbsolutePath()).inheritIO().start();
                        } else {
                            new ProcessBuilder("java", "-jar", "-server", "sovietBot-master.jar").inheritIO().start();
                        }
                    } catch (IOException ex) {
                        LOG.error("restart failed :-(", ex);
                    }
                }
            });
        }
        System.exit(0);
    }

    public boolean saveLog() {
        boolean successful = true;
        try {
            Files.copy(new File("events.log").toPath(), new File("events_" + Long.toString(System.currentTimeMillis()) + ".log").toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            Files.copy(new File("debug.log").toPath(), new File("debug_" + Long.toString(System.currentTimeMillis()) + ".log").toPath(), StandardCopyOption.COPY_ATTRIBUTES);

        } catch (IOException ex) {
            LOG.error("Error Archiving Disconnect Log", ex);
            successful = false;
        }
        return successful;
    }
}
