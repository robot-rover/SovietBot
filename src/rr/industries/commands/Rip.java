package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandInfo(
        commandName = "rip",
        helpText = "Gives you a link to a place where everything is Rip",
        permLevel = Permissions.REGULAR
)
public class Rip implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Specify the thing(s) that are rip (Works with @\u200Bmentions)", args = {@ArgSet(arg = Arguments.TEXT, num = -1)})})
    public void execute(CommContext cont) {
        String channelName;
        Pattern p = Pattern.compile("<@!?[0-9]{18}>");
        Pattern n = Pattern.compile("[0-9]{18}");
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel()).withContent("http://www.ripme.xyz/");
        String rawSubject = cont.getConcatArgs(1);
        Matcher matcher = p.matcher(rawSubject);
        while (matcher.find()) {
            Matcher num = n.matcher(matcher.group());
            num.find();
            String id = num.group();
            IUser mention = cont.getClient().getUserByID(id);
            if (mention != null) {
                rawSubject = rawSubject.replace(matcher.group(), mention.getDisplayName(cont.getMessage().getGuild()));
            } else {
                rawSubject = rawSubject.replace(matcher.group(), "");
            }
        }
        rawSubject = rawSubject.replace(" ", "%20");
        cont.getActions().sendMessage(message.appendContent(rawSubject));
    }
}
