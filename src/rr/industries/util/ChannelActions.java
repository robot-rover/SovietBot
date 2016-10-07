package rr.industries.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.istack.internal.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.Configuration;
import rr.industries.Exceptions.BotException;
import rr.industries.SovietBot;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
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
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author Sam
 */
public class ChannelActions {
    private static final Logger LOG = LoggerFactory.getLogger(ChannelActions.class);

    private IDiscordClient client;
    Configuration config;

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
        if (exception.criticalMessage().isPresent()) {
            messageOwner("[Critical Error] - " + exception.criticalMessage().get(), true);
        } else {
            sendMessage(builder.withContent(exception.getMessage()));
        }
    }

    @Deprecated
    public void sqlError(SQLException ex, String methodName, Logger log) {
        log.warn("SQL Error in " + methodName, ex);
        messageOwner("SQL Error in " + methodName + ": " + ex.getMessage(), true);
    }

    @Deprecated
    public void notFound(IMessage imessage, String methodName, String type, String name, Logger log) {
        String message = methodName + " failed to find " + type + ": \"" + name + "\" in Server: \"" + imessage.getGuild().getName() + "\"";
        log.info(message);
        sendMessage(new MessageBuilder(client).withContent(message).withChannel(imessage.getChannel()));
    }

    @Deprecated
    public void missingPermissions(IChannel channel, MissingPermissionsException ex) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent(ex.getErrorMessage()));
    }

    @Deprecated
    public void missingPermissions(IChannel channel, Permissions neededPerm) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent("You need to be a" + BotUtils.startsWithVowel(neededPerm.title, "n **", " **") + "** (" + neededPerm.level + ") to do that!"));
    }

    @Deprecated
    public void missingArgs(IChannel channel) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent("You have the wrong number of arguments"));
    }

    @Deprecated
    public void wrongArgs(IChannel channel) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent("Your arguments are incorrect"));
    }

    @Deprecated
    public void customException(String methodName, String message, @Nullable Exception ex, Logger log, boolean error) {
        String fullMessage = methodName + ": " + message;
        if (error) {
            log.error(fullMessage);
            if (ex != null) {
                log.error("Full Stack Trace - ", ex);
            }
            messageOwner("[ERROR] " + fullMessage, true);
        } else {
            log.info(fullMessage);
            if (ex != null) {
                log.debug("Full Stack Trace - ", ex);
            }
        }
    }

    public void connectToChannel(IVoiceChannel channel) throws BotException {
        if (!channel.isConnected()) {
            channel.getClient().getConnectedVoiceChannels().stream().filter(v -> v.getGuild().equals(channel.getGuild())).findAny().ifPresent((t) -> channel.leave());
            try {
                channel.join();
            } catch (MissingPermissionsException ex) {
                BotException.translateException(ex);
            }
        }
    }

    public void delayDelete(IMessage message, int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            LOG.warn("delayDelete - Sleep was interrupted - ", ex);
        }
        RequestBuffer.request(() -> {
            try {
                if (SovietBot.loggedIn && !message.getChannel().isPrivate()) {
                    message.delete();
                }
            } catch (MissingPermissionsException ex) {
                //fail silently
                LOG.debug("Did not delete message, missing permissions");
            } catch (DiscordException ex) {
                customException("delayDelete", ex.getErrorMessage(), ex, LOG, true);
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
            } catch (DiscordException ex) {
                customException("sendMessage", ex.getErrorMessage(), ex, LOG, true);
            } catch (MissingPermissionsException ex) {
                missingPermissions(builder.getChannel(), ex);
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
                        new ProcessBuilder("java", "-jar", "-server", "sovietBot-master.jar").inheritIO().start();
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
