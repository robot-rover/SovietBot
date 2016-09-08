package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "help",
        helpText = "Displays this help message"
)
public class Help implements Command {
    @Override
    public void execute(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel());
        message.appendContent("```markdown\n# " + SovietBot.botName + " v" + SovietBot.version + " - \"" + cont.getCommChar() + "\" #\n");
        for (Permissions perm : Permissions.values()) {
            message.appendContent("[Permission]: " + perm.title + "\n");
            cont.getActions().getCommands().getCommandList().stream().filter(comm -> comm.getClass().getDeclaredAnnotation(CommandInfo.class).permLevel().equals(perm)).forEach(comm -> {
                CommandInfo info = comm.getClass().getDeclaredAnnotation(CommandInfo.class);
                message.appendContent("\t" + cont.getCommChar() + info.commandName() + " - " + info.helpText() + "\n");
            });
        }
        cont.getActions().sendMessage(message.appendContent("```"));
    }
}
