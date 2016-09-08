package rr.industries.commands;

import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

@CommandInfo(
        commandName = "disconnect",
        helpText = "Disconnects a user from a voice channel",
        permLevel = Permissions.ADMIN
)
public class Disconnect implements Command {
    @Override
    public void execute(CommContext cont) {

        if (cont.getArgs().size() < 2) {
            cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
        }
        IUser user;
        try {
            user = cont.getMessage().getMessage().getMentions().get(0);
        } catch (IndexOutOfBoundsException ex) {
            cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
            return;
        }
        IVoiceChannel remove = null;
        try {
            remove = cont.getMessage().getMessage().getGuild().createVoiceChannel("Disconnect");
            user.moveToVoiceChannel(remove);
        } catch (DiscordException ex) {
            cont.getActions().customException("Disconnect", ex.getErrorMessage(), ex, LOG, true);
        } catch (MissingPermissionsException ex) {
            cont.getActions().missingPermissions(cont.getMessage().getMessage().getChannel(), ex);
        } catch (RateLimitException ex) {
            //todo: implement ratelimit
        } finally {
            try {
                if (remove != null) {
                    remove.delete();
                }
            } catch (DiscordException ex) {
                cont.getActions().customException("Disconnect", ex.getErrorMessage(), ex, LOG, true);
            } catch (MissingPermissionsException ex) {
                cont.getActions().missingPermissions(cont.getMessage().getMessage().getChannel(), ex);
            } catch (RateLimitException ex) {
                //todo: implement ratelimit
            }
        }
    }
}
