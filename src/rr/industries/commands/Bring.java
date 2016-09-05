package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Created by Sam on 8/28/2016.
 */
@CommandInfo(
        commandName = "bring",
        helpText = "Brings all current users of a server to you.",
        permLevel = Permissions.ADMIN
)
public class Bring implements Command {
    @Override
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
                    Logging.error(cont.getMessage().getMessage().getGuild(), "bring", ex, LOG);
                } catch (MissingPermissionsException ex) {
                    Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "bring", ex, LOG);
                    return;
                } catch (RateLimitException ex) {
                    Logging.rateLimit(ex, this::execute, cont, LOG);
                }
            }
        }
        if (!found) {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("No Users found in Outside of your Channel").withChannel(cont.getMessage().getMessage().getChannel()));
        } else {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
        }
    }
}
