package rr.industries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import gigadot.rebound.Rebound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.Command;
import rr.industries.commands.Info;
import rr.industries.commands.PermWarning;
import rr.industries.exceptions.*;
import rr.industries.modules.Console;
import rr.industries.modules.Module;
import rr.industries.modules.UTCStatus;
import rr.industries.modules.Webhooks;
import rr.industries.util.*;
import rr.industries.util.sql.*;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.modules.IModule;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.MessageBuilder;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * todo: XP and Levels
 * todo: RSS feeds module
 * todo: refractor to modules to allow hotswap
 * todo: [Long Term] Write unit tests
 * todo: impl stable async client
 * Commands -
 * Command: twitch stream / video on interweb :-P
 * Command: Reminder
 * Command: voting
 * Command: search stuff
 * Command: Find in message history
 * Command: Repl
 */

public class SovietBot implements IModule {
    private static final Logger LOG = LoggerFactory.getLogger(SovietBot.class);
    private static final File configFile = new File("configuration.json");
    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private Configuration config;
    private CommandList commandList;
    private volatile IDiscordClient client;
    private ITable[] tables;
    BotActions actions;
    ClassLoader resourceLoader;
    Information info;


    public SovietBot() {
        resourceLoader = SovietBot.class.getClassLoader();
    }

    @EventSubscriber
    public void onGuildJoin(GuildCreateEvent e) {
        LOG.info("[GuildCreateEvent] Joined Guild {} - ({})", e.getGuild().getName(), e.getGuild().getID());
    }

    @EventSubscriber
    public void onUserJoin(UserJoinEvent e) {
        try {
            Optional<String> messageContent = actions.getTable(GreetingTable.class).getJoinMessage(e.getGuild());
            if (messageContent.isPresent()) {
                MessageBuilder message = new MessageBuilder(client).withChannel(client.getChannelByID(e.getGuild().getID()))
                        .withContent(messageContent.get().replace("%user", e.getUser().mention()));
                actions.channels().sendMessage(message);
            }
        } catch (BotException ex) {
            actions.channels().exception(ex, new MessageBuilder(client).withChannel(client.getChannelByID(e.getGuild().getID())));
        }
    }

    @EventSubscriber
    public void onUserLeave(UserLeaveEvent e) {
        try {
            Optional<String> messageContent = actions.getTable(GreetingTable.class).getLeaveMessage(e.getGuild());
            if (messageContent.isPresent()) {
                MessageBuilder message = new MessageBuilder(client).withChannel(client.getChannelByID(e.getGuild().getID()))
                        .withContent(messageContent.get().replace("%user", "`" + e.getUser().getDisplayName(e.getGuild()) + "`"));
                actions.channels().sendMessage(message);
            }
        } catch (BotException ex) {
            actions.channels().exception(ex, new MessageBuilder(client).withChannel(client.getChannelByID(e.getGuild().getID())));
        }

    }

