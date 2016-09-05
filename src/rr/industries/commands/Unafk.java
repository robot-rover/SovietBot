package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

@CommandInfo(
        commandName = "unafk",
        helpText = "Brings AFK players to your channel.",
        permLevel = Permissions.ADMIN
)
public class Unafk implements Command {
    public void execute(CommContext cont) {
        String message = "";
        boolean found = false;
        IVoiceChannel afk = cont.getMessage().getMessage().getGuild().getAFKChannel();
        if (afk == null) {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("There is no AFK Channel.").withChannel(cont.getMessage().getMessage().getChannel()));
            return;
        }
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
            if (current == afk) {
                found = true;
                if (!message.equals("")) {
                    message = message + "\n";
                }
                message = message + "User Found in AFK: " + user.getName() + " - Moving to " + back.toString();
                try {
                    user.moveToVoiceChannel(back);
                } catch (DiscordException | RateLimitException ex) {
                    Logging.error(cont.getMessage().getMessage().getGuild(), "unafk", ex, LOG);
                } catch (MissingPermissionsException ex) {
                    Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "unafk", ex, LOG);
                    return;
                }
            }
        }
        if (!found) {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("No Users found in AFK Channel").withChannel(cont.getMessage().getMessage().getChannel()));

        } else {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
        }
    }
}
