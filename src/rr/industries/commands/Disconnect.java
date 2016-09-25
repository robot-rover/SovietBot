package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.concurrent.atomic.AtomicReference;

@CommandInfo(
        commandName = "disconnect",
        helpText = "Disconnects a user from a voice channel",
        permLevel = Permissions.ADMIN
)
//todo: disconnect multiple users
public class Disconnect implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Disconnects the mentioned user", args = {Arguments.MENTION})})
    public void execute(CommContext cont) {

        if (cont.getMessage().getMentions().size() == 0) {
            cont.getActions().missingArgs(cont.getMessage().getChannel());
            return;
        }
        IUser user = cont.getMessage().getMentions().get(0);
        AtomicReference<IVoiceChannel> remove = new AtomicReference<>(null);
        RequestBuffer.request(() -> {
            try {
                remove.set(cont.getMessage().getGuild().createVoiceChannel("Disconnect"));
                user.moveToVoiceChannel(remove.get());
            } catch (DiscordException ex) {
                cont.getActions().customException("Disconnect", ex.getErrorMessage(), ex, LOG, true);
            } catch (MissingPermissionsException ex) {
                cont.getActions().missingPermissions(cont.getMessage().getChannel(), ex);
            } finally {
                RequestBuffer.request(() -> {
                    try {
                        if (remove.get() != null) {
                            remove.get().delete();
                        }
                    } catch (DiscordException ex) {
                        cont.getActions().customException("Disconnect", ex.getErrorMessage(), ex, LOG, true);
                    } catch (MissingPermissionsException ex) {
                        cont.getActions().missingPermissions(cont.getMessage().getChannel(), ex);
                    }
                });
            }
        });
    }
}
