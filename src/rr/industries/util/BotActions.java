package rr.industries.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        try {
            client.logout();
        } catch (RateLimitException | DiscordException ex) {
            LOG.warn("Logout Failed, Forcing Shutdown", ex);
        }
        LOG.info("\n------------------------------------------------------------------------\n"
                + "Terminated\n"
                + "------------------------------------------------------------------------");
        if (restart) {
            try {
                new ProcessBuilder("java", "-jar", "sovietBot-master.jar").inheritIO().start();
            } catch (IOException ex) {
                LOG.error("restart failed :-(", ex);
            }
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
        File archive = new File("SovietBot-archive.zip");
        File backupFile = new File("sovietBot-backup.jar");
        try {
            Files.copy(jarFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOG.error("Error Copying jar to backup file", ex);
            return;
        }
        try {
            FileUtils.copyURLToFile(new URL(url), archive, 10000, 10000);
            new ZipFile(archive).extractAll("./");
        } catch (IOException | ZipException ex) {
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
        BotActions.terminate(true, client);
    }
}
