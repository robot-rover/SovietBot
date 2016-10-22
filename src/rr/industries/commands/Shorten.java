package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rr.industries.exceptions.BotException;
import rr.industries.pojos.ShortenedURL;
import rr.industries.pojos.URLRequest;
import rr.industries.util.*;

/**
 * @author Sam
 */
@CommandInfo(commandName = "shorten", helpText = "Shortens URLs", pmSafe = true)
public class Shorten implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Gives you a shortened version of the link", args = {Arguments.LINK})})
    public void execute(CommContext cont) throws BotException {
        String url = cont.getArgs().get(1);
        try {
            HttpResponse<String> response = Unirest.post("https://www.googleapis.com/urlshortener/v1/url").header("Content-Type", "application/json")
                    .queryString("key", cont.getActions().getConfig().googleKey).body(gson.toJson(new URLRequest(url))).asString();
            ShortenedURL shorten = gson.fromJson(response.getBody(), ShortenedURL.class);
            cont.getActions().channels().sendMessage(cont.builder().appendContent(cont.getMessage().getAuthor().mention())
                    .appendContent(": ").appendContent(shorten.id));
        } catch (UnirestException ex) {
            throw BotException.returnException(ex);
        }
    }
}
