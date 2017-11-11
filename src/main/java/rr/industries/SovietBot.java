package rr.industries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import gigadot.rebound.Rebound;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.Command;
import rr.industries.commands.Info;
import rr.industries.commands.PermWarning;
import rr.industries.exceptions.*;
import rr.industries.modules.Console;
import rr.industries.modules.UTCStatus;
import rr.industries.modules.Webserver;
import rr.industries.util.*;
import rr.industries.util.sql.*;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.shard.ShardReadyEvent;
import sx.blah.discord.modules.IModule;
import sx.blah.discord.util.MessageBuilder;
import rr.industries.modules.Module;

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
 * todo: [Long Term] Write unit tests
 * Commands -
 * Command: twitch stream / video on interweb :-P
 * Command: Reminder
 * Command: voting
 * Command: search stuff
 * Command: Find in message history
 * Command: Repl
 */

public class SovietBot implements IModule {
    private boolean readyCalled = false;
    private static final Logger LOG = LoggerFactory.getLogger(SovietBot.class);
    private static final File configFile = new File("configuration.json");
    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private Configuration config;
    private volatile IDiscordClient client;
    private ITable[] tables;
    BotActions actions;
    ClassLoader resourceLoader;
    Information info;

    //this is bad practice but its just for debugging so I don't have to constantly log in so ¯\_(ツ)_/¯
    static SovietBot singleton;


    public SovietBot() {
        resourceLoader = SovietBot.class.getClassLoader();
        singleton = this;
    }

    //ditto with the bad practice. Its just to debug the webserver...
    public static BotActions getBotActions() {
        if (singleton == null)
            throw new RuntimeException("SovietBot has not yet been initialized!");
        return singleton.actions;
    }

    @EventSubscriber
    public void onGuildJoin(GuildCreateEvent e) {
        try {
            actions.getTable(PermTable.class).setPerms(e.getGuild(), e.getGuild().getOwner(), Permissions.ADMIN);
            for (String op : config.operators)
                actions.getTable(PermTable.class).setPerms(e.getGuild(), client.getUserByID(Long.parseLong(op)), Permissions.BOTOPERATOR);
            LOG.info("Connected to Guild: {} ({})", e.getGuild().getName(), e.getGuild().getStringID());
            PermWarning permTool = CommandList.getCommandList().getCommand(PermWarning.class);
            List<sx.blah.discord.handle.obj.Permissions> missingPerms = permTool.checkPerms(e.getGuild(), client.getOurUser(), Information.neededPerms.stream().map(Entry::first).collect(Collectors.toCollection(ArrayList::new)));
            if (!missingPerms.isEmpty())
                LOG.info("Missing Perms in guild() {} ({}): {}", e.getGuild().getName(), e.getGuild().getStringID(), permTool.formatPerms(missingPerms));
        } catch (BotException ex) {
            actions.channels().exception(ex);
        }
    }

    @EventSubscriber
    public void onUserJoin(UserJoinEvent e) {
        try {
            Optional<String> messageContent = actions.getTable(GreetingTable.class).getJoinMessage(e.getGuild());
            messageContent.ifPresent(s -> {
                MessageBuilder message = new MessageBuilder(client).withChannel(client.getChannelByID(e.getGuild().getLongID()))
                        .withContent(s.replace("%user", e.getUser().mention()));
                actions.channels().sendMessage(message);
            });
        } catch (BotException ex) {
            actions.channels().exception(ex, new MessageBuilder(client).withChannel(client.getChannelByID(e.getGuild().getLongID())));
        }
    }

    @EventSubscriber
    public void onUserLeave(UserLeaveEvent e) {
        try {
            Optional<String> messageContent = actions.getTable(GreetingTable.class).getLeaveMessage(e.getGuild());
            messageContent.ifPresent(s -> {
                MessageBuilder message = new MessageBuilder(client).withChannel(client.getChannelByID(e.getGuild().getLongID()))
                        .withContent(s.replace("%user", "`" + e.getUser().getDisplayName(e.getGuild()) + "`"));
                actions.channels().sendMessage(message);
            });
        } catch (BotException ex) {
            actions.channels().exception(ex, new MessageBuilder(client).withChannel(client.getChannelByID(e.getGuild().getLongID())));
        }

    }

