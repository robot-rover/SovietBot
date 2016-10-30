package rr.industries.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.exceptions.BotException;
import rr.industries.util.ChannelActions;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;

/**
 * @author robot_rover
 */
public class Reboot implements Module {
    private static final Logger LOG = LoggerFactory.getLogger(Reboot.class);
    private boolean isEnabled;
    ChannelActions actions;

    public Reboot(ChannelActions actions) {
        this.actions = actions;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Module enable() {
        actions.getClient().getDispatcher().registerListener(this);
        isEnabled = true;
        return this;
    }

    @Override
    public Module disable() {
        actions.getClient().getDispatcher().unregisterListener(this);
        isEnabled = false;
        return this;
    }

    @EventSubscriber
    public void onDisconnect(DiscordDisconnectedEvent e) throws BotException {
        if (e.getReason().equals(DiscordDisconnectedEvent.Reason.LOGGED_OUT)) {
            LOG.info("Successfully Logged Out...");
        } else {
            LOG.warn("Disconnected Unexpectedly: " + e.getReason().name(), e);
            if (e.getReason().equals(DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED)) {
                LOG.info("All Reconnections Failed... Restarting");
                actions.saveLog();
                actions.terminate(true);
            }
        }
    }
}
