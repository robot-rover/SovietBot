package rr.industries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.*;
import rr.industries.util.CommandInfo;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sam
 * @project sovietBot
 * @created 8/28/2016
 */
public class CommandList {
    public List<Command> commandList;
    public Logger LOG;

    public CommandList() {
        LOG = LoggerFactory.getLogger(this.getClass());
        commandList = Arrays.asList(
                new Bring(), new Cat(), new Coin(), new Connect(),
                new Disconnect(), new Help(), new Info(), new Invite(), new Log(), new Music(),
                new Purge(), new Quote(), new Rekt(), new Restart(), new Roll(), new Stop(),
                new Unafk(), new Uptime(), new Weather(), new Prefix(), new Rip(), new Regenerate(),
                new Environment(), new Echo()
        );

    }

    public Command getCommand(String findCommand) {
        return commandList.stream().filter((Command v) -> v.getClass().isAnnotationPresent(CommandInfo.class) && v.getClass().getDeclaredAnnotation(CommandInfo.class).commandName().equals(findCommand))
                .findAny().orElse(null);
    }
}
