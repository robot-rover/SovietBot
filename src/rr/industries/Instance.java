package rr.industries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.Command;
import rr.industries.modules.Console;
import rr.industries.modules.Module;
import rr.industries.modules.UTCStatus;
import rr.industries.modules.githubwebhooks.GithubWebhooks;
import rr.industries.util.*;
import rr.industries.util.sql.Column;
import rr.industries.util.sql.SQLUtils;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RateLimitException;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static rr.industries.SovietBot.botName;
import static rr.industries.SovietBot.defaultConfig;

/**
 * todo: Add Permissions
 * todo: XP and Levels
 * todo: RSS feeds module
 * todo: reintegrate as modules
 * todo: add better structure for subcommands
 * todo: make sql commands more generic
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
    private static final File opFile = new File("botOperators.json");
    public static volatile boolean loggedIn = true;
    private final Configuration config;
    private final CommandList commandList;
    private volatile IDiscordClient client;
    private Module webHooks;
    private Statement statement;
    private BotActions actions;
    private Module updateStatus;
    private Module console;
    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    Instance() throws DiscordException {
        Discord4J.disableChannelWarnings();
        config = loadConfig(configFile, gson.toJson(defaultConfig), Configuration.class).orElse(null);
        if (config == null) {
            LOG.info("One or more config files are empty, Exiting...");
            System.exit(0);
        }
        for (Field f : config.getClass().getFields()) {
            try {
                if (f.get(config) == null) {
                    f.set(config, f.get(defaultConfig));
                }
            } catch (IllegalAccessException e) {
                LOG.error("Error Defaulting Nulls");
            }
        }
        commandList = new CommandList();
        Connection connection;
        List<Column> permsColumns = Arrays.asList(
                new Column("guildid", "text", false),
                new Column("userid", "text", false),
                new Column("perm", "int", false)
        );
        List<Column> usersColumns = Arrays.asList(
                new Column("userid", "text", false),
                new Column("timezone", "text", true)
        );
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:sovietBot.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            SQLUtils.initTable("perms", permsColumns, statement, null);
            SQLUtils.createIndex("perms", "searchindex", "guildid, userid", true, statement, null);
            SQLUtils.initTable("users", usersColumns, statement, null);
            SQLUtils.createIndex("users", "searchindex", "userid", true, statement, null);
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
    public void onGuildCreate(GuildCreateEvent e) {
        for (String op : config.operators)
            SQLUtils.updatePerms(op, e.getGuild().getID(), Permissions.BOTOPERATOR, statement, actions);
        SQLUtils.updatePerms(e.getGuild().getOwnerID(), e.getGuild().getID(), Permissions.ADMIN, statement, actions);
        LOG.info("Connected to Guild: " + e.getGuild().getName() + " (" + e.getGuild().getID() + ")");
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) throws DiscordException, RateLimitException {
        console = new Console(actions);
        console.enable();
        updateStatus = new UTCStatus(client);
        updateStatus.enable();
        Discord4J.disableChannelWarnings();
        LOG.info("*** " + botName + " armed ***");
        webHooks = new GithubWebhooks(actions);
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
        if (e.getMessage().getChannel().isPrivate()) {
            actions.sendMessage(new MessageBuilder(client).withContent("Sorry, until PM channesl are thoroughly tested, the bot cannot reply to you in them. Its really buggy XP")
                    .withChannel(e.getMessage().getChannel()));
            return;
        }
        String message = e.getMessage().getContent();
        if (message.startsWith(config.commChar)) {
            CommContext cont = new CommContext(e, actions);
            Command command = commandList.getCommand(cont.getArgs().get(0));
            if (command != null) {
                CommandInfo info = command.getClass().getDeclaredAnnotation(CommandInfo.class);
                if (cont.getCallerPerms().level < info.permLevel().level) {
                    actions.missingPermissions(cont.getMessage().getMessage().getChannel(), info.permLevel());
                } else {
                    Method subCommand = null;
                    Method baseSubCommand = null;
                    for (Method subComm : command.getClass().getDeclaredMethods()) {
                        if (subComm.getAnnotation(SubCommand.class) == null) {
                            continue;
                        }
                        if (cont.getArgs().size() >= 2 && subComm.getAnnotation(SubCommand.class).name().equals(cont.getArgs().get(1))) {
                            subCommand = subComm;
                        }
                        if (subComm.getAnnotation(SubCommand.class).name().equals("")) {
                            baseSubCommand = subComm;
                        }

                    }
                    if (subCommand == null && baseSubCommand != null) {
                        subCommand = baseSubCommand;
                    }
                    if (subCommand != null) {
                        try {
                            subCommand.invoke(command, cont);
                        } catch (IllegalAccessException ex) {
                            actions.customException("onMessage", "Could not access subcommand", ex, LOG, true);
                        } catch (InvocationTargetException ex) {
                            if (ex.getCause() instanceof Exception)
                                actions.customException("onMessage", "Subcommand Invocation Failed", (Exception) ex.getCause(), LOG, true);
                        }
                        if (info.deleteMessage() && !cont.getMessage().getMessage().getChannel().isPrivate()) {
                            actions.delayDelete(cont.getMessage().getMessage(), 2500);
                        }
                    }
                }
            }
        }
        Thread.currentThread().interrupt();
    }

    @EventSubscriber
    public void onReconnect(DiscordReconnectedEvent e) {
        loggedIn = true;
    }

    @EventSubscriber
    public void onDisconnect(DiscordDisconnectedEvent e) {
        loggedIn = false;
        if (e.getReason().equals(DiscordDisconnectedEvent.Reason.LOGGED_OUT)) {
            LOG.info("Successfully Logged Out...");
        } else {
            LOG.warn("Disconnected Unexpectedly: " + e.getReason().name(), e);
        }
    }

    private <T> Optional<T> loadConfig(File file, String defaultValue, Class<T> t) {
        LOG.info("Looking for File at: " + file.getAbsolutePath());
        if (!file.exists()) {
            LOG.warn("File not found, generating new one...");
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
                writer.write(defaultValue);
            } catch (IOException e) {
                LOG.error("Could not initialize new File");
            }
            LOG.info("Please configure generated Files");
            return Optional.empty();
        }
        try (FileReader fileReader = new FileReader(file)) {
            return Optional.ofNullable(gson.fromJson(fileReader, t));
        } catch (FileNotFoundException ex) {
            LOG.error("Config file not found and was not generated", ex);
        } catch (IOException ex) {
            LOG.warn("Exception Reading " + file.getName(), ex);
        }
        return Optional.empty();
    }
}
