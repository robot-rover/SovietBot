package rr.industries;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.Unirest;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import gigadot.rebound.Rebound;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import rr.industries.commands.Command;
import rr.industries.commands.Info;
import rr.industries.exceptions.*;
import rr.industries.modules.SwearFilter;
import rr.industries.modules.Webserver;
import rr.industries.util.*;
import rr.industries.util.sql.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SovietBot - the Youtube Streaming Discord Bot
 * @author robot_rover
 */

public class SovietBot {

    //gets rid of obnoxious jooq banner in logs
    static {System.setProperty("org.jooq.no-logo", "true");}

    private boolean readyCalled = false;
    private static final Logger LOG = LoggerFactory.getLogger(SovietBot.class);
    private static final File configFile = new File("configuration.json");
    public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private Configuration config;
    private volatile DiscordClient client;
    private Table[] tables;
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

    public Mono<Void> onUserJoin(MemberJoinEvent e) {
        Entry<String, Long> messageContent = actions.getTable(GreetingTable.class).getJoinMessage(e.getGuildId());
        if(messageContent.first() == null || messageContent.second() == null)
            return Mono.empty();
        String content = messageContent.first().replace("%user", e.getMember().getMention());
        return e.getGuild().flatMap(v -> v.getChannelById(Snowflake.of(messageContent.second()))).cast(TextChannel.class).flatMap(v -> v.createMessage(content)).then();
    }

    public Mono<Void> onUserLeave(MemberLeaveEvent e) {
        Entry<String, Long> messageContent = actions.getTable(GreetingTable.class).getLeaveMessage(e.getGuildId());
        if(messageContent.first() == null || messageContent.second() == null)
            return Mono.empty();
        actions.getTable(PermTable.class).setPerms(e.getGuildId(), e.getUser().getId(), Permissions.NORMAL);
        String content = messageContent.first().replace("%user", "`" + e.getUser().getUsername() + "`");
        return e.getGuild().flatMap(v -> v.getChannelById(Snowflake.of(messageContent.second()))).cast(TextChannel.class).flatMap(v -> v.createMessage(content)).then();
    }

    private Mono<Void> handleBotException(BotException e, Channel channel) {
        if(e.shouldLog()) {
            LOG.error("Bot threw an error", e);
        }
        if(channel != null) {
            e.setChannel(channel);
            return e.handle();
        }
        return Mono.empty();
    }

    public Mono<Void> onReady(ReadyEvent e) {
        readyCalled = true;
        if(client.getSelfId().isPresent()) {
            Information.setClientID(client.getSelfId().get());
        } else {
            throw new RuntimeException("Client was not logged in when ready was called");
        }
        actions.enableModules();
        LOG.info("\n------------------------------------------------------------------------\n"
                + "*** " + Information.botName + " Ready ***\n"
                + "------------------------------------------------------------------------");
        return client.updatePresence(Presence.online(Activity.watching(e.getGuilds().size() + " servers")));
    }

    public Mono<Void> onGuildJoin(GuildCreateEvent e) {
        return client.getGuilds().count().flatMap(v -> client.updatePresence(Presence.online(Activity.watching(v + " servers"))));
    }

