package rr.industries.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.istack.internal.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.CommandList;
import rr.industries.Configuration;
import rr.industries.SovietBot;
import rr.industries.util.sql.Table;
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
import java.sql.Statement;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * @author Sam
 * @project sovietBot
 * @created 8/29/2016
 */
public final class BotActions {
    private static final Logger LOG = LoggerFactory.getLogger(BotActions.class);
    private final IDiscordClient client;
    private final Configuration config;
    private final CommandList commands;
    private final Statement sql;
    private final Table[] tables;

    public BotActions(IDiscordClient client, Configuration config, CommandList commands, Statement sql, Table[] tables) {
        this.client = client;
        this.tables = tables;
        this.config = config;
        this.commands = commands;
        this.sql = sql;
    }

    public static void disconnectFromChannel(IGuild guild, List<IVoiceChannel> connectedChannels) {
        IVoiceChannel possible = connectedChannels.stream().filter(v -> v.getGuild().equals(guild)).findAny().orElse(null);
        if (possible != null) {
            possible.leave();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Table> T getTable(Class<T> tableType) {
        for (Table table : tables) {
            if (table.getClass().equals(tableType))
                return (T) table;
        }
        throw new NoSuchElementException("Table of type: " + tableType.getName() + " not found!");
    }

    public IDiscordClient getClient() {
        return client;
    }

    @Deprecated
    public Statement getSQL() {
        return sql;
    }

    public CommandList getCommands() {
        return commands;
    }

    public Configuration getConfig() {
        return config;
    }

    public void sqlError(SQLException ex, String methodName, Logger log) {
        log.warn("SQL Error in " + methodName, ex);
        messageOwner("SQL Error in " + methodName + ": " + ex.getMessage(), true);
    }

    public void threadInterrupted(InterruptedException ex, String methodName, Logger log) {
        log.warn(methodName + " - Sleep was interrupted - ", ex);
    }

    public void notFound(IMessage imessage, String methodName, String type, String name, Logger log) {
        String message = methodName + " failed to find " + type + ": \"" + name + "\" in Server: \"" + imessage.getGuild().getName() + "\"";
        log.info(message);
        sendMessage(new MessageBuilder(imessage.getClient()).withContent(message).withChannel(imessage.getChannel()));
    }

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

    public void missingPermissions(IChannel channel, MissingPermissionsException ex) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent(ex.getErrorMessage()));
    }

    public void missingPermissions(IChannel channel, Permissions neededPerm) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent("You need to be a" + BotUtils.startsWithVowel(neededPerm.title, "n **", " **") + "** (" + neededPerm.level + ") to do that!"));
    }

    public void missingArgs(IChannel channel) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent("You have the wrong number of arguments"));
    }

    public void wrongArgs(IChannel channel) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent("Your arguments are incorrect"));
    }

    public void connectToChannel(IVoiceChannel channel, List<IVoiceChannel> connectedChannels) {
        IVoiceChannel possible = connectedChannels.stream().filter(v -> v.getGuild().equals(channel.getGuild())).findAny().orElse(null);
        if (!channel.isConnected()) {
            if (possible != null) {
                possible.leave();
            }
            try {
                channel.join();
            } catch (MissingPermissionsException ex) {
                missingPermissions(channel, ex);
            }
        }
    }

    public void delayDelete(IMessage message, int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            threadInterrupted(ex, "onMessage", LOG);
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
        RequestBuffer.RequestFuture message = RequestBuffer.request(() -> {
            try {
                return Optional.of(builder.send());
            } catch (DiscordException ex) {
                customException("sendMessage", ex.getErrorMessage(), ex, LOG, true);
            } catch (MissingPermissionsException ex) {
                missingPermissions(builder.getChannel(), ex);
            }
            return Optional.empty();
        });
        return (Optional<IMessage>) message.get();
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
