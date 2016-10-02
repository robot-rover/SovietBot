package rr.industries.modules;

import com.google.gson.Gson;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.modules.githubwebhooks.Ping;
import rr.industries.modules.githubwebhooks.Restart;
import rr.industries.modules.travisciwebhooks.TravisWebhook;
import rr.industries.util.ChannelActions;
import spark.Request;
import spark.Response;
import spark.Spark;
import sx.blah.discord.util.MessageBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Credit to Chrislo for the basis and the POJOs
 */
public class Webhooks implements Module {

    private static final HttpClient httpclient = HttpClients.createDefault();

    private final List<ChannelSettings> channels = new ArrayList<>();
    private final Gson gson = new Gson();
    private final Mac mac;
    private final ChannelActions actions;
    private final Logger LOG = LoggerFactory.getLogger(Webhooks.class);
    private boolean isEnabled;

    /**
     * Should be initalized in ReadyEvent
     */
    public Webhooks(ChannelActions actions) {
        isEnabled = false;
        this.actions = actions;

        final SecretKeySpec keySpec = actions.getConfig().secret == null ? null : new SecretKeySpec(actions.getConfig().secret.getBytes(), "HmacSHA1");
        Mac tmpMac = null;
        if (actions.getConfig().secret != null) {
            try {
                tmpMac = Mac.getInstance("HmacSHA1");
                tmpMac.init(keySpec);
            } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
                LOG.warn("Error Getting Decryption Mac", ex);
            }
        }
        mac = tmpMac;
    }

    @Override
    public Module enable() {
        Spark.port(actions.getConfig().webhooksPort);
        Spark.post("/github", (Request request, Response response) -> {
            if (!"application/json".equals(request.headers("Content-Type"))) {
                LOG.warn("Received Non-JSON POST");
                response.status(500);
                return "Content-Type must be application/json!";
            }

            if (actions.getConfig().secret != null && mac != null) {
                String signature = request.headers("X-Hub-Signature");

                byte[] digestBytes = mac.doFinal(request.body().getBytes());
                StringBuilder builder = new StringBuilder();
                for (byte b : digestBytes) {
                    builder.append(String.format("%02x", b));
                }

                String digest = "sha1=" + builder.toString();
                if (!signature.equals(digest)) {
                    LOG.warn("Signature did not match: got " + signature + ", digested " + digest);
                    response.status(500);
                    return "Signatures did not match!";
                }
            }

            String event = request.headers("X-Github-Event");

            if (event.equalsIgnoreCase("ping")) {
                Ping ping = gson.fromJson(request.body(), Ping.class);
                String pingMessage = "Ping from webhook " + ping.hook_id + " with zen " + ping.zen;
                LOG.info(pingMessage);
                sendMessageToChannels("Ping", pingMessage);
            }
            // 👌 OK
            response.status(200);
            return "\uD83D\uDC4C OK";

        });
        Spark.post("/command", (Request request, Response response) -> {
            Restart restart = gson.fromJson(request.body(), Restart.class);
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

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public Webhooks addChannel(ChannelSettings cs) {
        channels.add(cs);

        return this;
    }

    private void sendMessageToChannels(String event, String content) {
        LOG.info("Sent a webhook message to channels for event " + event);
        actions.sendMessage(new MessageBuilder(actions.getClient()).withContent(content).withChannel(actions.getClient().getChannelByID("170685308273164288")));
    }

    public String getJsonFromUrl(String address) throws IOException {
        URL url;
        url = new URL(address);
        InputStream is;
        URLConnection con = url.openConnection();
        is = con.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String inputLine;
        StringBuilder builder = new StringBuilder();
        while ((inputLine = br.readLine()) != null)
            builder.append(inputLine);
        br.close();
        return builder.toString();
    }

    private static class ChannelSettings {

        String id;

        /**
         * If empty, will not accept any events. Use * to indicate any event.
         */

        List<String> events = new ArrayList<>();
        /**
         * If empty, takes all repos. Syntax is name/repo.
         */

        List<String> branches = new ArrayList<>();

        public ChannelSettings(String id) {
            this.id = id;
        }

        public ChannelSettings addEvent(String event) {
            events.remove(event);
            events.add(event);

            return this;
        }

        public ChannelSettings removeEvent(String event) {
            events.remove(event);

            return this;
        }

        public ChannelSettings addRepo(String repo) {
            repo = repo.toLowerCase(Locale.ROOT);

            branches.remove(repo);
            branches.add(repo);

            return this;
        }

        public ChannelSettings removeRepo(String repo) {
            branches.remove(repo);

            return this;
        }
    }
}
