package rr.industries.modules;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.pojos.RestartPost;
import rr.industries.pojos.travisciwebhooks.TravisWebhook;
import rr.industries.util.ChannelActions;
import spark.Request;
import spark.Response;
import spark.Spark;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.MessageBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Credit to Chrislo for the basis and the POJOs
 */
public class Webhooks implements Module {

    private final Gson gson = new Gson();
    private final ChannelActions actions;
    private final Logger LOG = LoggerFactory.getLogger(Webhooks.class);
    private boolean isEnabled;

    /**
     * Should be initalized in ReadyEvent
     */
    public Webhooks(ChannelActions actions) {
        isEnabled = false;
        this.actions = actions;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Module enable() {
        Spark.port(actions.getConfig().webhooksPort);
        Spark.post("/command", (Request request, Response response) -> {
            RestartPost restart = gson.fromJson(request.body(), RestartPost.class);
            LOG.info("Command POST received - " + restart.command);
            if (!restart.secret.equals(actions.getConfig().secret)) {
                response.status(418);
                return "I'm a teapot";
            }
            if (restart.command.equals("restart")) {
                LOG.info("Everything Looks good, Restarting...");
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        actions.terminate(true);
                    }
                };
                thread.start();
                response.status(200);
                return "Looks good: Restarting...";
            }
            if (restart.command.equals("profile")) {
                String channelID = "236640204222496768";
                IGuild guild = actions.getClient().getChannelByID(channelID).getGuild();
                LOG.info("Developer Profile Recieved, Uploading to " + guild.getName() + " (" + guild.getID()
                        + ") @ " + actions.getClient().getChannelByID(channelID).getName() + " (" + channelID + ")");
                File profile = new File(restart.name + "_" + System.currentTimeMillis() + ".txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(profile));
                for (String line : restart.linesOfLog) {
                    writer.write(line);
                    writer.write("\n");
                }
                writer.close();
                actions.getClient().getChannelByID(channelID).sendFile(profile);

            }
            response.status(200);
            return "\uD83D\uDC4C OK";
        });
        Spark.post("/travis", (Request request, Response response) -> {
            try {
                LOG.info("Received Travis Post");
                TravisWebhook payload = gson.fromJson(URLDecoder.decode(request.body(), "UTF-8").replace("payload=", ""), TravisWebhook.class);
                StringBuilder message = new StringBuilder("Travis-Ci Build");
                if (payload.repository != null)
                    if (payload.repository.name != null)
                        message.append(": ").append(payload.repository.name);
                if (payload.statusMessage != null)
                    message.append(" **").append(payload.statusMessage).append("**\n");
                if (payload.authorName != null)
                    message.append("[*").append(payload.authorName).append("*]");
                if (payload.repository != null)
                    if (payload.branch != null)
                        message.append(" - ").append(payload.branch);
                message.append("\n");
                if (payload.startedAt != null) {
                    message.append("Started ").append(DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).format(ZonedDateTime.parse(payload.startedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC")))));

                }
                if (payload.duration != null)
                    message.append(" -> ").append(payload.duration).append("s");
                sendMessageToChannels("Travis Build", message.toString());
            } catch (Exception e) {
                LOG.error("Error Responding to Travis Webhook", e);
                response.status(500);
                return e.getMessage();
            }
            response.status(200);
            return "\uD83D\uDC4C OK";
        });

        LOG.info("Initialized webhooks on port " + actions.getConfig().webhooksPort);
        isEnabled = true;
        return this;
    }

    @Override
    public Module disable() {
        Spark.stop();
        isEnabled = false;
        return this;
    }

    private void sendMessageToChannels(String event, String content) {
        LOG.info("Sent a webhook message to channels for event " + event);
        actions.sendMessage(new MessageBuilder(actions.getClient()).withContent(content).withChannel(actions.getClient().getChannelByID("170685308273164288")));
    }

}
