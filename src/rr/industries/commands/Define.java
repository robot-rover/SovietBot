package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rr.industries.exceptions.BotException;
import rr.industries.pojos.dictionary.DictionaryResponse;
import rr.industries.pojos.dictionary.Result;
import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

import java.util.stream.Collectors;

/**
 * @author Sam
 */

@CommandInfo(commandName = "define", helpText = "defines a word or phrase", pmSafe = true)
public class Define implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "defines a word", args = {@Argument(description = "Word", value = Validate.TEXT)}),
            @Syntax(helpText = "defines a phrase", args = {@Argument(description = "Phrase", value = Validate.LONGTEXT)})
    })
    public void execute(CommContext cont) throws BotException {
        try {
            HttpResponse<String> response = Unirest.get("http://api.pearson.com/v2/dictionaries/wordwise/entries")
                    .queryString("token", cont.getActions().getConfig().dictKey)
                    .queryString("headword", cont.getConcatArgs(1).toLowerCase()).asString();
            DictionaryResponse dict = gson.fromJson(response.getBody(), DictionaryResponse.class);
            if (dict.status != 200)
                throw new InternalError("Dictionary API returned status: " + dict.status);
            MessageBuilder message = cont.builder();
            if (dict.results.size() == 0) {
                message.appendContent("*No Definitions Found...*");
            } else {
                Result result = dict.results.get(0);
                message.withContent("Definitions of ").appendContent(result.headword).appendContent("\n");
                message.appendContent(result.senses.stream().filter(v -> v.definition != null).map(v -> "`+` " + v.definition +
                        (v.examples.size() != 0 ? v.examples.stream().map(j -> "\t`-` " + j.text).collect(Collectors.joining("\n", "\n", "")) : ""))
                        .collect(Collectors.joining("\n")));
            }
            cont.getActions().channels().sendMessage(message);
        } catch (UnirestException ex) {
            throw BotException.returnException(ex);
        }
    }
}
