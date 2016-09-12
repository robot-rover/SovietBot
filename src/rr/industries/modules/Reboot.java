package rr.industries.modules;

import rr.industries.util.BotActions;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.DiscordDisconnectedEvent;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/9/2016
 */
public class Reboot implements Module {
    private boolean isEnabled;
    BotActions actions;

    public Reboot(BotActions actions) {
        this.actions = actions;
        actions.getClient().getDispatcher().registerListener(this);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void enable() {
        isEnabled = true;
    }

    @Override
    public void disable() {
        isEnabled = false;
    }

    @EventSubscriber
    public void onDisconnect(DiscordDisconnectedEvent e) {
        if (e.getReason() == DiscordDisconnectedEvent.Reason.RECONNECTION_FAILED) {
            boolean restart = actions.saveLog();
            actions.terminate(restart);
        }
    }
}
