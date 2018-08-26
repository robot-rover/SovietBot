package rr.industries.commands;

import org.apache.commons.lang3.text.WordUtils;
import rr.industries.exceptions.BotException;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.MessageBuilder;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.stream.Collectors;

/**
 * @author Sam
 */

@CommandInfo(commandName = "whois", helpText = "Tells you information about users.")
public class WhoIs implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Tells you information about yourself", args = {}),
            @Syntax(helpText = "Tells you information about the mentioned user", args = {@Argument(Validate.MENTION)})
    })
    public void execute(CommContext cont) throws BotException {
        IUser examine;
        if (cont.getMessage().getMentions().size() > 0) {
            examine = cont.getMessage().getMentions().get(0);
        } else {
            examine = cont.getMessage().getAuthor();
        }
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        message.appendContent(examine.getName() + "`#" + examine.getDiscriminator() + "`");
        String nick = examine.getNicknameForGuild(cont.getMessage().getGuild());
        if (nick != null) {
            message.appendContent(" aka *" + nick + "*");
        }
        if (examine.isBot()) {
            message.appendContent(" - `|BOT|`");
        }
        message.appendContent("\n**--------------**\n");
        message.appendContent("ID: " + examine.getStringID() + "\n");
        message.appendContent("Joined: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneOffset.UTC).format(examine.getCreationDate()) + "\n");
        message.appendContent("Status: " + WordUtils.capitalizeFully(examine.getPresence().getStatus().name()) + "\n");
        message.appendContent("Roles: " + examine.getRolesForGuild(cont.getMessage().getGuild()).stream().filter(v -> !v.isEveryoneRole()).map(IRole::getName).collect(Collectors.joining(", ")) + "\n");
        if (examine.getAvatar() != null)
            message.appendContent("Avatar: " + examine.getAvatarURL() + "\n");
        cont.getActions().channels().sendMessage(message);
    }
}
