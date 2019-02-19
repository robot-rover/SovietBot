package rr.industries.commands;

import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.ServerError;
import rr.industries.pojos.CryptoCurrency;
import rr.industries.util.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;

@CommandInfo(commandName = "crypto", helpText = "Finds info on a crypto currency", pmSafe = true)
public class Crypto implements Command {

    List<CryptoCurrency> cache;
    TypeToken<List<CryptoCurrency>> cacheTypeToken = new TypeToken<>() {
    };
    long lastRefresh = 0L;
    long refreshDelay = 1000 * 60 * 60; // 1 hour

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Finds the info on this cryptocurrency", args = @Argument(value = Validate.TEXT, description = "Name"))})
    public Mono<Void> execute(CommContext cont) throws BotException{
        String term = cont.getArgs().get(1);
        CryptoCurrency find = searchCache(term);
        if(find == null && updateCache()){
            find = searchCache(term);
        }
        if(find == null){
            return cont.getMessage().getMessage().getChannel().flatMap(v -> v.createMessage("The currency " + term + " cannot be found")).then();
        } else {
                CryptoCurrency currency = getCurrency(find.id).get(0);
                return cont.getMessage().getMessage().getChannel().flatMap(channel -> channel.createMessage(messageSpec -> messageSpec.setEmbed(embedSpec -> {
                    embedSpec.setTitle(currency.name);
                    embedSpec.setDescription("$" + currency.priceUsd);
                    String percentChange = "1 Hour: " + currency.percentChange1h + "%\n" +
                            "1 Day:  " + currency.percentChange24h + "%\n" +
                            "7 Days: " + currency.percentChange7d + "%\n";
                    embedSpec.addField("Percent Change -", percentChange, false);
                    Instant instant = Instant.ofEpochSecond(Long.parseLong(currency.lastUpdated));
                    ZonedDateTime time = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
                    embedSpec.setFooter(time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL)), null);
                }))).then();
        }
    }

    private List<CryptoCurrency> getCurrency(String id) throws BotException {
        InputStream in;
        try {
            in = Unirest.get("https://api.coinmarketcap.com/v1/ticker/" + id + "/").asBinary().getBody();
        } catch (UnirestException e) {
            throw new ServerError("Unable to connect to coinmarketcap", e);
        }
        return gson.fromJson(new InputStreamReader(in), cacheTypeToken.getType());
    }

    private boolean updateCache() throws BotException {
        if(System.currentTimeMillis() < lastRefresh + refreshDelay)
            return false;
        InputStream in;
        try {
            in = Unirest.get("https://api.coinmarketcap.com/v1/ticker/").queryString("limit", "0").asBinary().getBody();
        } catch (UnirestException e) {
            throw new ServerError("Unable to connect to coinmarketcap", e);
        }
        long ramBefore = Runtime.getRuntime().freeMemory();
        cache = Arrays.asList(gson.fromJson(new InputStreamReader(in), CryptoCurrency[].class));
        long extraRam = ramBefore - Runtime.getRuntime().freeMemory();
        LOG.info("Cache updated with {} currencies. Will not refresh again until {}, Extra memory used: {}/{}", cache.size(),System.currentTimeMillis() + refreshDelay, extraRam, Runtime.getRuntime().maxMemory());
        lastRefresh = System.currentTimeMillis();
        return true;
    }

    public CryptoCurrency searchCache(String term){
        if(cache == null)
            return null;
        if(term.equals(term.toUpperCase())){
            return cache.stream().filter(v -> v.symbol.equals(term)).findAny().orElse(null);
        } else if(term.equals(term.toLowerCase())) {
            return cache.stream().filter(v -> v.id.equals(term)).findAny().orElse(null);
        }
        return cache.stream().filter(v -> v.name.equals(term)).findAny().orElse(null);
    }
}
