package rr.industries.commands;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import rr.industries.exceptions.BotException;
import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

//yes I know this is horrible code and I don't care :-P
@CommandInfo(
        commandName = "test",
        helpText = "Temporary command for testing new features",
        permLevel = Permissions.BOTOPERATOR
)
public class Test implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "searches for the youtube video", args = {Arguments.LONGTEXT})})
    public void execute(CommContext cont) throws BotException {

    }

    @SubCommand(name = "tester", Syntax = {@Syntax(helpText = "Test the tester test", args = {})})
    public void testSub(CommContext cont) {

    }

    @SubCommand(name = "repeat", Syntax = {})
    public void repeat(CommContext cont) {
        cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel())
                .withContent("```" + cont.getMessage().getContent() + "```"));
    }

    @SubCommand(name = "ping", Syntax = {})
    public void ping(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel()).withContent("**Pinged Webhook**\n");
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost("https://api.github.com/repos/robot-rover/SovietBot/hooks/1/pings");
        HttpResponse response = null;
        try {
            response = client.execute(post);
        } catch (IOException e) {
            message.appendContent("IOException: " + e.getMessage());
            LOG.error(IOException.class.getName(), e);
        }
        if (response != null) {
            message.appendContent("Response Code " + response.getStatusLine().getStatusCode() + ": " + response.getStatusLine().getReasonPhrase() + "\n");
            try {
                message.appendContent("Body: " + IOUtils.toString(response.getEntity().getContent()));
            } catch (IOException e) {
                LOG.error(IOException.class.getName(), e);
            }
            cont.getActions().channels().sendMessage(message);
        }

    }

    @Override
    public Predicate<List<String>> getValiddityOverride() {
        return (v) -> true;
    }
}