    @EventSubscriber
    public void onReady(ShardReadyEvent e) {
        readyCalled = true;
        actions.enableModules();
        LOG.info("*** " + Information.botName + " armed ***");
        if (!client.getOurUser().getName().equals(Information.botName)) {
            //todo: uncomment when not testing...
            LOG.info("Changing Username...");
            /*try {
                BotUtils.bufferRequest(() -> {
                    try {
                        client.changeUsername(config.botName);
                    } catch (DiscordException ex) {
                        throw BotException.returnException(ex);
                    }
                });
            } catch (BotException ex) {
                actions.channels().exception(ex);
            }*/

        }
        /*String[] filename = Information.botAvatar.split("[.]");
        try {
            BotUtils.bufferRequest(() -> {
                try {
                    client.changeAvatar(Image.forStream(filename[filename.length - 1], resourceLoader.getResourceAsStream(Information.botAvatar)));
                } catch (DiscordException ex) {
                    throw BotException.returnException(ex);
                }
            });
        } catch (BotException ex) {
            actions.channels().exception(ex);
        }*/
        LOG.info("\n------------------------------------------------------------------------\n"
                + "*** " + Information.botName + " Ready ***\n"
                + "------------------------------------------------------------------------");
        //actions.channels().messageOwner("Startup Successful", false);
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getAuthor().isBot()) {
            return;
        }
        String message = e.getMessage().getContent();
        boolean command = message.startsWith(e.getMessage().getChannel().isPrivate() ? actions.getConfig().commChar : actions.getTable(PrefixTable.class).getPrefix(e.getMessage()));
        boolean mention = e.getMessage().getMentions().contains(client.getOurUser()) && !e.getMessage().mentionsEveryone();
        if (command || mention) {
            CommContext cont = new CommContext(e, actions);
            try {
                if (command) {
                    Entry<Command, Method> commandSet = CommandList.getCommandList().getSubCommand(cont.getArgs());
                    if (commandSet.second() != null) {
                        SubCommand subComm = commandSet.second().getAnnotation(SubCommand.class);
                        CommandInfo commandInfo = commandSet.first().getClass().getAnnotation(CommandInfo.class);
                        if (e.getMessage().getChannel().isPrivate() && (!commandInfo.pmSafe() || !subComm.pmSafe())) {
                            throw new PMNotSupportedException();
                        }
                        if (subComm.permLevel().level > cont.getCallerPerms().level || commandInfo.permLevel().level > cont.getCallerPerms().level) {
                            throw new MissingPermsException("use the " + commandInfo.commandName() + " command", subComm.permLevel().level > commandInfo.permLevel().level ? subComm.permLevel() : commandInfo.permLevel());
                        } else {
                            final int iteratorConstant = (subComm.name().equals("") ? 1 : 2);
                            List<Syntax> syntax = Arrays.stream(subComm.Syntax()).filter(
                                    v -> v.args().length + iteratorConstant == cont.getArgs().size() ||
                                            v.args().length + iteratorConstant <= cont.getArgs().size() && Arrays.stream(v.args()).anyMatch(w -> w.value() == Validate.LONGTEXT))
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
                                        if (syntax1.args()[i].equals(Validate.LONGTEXT) && Validate.LONGTEXT.isValid.test(cont.getArgs().get(i + iteratorConstant))) {
                                            break outerLoop;
                                        }
                                        if (!syntax1.args()[i].value().isValid.test(cont.getArgs().get(i + iteratorConstant))) {
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
                        invokeCommand(CommandList.getCommandList().getCommand("info"), Info.class.getMethod("execute", CommContext.class), cont);
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
        this.client = client;
        Unirest.setTimeouts(1000, 1000);
        config = loadConfig(configFile, gson.toJson(new Configuration()), Configuration.class).orElse(null);
        if (config == null) {
            LOG.info("One or more config files are empty, Exiting...");
            System.exit(1);
        }
        for (Field f : config.getClass().getFields()) {
            try {
                if (f.get(config) == null) {
                    f.set(config, f.get(new Configuration()));
                }
            } catch (IllegalAccessException e) {
                LOG.error("Error Defaulting Nulls");
            }
        }
        Rebound r = new Rebound("rr.industries.commands", false, true);
        r.getSubClassesOf(Command.class).forEach(CommandList::addCommand);
        try {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                LOG.error("Could not load SQLite JDBC Driver", ex);
            }
            Connection conn = DriverManager.getConnection("jdbc:sqlite:sovietBot.db");
            DSLContext connection = DSL.using(conn, SQLDialect.SQLITE);
            tables = new ITable[]{new PermTable(connection, config), new TimeTable(connection), new TagTable(connection), new PrefixTable(connection, config), new GreetingTable(connection)};
        } catch (SQLException | BotException ex) {
            LOG.error("Unable to Initialize Database", ex);
            System.exit(1);
        }
        LOG.info("Database Initialized");
        ChannelActions ca = new ChannelActions(client, config, info);
        actions = new BotActions(client, CommandList.getCommandList(), tables, new Module[]{new Console(ca), new UTCStatus(client), new Webserver(ca)}, ca);
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