    @EventSubscriber
    public void onReady(ReadyEvent e) {
        actions.enableModules();
        Discord4J.disableChannelWarnings();
        LOG.info("*** " + info.botName + " armed ***");
        if (!client.getOurUser().getName().equals(config.botName)) {
            try {
                BotUtils.bufferRequest(() -> {
                    try {
                        client.changeUsername(config.botName);
                    } catch (DiscordException ex) {
                        throw BotException.returnException(ex);
                    }
                });
            } catch (BotException ex) {
                actions.channels().exception(ex);
            }

        }
        String[] filename = config.botAvatar.split("[.]");
        try {
            BotUtils.bufferRequest(() -> {
                try {
                    client.changeAvatar(Image.forStream(filename[filename.length - 1], resourceLoader.getResourceAsStream(config.botAvatar)));
                } catch (DiscordException ex) {
                    throw BotException.returnException(ex);
                }
            });
        } catch (BotException ex) {
            actions.channels().exception(ex);
        }
        for (IGuild guild : client.getGuilds()) {
            try {
                actions.getTable(PermTable.class).setPerms(guild, guild.getOwner(), Permissions.ADMIN);
                for (String op : config.operators)
                    actions.getTable(PermTable.class).setPerms(guild, client.getUserByID(op), Permissions.BOTOPERATOR);
                LOG.info("Connected to Guild: " + guild.getName() + " (" + guild.getID() + ")");
                PermWarning permTool = commandList.getCommand(PermWarning.class);
                List<sx.blah.discord.handle.obj.Permissions> missingPerms = permTool.checkPerms(guild, client.getOurUser(), info.neededPerms.stream().map(Entry::first).collect(Collectors.toCollection(ArrayList::new)));
                actions.getTable(PermTable.class).setPerms(guild, guild.getOwner(), Permissions.ADMIN);
                for (String op : config.operators)
                    actions.getTable(PermTable.class).setPerms(guild, client.getUserByID(op), Permissions.BOTOPERATOR);
                if (!missingPerms.isEmpty())
                    LOG.info("Missing Perms in guild {} ({}): {}", guild.getName(), guild.getID(), permTool.formatPerms(missingPerms));
            } catch (BotException ex) {
                actions.channels().exception(ex);
            }
        }
        LOG.info("\n------------------------------------------------------------------------\n"
                + "*** " + info.botName + " Ready ***\n"
                + "------------------------------------------------------------------------");
        actions.channels().messageOwner("Startup Successful", false);
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getAuthor().isBot()) {
            return;
        }
        String message = e.getMessage().getContent();
        boolean command = message.startsWith(actions.getTable(PrefixTable.class).getPrefix(e.getMessage().getGuild()));
        boolean mention = e.getMessage().getMentions().contains(client.getOurUser()) && !e.getMessage().mentionsEveryone();
        if (command || mention) {
            CommContext cont = new CommContext(e, actions);
            try {
                if (command) {
                    Entry<Command, Method> commandSet = commandList.getSubCommand(cont.getArgs());
                    if (commandSet.second() != null) {
                        SubCommand subComm = commandSet.second().getAnnotation(SubCommand.class);
                        CommandInfo commandInfo = commandSet.first().getClass().getAnnotation(CommandInfo.class);
                        if (e.getMessage().getChannel().isPrivate() && !commandInfo.pmSafe() || !subComm.pmSafe()) {
                            throw new PMNotSupportedException();
                        }
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
                            invokeCommand(commandSet.first(), commandSet.second(), cont);
                        }
                    }
                } else {
                    try {
                        invokeCommand(commandList.getCommand("info"), Info.class.getMethod("execute", CommContext.class), cont);
                    } catch (NoSuchMethodException ex) {
                        throw new ServerError("Could not execute Info command for mention", ex);
                    }
                }
            } catch (BotException ex) {
                cont.getActions().channels().exception(ex, cont.builder());
            }
        }
    }

    private void invokeCommand(Command c, Method m, CommContext cont) throws BotException {
        try {
            m.invoke(c, cont);
        } catch (IllegalAccessException ex) {
            throw new ServerError("Could not access subcommand", ex);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof Exception) {
                Exception cause = (Exception) ex.getCause();
                if (cause instanceof BotException) {
                    throw (BotException) cause;
                } else
                    throw new ServerError("The subcommand threw an uncaught " + cause.getClass().getName(), cause);
            }
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

    @Override
    public boolean enable(IDiscordClient client) {
        info = new Information();
        this.client = client;
        Unirest.setTimeouts(1000, 1000);
        Discord4J.disableChannelWarnings();
        config = loadConfig(configFile, gson.toJson(info.defaultConfig), Configuration.class).orElse(null);
        if (config == null) {
            LOG.info("One or more config files are empty, Exiting...");
            System.exit(0);
        }
        for (Field f : config.getClass().getFields()) {
            try {
                if (f.get(config) == null) {
                    f.set(config, f.get(info.defaultConfig));
                }
            } catch (IllegalAccessException e) {
                LOG.error("Error Defaulting Nulls");
            }
        }
        Rebound r = new Rebound("rr.industries.commands", false, true);
        r.getSubClassesOf(Command.class).forEach(CommandList::addCommand);
        commandList = new CommandList();
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                LOG.error("Could not load SQLite JDBC Driver", ex);
            }
            Connection connection = DriverManager.getConnection("jdbc:sqlite:sovietBot.db");
            tables = new ITable[]{new PermTable(connection, config), new TimeTable(connection), new TagTable(connection), new PrefixTable(connection, config), new GreetingTable(connection)};
        } catch (SQLException | BotException ex) {
            LOG.error("Unable to Initialize Database", ex);
            System.exit(1);
        }
        LOG.info("Database Initialized");
        ChannelActions ca = new ChannelActions(client, config, info);
        actions = new BotActions(client, commandList, tables, new Module[]{new Console(ca), new UTCStatus(client), new Webhooks(ca)}, ca);
        return true;
    }

    public void disable() {
        actions.channels().finalizeResources();
    }

    @Override
    public String getName() {
        return "SovietBot";
    }

    @Override
    public String getAuthor() {
        return "robot_rover";
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    @Override
    public String getMinimumDiscord4JVersion() {
        return "2.6.2";
    }
}
