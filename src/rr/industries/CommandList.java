package rr.industries;

import rr.industries.commands.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sam
 * @project sovietBot
 * @created 8/28/2016
 */
public class CommandList {
    public List<Command> commandList;

    public CommandList() {
        commandList = Arrays.asList(
                new Bring(), new Cat(), new Coin(), new Connect(),
                new Disconnect(), new Help(), new Info(), new Invite(), new Log(), new Music(),
                new Purge(), new Quote(), new Rekt(), new Restart(), new Roll(), new Stop(),
                new Unafk(), new Uptime(), new Weather(), new Prefix(), new Rip(), new Regenerate(),
                new Environment()
        );

    }

    public Command getCommand(String findCommand) {
        Command comm = commandList.stream().filter((Command v) -> v.commandName.equals(findCommand)).findAny().orElse(null);
        return comm;
    }
}
