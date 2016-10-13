package rr.industries.commands;

import com.google.gson.JsonSyntaxException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rr.industries.exceptions.BotException;
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
        permLevel = Permissions.BOTOPERATOR
)
public class Log implements Command {
    @SubCommand(name = "full", Syntax = {@Syntax(helpText = "Uploads the log at Debug Level", args = {})})
    public void fullLog(CommContext cont) throws BotException {
        uploadLog(new File("debug.log").toPath(), cont);
    }

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Uploads the log to hastebin and sends you the link", args = {})})
    public void execute(CommContext cont) throws BotException {
        uploadLog(new File("events.log").toPath(), cont);
    }

    public void uploadLog(Path path, CommContext cont) throws BotException {
        MessageBuilder message = cont.builder();
        try {
            String log = Files.readAllLines(path).stream().collect(Collectors.joining("\n"));
            HttpResponse<String> response = Unirest.post("http://hastebin.com/documents").header("Content-Type", "text/plain").body(log).asString();
            message.withContent("**Log** -  http://hastebin.com/" + gson.fromJson(response.getBody(), Hastebin.class).getKey());
            cont.getActions().channels().sendMessage(message);
        } catch (IOException ex) {
            throw new InternalError("IOException on Log Command", ex);
        } catch (JsonSyntaxException ex) {
            LOG.warn("Hastebin is Down!");
            cont.getActions().channels().sendMessage(message.withContent("Hastebin appears to be down..."));
        } catch (UnirestException ex) {
            BotException.translateException(ex);
        }
    }

    public class Hastebin {

        private String key;

        public String getKey() {
            return key;
        }
    }
}
