package rr.industries;

import rr.industries.commands.Help;
import rr.industries.util.CommandInfo;
import rr.industries.util.Entry;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sam
 */
public class Information {
    public final Configuration defaultConfig;
    public final String botName;
    public final String frameName;
    public final String frameVersion;
    public final String helpCommand;
    public final String author;
    public final String website;
    public final String invite;
    public final List<Entry<sx.blah.discord.handle.obj.Permissions, Integer>> neededPerms;

    public Information() {
        defaultConfig = new Configuration("SovietBot", "person.jpeg", ">", "", "", "", 1000, new String[0], "", "", "");
        botName = "SovietBot";
        frameName = sx.blah.discord.Discord4J.NAME;
        frameVersion = sx.blah.discord.Discord4J.VERSION;
        helpCommand = Help.class.getAnnotation(CommandInfo.class).commandName();
        author = "robot_rover";
        website = "https://robot-rover.github.io/SovietBot/";
        neededPerms = Arrays.asList(
                perm(sx.blah.discord.handle.obj.Permissions.CREATE_INVITE, "00000001"),
                perm(sx.blah.discord.handle.obj.Permissions.MANAGE_CHANNEL, "00000010"),
                perm(sx.blah.discord.handle.obj.Permissions.READ_MESSAGES, "00000400"),
                perm(sx.blah.discord.handle.obj.Permissions.SEND_MESSAGES, "00000800"),
                perm(sx.blah.discord.handle.obj.Permissions.MANAGE_MESSAGES, "00002000"),
                perm(sx.blah.discord.handle.obj.Permissions.EMBED_LINKS, "00004000"),
                perm(sx.blah.discord.handle.obj.Permissions.ATTACH_FILES, "00008000"),
                perm(sx.blah.discord.handle.obj.Permissions.READ_MESSAGE_HISTORY, "00010000"),
                perm(sx.blah.discord.handle.obj.Permissions.USE_EXTERNAL_EMOJIS, "00040000"),
                perm(sx.blah.discord.handle.obj.Permissions.VOICE_CONNECT, "00100000"),
                perm(sx.blah.discord.handle.obj.Permissions.VOICE_SPEAK, "00200000"),
                perm(sx.blah.discord.handle.obj.Permissions.VOICE_MOVE_MEMBERS, "01000000"),
                perm(sx.blah.discord.handle.obj.Permissions.CHANGE_NICKNAME, "04000000")
        );
        Integer permID = neededPerms.stream().mapToInt(Entry::second).sum();
        invite = "https://discordapp.com/oauth2/authorize?&client_id=184445488093724672&scope=bot&permissions=" + permID;

    }

    private static Entry<sx.blah.discord.handle.obj.Permissions, Integer> perm(sx.blah.discord.handle.obj.Permissions perm, String hex) {
        return new Entry<>(perm, Integer.parseInt(hex, 16));
    }
}
