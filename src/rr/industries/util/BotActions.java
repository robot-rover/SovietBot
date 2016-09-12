package rr.industries.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.istack.internal.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.CommandList;
import rr.industries.Configuration;
import rr.industries.Instance;
import rr.industries.SovietBot;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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

    public BotActions(IDiscordClient client, Configuration config, CommandList commands, Statement sql) {
        this.client = client;
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

    public IDiscordClient getClient() {
        return client;
    }

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
        String message = "SQL Error in " + methodName + ": " + ex.getMessage() + "\n" + ex.toString();
        log.warn(message);
        messageOwner(message, true);
    }

    public void threadInterrupted(InterruptedException ex, String methodName, Logger log) {
        log.warn(methodName + " - Sleep was interrupted - ", ex);
    }

    public void notFound(MessageReceivedEvent e, String methodName, String type, String name, Logger log) {
        String message = methodName + " failed to find " + type + ": \"" + name + "\" in Server: \"" + e.getMessage().getGuild().getName() + "\"";
        log.info(message);
        try {
            e.getMessage().reply(message);
        } catch (MissingPermissionsException ex) {
            missingPermissions(e.getMessage().getChannel(), ex);
        } catch (RateLimitException ex) {
            //todo: implement ratelimit fix
        } catch (DiscordException ex) {
            customException("notFound", ex.getMessage(), ex, log, true);
        }
    }

    public void customException(String methodName, String message, @Nullable Exception ex, Logger log, boolean error) {
        String fullMessage = methodName + message;
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
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent("You need to be a" + BotUtils.startsWithVowel(neededPerm.title, "n **", " **") + neededPerm.title + "** (" + neededPerm.level + ") to do that!"));
    }

    public void missingArgs(IChannel channel) {
        sendMessage(new MessageBuilder(client).withChannel(channel).withContent("You are missing some arguments"));
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

    public void delayDelete(IMessage message, int delay, CommContext cont) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            threadInterrupted(ex, "onMessage", SovietBot.LOG);
        }
        try {
            if (Instance.loggedIn) {
                message.delete();
                cont.eraseMessage();
            }
        } catch (MissingPermissionsException ex) {
            //fail silently
            SovietBot.LOG.debug("Did not delete message, missing permissions");
        } catch (RateLimitException ex) {
            //todo: fix ratelimit
        } catch (DiscordException ex) {
            customException("delayDelete", ex.getErrorMessage(), ex, LOG, true);
        }
    }

    public void messageOwner(String message, boolean notify) {
        MessageBuilder messageBuilder = new MessageBuilder(client).withContent(message);
        try {
            if (!notify) {
                for (IGuild guild : client.getGuilds()) {
                    if (guild.getID().equals("161155978199302144"))
                        messageBuilder.withChannel("161155978199302144");
                    if (guild.getID().equals("141313424880566272"))
                        messageBuilder.withChannel("170685308273164288");
                }
            }
            if (messageBuilder.getChannel() == null) {
                messageBuilder.withChannel(client.getOrCreatePMChannel(client.getUserByID("141981833951838208")));
            }
            sendMessage(messageBuilder);
        } catch (DiscordException ex) {
            LOG.error("Error messaging bot owner", ex);
        } catch (RateLimitException ex) {
            //todo: implement ratelimit
        }
    }

    public IMessage sendMessage(MessageBuilder builder) {
        IMessage messageObject = null;
        try {
            messageObject = builder.send();
        } catch (DiscordException ex) {
            customException("sendMessage", ex.getErrorMessage(), ex, LOG, true);
        } catch (RateLimitException ex) {
            //todo: fix ratelimit
        } catch (MissingPermissionsException ex) {
            missingPermissions(builder.getChannel(), ex);
        }
        return messageObject;
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

    /*public void downloadUpdate(String url) {
        LOG.info("Downloading new .jar");
        File jarFile = new File("sovietBot-master.jar");
        File backupFile = new File("sovietBot-backup.jar");
        try {
            Files.copy(jarFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOG.error("Error Copying jar to backup file", ex);
            return;
        }
        try {
            Files.delete(jarFile.toPath());
            FileUtils.copyURLToFile(new URL(url), jarFile, 10000, 10000);
        } catch (IOException ex) {
            LOG.error("Error Downloading Jar", ex);
            try {
                LOG.warn("Restoring from Backup");
                Files.copy(backupFile.toPath(), jarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex2) {
                LOG.error("Error Restoring Backup", ex2);
                return;
                //todo: add send to owner message
            }
        }
        try {
            Files.delete(backupFile.toPath());
        } catch (IOException ex) {
            LOG.warn("Unable to delete backup after successful extraction", ex);
        }
        saveLog();
        try {
            sendMessage(new MessageBuilder(client).withContent("Updated successfully.").withChannel(client.getOrCreatePMChannel(client.getUserByID("141981833951838208"))));
        } catch (DiscordException ex) {
            LOG.error("Error Sending Successful Update Message", ex);
        } catch (RateLimitException ex) {
            //todo: Fix ratelimits
        }
    }*/
}
