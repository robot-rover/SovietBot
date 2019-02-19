package rr.industries;

import discord4j.core.object.util.Permission;
import discord4j.core.object.util.Snowflake;
import rr.industries.commands.Help;
import rr.industries.util.CommandInfo;
import rr.industries.util.Entry;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sam
 */
public class Information {
    public static final Instant launchTime = Instant.now();
    public static final String botName = "SovietBot";
    public static final String frameName = "Discord4J";
    public static final String helpCommand = Help.class.getAnnotation(CommandInfo.class).commandName();
    public static final String author = "robot_rover";
    public static final String botAvatar = "avatars/person.jpeg";
    public static final String botIcon = "icon.png";
    public static final String defaultCommChar = ">";
    public static final List<Entry<Permission, Integer>> neededPerms = Arrays.asList(
            perm(Permission.CREATE_INSTANT_INVITE, "00000001"),
            perm(Permission.MANAGE_CHANNELS, "00000010"),
            perm(Permission.VIEW_CHANNEL, "00000400"),
            perm(Permission.SEND_MESSAGES, "00000800"),
            perm(Permission.MANAGE_MESSAGES, "00002000"),
            perm(Permission.EMBED_LINKS, "00004000"),
            perm(Permission.ATTACH_FILES, "00008000"),
            perm(Permission.READ_MESSAGE_HISTORY, "00010000"),
            perm(Permission.USE_EXTERNAL_EMOJIS, "00040000"),
            perm(Permission.CONNECT, "00100000"),
            perm(Permission.SPEAK, "00200000"),
            perm(Permission.MOVE_MEMBERS, "01000000"),
            perm(Permission.CHANGE_NICKNAME, "04000000")
    );
    public static String invite = "";

    private Information() {
    }

    public static void setClientID(Snowflake id){
        invite = "https://discordapp.com/oauth2/authorize?&client_id=" + id.asString() + "&scope=bot&permissions=" + neededPerms.stream().mapToInt(Entry::second).sum();
    }

    private static Entry<Permission, Integer> perm(Permission perm, String hex) {
        return new Entry<>(perm, Integer.parseInt(hex, 16));
    }
}
