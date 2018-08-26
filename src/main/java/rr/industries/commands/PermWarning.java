package rr.industries.commands;

import rr.industries.Information;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.BotMissingPermsException;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.MessageBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sam
 */

@CommandInfo(commandName = "permwarning", helpText = "sends a warning to all guilds where I don't have perms I need", permLevel = rr.industries.util.Permissions.BOTOPERATOR, pmSafe = true)
public class PermWarning implements Command {

    public static List<Permissions> BLANK_LIST = new ArrayList<>(0);

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "sends the warning", args = {})})
    public void execute(CommContext cont) throws BotException {
        cont.getActions().channels().sendMessage(cont.builder().withContent("Sending Perm Warning Message out to Guilds..."));
        Collection<Permissions> allPerms = Information.neededPerms.stream().map(Entry::first).collect(Collectors.toList());
        for (IGuild guild : cont.getClient().getGuilds()) {
            List<Permissions> missingPerms = checkPerms(guild, cont.getClient().getOurUser(), allPerms);
            MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(guild.getChannelByID(guild.getLongID()))
                    .withContent(":warning: **Attention** :warning: SovietBot is missing Permissions it requires to *function* properly\n")
                    .appendContent("Missing: ").appendContent(formatPerms(missingPerms));
            cont.getActions().channels().sendMessage(message);
        }
    }

    public List<Permissions> checkPerms(IGuild guild, IUser user, Collection<Permissions> checkFor) {
        List<Permissions> guildPerms = new ArrayList<>();
        for (IRole role : guild.getRolesForUser(user)) {
            guildPerms.addAll(role.getPermissions());
        }
        if (guildPerms.containsAll(checkFor)) {
            return BLANK_LIST;
        }
        List<Permissions> missingPerms = new ArrayList<>();
        missingPerms.addAll(checkFor);
        missingPerms.removeAll(guildPerms);
        return missingPerms;
    }

    public String formatPerms(Collection<Permissions> toFormat) {
        return toFormat.stream().map(v -> BotMissingPermsException.formatDiscordPerms(v.name())).collect(Collectors.joining(", "));
    }
}
