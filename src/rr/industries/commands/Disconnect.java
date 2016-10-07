package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;

@CommandInfo(
        commandName = "disconnect",
        helpText = "Disconnects a user from a voice channel",
        permLevel = Permissions.ADMIN
)
//todo: disconnect multiple users
public class Disconnect implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Disconnects the mentioned user", args = {Arguments.MENTION})})
    public void execute(CommContext cont) throws BotException {

        if (cont.getMessage().getMentions().size() == 0) {
            throw new IncorrectArgumentsException("You didn't mention any users");
        }
        IUser user = cont.getMessage().getMentions().get(0);
        BotUtils.bufferRequest(() -> {
            try {
                IVoiceChannel channel = cont.getMessage().getGuild().createVoiceChannel("Disconnect");
                user.moveToVoiceChannel(channel);
                channel.delete();
            } catch (DiscordException | MissingPermissionsException ex) {
                BotException.translateException(ex);
            }
        });
    }
}
