package rr.industries.commands;

import rr.industries.CommandList;
import rr.industries.SovietBot;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/2/2016
 */
public class Regenerate extends Command {
    public Regenerate() {
        permLevel = Permissions.BOTOPERATOR;
        commandName = "regen";
        helpText = "Reinstantiates the bot's commandlist";
        deleteMessage = true;
    }

    @Override
    public void execute(CommContext cont) {
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel()).withContent("Regenerated Commands"));
        SovietBot.getBot().commandList = new CommandList();
    }
}
