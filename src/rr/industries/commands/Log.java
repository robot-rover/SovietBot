package rr.industries.commands;

import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.ServerError;
import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@CommandInfo(
        commandName = "log",
        helpText = "Uploads the bots log to Chat",
        permLevel = Permissions.BOTOPERATOR,
        pmSafe = true
)
public class Log implements Command {
    @SubCommand(name = "full", Syntax = {@Syntax(helpText = "Uploads the log at Debug Level", args = {})})
    public void fullLog(CommContext cont) throws BotException {
        uploadLog(new File("debug.log").toPath(), cont);
    }

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Uploads the log to hastebin and sends you the link", args = {})})
    public void execute(CommContext cont) throws BotException {
        File log = new File("events.log");
        LOG.info("{} - exists: {}", log.getAbsolutePath(), log.exists());
        uploadLog(new File("events.log").toPath(), cont);
    }

    public void uploadLog(Path path, CommContext cont) throws BotException {
        LOG.info("Uploading Log: {}", path.toString());
        MessageBuilder message = cont.builder();
        try {
            LOG.info("Reading Lines");
            String log = Files.readAllLines(path).stream().collect(Collectors.joining("\n"));
            LOG.info("{} chars found found", log.length());
            LOG.info("Posting to Hastebin");
            HttpResponse<String> response = Unirest.post("http://hastebin.com/documents").header("Content-Type", "text/plain").body(log).asString();
            LOG.info("Response - \n{}", response.getBody());
            LOG.info("Adding Message Content");
            message.withContent("**Log** -  http://hastebin.com/" + gson.fromJson(response.getBody(), Hastebin.class).getKey());
            LOG.info("Sending Message");
            cont.getActions().channels().sendMessage(message);
        } catch (IOException ex) {
            LOG.warn("Error", ex);
            throw new ServerError("IOException on Log Command", ex);
        } catch (JsonSyntaxException ex) {
            LOG.warn("Hastebin is Down!");
            cont.getActions().channels().sendMessage(message.withContent("Hastebin appears to be down..."));
        } catch (UnirestException ex) {
            LOG.warn("Error", ex);
            throw BotException.returnException(ex);
        }
        LOG.info("Returning");
    }

    public class Hastebin {

        private String key;

        public String getKey() {
            return key;
        }
    }
}
