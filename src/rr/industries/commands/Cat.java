package rr.industries.commands;

import org.apache.commons.io.IOUtils;
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

@CommandInfo(
        commandName = "cat",
        helpText = "Posts a random cat picture.",
        pmSafe = true
)
public class Cat implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Sends a random picture of a cat in a text channel", args = {})})
    public void execute(CommContext cont) throws BotException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL("http://random.cat/meow").openConnection().getInputStream()))) {
            CatRequest cat = gson.fromJson(IOUtils.toString(br), CatRequest.class);
            cont.getActions().channels().sendMessage(cont.builder().withContent(cont.getMessage().getAuthor().mention() + " " + cat.file));
        } catch (MalformedURLException ex) {
            throw new ServerError("The cat api URL is Malformed", ex);
        } catch (IOException ex) {
            throw new ServerError("IOException in Cat command", ex);
        }
    }
}
