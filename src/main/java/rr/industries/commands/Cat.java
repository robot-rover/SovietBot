package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.io.IOUtils;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.ServerError;
import rr.industries.pojos.CatRequest;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandInfo(
        commandName = "cat",
        helpText = "Posts a random cat picture.",
        pmSafe = true
)
public class Cat implements Command {
    Pattern link = Pattern.compile("<url>(.*)</url>");

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Sends a random picture of a cat in a text channel", args = {})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        try {
            HttpResponse<String> response = Unirest.get("http://thecatapi.com/api/images/get").queryString("format", "xml").asString();
            Matcher m = link.matcher(response.getBody());
            if(!m.find())
                throw new IOException("Could not find link in XML");
            return cont.getMessage().getMessage().getChannel().zipWith(Mono.justOrEmpty(cont.getMessage().getMember())).flatMap(v -> v.getT1().createMessage(v.getT2().getMention() + " " + m.group(1))).then();
        } catch (IOException ex) {
            throw new ServerError("IOException in Cat command", ex);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return Mono.empty();
    }
}
