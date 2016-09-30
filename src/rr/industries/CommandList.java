package rr.industries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.Command;
import rr.industries.util.CommandInfo;
import rr.industries.util.Entry;
import rr.industries.util.SubCommand;

import java.lang.reflect.Method;
import java.security.ProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Sam
 * @project sovietBot
 * @created 8/28/2016
 */
public class CommandList {
    private static Logger LOG = LoggerFactory.getLogger(CommandList.class);
    private static volatile List<Class<? extends Command>> defaultCommandList = new ArrayList<>();
    private final List<Command> commandList;

    public CommandList() {
        this.commandList = new ArrayList<>();
        for (Class<? extends Command> com : defaultCommandList) {
            try {
                this.commandList.add(com.newInstance());
            } catch (InstantiationException | IllegalAccessException ex) {
                LOG.error("Unable to Instantiate Command Class " + com.getCanonicalName(), ex);
            }
        }
        LOG.info("Initialized CommandList - Size: {}", defaultLength());

    }

    public static boolean isCommand(String commName) {
        for (Class<? extends Command> comm : defaultCommandList) {
            CommandInfo commandInfo = comm.getAnnotation(CommandInfo.class);
            if (commandInfo != null && commandInfo.commandName().equals(commName)) {
                return true;
            }
        }
        return false;
    }

    public Command getCommand(String findCommand) {
        try {
            return commandList.stream().filter((Command v) -> v.getClass().getDeclaredAnnotation(CommandInfo.class).commandName().equals(findCommand))
                    .findAny().orElse(null);
        } catch (NullPointerException ex) {
            throw new ProviderException("CommandInfo annotation is misconfigured", ex);
        }
    }

    public static void addCommand(Class<? extends Command> comm) {
        defaultCommandList.add(comm);
    }

    public List<Command> getCommandList() {
        return commandList;
    }

    public static int defaultLength() {
        return defaultCommandList.size();
    }

    @Override
    public String toString() {
        return commandList.size() + " commands registered";
    }

    public Entry<Command, Method> getSubCommand(List<String> args) {
        Command command = getCommand(args.get(0));
        Method subCommand = null;
        if (command != null) {
            CommandInfo info = command.getClass().getDeclaredAnnotation(CommandInfo.class);
            Method baseSubCommand = null;
            for (Method subComm : command.getClass().getDeclaredMethods()) {
                if (subComm.getAnnotation(SubCommand.class) == null) {
                    continue;
                }
                if (args.size() >= 2 && subComm.getAnnotation(SubCommand.class).name().equals(args.get(1))) {
                    subCommand = subComm;
                }
                if (subComm.getAnnotation(SubCommand.class).name().equals("")) {
                    baseSubCommand = subComm;
                }

            }
            if (subCommand == null && baseSubCommand != null) {
                subCommand = baseSubCommand;
            }
        }
        if (subCommand == null) {
            throw new NoSuchElementException("Couldn't find command " + args.get(0));
        }
        return new Entry<>(command, subCommand);
    }
}
