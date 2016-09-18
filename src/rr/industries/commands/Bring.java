package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;


@CommandInfo(
        commandName = "bring",
        helpText = "Brings all current users of a server to you.",
        permLevel = Permissions.ADMIN
)
//todo: @mention to bring one user to your channel
public class Bring implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Moves all users connected to a voice channel to your channel", args = {})})
    public void execute(CommContext cont) {

        String message = "";
        boolean found = false;
        IUser[] Users = cont.getMessage().getMessage().getGuild().getUsers().toArray(new IUser[0]);
        IVoiceChannel back;
        IVoiceChannel current;
        try {
            back = cont.getMessage().getMessage().getAuthor().getConnectedVoiceChannels().get(0);
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
                if (!message.equals("")) {
                    message = message + "\n";
                }
                message = message + "Moving " + user.getName() + " to " + back.toString();
                try {
                    user.moveToVoiceChannel(back);
                } catch (DiscordException ex) {
                    cont.getActions().customException("Bring", ex.getErrorMessage(), ex, LOG, true);
                } catch (MissingPermissionsException ex) {
                    cont.getActions().missingPermissions(cont.getMessage().getMessage().getChannel(), ex);
                    return;
                } catch (RateLimitException ex) {
                    //todo: implement ratelimit
                }
            }
        }
        if (!found) {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("No Users found in Outside of your Channel")
                    .withChannel(cont.getMessage().getMessage().getChannel()));
        } else {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent(message)
                    .withChannel(cont.getMessage().getMessage().getChannel()));
        }
    }
}
