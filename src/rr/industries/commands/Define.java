package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import rr.industries.exceptions.BotException;
import rr.industries.pojos.dictionary.DictionaryResponse;
import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

import java.util.stream.Collectors;

/**
 * @author Sam
 */

@CommandInfo(commandName = "define", helpText = "defines a word or phrase")
public class Define implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "defines a word", args = {Arguments.TEXT}),
            @Syntax(helpText = "defines a phrase", args = {Arguments.LONGTEXT})
    })
    public void execute(CommContext cont) throws BotException {
        try {
            HttpResponse<String> response = Unirest.get("https://glosbe.com/gapi_v0_1/translate").queryString("from", "eng")
                    .queryString("dest", "eng").queryString("format", "json").queryString("phrase", cont.getConcatArgs(1).toLowerCase()).asString();
            DictionaryResponse dict = gson.fromJson(response.getBody(), DictionaryResponse.class);
            if (!dict.result.equals("ok"))
                throw new InternalError("Dictionary API returned status: " + dict.result);
            MessageBuilder message = cont.builder();
            message.withContent("Definitions of ").appendContent(dict.phrase).appendContent("\n");
            String defs = dict.tuc.stream().filter(v -> v.phrase == null)
                    .map(v -> v.meanings.stream().map(q -> "`+` ".concat(q.text)).collect(Collectors.joining("\n")))
                    .collect(Collectors.joining("\n"));
            if (defs.length() == 0) {
                message.appendContent("*No Definitions Found...*");
            } else {
                message.appendContent(BotUtils.htmlToDiscord(defs));
            }
            cont.getActions().channels().sendMessage(message);
        } catch (UnirestException ex) {
            BotException.translateException(ex);
        }
    }
}
