package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.Discord4J;
import sx.blah.discord.util.MessageBuilder;

import java.time.format.DateTimeFormatter;

@CommandInfo(
        commandName = "env",
        helpText = "Displays statistics about the bot's operating environment.",
        permLevel = Permissions.BOTOPERATOR
)
public class Environment implements Command {
    private static int byteToMegabyte = 1048576;
    @Override
    public void execute(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel()).withContent("```markdown\n");
        Runtime runtime = Runtime.getRuntime();
        message.appendContent("# SovietBot v" + SovietBot.version + " System Environment #\n");
        message.appendContent("<API> Discord4J v" + Discord4J.VERSION + "\n");
        message.appendContent("<Memory> " + (double) runtime.totalMemory() / (double) byteToMegabyte + " MB / " + (double) runtime.maxMemory() / (double) byteToMegabyte + " MB\n");
        message.appendContent("<OS> " + System.getProperty("os.name") + " v" + System.getProperty("os.version") + "\n");
        message.appendContent("<Java> Java v" + System.getProperty("java.version") + "\n");
        message.appendContent("<Launch> " + Discord4J.getLaunchTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
        message.appendContent("<Bot_ID> " + cont.getClient().getOurUser().getID() + "\n");
        BotActions.sendMessage(message.appendContent("```"));
    }
}
