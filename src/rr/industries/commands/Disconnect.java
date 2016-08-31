package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.Logging;
import rr.industries.util.Permissions;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Created by Sam on 8/28/2016.
 */
public class Disconnect extends Command {
    public Disconnect() {
        permLevel = Permissions.ADMIN;
        commandName = "disconnect";
        helpText = "Disconnects a user from a voice channel.";
    }

    @Override
    public void execute(CommContext cont) {

        if (cont.getArgs().size() < 2) {
            Logging.missingArgs(cont.getMessage(), "disconnect", cont.getArgs(), LOG);
        }
        IUser user;
        try {
            user = cont.getMessage().getMessage().getMentions().get(0);
        } catch (IndexOutOfBoundsException ex) {
            Logging.missingArgs(cont.getMessage(), "disconnect", cont.getArgs(), LOG);
            return;
        }
        IVoiceChannel remove;
        try {
            remove = cont.getMessage().getMessage().getGuild().createVoiceChannel("Disconnect");
            try {
                user.moveToVoiceChannel(remove);
            } catch (DiscordException ex) {
                Logging.error(cont.getMessage().getMessage().getGuild(), "disconnect", ex, LOG);
            } catch (MissingPermissionsException ex) {
                Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "disconnect", ex, LOG);
            } catch (RateLimitException ex) {
                Logging.rateLimit(ex, this::execute, cont, LOG);
            }
            remove.delete();
        } catch (DiscordException ex) {
            Logging.error(cont.getMessage().getMessage().getGuild(), "disconnect", ex, LOG);
        } catch (MissingPermissionsException ex) {
            Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "disconnect", ex, LOG);
        } catch (RateLimitException ex) {
            Logging.rateLimit(ex, this::execute, cont, LOG);
        }
    }
}
