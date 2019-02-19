package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.ServerError;
import rr.industries.pojos.xkcd.XkcdComic;
import rr.industries.util.*;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@CommandInfo(commandName = "xkcd", helpText = "Searches for the \"Relevant XKCD\"", pmSafe = true)
public class Xkcd implements Command {

    static final DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Searches for an XKCD that contains the search terms", args = {@Argument(Validate.LONGTEXT)}),
            @Syntax(helpText = "Finds the numbered XKCD", args = {@Argument(Validate.NUMBER)}),
            @Syntax(helpText = "Shows the latest XKCD", args ={})
    })
    public Mono<Void> execute(CommContext cont) throws BotException {
        Integer number = null;
        if(cont.getArgs().size() == 1){
            return getAndSend("http://xkcd.com/info.0.json", cont);
        }
        try {
            number = Integer.parseInt(cont.getArgs().get(1));
        } catch (NumberFormatException ignored) {}
        if(number == null) {
            throw new IncorrectArgumentsException(cont.getArgs().get(1) + " is not a number");
        }
        return getAndSend("https://xkcd.com/" + number.toString() + "/info.0.json", cont);
    }

    public Mono<Void> getAndSend(String url, CommContext cont) throws BotException {
        HttpResponse<String> response;
        try {
            response = Unirest.get(url).asString();
        } catch (UnirestException e) {
            throw new ServerError("Bad response from XKC Server", e);
        }
        if(response.getStatus() == 404){
            return cont.getChannel().createMessage(messageSpec -> messageSpec.setEmbed(embedSpec -> embedSpec.setDescription("This XKC doesn't not exist...").setColor(Color.RED))).then();
        }
        XkcdComic result = gson.fromJson(response.getBody(), XkcdComic.class);

        return cont.getChannel().createMessage(v -> v.setEmbed(embedSpec -> createEmbed(embedSpec, result))).then();
    }

    private void createEmbed(EmbedCreateSpec embedSpec, XkcdComic result) {
        embedSpec/*.withThumbnail(cont.getActions().getConfig().url + "/image/xkcd.png")*/.setAuthor(result.title + " - XKCD#" + result.num, null, "https://xkcd.com/" + result.num + "/")
                .setDescription(result.alt);
        try {
            embedSpec.setFooter(LocalDate.of(Integer.parseInt(result.year), Integer.parseInt(result.month), Integer.parseInt(result.day)).format(dtf), null);
        } catch (NumberFormatException e) {
            LOG.warn("Cannot parse date", e);
        }
        embedSpec.setImage(result.img);
    }
}
