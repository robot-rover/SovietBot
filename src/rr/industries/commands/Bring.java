package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;


@CommandInfo(
        commandName = "bring",
        helpText = "Brings all current users of a server to you.",
        permLevel = Permissions.ADMIN
)
//todo: @mention to bring one user to your channel
public class Bring implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Moves all users connected to a voice channel to your channel", args = {})})
    public void execute(CommContext cont) throws BotException {
        boolean found = false;
        IUser[] Users = cont.getMessage().getGuild().getUsers().toArray(new IUser[0]);
        IVoiceChannel back;
        IVoiceChannel current;
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        try {
            back = cont.getMessage().getAuthor().getConnectedVoiceChannels().get(0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return;
        }
        for (IUser user : Users) {
            try {
                current = user.getConnectedVoiceChannels().get(0);
            } catch (ArrayIndexOutOfBoundsException ex) {
                continue;
            }
            if (current != back) {
                found = true;
                if (!message.getContent().equals("")) {
                    message.appendContent("\n");
                }
                message.appendContent("Moving " + user.getName() + " to " + back.toString());
                BotUtils.bufferRequest(() -> {
                    try {
                        user.moveToVoiceChannel(back);
                    } catch (DiscordException | MissingPermissionsException ex) {
                        BotException.translateException(ex);
                    }
                });
            }
        }
        if (!found) {
            cont.getActions().channels().sendMessage(message.withContent("No Users found in Outside of your Channel"));
        } else {
            cont.getActions().channels().sendMessage(message);
        }
    }
}
