package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.ServerError;
import rr.industries.pojos.xkcd.XkcdComic;
import rr.industries.pojos.xkcd.XkcdSearch;
import rr.industries.util.*;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

@CommandInfo(commandName = "xkcd", helpText = "Searches for the \"Relevant XKCD\"", pmSafe = true)
public class Xkcd implements Command {

    static final DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL);

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Searches for an XKCD that contains the search terms", args = {@Argument(Validate.LONGTEXT)}),
            @Syntax(helpText = "Finds the numbered XKCD", args = {@Argument(Validate.NUMBER)})
    })
    public void execute(CommContext cont) throws BotException {
        Integer number = null;
        EmbedBuilder embedBuilder = new EmbedBuilder();
        try {
            number = Integer.parseInt(cont.getArgs().get(1));
        } catch (NumberFormatException ignored) {}
        if(number == null) {
            HttpResponse<String> response;
            try {
                response = Unirest.post("http://adtac.pw:8000/search").field("search", cont.getConcatArgs(1)).asString();

            } catch (UnirestException e) {
                throw new ServerError("Bad response from XKC Server", e);
            }
            XkcdSearch results = gson.fromJson(response.getBody(), XkcdSearch.class);
            if (!results.success)
                throw new ServerError("Faliure from XKCD Server", new UnirestException(response.getBody()));
            if(results.results.size() == 0){
                embedBuilder.withDescription("No results...").withColor(Color.RED);
                cont.getActions().channels().sendMessage(cont.builder().withEmbed(embedBuilder.build()));
                return;
            }
            LOG.info(results.results.stream().map(v -> v.title).collect(Collectors.joining(", ", "[", "]")));
            number = results.results.get(rn.nextInt(results.results.size())).number;
        }

        HttpResponse<String> response;
        try {
            response = Unirest.get("https://xkcd.com/" + number.toString() + "/info.0.json").asString();
        } catch (UnirestException e) {
            throw new ServerError("Bad response from XKC Server", e);
        }
        if(response.getStatus() == 404){
            embedBuilder.withDescription("XKCD #" + number.toString() + " Does not exist...").withColor(Color.RED);
            cont.getActions().channels().sendMessage(cont.builder().withEmbed(embedBuilder.build()));
            return;
        }
        XkcdComic result = gson.fromJson(response.getBody(), XkcdComic.class);
        embedBuilder/*.withThumbnail(cont.getActions().getConfig().url + "/image/xkcd.png")*/.withAuthorName(result.title + " - XKCD#" + result.num)
                .withAuthorUrl("https://xkcd.com/" + result.num + "/").withDescription(result.alt);
        try {
            embedBuilder.withFooterText(LocalDate.of(Integer.parseInt(result.year), Integer.parseInt(result.month), Integer.parseInt(result.day)).format(dtf));
        } catch (NumberFormatException e) {
            throw new ServerError("Unable to parse XKCD Date", e);
        }
        embedBuilder.withImage(result.img);
        LOG.info("Date: {}-{}-{}", result.month, result.day, result.year);
        cont.getActions().channels().sendMessage(cont.builder().withEmbed(embedBuilder.build()));
    }
}
