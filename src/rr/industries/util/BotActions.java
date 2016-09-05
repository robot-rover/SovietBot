package rr.industries.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.Instance;
import rr.industries.SovietBot;
import sx.blah.discord.api.IDiscordClient;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @author Sam
 * @project sovietBot
 * @created 8/29/2016
 */
public final class BotActions {
    public static Logger LOG = LoggerFactory.getLogger(BotActions.class);

    public static void connectToChannel(IVoiceChannel channel, List<IVoiceChannel> connectedChannels) {
        IVoiceChannel possible = connectedChannels.stream().filter(v -> v.getGuild().equals(channel.getGuild())).findAny().orElse(null);
        if (!channel.isConnected()) {
            if (possible != null) {
                possible.leave();
            }
            try {
                channel.join();
            } catch (MissingPermissionsException ex) {
                Logging.missingPermissions(channel, "connectToChannel", ex, LOG);
            }
        }
    }

    public static void disconnectFromChannel(IGuild guild, List<IVoiceChannel> connectedChannels) {
        IVoiceChannel possible = connectedChannels.stream().filter(v -> v.getGuild().equals(guild)).findAny().orElse(null);
        if (possible != null) {
            possible.leave();
        }
    }

    public static void delayDelete(IMessage message, int delay) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    Logging.threadInterrupted(ex, "onMessage", SovietBot.LOG);
                }
                try {
                    if (Instance.loggedIn)
                        message.delete();
                } catch (MissingPermissionsException ex) {
                    //fail silently
                    SovietBot.LOG.debug("Did not delete message, missing permissions");
                } catch (RateLimitException ex) {
                    //todo: fix ratelimit
                } catch (DiscordException ex) {
                    Logging.error(message.getGuild(), "onMessage", ex, LOG);
                }
            }
        };
        thread.start();
    }

    public static void messageOwner(String message, IDiscordClient client, boolean notify) {
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
            BotActions.sendMessage(messageBuilder);
        } catch (DiscordException ex) {
            LOG.error("Error messaging bot owner", ex);
        } catch (RateLimitException ex) {
            //todo: implement ratelimit
        }
    }

    public static IMessage sendMessage(MessageBuilder builder) {
        IMessage messageObject = null;
        try {
            messageObject = builder.send();
        } catch (DiscordException ex) {
            Logging.error(builder.getChannel().getGuild(), "sendMessage(event)", ex, LOG);
        } catch (RateLimitException ex) {
            //todo: fix ratelimit
        } catch (MissingPermissionsException ex) {
            Logging.missingPermissions(builder.getChannel(), "sendMessage(event)", ex, LOG);
        }
        return messageObject;
    }

    public static void terminate(Boolean restart, IDiscordClient client) {
        LOG.info("Writing Config");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter("configuration.json", false)) {
            writer.write(gson.toJson(SovietBot.getBot().config));
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
                            LOG.error("Failed to Update SovietBot", ex);
                        }
                    }
                    saveLog();
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

    public static boolean saveLog() {
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

    public static void downloadUpdate(String url, IDiscordClient client) {
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
        BotActions.saveLog();
        try {
            sendMessage(new MessageBuilder(client).withContent("Updated successfully.").withChannel(client.getOrCreatePMChannel(client.getUserByID("141981833951838208"))));
        } catch (DiscordException ex) {
            LOG.error("Error Sending Successful Update Message", ex);
        } catch (RateLimitException ex) {
            //todo: Fix ratelimits
        }
        BotActions.terminate(true, client);
    }
}
