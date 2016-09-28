package rr.industries.commands;

import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import rr.industries.util.*;
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
    @SubCommand(name = "full", Syntax = {@Syntax(helpText = "Uploads the log at Debug Level", args = {})})
    public void fullLog(CommContext cont) {
        uploadLog(new File("debug.log").toPath(), cont);
    }

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Uploads the log to hastebin and sends you the link", args = {})})
    public void execute(CommContext cont) {
        uploadLog(new File("events.log").toPath(), cont);
    }

    public void uploadLog(Path path, CommContext cont) {
        final AtomicReference<String> log = new AtomicReference<>("");
        String result = "";
        try {
            Files.readAllLines(path).forEach(v -> log.set(log.get().concat(v.concat("\n"))));
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost("http://hastebin.com/documents");
            post.setEntity(new StringEntity(log.get()));
            post.addHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(post);
            result = EntityUtils.toString(response.getEntity());
            String message = "**Log** -  http://hastebin.com/" + gson.fromJson(result, Hastebin.class).getKey();
            cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient())
                    .withChannel(cont.getMessage().getChannel()).withContent(message));
        } catch (IOException ex) {
            LOG.warn("Exception Sending Log", ex);
        } catch (JsonSyntaxException ex) {
            LOG.warn("Hastebin is Down!");
            cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel())
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
