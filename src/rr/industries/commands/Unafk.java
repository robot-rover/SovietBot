package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

@CommandInfo(
        commandName = "unafk",
        helpText = "Brings AFK players to your channel.",
        permLevel = Permissions.ADMIN
)
public class Unafk implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "All players in the AFK channel will be moved to your channel", args = {})})
    public void execute(CommContext cont) {
        String message = "";
        boolean found = false;
        IVoiceChannel afk = cont.getMessage().getGuild().getAFKChannel();
        if (afk == null) {
            cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withContent("There is no AFK Channel.").withChannel(cont.getMessage().getChannel()));
            return;
        }
        IUser[] Users = cont.getMessage().getGuild().getUsers().toArray(new IUser[0]);
        IVoiceChannel back;
        IVoiceChannel current;
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
            if (current == afk) {
                found = true;
                if (!message.equals("")) {
                    message = message + "\n";
                }
                message = message + "User Found in AFK: " + user.getName() + " - Moving to " + back.toString();
                RequestBuffer.request(() -> {
                    try {
                        user.moveToVoiceChannel(back);
                    } catch (DiscordException ex) {
                        cont.getActions().channels().customException("Unafk", ex.getMessage(), ex, LOG, true);
                    } catch (MissingPermissionsException ex) {
                        cont.getActions().channels().missingPermissions(cont.getMessage().getChannel(), ex);
                    }
                });
            }
        }
        if (!found) {
            cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withContent("No Users found in AFK Channel").withChannel(cont.getMessage().getChannel()));

        } else {
            cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getChannel()));
        }
    }
}
