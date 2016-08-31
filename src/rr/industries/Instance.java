package rr.industries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.Command;
import rr.industries.modules.Module;
import rr.industries.modules.githubwebhooks.GithubWebhooks;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static rr.industries.SovietBot.botName;
import static rr.industries.SovietBot.resourceLoader;

/* todo: Add SQLite Storage: https://github.com/xerial/sqlite-jdbc
 * todo: Add Permissions
 * todo: Restructure to modules
 * todo: Write config changes
 * Commands -
 * Command: config command
 * Command: Add Strawpole Command
 * Command: Add Prefix Switch Command
 * Command: Add Weather Command: https://bitbucket.org/akapribot/owm-japis
 * Command: Tag Command
 * Command: Echo Command
 * Command: Rip Command: https://www.ripme.xyz/<phrase>
 * Command: Whois Command
 * Command: Dictionary Command
 * Command: Triggered Text Command: http://eeemo.net/
 * Command: environment command
 */

public class Instance {
    public static final Logger LOG = LoggerFactory.getLogger(Instance.class);
    static final File configFile = new File("configuration.json");
    public volatile IDiscordClient client;
    public Configuration config;
    public CommandList commandList;
    private Module webHooks;

    Instance() {
        webHooks = null;
        Discord4J.disableChannelWarnings();

        LOG.info("Looking for config at: " + configFile.getAbsolutePath());
        if (!configFile.exists()) {
            LOG.warn("config not found, generating new one...");
            try {
                Files.copy(resourceLoader.getResourceAsStream("defaultConfiguration.json"), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (NullPointerException ex) {
                LOG.error("default config.json not found. exiting...", ex);
                System.exit(1);
            } catch (IOException ex) {
                LOG.error("failed to initialize new config file. exiting...", ex);
                System.exit(1);
            }
        } else {
            LOG.info("config found");
        }
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(configFile);
        } catch (FileNotFoundException ex) {
            LOG.error("Config file not found and was not rebuild. exiting", ex);
            System.exit(1);
        }
        Gson gson = new GsonBuilder().create();
        config = gson.fromJson(fileReader, Configuration.class);
        commandList = new CommandList();

    }

    void login() throws DiscordException {
        if (config.token.equals("")) {
            LOG.error("you must set a token in the config!");
            System.exit(1);
        }
        client = new ClientBuilder().withToken(config.token).build();
        client.getDispatcher().registerListener(this);
        client.login();
    }

    public void downloadUpdate(String url) {
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
        BotActions.terminate(true, client);
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) throws DiscordException, RateLimitException {
        LOG.info("*** " + botName + " armed ***");
        webHooks = new GithubWebhooks(1000, client, "welp2.0");
        webHooks.enable();
        if (!client.getOurUser().getName().equals(config.botName)) {
            client.changeUsername(config.botName);
        }
        String[] filename = config.botAvatar.split("[.]");
        client.changeAvatar(Image.forStream(filename[filename.length - 1], SovietBot.resourceLoader.getResourceAsStream(config.botAvatar)));
        LOG.info("\n------------------------------------------------------------------------\n"
                + "*** " + botName + " Ready ***\n"
                + "------------------------------------------------------------------------");
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getAuthor().isBot()) {
            return;
        }
        String message = e.getMessage().getContent();
        if (message.startsWith(config.commChar)) {
            CommContext cont = new CommContext(e, client, config.commChar);
            Command command;
            command = commandList.getCommand(cont.getArgs().get(0));
            if (command != null) {
                if (command.deleteMessage && !cont.getMessage().getMessage().getChannel().isPrivate()) {
                    BotActions.delayDelete(cont.getMessage().getMessage(), 5000);
                }
                command.execute(cont);
            }
        }
    }

    @EventSubscriber
    public void onDisconnect(DiscordDisconnectedEvent ex) {
        LOG.info("DiscordDisconnectedEvent Received with reason: " + ex.getReason().name(), ex);
        if (ex.getReason() == DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED) {
            boolean restart = BotActions.saveLog();
            BotActions.terminate(restart, client);
        }
    }
}
