package rr.industries.modules;

import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.SovietBot;
import rr.industries.exceptions.BotException;
import rr.industries.pojos.RestartPost;
import rr.industries.pojos.travisciwebhooks.TravisWebhook;
import rr.industries.util.ChannelActions;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.utils.IOUtils;
import sx.blah.discord.util.MessageBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

/**
 * Credit to Chrislo for the basis and the POJOs
 */
public class Webserver implements Module {

    private final Gson gson = new Gson();
    private final ChannelActions actions;
    private final Logger LOG = LoggerFactory.getLogger(Webserver.class);
    private static final File imageDirectory = new File("image");
    private boolean isEnabled;

    /**
     * Should be initalized in ReadyEvent
     */
    public Webserver(ChannelActions actions) {
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
        Spark.get("/ping", ((request, response) -> {
            response.status(418);
            return "I'm a Teapot!";
        }));
        Spark.post("/command", (Request request, Response response) -> {
            try {
                RestartPost restart = null;
                try {
                    restart = gson.fromJson(request.body(), RestartPost.class);
                } catch (Exception e) {
                    LOG.error("Spark Error: ", e);
                    response.status(418);
                    return "I'm a teapot and Have an ERROR!!!";
                }
                if (restart.command != null && restart.command.equals("restart")) {
                    if (!restart.secret.equals(actions.getConfig().secret)) {
                        response.status(401);
                        return "Incorrect Secret!";
                    }
                    if (restart.name == null) {
                        response.status(400);
                        return "Missing MD5 hash in field \"name\"";
                    }
                    File updated = new File("sovietBot-update.jar");
                    if (!updated.exists()) {
                        response.status(400);
                        return "Missing Uploaded Jar";
                    }
                    String fileHash = DigestUtils.md5Hex(new FileInputStream(new File("sovietBot-update.jar")));
                    if (!restart.name.equals(fileHash)) {
                        response.status(400);
                        return "MD5 hashes do not match! Post:(" + restart.name + "), File:(" + fileHash + ")";
                    }
                    LOG.info("Everything Looks good, Restarting...");
                    Thread thread = new Thread(() -> {
                        try {
                            actions.terminate(true);
                        } catch (BotException ex) {
                            LOG.error("Could Not Restart", ex);
                        }
                    });
                    thread.start();
                    response.status(200);
                    return "Looks good: Restarting...";
                }
                response.status(418);
                return "I'm a teapot";
            } catch (Exception e) {
                LOG.error("Spark Error: ", e);
                response.status(500);
                return e.getMessage();
            }
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
        Spark.get("/procelio", (Request request, Response response) -> {
            File launcher = new File("launcher.json");
            response.type("application/json");
            if (!launcher.exists()) {
                LOG.error("No Launcher File Found at {}", launcher.getAbsolutePath());
                response.status(500);
                return "{}";
            }
            response.status(200);
            return Files.readAllLines(launcher.toPath()).stream().collect(Collectors.joining("\n"));
        });
        Spark.get("/image/*", (Request request, Response response) -> {
            File image = new File(request.pathInfo().substring(1));
            if (image.isDirectory() || !image.exists()) {
                response.status(404);
                return "Requested image not found";
            }

            byte[] bytes = Files.readAllBytes(image.toPath());
            HttpServletResponse raw = response.raw();
            response.raw().setContentType("image/" + FilenameUtils.getExtension(image.getAbsolutePath()));
            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();

            return response.raw();
        });
        Spark.get("/favicon.ico", ((Request request, Response response) -> {
            byte[] bytes = IOUtils.toByteArray(SovietBot.class.getClassLoader().getResourceAsStream(actions.getConfig().icon));
            if (bytes.length == 0) {
                LOG.error("Unable to load favicon!");
            }
            HttpServletResponse raw = response.raw();
            response.raw().setContentType("image/png");
            raw.getOutputStream().write(bytes);
            raw.getOutputStream().flush();
            raw.getOutputStream().close();

            return response.raw();
        }));
        Spark.init();
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
        actions.sendMessage(new MessageBuilder(actions.getClient()).withContent(content).withChannel(actions.getClient().getChannelByID(170685308273164288L)));
    }

}
