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

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.gateway.retry.RetryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class Launcher {
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    static SovietBot bot;
    public static String token;

    public static void main(String[] args) {
        token = args[0];
        DiscordClientBuilder builder = new DiscordClientBuilder(token);
        builder.setRetryOptions(new RetryOptions(Duration.ofSeconds(10), Duration.ofSeconds(30), 6, Schedulers.elastic()));
        DiscordClient client = builder.build();
        LOG.info("Starting SovietBot");
        bot = new SovietBot();
        bot.enable(client).and(client.login()).block();
    }
}
