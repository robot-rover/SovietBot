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
//todo: Connect to Multi-Word Channels with "
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
                IVoiceChannel next = cont.getMessage().getMessage().getGuild().getVoiceChannelsByName(cont.getArgs().get(1)).get(0);
                if (!next.isConnected()) {
                    next.join();
                }
            } catch (MissingPermissionsException ex) {
                Logging.missingPermissions(cont.getMessage().getMessage().getChannel(), "connect", ex, LOG);
            } catch (NullPointerException | IndexOutOfBoundsException ex) {
                LOG.debug("Could not connect: Channel called " + cont.getArgs().get(1) + " not found");
            }
        } else {
            List<IVoiceChannel> next = cont.getMessage().getMessage().getAuthor().getConnectedVoiceChannels();
            if (next.size() >= 1) {
                BotActions.connectToChannel(next.get(0), cont.getClient().getConnectedVoiceChannels());
            }
        }
    }
}
