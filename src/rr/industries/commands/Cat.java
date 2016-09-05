package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Logging;
import sx.blah.discord.util.MessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Sam on 8/28/2016.
 */
@CommandInfo(
        commandName = "cat",
        helpText = "Posts a random cat picture."
)
public class Cat implements Command {
    @Override
    public void execute(CommContext cont) {

        URL url;
        try {
            url = new URL("http://random.cat/meow");
        } catch (MalformedURLException ex) {
            Logging.customException(cont.getMessage(), "cat", "Cat URL is Malformed", ex, LOG);
            return;
        }
        InputStream is;
        try {
            URLConnection con = url.openConnection();
            is = con.getInputStream();
        } catch (IOException ex) {
            Logging.error(cont.getMessage().getMessage().getGuild(), "cat", ex, LOG);
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String message;
        try {
            message = br.readLine();
        } catch (IOException ex) {
            Logging.error(cont.getMessage().getMessage().getGuild(), "cat", ex, LOG);
            return;
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                LOG.warn("unable to close Cat url reader", ex);
            }
        }
        message = message.substring(9, message.length() - 2);
        message = message.replace("\\/", "/");
        message = cont.getMessage().getMessage().getAuthor().mention() + " " + message;
        BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
    }
}
