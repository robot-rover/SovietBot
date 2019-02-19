package rr.industries.util;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import rr.industries.CommandList;
import rr.industries.Configuration;
import rr.industries.Information;
import rr.industries.exceptions.BotException;
import rr.industries.modules.Module;
import rr.industries.util.sql.Table;

import java.util.NoSuchElementException;

/**
 * @author robot_rover
 */
public final class BotActions {
    private static final Logger LOG = LoggerFactory.getLogger(BotActions.class);
    private final DiscordClient client;
    private final CommandList commands;
    private final Table[] tables;
    private final Module[] modules;
    private final Configuration config;
    private final Information info;

    public BotActions(DiscordClient client, CommandList commands, Table[] tables, Module[] modules, Configuration config, Information info) {
        this.client = client;
        this.tables = tables;
        this.commands = commands;
        this.modules = modules;
        this.config = config;
        this.info = info;
    }

    public void enableModules() {
        for (Module m : modules) {
            if (!m.isEnabled())
                m.enableModule(this);
        }
    }

    public void disableModules() {
        for (Module m : modules) {
            if (m.isEnabled())
                m.disableModule();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> moduleType) {
        for (Module module : modules) {
            if (moduleType.isAssignableFrom(module.getClass()))
                return (T) module;
        }
        throw new NoSuchElementException("Table of type: " + moduleType.getName() + " not found!");
    }

    @SuppressWarnings("unchecked")
    public <T extends Table> T getTable(Class<T> tableType) {
        for (Table table : tables) {
            if (table.getClass().equals(tableType))
                return (T) table;
        }
        throw new NoSuchElementException("Table of type: " + tableType.getName() + " not found!");
    }

    public <T extends BotException> void exception(T exception) {
        throw new UnsupportedOperationException();
        /*if (exception.criticalMessage().isPresent()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String sStackTrace = sw.toString();
//            messageOwner("[Critical Error] - " + exception.criticalMessage().get() + "\n```" + sStackTrace + "```", true);
        }*/
    }

    public Mono<Void> messageOwner(String message) {
        Mono<Void> exec = Mono.empty();
        for(long opId : config.operators) {
            exec = exec.then(client.getUserById(Snowflake.of(opId)).flatMap(User::getPrivateChannel).flatMap(v -> v.createMessage(message))).then();
        }
        return exec;
    }

    public void terminate() {
        client.logout();
        System.exit(0);
    }

    public DiscordClient getClient() {
        return client;
    }

    public CommandList getCommands() {
        return commands;
    }

    public Configuration getConfig() {
        return config;
    }

}
