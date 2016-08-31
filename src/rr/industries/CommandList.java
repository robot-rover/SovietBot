package rr.industries;

import rr.industries.commands.*;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

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
                new Unafk(), new Uptime(), new Weather()
        );

    }

    public Command getCommand(String findCommand) throws NoSuchElementException {
        return commandList.stream().filter(v -> v.commandName.equals(findCommand)).findAny().orElse(null);
    }
}
