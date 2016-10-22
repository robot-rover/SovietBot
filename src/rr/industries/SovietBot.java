/*
Add This Bot to Your Server:
https://discordapp.com/oauth2/authorize?&client_id=184445488093724672&scope=bot&permissions=19950624
 */

 /* Permissions List
MANAGE_GUILD	0x00000020  Allows management and editing of the guild
READ_MESSAGES	0x00000400  Allows reading messages in a channel. The channel will not appear for users without this permission
SEND_MESSAGES	0x00000800  Allows for sending messages in a channel
EMBED_LINKS     0x00004000  Links sent by this user will be auto-embedded
CONNECT 	    0x00100000  Allows for joining of a voice channel
SPEAK           0x00200000  Allows for speaking in a voice channel
MOVE_MEMBERS	0x01000000  Allows for moving of members between voice channels
MANAGE_MESSAGES 0x00002000  Allows for deletion of other users messages

Total:          19950624
 */
package rr.industries;

import gigadot.rebound.Rebound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.commands.Command;
import rr.industries.commands.Help;
import rr.industries.util.CommandInfo;
import rr.industries.util.Entry;
import rr.industries.util.GenHelpDocs;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SovietBot {
    private static final Logger LOG = LoggerFactory.getLogger(SovietBot.class);
    public static volatile boolean loggedIn = false;
    public static final ClassLoader resourceLoader = Instance.class.getClassLoader();
    public static final Configuration defaultConfig = new Configuration("SovietBot", "person.jpeg", ">", "", "", 1000, new String[0], "", "", "");
    public static final String botName = "SovietBot";
    public static final String frameName = sx.blah.discord.Discord4J.NAME;
    public static final String frameVersion = sx.blah.discord.Discord4J.VERSION;
    public static final String helpCommand = Help.class.getAnnotation(CommandInfo.class).commandName();
    public static final String author = "robot_rover";
    public static final String website = "https://robot-rover.github.io/SovietBot/";
    public static Instance bot;
    public static List<Entry<Permissions, Integer>> neededPerms;
    public static Integer permID;

    static {
        neededPerms = Arrays.asList(
                perm(Permissions.CREATE_INVITE, "00000001"),
                perm(Permissions.MANAGE_CHANNEL, "00000010"),
                perm(Permissions.READ_MESSAGES, "00000400"),
                perm(Permissions.SEND_MESSAGES, "00000800"),
                perm(Permissions.MANAGE_MESSAGES, "00002000"),
                perm(Permissions.EMBED_LINKS, "00004000"),
                perm(Permissions.ATTACH_FILES, "00008000"),
                perm(Permissions.READ_MESSAGE_HISTORY, "00010000"),
                perm(Permissions.USE_EXTERNAL_EMOJIS, "00040000"),
                perm(Permissions.VOICE_CONNECT, "00100000"),
                perm(Permissions.VOICE_SPEAK, "00200000"),
                perm(Permissions.VOICE_MOVE_MEMBERS, "01000000"),
                perm(Permissions.CHANGE_NICKNAME, "04000000")
        );
        permID = neededPerms.stream().collect(Collectors.summingInt(Entry::second));
    }

    public static final String invite = "https://discordapp.com/oauth2/authorize?&client_id=184445488093724672&scope=bot&permissions=" + permID;

    private static Entry<Permissions, Integer> perm(Permissions perm, String hex) {
        return new Entry<>(perm, Integer.parseInt(hex, 16));
    }

    public static void main(String[] args) {
        Rebound r = new Rebound("rr.industries.commands", false, true);
        r.getSubClassesOf(Command.class).forEach(CommandList::addCommand);
        if (args.length >= 1 && args[0].equals("generate")) {
            GenHelpDocs.generate(new CommandList().getCommandList());
            return;
        }
        LOG.info("\n------------------------------------------------------------------------\n### {} ###\n------------------------------------------------------------------------", botName);
        try {
            bot = new Instance();
        } catch (DiscordException e) {
            LOG.warn("Bot could not start", e);
        }
    }
}
