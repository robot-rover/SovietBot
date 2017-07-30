package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandInfo(
        commandName = "rip",
        helpText = "Gives you a link to a place where everything is Rip",
        permLevel = Permissions.REGULAR,
        pmSafe = true
)
public class Rip implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "No Messing about here, just gives you the link", args = {}),
            @Syntax(helpText = "Specify the thing(s) that are rip (Works with @\u200Bmentions)", args = {Arguments.LONGTEXT})
    })
    public void execute(CommContext cont) {
        Pattern p = Pattern.compile("<@!?([0-9]{18})>");
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel()).withContent("http://www.ripme.xyz/");
        String rawSubject = cont.getConcatArgs(1);
        Matcher matcher = p.matcher(rawSubject);
        while (matcher.find()) {
            String id = matcher.group(1);
            IUser mention = cont.getClient().getUserByID(Long.parseLong(id));
            if (mention != null) {
                rawSubject = rawSubject.replace(matcher.group(), mention.getDisplayName(cont.getMessage().getGuild()));
            } else {
                rawSubject = rawSubject.replace(matcher.group(), "");
            }
        }
        cont.getActions().channels().sendMessage(message.appendContent(rawSubject.replace(" ", "%20")));
    }
}
