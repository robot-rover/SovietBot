package rr.industries.commands;

import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Permissions;
import sx.blah.discord.util.MessageBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

@CommandInfo(
        commandName = "log",
        helpText = "Uploads the bots log to Chat",
        permLevel = Permissions.BOTOPERATOR
)
public class Log implements Command {
    @Override
    public void execute(CommContext cont) {
        String path;
        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).equals("full")) {
            path = "debug.log";
        } else {
            path = "events.log";
        }
        Path logFile = new File(path).toPath();
        final AtomicReference<String> log = new AtomicReference<>("");
        String result = "";
        try {
            Files.readAllLines(logFile).forEach(v -> log.set(log.get().concat(v.concat("\n"))));
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost("http://hastebin.com/documents");
            post.setEntity(new StringEntity(log.get()));
            post.addHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(post);
            result = EntityUtils.toString(response.getEntity());
            LOG.info(result);
            String message = "**Log** -  http://hastebin.com/" + gson.fromJson(result, Hastebin.class).getKey();
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel()).withContent(message));
        } catch (IOException ex) {
            LOG.warn("Exception Sending Log", ex);
        } catch (JsonSyntaxException ex) {
            LOG.warn("Hastebin is Down!");
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getMessage().getChannel())
                    .withContent("Hastebin appears to be down..."));
        }
    }

    public class Hastebin {

        public String key;

        public String getKey() {
            return key;
        }
    }
}
