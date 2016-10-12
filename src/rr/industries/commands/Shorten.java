package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rr.industries.exceptions.BotException;
import rr.industries.pojos.ShortenedURL;
import rr.industries.pojos.URLRequest;
import rr.industries.util.*;

/**
 * @author Sam
 */
@CommandInfo(commandName = "shorten", helpText = "Shortens URLs")
public class Shorten implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Gives you a shortened version of the link", args = {Arguments.LINK})})
    public void execute(CommContext cont) throws BotException {
        String url = cont.getArgs().get(1);
        LOG.info(url);
        try {
            HttpResponse<JsonNode> response = Unirest.post("https://www.googleapis.com/urlshortener/v1/url").header("Content-Type", "application/json")
                    .queryString("key", cont.getActions().getConfig().googleKey).body(gson.toJson(new URLRequest(url))).asJson();
            LOG.info(response.getBody().toString());
            ShortenedURL shorten = gson.fromJson(response.getBody().toString(), ShortenedURL.class);
            cont.getActions().channels().sendMessage(cont.builder().appendContent(cont.getMessage().getAuthor().mention())
                    .appendContent(": ").appendContent(shorten.id));
        } catch (UnirestException ex) {
            throw new InternalError("Error Shortening URL", ex);
        } /*catch (IOException e) {
            LOG.error(java.io.IOException.class.getName(), e);
        }*/
    }
}
