package rr.industries.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.Status;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/9/2016
 */
public class UTCStatus implements Module {
    private static Logger LOG = LoggerFactory.getLogger(UTCStatus.class);
    private static java.util.TimeZone tz = java.util.TimeZone.getTimeZone("GMT");
    private static java.util.Calendar c;
    private boolean isEnabled;
    private int displayedTime;
    TimerTask updateStatus;
    Timer executor;
    public UTCStatus(IDiscordClient client) {
        isEnabled = false;
        this.executor = null;
        displayedTime = 0;
        updateStatus = new TimerTask() {
            public void run() {
                if (java.util.Calendar.getInstance(tz).get(Calendar.MINUTE) != displayedTime) {
                    c = java.util.Calendar.getInstance(tz);
                    displayedTime = c.get(Calendar.MINUTE);
                    client.changeStatus(Status.game("UTC " + Integer.toString(c.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%2s", c.get(Calendar.MINUTE)).replace(" ", "0")));
                }
            }
        };
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Module enable() {
        if (!isEnabled) {
            executor = new Timer(false);
            executor.scheduleAtFixedRate(updateStatus, 0, 20000);
            isEnabled = true;
        }
        return this;
    }

    @Override
    public Module disable() {
        if (isEnabled) {
            if (executor != null)
                executor.cancel();
            isEnabled = false;
        }
        return this;
    }
}
