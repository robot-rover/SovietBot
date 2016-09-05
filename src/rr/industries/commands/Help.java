package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;

/**
 * Created by Sam on 8/28/2016.
 */
public class Help extends Command {
    public Help() {
        commandName = "help";
        helpText = "Displays this help message";
    }

    @Override
    public void execute(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel());
        message.appendContent("```markdown\n# " + SovietBot.botName + " v" + SovietBot.version + " - \"" + cont.getCommChar() + "\" #\n");
        for (Permissions perm : Permissions.values()) {
            message.appendContent("[Permission]: " + perm.title + "\n");
            SovietBot.getBot().commandList.commandList.stream().filter(comm -> comm.permLevel.equals(perm)).forEach(comm -> {
                message.appendContent("\t" + cont.getCommChar() + comm.commandName + " - " + comm.helpText + "\n");
            });
        }
        BotActions.sendMessage(message.appendContent("```"));
    }
}
