package rr.industries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.*;
import rr.industries.modules.Module;
import rr.industries.modules.githubwebhooks.GithubWebhooks;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.sql.Column;
import rr.industries.util.sql.SQLUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;
import sx.blah.discord.handle.impl.events.DiscordReconnectedEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static rr.industries.SovietBot.botName;
import static rr.industries.SovietBot.resourceLoader;

/**
 * todo: Add Permissions
 * todo: XP and Levels
 * todo: RSS feeds module
 * Commands -
 * Command: Add Strawpole Command
 * Command: Add Weather Command: https://bitbucket.org/akapribot/owm-japis
 * Command: Tag Command
 * Command: Whois Command
 * Command: Dictionary Command
 * Command: Triggered Text Command: http://eeemo.net/
 * Command: environment command
 * Command: URL Shortener
 * Command: Reminder
 * Command: voting
 * Command: search stuff
 */

public class Instance {
    public static final Logger LOG = LoggerFactory.getLogger(Instance.class);
    private static final File configFile = new File("configuration.json");
    private volatile IDiscordClient client;
    private final Configuration config;
    private final CommandList commandList;
    private Module webHooks;
    public static volatile boolean loggedIn = true;
    private Statement statement;
    private BotActions actions;

    Instance() throws DiscordException {
        List<Command> commands = Arrays.asList(
                new Bring(), new Cat(), new Coin(), new Connect(),
                new Disconnect(), new Help(), new Info(), new Invite(), new Log(), new Music(),
                new Purge(), new Quote(), new Rekt(), new Restart(), new Roll(), new Stop(),
                new Unafk(), new Uptime(), new Weather(), new Prefix(), new Rip(),
                new Environment(), new Echo(), new Test(), new Perms());
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
        commandList = new CommandList(commands);
        Connection connection;
        List<Column> columns = Arrays.asList(
                new Column("guildid", "text", false),
                new Column("userid", "text", false),
                new Column("perm", "int", false));
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:sovietBot.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            SQLUtils.initTable("perms", columns, statement, null);
            SQLUtils.createIndex("perms", "searchindex", "guildid, userid", true, statement, null);
        } catch (SQLException ex) {
            LOG.error("Unable to Initialize Database", ex);
            System.exit(1);
        }
        LOG.info("Database Initialized");
        if (config.token.equals("")) {
            LOG.error("you must set a token in the config!");
            System.exit(1);
        }
        client = new ClientBuilder().withToken(config.token).build();
        client.getDispatcher().registerListener(this);
        client.login();
        actions = new BotActions(client, config, commandList, statement);

    }

    @EventSubscriber
    public void onReady(ReadyEvent e) throws DiscordException, RateLimitException {
        Discord4J.disableChannelWarnings();
        LOG.info("*** " + botName + " armed ***");
        webHooks = new GithubWebhooks(1000, client, actions);
        webHooks.enable();
        if (!client.getOurUser().getName().equals(config.botName)) {
            client.changeUsername(config.botName);
        }
        String[] filename = config.botAvatar.split("[.]");
        client.changeAvatar(Image.forStream(filename[filename.length - 1], SovietBot.resourceLoader.getResourceAsStream(config.botAvatar)));
        LOG.info("\n------------------------------------------------------------------------\n"
                + "*** " + botName + " Ready ***\n"
                + "------------------------------------------------------------------------");
        actions.messageOwner("Startup Successful", false);
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getAuthor().isBot()) {
            return;
        }
        String message = e.getMessage().getContent();
        if (message.startsWith(config.commChar)) {
            CommContext cont = new CommContext(e, actions);
            Command command;
            command = commandList.getCommand(cont.getArgs().get(0));
            if (command != null) {
                CommandInfo info = command.getClass().getDeclaredAnnotation(CommandInfo.class);
                command.execute(cont);
                if (info.deleteMessage() && !cont.getMessage().getMessage().getChannel().isPrivate()) {
                    actions.delayDelete(cont.getMessage().getMessage(), 5000);
                }
            }
        }
    }

    @EventSubscriber
    public void onReconnect(DiscordReconnectedEvent ex) {
        loggedIn = true;
    }

    @EventSubscriber
    public void onDisconnect(DiscordDisconnectedEvent ex) {
        loggedIn = false;
        LOG.info("DiscordDisconnectedEvent Received with reason: " + ex.getReason().name(), ex);
        if (ex.getReason() == DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED) {
            boolean restart = actions.saveLog();
            actions.terminate(restart);
        }
    }
}