    public Mono<Void> onMessage(MessageCreateEvent e) {
        String message = e.getMessage().getContent().orElse(null);
        if(message == null)
            return Mono.empty();
        Mono<Boolean> isCommand = e.getMessage().getChannel().map(Channel::getType)
                .map(v -> v.equals(Channel.Type.GUILD_TEXT))
                .flatMap(v -> v ? actions.getTable(GreetingTable.class).getPrefix(e.getMessage()) : Mono.just(actions.getConfig().commChar))
                .map(message::startsWith);
        boolean isMentionUs = client.getSelfId().map(v -> e.getMessage().getUserMentionIds().contains(v)).orElse(false);
        Mono<CommContext> cont = CommContext.getCommContext(e, actions);
        return Mono.zip(isCommand, e.getMessage().getGuild().hasElement(), cont).flatMap(data -> {
            try {
                if (data.getT1()) {
                    Entry<Command, Method> commandSet = CommandList.getCommandList().getSubCommand(data.getT3().getArgs());
                    if (commandSet.second() != null) {
                        SubCommand subComm = commandSet.second().getAnnotation(SubCommand.class);
                        CommandInfo commandInfo = commandSet.first().getClass().getAnnotation(CommandInfo.class);
                        if (!data.getT2() && (!commandInfo.pmSafe() || !subComm.pmSafe())) {
                            throw new PMNotSupportedException();
                        }
                        if (subComm.permLevel().level > data.getT3().getCallerPerms().level || commandInfo.permLevel().level > data.getT3().getCallerPerms().level) {
                            throw new MissingPermsException("use the " + commandInfo.commandName() + " command", subComm.permLevel().level > commandInfo.permLevel().level ? subComm.permLevel() : commandInfo.permLevel());
                        } else {
                            final int iteratorConstant = (subComm.name().equals("") ? 1 : 2);
                            List<Syntax> syntax = Arrays.stream(subComm.Syntax()).filter(
                                    v -> v.args().length + iteratorConstant == data.getT3().getArgs().size() ||
                                            v.args().length + iteratorConstant <= data.getT3().getArgs().size() && Arrays.stream(v.args()).anyMatch(w -> w.value() == Validate.LONGTEXT))
                                    .collect(Collectors.toList());
                            if (commandSet.first().getValiddityOverride() != null) {
                                if (!commandSet.first().getValiddityOverride().test(data.getT3().getArgs())) {
                                    throw new IncorrectArgumentsException();
                                }
                            } else if (!syntax.isEmpty()) {
                                boolean found = false;
                                outerLoop:
                                for (Syntax syntax1 : syntax) {
                                    found = true;
                                    for (int i = 0; i < syntax1.args().length; i++) {
                                        if (syntax1.args()[i].value().equals(Validate.LONGTEXT) && Validate.LONGTEXT.isValid.test(data.getT3().getArgs().get(i + iteratorConstant))) {
                                            break outerLoop;
                                        }
                                        if (!syntax1.args()[i].value().isValid.test(data.getT3().getArgs().get(i + iteratorConstant))) {
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
                            return invokeCommand(commandSet.first(), commandSet.second(), data.getT3(), e);
                        }
                    }
                } else if (isMentionUs) {
                    try {
                        return invokeCommand(CommandList.getCommandList().getCommand("info"), Info.class.getMethod("execute", CommContext.class), data.getT3(), e);
                    } catch (NoSuchMethodException ex) {
                        throw new ServerError("Could not execute Info command for mention", ex);
                    }
                }
            } catch (BotException ex) {
                return handleBotException(ex, data.getT3().getChannel());
            }
            return Mono.empty();
        });
    }

    private Mono<Void> invokeCommand(Command c, Method m, CommContext cont, MessageCreateEvent e) throws BotException {
        try {
            Mono<Void> ret = ((Mono<Void>) m.invoke(c, cont));
            if(ret == null)
                throw new ServerError("Command returned null");
            return ret;
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
        return Mono.empty();
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

    public Mono<Void> enable(DiscordClient client) {
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
            tables = new Table[]{new PermTable(connection, config), new TagTable(connection), new GreetingTable(connection, config)};
        } catch (SQLException | BotException ex) {
            LOG.error("Unable to Initialize Database", ex);
            System.exit(1);
        }
        LOG.info("Database Initialized");
        //ChannelActions ca = new ChannelActions(client, config, info);
        actions = new BotActions(client, CommandList.getCommandList(), tables, new rr.industries.modules.Module[]{new Webserver(), new SwearFilter()}, config, info);

        Mono<Void> userLeft = client.getEventDispatcher()
                .on(MemberLeaveEvent.class)
                .flatMap(this::onUserLeave)
                .onErrorContinue(this::logError)
                .then();
        Mono<Void> userJoined = client.getEventDispatcher()
                .on(MemberJoinEvent.class)
                .flatMap(this::onUserJoin)
                .onErrorContinue(this::logError)
                .then();
        Mono<Void> messageRecieved = client.getEventDispatcher()
                .on(MessageCreateEvent.class)
                .filter(event -> event.getMessage().getAuthor().map(user -> !user.isBot()).orElse(true))
                .flatMap(this::onMessage)
                .onErrorContinue(this::logError)
                .then();
        Mono<Void> onReady = client.getEventDispatcher()
                .on(ReadyEvent.class)
                .flatMap(this::onReady)
                .then();
        return messageRecieved.and(onReady).and(userJoined).and(userLeft);
    }

    private void logError(Throwable e, Object idk) {
        LOG.error("Bot threw an uncaught exception - " + idk, new RuntimeException(e));
    }
}
