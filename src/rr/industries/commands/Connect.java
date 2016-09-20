package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandInfo(
        commandName = "connect",
        helpText = "Connects and disconnects the bot from voice channels."
)
public class Connect implements Command {
    @SubCommand(name = "/", Syntax = {@Syntax(helpText = "Disconnects the bot from all voice channels", args = {})})
    public void disconnect(CommContext cont) {
        BotActions.disconnectFromChannel(cont.getMessage().getGuild(), cont.getClient().getConnectedVoiceChannels());
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Connects the bot to the Voice Channel provided", args = {@ArgSet(arg = Arguments.VOICECHANNEL)}),
            @Syntax(helpText = "Connects the bot to the voice channel you are connected too", args = {})
    })
    public void execute(CommContext cont) {

        if (cont.getArgs().size() >= 2) {
            try {
                String channelName;
                Pattern p = Pattern.compile("\".+\"");
                Matcher m = p.matcher(cont.getMessage().getContent());
                if (m.find()) {
                    channelName = cont.getMessage().getContent().split("\"")[1];
                } else {
                    channelName = cont.getArgs().get(1);
                }
                List<IVoiceChannel> next = cont.getMessage().getGuild().getVoiceChannelsByName(channelName);
                if (next.size() > 0) {
                    if (!next.get(0).isConnected())
                        next.get(0).join();
                } else {
                    cont.getActions().notFound(cont.getMessage(), "connect", "Voice Channel", channelName, LOG);
                }
            } catch (MissingPermissionsException ex) {
                cont.getActions().missingPermissions(cont.getMessage().getChannel(), ex);
            }
        } else {
            List<IVoiceChannel> next = cont.getMessage().getAuthor().getConnectedVoiceChannels();
            if (next.size() >= 1) {
                cont.getActions().connectToChannel(next.get(0), cont.getClient().getConnectedVoiceChannels());
            }
        }
    }
}
