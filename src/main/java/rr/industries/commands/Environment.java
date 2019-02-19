package rr.industries.commands;

import discord4j.core.DiscordClient;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;
import rr.industries.Information;
import rr.industries.exceptions.BotException;
import rr.industries.util.*;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Instant;
import java.time.ZoneOffset;
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
    public Mono<Void> execute(CommContext cont) throws BotException {
        return cont.getMessage().getMessage().getChannel().flatMap(channel ->
                channel.createMessage(messageSpec ->
                        messageSpec.setEmbed(v -> this.formatEmbed(v, cont.getClient()))))
                .then();
    }

    private static String formatToMegabyte(long bytes) {
        return String.format("%.2f", (double) bytes / (double) byteToMegabyte);
    }

    private void formatEmbed(EmbedCreateSpec embedSpec, DiscordClient client) {
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        embedSpec.setTitle("SovietBot System Environment");
        embedSpec.addField("Memory", formatToMegabyte(runtime.totalMemory()) + " MB / " + formatToMegabyte(runtime.maxMemory()) + " MB", true);
        embedSpec.addField("CPU", bean.getAvailableProcessors() + " cpu(s) - " + ((int) (bean.getSystemLoadAverage() * 10000) / 100f) + "%", true);
        embedSpec.addField("OS", bean.getName() + " v" + bean.getVersion(), true);
        embedSpec.addField("Java", "Java v" + System.getProperty("java.version"), true);
        embedSpec.addField("Launch", DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneOffset.UTC).format(Information.launchTime), true);
        embedSpec.addField("Bot_ID", client.getSelfId().map(Snowflake::asString).orElse("Error"), true);
    }
}
