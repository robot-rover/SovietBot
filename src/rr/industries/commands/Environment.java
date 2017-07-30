package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.MessageBuilder;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@CommandInfo(
        commandName = "env",
        helpText = "Displays stats about the bot",
        permLevel = Permissions.BOTOPERATOR,
        pmSafe = true
)
public class Environment implements Command {
    private static final int byteToMegabyte = 1048576;

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Display's statistics about the bots operating environment", args = {})})
    public void execute(CommContext cont) {
        MessageBuilder message = cont.builder().withContent("```markdown\n");
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        message.appendContent("# SovietBot System Environment #\n");
        message.appendContent("<API> Discord4J v" + Discord4J.VERSION + "\n");
        message.appendContent("<Memory> " + (double) runtime.totalMemory() / (double) byteToMegabyte + " MB / " + (double) runtime.maxMemory() / (double) byteToMegabyte + " MB\n");
        message.appendContent("<CPU> " + bean.getAvailableProcessors() + " available processor(s) - " + ((int) (bean.getSystemLoadAverage() * 10000) / 100f) + "%\n");
        message.appendContent("<OS> " + bean.getName() + " v" + bean.getVersion() + "\n");
        message.appendContent("<Java> Java v" + System.getProperty("java.version") + "\n");
        message.appendContent("<Launch> " + Discord4J.getLaunchTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)) + "\n");
        message.appendContent("<Bot_ID> " + cont.getClient().getOurUser().getStringID() + "\n");
        cont.getActions().channels().sendMessage(message.appendContent("```"));
    }
}
