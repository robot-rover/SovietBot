package rr.industries;

import rr.industries.commands.Help;
import rr.industries.util.CommandInfo;
import rr.industries.util.Entry;
import sx.blah.discord.Discord4J;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sam
 */
public class Information {
    private static String clientId;
    public static final String botName = "SovietBot";
    public static final String frameName = Discord4J.NAME;
    public static final String frameVersion = Discord4J.VERSION.split(" ")[0];
    public static final String helpCommand = Help.class.getAnnotation(CommandInfo.class).commandName();
    public static final String author = "robot_rover";
    public static final String botAvatar = "avatars/person.jpeg";
    public static final String botIcon = "icon.png";
    public static final String defaultCommChar = ">";
    public static final int webhookAPIPort = 1000;
    public static final List<Entry<sx.blah.discord.handle.obj.Permissions, Integer>> neededPerms = Arrays.asList(
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
    public static String invite = "";

    private Information() {
    }

    public static void setClientID(String id){
        invite = "https://discordapp.com/oauth2/authorize?&client_id=" + id + "&scope=bot&permissions=" + neededPerms.stream().mapToInt(Entry::second).sum();
    }

    private static Entry<sx.blah.discord.handle.obj.Permissions, Integer> perm(sx.blah.discord.handle.obj.Permissions perm, String hex) {
        return new Entry<>(perm, Integer.parseInt(hex, 16));
    }
}
