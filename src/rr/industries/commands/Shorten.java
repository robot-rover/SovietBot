package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.ServerError;
import rr.industries.pojos.urlshortener.Error;
import rr.industries.pojos.urlshortener.URLResponse;
import rr.industries.util.*;

/**
 * @author Sam
 */
@CommandInfo(commandName = "shorten", helpText = "Shortens URLs", pmSafe = true)
public class Shorten implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Gives you a shortened version of the link", args = {@Argument(description = "Video Link", value = Validate.LINK)})})
    public void execute(CommContext cont) throws BotException {
        String url = cont.getArgs().get(1);
        try {
            HttpResponse<String> response = Unirest.post("https://www.googleapis.com/urlshortener/v1/url").header("Content-Type", "application/json")
                    .queryString("key", cont.getActions().getConfig().googleKey).body(gson.toJson(new URLResponse(url))).asString();
            URLResponse shorten = gson.fromJson(response.getBody(), URLResponse.class);
            if (shorten.error != null) {
                StringBuilder error = new StringBuilder("Recieved Error from google: Code ").append(shorten.error.code).append(" - ").append(shorten.error.message);
                for (Error e : shorten.error.errors) {
                    error.append("\n").append(e.toString());
                }
                throw new ServerError(error.toString());
            }
            cont.getActions().channels().sendMessage(cont.builder().appendContent(cont.getMessage().getAuthor().mention())
                    .appendContent(": ").appendContent(shorten.id));
        } catch (UnirestException ex) {
            throw BotException.returnException(ex);
        }
    }
}
