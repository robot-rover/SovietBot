package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.MessageBuilder;

import java.time.format.DateTimeFormatter;

@CommandInfo(
        commandName = "env",
        helpText = "Displays stats about the bot",
        permLevel = Permissions.BOTOPERATOR
)
public class Environment implements Command {
    private static final int byteToMegabyte = 1048576;

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Display's statistics about the bots operating environment", args = {})})
    public void execute(CommContext cont) {
        MessageBuilder message = cont.builder().withContent("```markdown\n");
        Runtime runtime = Runtime.getRuntime();
        message.appendContent("# SovietBot System Environment #\n");
        message.appendContent("<API> Discord4J v" + Discord4J.VERSION + "\n");
        message.appendContent("<Memory> " + (double) runtime.totalMemory() / (double) byteToMegabyte + " MB / " + (double) runtime.maxMemory() / (double) byteToMegabyte + " MB\n");
        message.appendContent("<OS> " + System.getProperty("os.name") + " v" + System.getProperty("os.version") + "\n");
        message.appendContent("<Java> Java v" + System.getProperty("java.version") + "\n");
        message.appendContent("<Launch> " + Discord4J.getLaunchTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
        message.appendContent("<Bot_ID> " + cont.getClient().getOurUser().getID() + "\n");
        cont.getActions().channels().sendMessage(message.appendContent("```"));
    }
}
