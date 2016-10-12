package rr.industries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.Command;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.InternalError;
import rr.industries.exceptions.MissingPermsException;
import rr.industries.modules.Console;
import rr.industries.modules.Module;
import rr.industries.modules.UTCStatus;
import rr.industries.modules.Webhooks;
import rr.industries.util.*;
import rr.industries.util.sql.ITable;
import rr.industries.util.sql.PermTable;
import rr.industries.util.sql.TagTable;
import rr.industries.util.sql.TimeTable;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.IGuild;
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
import java.util.stream.Collectors;

import static rr.industries.SovietBot.botName;
import static rr.industries.SovietBot.defaultConfig;

/**
 * todo: XP and Levels
 * todo: RSS feeds module
 * todo: refractor to modules to allow hotswap
 * todo: [Long Term] Write unit tests
 * todo: add help options
 * todo: per guild prefix
 * todo: add volumeprovider to music and rekt command
 * Commands -
 * Command: twitch stream / video on interweb
 * Command: Add Strawpole Command
 * Command: Dictionary Command
 * Command: Triggered Text Command: http://eeemo.net/
 * Command: URL Shortener
 * Command: Reminder
 * Command: voting
 * Command: search stuff
 * Command: Find in message history
 * Command: Repl
 */

public class Instance {
    private static final Logger LOG = LoggerFactory.getLogger(Instance.class);
    private static final File configFile = new File("configuration.json");
    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Configuration config;
    private final CommandList commandList;
    private volatile IDiscordClient client;
    private ITable[] tables;
    private BotActions actions;

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
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:sovietBot.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(20);  // set timeout to 20 sec.
            tables = new ITable[]{new PermTable(statement), new TimeTable(statement), new TagTable(statement)};
        } catch (SQLException ex) {
            LOG.error("Unable to Initialize Database", ex);
            System.exit(1);
        }
        LOG.info("Database Initialized");
        if (config.token.equals("")) {
            LOG.error("you must set a token in the config!");
            System.exit(1);
        }
        client = new ClientBuilder().withToken(config.token).setMaxReconnectAttempts(6).build();
        client.getDispatcher().registerListener(this);
        client.login(false);
        ChannelActions ca = new ChannelActions(client, config);
        actions = new BotActions(client, commandList, tables, new Module[]{new Console(ca), new UTCStatus(client), new Webhooks(ca)}, ca);
    }

    @EventSubscriber
    public void onGuildCreate(GuildCreateEvent e) {
        actions.getTable(PermTable.class).setPerms(e.getGuild(), e.getGuild().getOwner(), Permissions.ADMIN);
        for (String op : config.operators)
            actions.getTable(PermTable.class).setPerms(e.getGuild(), client.getUserByID(op), Permissions.BOTOPERATOR);
        LOG.info("Connected to Guild: " + e.getGuild().getName() + " (" + e.getGuild().getID() + ")");
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) throws DiscordException, RateLimitException {
        SovietBot.loggedIn = true;
        actions.enableModules();
        Discord4J.disableChannelWarnings();
        LOG.info("*** " + botName + " armed ***");
        if (!client.getOurUser().getName().equals(config.botName)) {
            client.changeUsername(config.botName);
        }
        String[] filename = config.botAvatar.split("[.]");
        client.changeAvatar(Image.forStream(filename[filename.length - 1], SovietBot.resourceLoader.getResourceAsStream(config.botAvatar)));
        for (IGuild guild : client.getGuilds()) {
            actions.getTable(PermTable.class).setPerms(guild, guild.getOwner(), Permissions.ADMIN);
            for (String op : config.operators)
                actions.getTable(PermTable.class).setPerms(guild, client.getUserByID(op), Permissions.BOTOPERATOR);
            LOG.info("Connected to Guild: " + guild.getName() + " (" + guild.getID() + ")");
        }
        LOG.info("\n------------------------------------------------------------------------\n"
                + "*** " + botName + " Ready ***\n"
                + "------------------------------------------------------------------------");
        actions.channels().messageOwner("Startup Successful", false);
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getAuthor().isBot()) {
            return;
        }
        if (e.getMessage().getChannel().isPrivate()) {
            actions.channels().sendMessage(new MessageBuilder(client).withContent("Sorry, until PM channels are thoroughly tested, the bot cannot reply to you in them. Its really buggy XP")
                    .withChannel(e.getMessage().getChannel()));
            return;
        }
        String message = e.getMessage().getContent();
        if (message.startsWith(config.commChar)) {
            CommContext cont = new CommContext(e, actions);
            try {
                Entry<Command, Method> commandSet = commandList.getSubCommand(cont.getArgs());
                if (commandSet.second() != null) {
                    SubCommand subComm = commandSet.second().getAnnotation(SubCommand.class);
                    CommandInfo commandInfo = commandSet.first().getClass().getAnnotation(CommandInfo.class);
                    if (subComm.permLevel().level > cont.getCallerPerms().level || commandInfo.permLevel().level > cont.getCallerPerms().level) {
                        throw new MissingPermsException("use the " + commandInfo.commandName() + " command", subComm.permLevel().level > commandInfo.permLevel().level ? subComm.permLevel() : commandInfo.permLevel());
                    } else {
                        final int iteratorConstant = (subComm.name().equals("") ? 1 : 2);
                        List<Syntax> syntax = Arrays.stream(subComm.Syntax()).filter(
                                v -> v.args().length + iteratorConstant == cont.getArgs().size() ||
                                        v.args().length + iteratorConstant <= cont.getArgs().size() && Arrays.asList(v.args()).contains(Arguments.LONGTEXT))
                                .collect(Collectors.toList());
                        if (commandSet.first().getValiddityOverride() != null) {
                            if (!commandSet.first().getValiddityOverride().test(cont.getArgs())) {
                                throw new IncorrectArgumentsException();
                            }
                        } else if (!syntax.isEmpty()) {
                            boolean found = false;
                            outerLoop:
                            for (Syntax syntax1 : syntax) {
                                found = true;
                                for (int i = 0; i < syntax1.args().length; i++) {
                                    if (syntax1.args()[i].equals(Arguments.LONGTEXT) && Arguments.LONGTEXT.isValid.test(cont.getArgs().get(i + iteratorConstant))) {
                                        break outerLoop;
                                    }
                                    if (!syntax1.args()[i].isValid.test(cont.getArgs().get(i + iteratorConstant))) {
                                        found = false;
                                        break;
                                    }
                                }
                                if (found) {
                                    break;
                                }
                            }
                            if (!found)
                                throw new IncorrectArgumentsException();
                        } else {
                            throw new IncorrectArgumentsException();
                        }
                        try {
                            commandSet.second().invoke(commandSet.first(), cont);
                        } catch (IllegalAccessException ex) {
                            throw new InternalError("Could not access subcommand", ex);
                        } catch (InvocationTargetException ex) {
                            if (ex.getCause() instanceof Exception) {
                                Exception cause = (Exception) ex.getCause();
                                if (cause instanceof BotException) {
                                    throw (BotException) cause;
                                } else
                                    throw new InternalError("The subcommand threw an uncaught " + cause.getClass().getName(), cause);
                            }
                        }
                    }
                }
            } catch (BotException ex) {
                cont.getActions().channels().exception(ex, cont.builder());
            }
        }
    }

    @EventSubscriber
    public void onReconnect(DiscordReconnectedEvent e) {
        SovietBot.loggedIn = true;
    }

    @EventSubscriber
    public void onDisconnect(DiscordDisconnectedEvent e) {
        SovietBot.loggedIn = false;
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
