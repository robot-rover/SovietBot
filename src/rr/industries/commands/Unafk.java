package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;

@CommandInfo(
        commandName = "unafk",
        helpText = "Brings AFK players to your channel.",
        permLevel = Permissions.ADMIN
)
public class Unafk implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "All players in the AFK channel will be moved to your channel", args = {})})
    public void execute(CommContext cont) throws BotException {
        MessageBuilder message = cont.builder();
        if (cont.getMessage().getAuthor().getVoiceStateForGuild(cont.getMessage().getGuild()).getChannel() == null)
            throw new IncorrectArgumentsException("You aren't connected to a voice channel");
        IVoiceChannel afk = cont.getMessage().getGuild().getAFKChannel();
        IVoiceChannel bringToo = cont.getMessage().getAuthor().getVoiceStateForGuild(cont.getMessage().getGuild()).getChannel();
        if (afk == null) {
            message.withContent("There is no AFK Channel.");
        } else {
            for (IUser user : afk.getConnectedUsers()) {
                message.appendContent("User Found in AFK: " + user.getName() + " - Moving...");
                BotUtils.bufferRequest(() -> {
                    try {
                        user.moveToVoiceChannel(bringToo);
                    } catch (DiscordException | MissingPermissionsException ex) {
                        throw BotException.returnException(ex);
                    }
                });
            }
            if (message.getContent().length() == 0)
                message.withContent("No users found in AFK channel (" + afk.getName() + ")");
        }
        cont.getActions().channels().sendMessage(message);
    }
}
