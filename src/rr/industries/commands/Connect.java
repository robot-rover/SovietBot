package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.Logging;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.List;

/**
 * Created by Sam on 8/28/2016.
 */
public class Connect extends Command {
    public Connect() {
        commandName = "connect";
        helpText = "Connects and disconnects the bot from voice channels.";
    }

    @Override
    public void execute(CommContext cont) {

        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).equals("/")) {
            BotActions.disconnectFromChannel(cont.getMessage().getMessage().getGuild(), cont.getClient().getConnectedVoiceChannels());
        } else if (cont.getArgs().size() >= 2) {
            try {
                String channelName;
                if (cont.getMessage().getMessage().getContent().matches("\".+\"")) {
                    channelName = cont.getMessage().getMessage().getContent().split("\"")[1];
                } else {
                    channelName = cont.getArgs().get(1);
                }
                List<IVoiceChannel> next = cont.getMessage().getMessage().getGuild().getVoiceChannelsByName(channelName);
                if (next.size() > 0) {
                    if (!next.get(0).isConnected())
                        next.get(0).join();
                } else {
                    Logging.notFound(cont.getMessage(), "connect", "Voice Channel", channelName, LOG);
                }
            } catch (MissingPermissionsException ex) {
                Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "connect", ex, LOG);
            }
        } else {
            List<IVoiceChannel> next = cont.getMessage().getMessage().getAuthor().getConnectedVoiceChannels();
            if (next.size() >= 1) {
                BotActions.connectToChannel(next.get(0), cont.getClient().getConnectedVoiceChannels());
            }
        }
    }
}
