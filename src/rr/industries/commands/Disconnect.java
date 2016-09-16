package rr.industries.commands;

import rr.industries.CommandList;
import rr.industries.util.*;
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
    static {
        CommandList.defaultCommandList.add(Disconnect.class);
    }
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Disconnects the mentioned user", args = {Arguments.MENTION})})
    public void execute(CommContext cont) {

        if (cont.getArgs().size() < 2) {
            cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
            return;
        }
        if (cont.getMessage().getMessage().getMentions().size() == 0) {
            cont.getActions().wrongArgs(cont.getMessage().getMessage().getChannel());
            return;
        }
        IUser user = cont.getMessage().getMessage().getMentions().get(0);
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
