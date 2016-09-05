package rr.industries.commands;

import rr.industries.CommandList;
import rr.industries.SovietBot;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "regen",
        helpText = "Reinstantiates the bot's list of commands.",
        permLevel = Permissions.BOTOPERATOR
)
public class Regenerate implements Command {

    @Override
    public void execute(CommContext cont) {
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel()).withContent("Regenerated Commands"));
        SovietBot.getBot().commandList = new CommandList();
    }
}
