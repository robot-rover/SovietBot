package rr.industries.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.util.BotActions;
import sx.blah.discord.api.IDiscordClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author robot_rover
 */
public class UTCStatus implements Module {
    private static Logger LOG = LoggerFactory.getLogger(UTCStatus.class);
    private static java.util.TimeZone tz = java.util.TimeZone.getTimeZone("GMT");
    private static java.util.Calendar c;
    private boolean isEnabled;
    TimerTask updateStatus;
    Timer executor;
    public UTCStatus(IDiscordClient client) {
        isEnabled = false;
        this.executor = null;
        updateStatus = new TimerTask() {
            public void run() {
                client.changePlayingText(ZonedDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("kk':'mm zzz")));
            }
        };
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Module enable(BotActions actions) {
        if (!isEnabled) {
            executor = new Timer(true);
            LocalDateTime now = LocalDateTime.now();
            executor.scheduleAtFixedRate(updateStatus, (60 - now.get(ChronoField.SECOND_OF_MINUTE)) * 1000, 60000);
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
