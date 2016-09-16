package rr.industries.commands;

import rr.industries.CommandList;
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
    static {
        CommandList.defaultCommandList.add(Unafk.class);
    }
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "All players in the AFK channel will be moved to your channel", args = {})})
    public void execute(CommContext cont) {
        String message = "";
        boolean found = false;
        IVoiceChannel afk = cont.getMessage().getMessage().getGuild().getAFKChannel();
        if (afk == null) {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("There is no AFK Channel.").withChannel(cont.getMessage().getMessage().getChannel()));
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
                } catch (DiscordException ex) {
                    cont.getActions().customException("Unafk", ex.getMessage(), ex, LOG, true);
                } catch (MissingPermissionsException ex) {
                    cont.getActions().missingPermissions(cont.getMessage().getMessage().getChannel(), ex);
                    return;
                } catch (RateLimitException ex) {
                    //todo: impl ratelimit
                }
            }
        }
        if (!found) {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("No Users found in AFK Channel").withChannel(cont.getMessage().getMessage().getChannel()));

        } else {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
        }
    }
}
