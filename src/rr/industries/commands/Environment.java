package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.EmbedBuilder;

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
        EmbedBuilder embed = new EmbedBuilder();
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        embed.withTitle("SovietBot System Environment");
        embed.appendField("API", "Discord4J v" + Discord4J.VERSION + "", true);
        embed.appendField("Memory", (double) runtime.totalMemory() / (double) byteToMegabyte + " MB / " + (double) runtime.maxMemory() / (double) byteToMegabyte + " MB", true);
        embed.appendField("CPU", bean.getAvailableProcessors() + " cpu(s) - " + ((int) (bean.getSystemLoadAverage() * 10000) / 100f) + "%", true);
        embed.appendField("OS", bean.getName() + " v" + bean.getVersion(), true);
        embed.appendField("Java", "Java v" + System.getProperty("java.version"), true);
        embed.appendField("Launch", Discord4J.getLaunchTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)) + "\n", true);
        embed.appendField("Bot_ID", cont.getClient().getOurUser().getStringID(), true);
        cont.getActions().channels().sendMessage(cont.builder().withEmbed(embed.build()));
    }
}
