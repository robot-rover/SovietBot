package rr.industries;

import rr.industries.commands.Command;
import rr.industries.util.CommandInfo;

import java.util.List;

/**
 * @author Sam
 * @project sovietBot
 * @created 8/28/2016
 */
public class CommandList {
    private final List<Command> commandList;

    public CommandList(List<Command> commandList) {
        this.commandList = commandList;
    }

    public Command getCommand(String findCommand) {
        return commandList.stream().filter((Command v) -> v.getClass().isAnnotationPresent(CommandInfo.class) && v.getClass().getDeclaredAnnotation(CommandInfo.class).commandName().equals(findCommand))
                .findAny().orElse(null);
    }

    public List<Command> getCommandList() {
        return commandList;
    }
}
