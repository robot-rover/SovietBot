package rr.industries.commands;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import rr.industries.util.*;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.util.*;

import java.awt.*;
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

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "nothing", args = {@Argument(description = "no comment", value = Validate.LONGTEXT)})})
    public void execute(CommContext cont) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.appendDescription("is Description");


        embed.withImage("https://ak.picdn.net/assets/cms/21c3901c18b5824f76305971fcf2cf030f1a89e7-stock-vector-elephant-flat-icon-318716621.jpg");

        embed.appendDescription("is Description");
        embed.appendField(new Embed.EmbedField("is field name", "<b>is field value</b>", false));


        embed.appendDescription("is Description");

        embed.appendField(new Embed.EmbedField("is inline field name", "**is field value**", true));
        embed.withAuthorName("is Author Name");
//        embed.withColor(Color.BLACK)
        embed.withFooterText("is Footer Text");
        embed.withTimestamp(0L);
        //embed.withTitle("is Title");
        embed.withThumbnail("http://oneminutestock.com/wp-content/uploads/2013/07/stock-market.jpg");
        embed.withAuthorIcon("https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/2b/2b4b0c80e5b785100f62105a322d9cf712db6d33_full.jpg");
        embed.withAuthorUrl("https://robot-rover.github.io/SovietBot/");
        embed.withColor(Color.CYAN);
        cont.getActions().channels().sendMessage(cont.builder().withEmbed(embed.build()));
    }

    @SubCommand(name = "tester", Syntax = {@Syntax(helpText = "Test the tester test", args = {})})
    public void testSub(CommContext cont) {
        try {
            cont.getMessage().reply("```" + cont.getMessage().getContent() + "```");
        } catch (MissingPermissionsException e) {
            LOG.error(MissingPermissionsException.class.getName(), e);
        } catch (DiscordException e) {
            LOG.error(DiscordException.class.getName(), e);
        } catch (RateLimitException e) {
            LOG.error(RateLimitException.class.getName(), e);
        }
    }

    @SubCommand(name = "repeat", Syntax = {})
    public void repeat(CommContext cont) {
        cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel())
                .withContent("```" + cont.getMessage().getContent() + "```"));
    }

    @SubCommand(name = "invite", Syntax = {})
    public void invite(CommContext cont) {
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
