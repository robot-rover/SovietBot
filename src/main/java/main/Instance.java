package main;

import net.dv8tion.d4j.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.impl.DefaultProvider;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackQueueEvent;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static main.parsable.tryInt;
import static sx.blah.discord.util.audio.AudioPlayer.getAudioPlayerForGuild;

public class Instance {

    /*public @interface command {

        String name() default "default";
    }*/

    private static final Logger log = LoggerFactory.getLogger(Instance.class);
    private volatile IDiscordClient client;
    private final String token;
    private final AtomicBoolean reconnect = new AtomicBoolean(true);
    private final String[] instSet;
    private final String[] helpText;
    private final AudioInputStream[] Ohhs;
    private final String[] OhhsIndex;
    private final Random rn;
    private final String[] quotes;
    private final String[] banWords;
    private static final String version = "1.0.0";
    private static final String botName = "SovietBot";
    private static final String frameName = sx.blah.discord.Discord4J.NAME;
    private static final String frameVersion = sx.blah.discord.Discord4J.VERSION;
    private static final String helpCommand = "help";
    private static final String author = "robot_rover";
    private final Map<String, Consumer<commContext>> commandsTest = new HashMap<String, Consumer<commContext>>();
    private HashMap<String, String> commands = new HashMap<String, String>();
    private String commChar;

    public Instance(String token) {
        this.commChar = ">";
        commands.put("quote", "defaultMessage");
        commands.put("stop", "terminate");
        commands.put("help", "help");
        commands.put("rekt", "rekt");
        commands.put("unafk", "unafk");
        commands.put("info", "info");
        commands.put("bring", "bring");
        commands.put("purge", "purge");
        commands.put("disconnect", "disconnect");
        commands.put("cat", "cat");
        commands.put("roll", "roll");
        commands.put("coin", "coin");
        commands.put("weather", "weather");
        commandsTest.put("quote", cont -> defaultMessage(cont));
        commandsTest.put("stop", cont -> terminate(cont));
        commandsTest.put("help", cont -> help(cont));
        commandsTest.put("rekt", cont -> rekt(cont));
        commandsTest.put("unafk", cont -> unafk(cont));
        commandsTest.put("info", cont -> info(cont));
        commandsTest.put("bring", cont -> bring(cont));
        commandsTest.put("purge", cont -> purge(cont));
        commandsTest.put("disconnect", cont -> disconnect(cont));
        commandsTest.put("cat", cont -> cat(cont));
        commandsTest.put("roll", cont -> roll(cont));
        commandsTest.put("coin", cont -> coin(cont));
        commandsTest.put("weather", cont -> weather(cont));
        commandsTest.put("connect", cont -> connect(cont));
        commandsTest.put("music", cont -> music(cont));
        this.quotes = new String[17];
        this.banWords = new String[6];
        this.OhhsIndex = new String[6];
        rn = new Random();
        this.token = token;
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.Ohhs = new AudioInputStream[6];
        try {
            Ohhs[0] = getAudioInputStream(classLoader.getResource("ohhs/womboCombo.mp3"));
            Ohhs[1] = getAudioInputStream(classLoader.getResource("ohhs/wrongNumber.mp3"));
            Ohhs[2] = getAudioInputStream(classLoader.getResource("ohhs/violinAirhorn.mp3"));
            Ohhs[3] = getAudioInputStream(classLoader.getResource("ohhs/noOneHasEver.mp3"));
            Ohhs[4] = getAudioInputStream(classLoader.getResource("ohhs/noscoped.mp3"));
            Ohhs[5] = getAudioInputStream(classLoader.getResource("nopes/nopeSong.mp3"));
        } catch (IOException | UnsupportedAudioFileException ex) {
            log.warn("Error initilizing audio streams", ex);
        }

        OhhsIndex[0] = "wombo";
        OhhsIndex[1] = "wrong";
        OhhsIndex[2] = "airhorn";
        OhhsIndex[3] = "never";
        OhhsIndex[4] = "scope";
        OhhsIndex[5] = "nope";
        banWords[0] = "rekt";
        banWords[1] = "mlg";
        banWords[2] = "rip";
        banWords[3] = "noob";
        banWords[4] = "fam";
        banWords[5] = "bruh";
        quotes[0] = "In Soviet Russia, command type you.";
        quotes[1] = "In Soviet Russia, the lowest rank in the military is Public, not Private";
        quotes[2] = "In soviet Russia, furball coughs up cat!";
        quotes[3] = "In Soviet Russia, noun verb you!";
        quotes[4] = "In soviet Russia, Chuck Norris still rules.";
        quotes[5] = "In Soviet Russia, party throw you!";
        quotes[6] = "In Soviet Russia, Christmas steals the grinch!!";
        quotes[7] = "In Soviet Russia, waldo finds you!";
        quotes[8] = "http://i1.kym-cdn.com/photos/images/original/000/008/724/ISR__Dividing_by_zero_by_RainbowJerk.png";
        quotes[9] = "http://i2.kym-cdn.com/photos/images/original/000/000/948/in-soviet-russia.png";
        quotes[10] = "In Soviet Russia, a van steals you";
        quotes[11] = "In Soviet Russia, jokes crack you.";
        quotes[12] = "https://qph.ec.quoracdn.net/main-qimg-99907edafce7fb6acb5dc766368bf9af-c?convert_to_webp=true";
        quotes[13] = "http://67.media.tumblr.com/tumblr_meknuzRXuD1rxustho1_500.jpg";
        quotes[14] = "https://cdn.meme.am/instances/10678438.jpg";
        quotes[15] = "http://files.sharenator.com/in_soviet_russia_holy_crap_not_another_internet_meme_demotivational_poster_1247942328-s640x458-173710.jpg";
        quotes[16] = "http://ci.memecdn.com/689/2331689.jpg";
        instSet = new String[15];
        helpText = new String[15];
        instSet[0] = "quote";
        helpText[0] = "Triggers a memorable quote.";
        instSet[1] = "stop";
        helpText[1] = "Shuts down Sovietbot.";
        instSet[2] = "help";
        helpText[2] = "Displays this help Message.";
        instSet[3] = "rekt";
        helpText[3] = "Plays a sound in the voicechat. Slightly Obnoxious...";
        instSet[4] = "unafk";
        helpText[4] = "Brings AFK players to your current channel.";
        instSet[5] = "info";
        helpText[5] = "Displays Basic bot Info.";
        instSet[6] = "weather";
        helpText[6] = "[Coming Soon] Interface for getting the weather in your area.";
        instSet[7] = "bring";
        helpText[7] = "Brings all current users of a server to you.";
        instSet[8] = "purge";
        helpText[8] = "Removes x amount of messages from the current channel.";
        instSet[9] = "coin";
        helpText[9] = "Flips a coin.";
        instSet[10] = "disconnect";
        helpText[10] = "Disconnects a user from a voice channel.";
        instSet[11] = "cat";
        helpText[11] = "Posts a random cat pic.";
        instSet[12] = "roll";
        helpText[12] = "Rolls a random number in a variety of ways.";
        instSet[13] = "connect";
        helpText[13] = "Connects and Disconnects to voice channels.";
        instSet[14] = "music";
        helpText[14] = "Adds youtube link to queue.";
    }

    void login() throws DiscordException {
        client = new ClientBuilder().withToken(token).login();
        client.getDispatcher().registerListener(this);
    }

    @EventSubscriber
    public void joinGuild(GuildCreateEvent e) {
    }

    @EventSubscriber
    public void leaveGuild(GuildLeaveEvent e) {
    }

    @EventSubscriber
    public void onReady(ReadyEvent event) throws DiscordException, RateLimitException {
        log.info("*** Discord bot armed ***");
        if (!client.getOurUser().getName().equals("SovietBot")) {
            client.changeUsername("SovietBot");
        }
        client.changeAvatar(Image.forUrl("jpeg", "https://drive.google.com/uc?export=download&id=0B1pMiSpsgGecSzlPUmhSamlvN2M"));
        log.info("\n------------------------------------------------------------------------\n"
                + "*** Discord bot Ready ***\n"
                + "------------------------------------------------------------------------");
    }

    @EventSubscriber
    public void onDisconnect(DiscordDisconnectedEvent event) {
        if (reconnect.get()) {
            log.info("Reconnecting bot");
            try {
                login();
            } catch (DiscordException e) {
                log.warn("Failed to reconnect bot", e);
            }
        }
    }

    @EventSubscriber
    public void trackFinishEvent(TrackFinishEvent e) {
    }

    @EventSubscriber
    public void trackQueueEvent(TrackQueueEvent e) {
    }

    public List<String> parseArgs(String input) {
        boolean next = true;
        List<String> args = new ArrayList<String>();
        Scanner parser = new Scanner(input);
        while (next) {
            try {
                args.add(parser.next());
            } catch (NoSuchElementException ex) {
                next = false;
            }
        }
        if (args.get(0).startsWith(commChar)) {
            args.set(0, args.get(0).substring(commChar.length()));
        }
        return args;
    }

    private List<String> parseArgs(MessageReceivedEvent e) {
        boolean next = true;
        List<String> args = new ArrayList<String>();
        Scanner parser = new Scanner(e.getMessage().getContent());
        while (next) {
            try {
                args.add(parser.next());
            } catch (NoSuchElementException ex) {
                next = false;
            }
        }
        if (args.get(0).startsWith(commChar)) {
            args.set(0, args.get(0).substring(commChar.length()));
        }
        return args;
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getAuthor().isBot()) {
            return;
        }
        String message = e.getMessage().getContent();
        if (message.startsWith(commChar)) {
            commContext cont = new commContext(e, commChar);
            Consumer exec;
            try {
                exec = commandsTest.get(cont.getArgs().get(0));
                if (exec == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException | IndexOutOfBoundsException ex) {
                return;
            }
            exec.accept(cont);
        }
    }

    private void leaveChannel(IGuild guild) {
        try {
            client.getConnectedVoiceChannels().stream().filter(v -> v.getGuild().equals(guild)).findAny().orElse(null).leave();
        } catch (NullPointerException ex) {
            log.debug("Did not leave channel: Not in Channel");
        }
    }

    private void connect(commContext cont) {
        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).equals("/")) {
            leaveChannel(cont.getMessage().getMessage().getGuild());
        } else if (cont.getArgs().size() >= 2) {
            try {
                IVoiceChannel next = cont.getMessage().getMessage().getGuild().getVoiceChannelsByName(cont.getArgs().get(1)).get(0);
                if (!next.isConnected()) {
                    next.join();
                }
            } catch (MissingPermissionsException ex) {
                missingPermissions(cont.getMessage(), "connect", ex);
            } catch (NullPointerException | IndexOutOfBoundsException ex) {
                log.debug("Could not connect: Channel called " + cont.getArgs().get(1) + " not found");
            }
        } else {
            try {
                IVoiceChannel next = cont.getMessage().getMessage().getAuthor().getConnectedVoiceChannels().get(0);
                if (!next.isConnected()) {
                    IVoiceChannel possible = null;
                    boolean disconnect = true;
                    try {
                        possible = client.getConnectedVoiceChannels().stream().filter(v -> v.getGuild().equals(cont.getMessage().getMessage().getGuild())).findAny().orElseThrow(() -> new NullPointerException());
                    } catch (NullPointerException ex) {
                        disconnect = false;
                    }
                    if (disconnect) {
                        possible.leave();
                    }
                    next.join();
                }
            } catch (MissingPermissionsException ex) {
                missingPermissions(cont.getMessage(), "connect", ex);
            } catch (NullPointerException | IndexOutOfBoundsException ex) {
                log.debug("Could not connect: Author not in voice channel");
            }
        }
    }

    private void music(commContext cont) {
        log.info("starting music");
        IAudioManager manager = cont.getMessage().getMessage().getGuild().getAudioManager();
        MusicPlayer player;
        if (manager.getAudioProvider() instanceof DefaultProvider) {
            log.info("making new musicplayer");
            player = new MusicPlayer();
            player.setVolume(1);
            manager.setAudioProvider(player);
        } else {
            log.info("musicplayer looks good");
            player = (MusicPlayer) manager.getAudioProvider();
        }

        String infoMsg = "";
        if (cont.getArgs().size() < 2) {
            log.info("exiting... not enough args");
            return;
        }
        String url = cont.getArgs().get(1);
        log.info("making playlist");
        Playlist playlist;
        try {
            playlist = Playlist.getPlaylist(url);
        } catch (NullPointerException ex) {
            log.warn("The YT-DL playlist process resulted in a null or zero-length INFO!");
            return;
        }
        log.info("got playlist");
        List<AudioSource> sources = new LinkedList<>(playlist.getSources());
        log.info("playlst into array");
        if (sources.size() > 1) {
            log.info("more than one source");
            final MusicPlayer fPlayer = player;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    log.info("running in thread");
                    for (Iterator<AudioSource> it = sources.iterator(); it.hasNext(); ) {
                        AudioSource source = it.next();
                        AudioInfo info = source.getInfo();
                        List<AudioSource> queue = fPlayer.getAudioQueue();
                        if (info.getError() == null) {
                            queue.add(source);
                            if (fPlayer.isStopped()) {
                                fPlayer.play();
                            }
                        } else {
                            log.warn("Error in music source, skipping...");
                            it.remove();
                        }
                    }
                }
            };
            thread.start();
        } else {
            log.info("only one source");
            AudioSource source = sources.get(0);
            AudioInfo info = source.getInfo();
            if (info.getError() == null) {
                log.info("no errors");
                player.getAudioQueue().add(source);
                if (player.isStopped()) {
                    log.info("resuming...");
                    player.play();
                }
            } else {
                log.warn("Error in music source, skipping...");
            }
        }
    }

    private void weather(commContext cont) {
    }

    private void coin(commContext cont) {
        String message;
        if (rn.nextBoolean()) {
            message = "Heads";
        } else {
            message = "Tails";
        }
        sendMessage(message, cont.getMessage());
    }

    private void roll(commContext cont) {
        int roll;
        boolean dnd;
        int d = 0;
        try {
            d = cont.getArgs().get(1).indexOf("d");
            dnd = cont.getArgs().get(1).contains("d") && tryInt(cont.getArgs().get(1).substring(0, d)) && tryInt(cont.getArgs().get(1).substring(d + 1));
        } catch (IndexOutOfBoundsException ex) {
            dnd = false;
        }
        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).equals("help")) {
            String message = "**Arguments: **\n"
                    + "5 - rolls a number between 1 and 5\n"
                    + "2d6 - rolls 2 dice with 6 sides each\n"
                    + " - rolls a number between 1 and 100";
        } else if (cont.getArgs().size() >= 2 && tryInt(cont.getArgs().get(1))) {
            roll = Integer.parseInt(cont.getArgs().get(1));
            if (roll < 1) {
                sendMessage("Rolling 0 to 0: 0", cont.getMessage());
                return;
            }
            String message = "Rolling 1 to " + Integer.toString(roll) + ": " + (rn.nextInt(roll) + 1);
            sendMessage(message, cont.getMessage());
        } else if (cont.getArgs().size() >= 2 && dnd) {
            int reps = Integer.parseInt(cont.getArgs().get(1).substring(0, d));
            int value = Integer.parseInt(cont.getArgs().get(1).substring(d + 1));
            int total = 0;
            String message = "**Rolling: **" + cont.getArgs().get(1) + "\n";
            for (int i = 0; i < reps; i++) {
                roll = (rn.nextInt(value) + 1);
                message = message + roll + ", ";
                total += roll;
            }
            message = message.substring(0, message.length() - 2);
            message = message + "\n**Total: **" + total;
            sendMessage(message, cont.getMessage());
        } else {
            sendMessage("Rolling 1 to 100: " + (rn.nextInt(100) + 1), cont.getMessage());
        }
    }

    private void cat(commContext cont) {
        URL url;
        try {
            url = new URL("http://random.cat/meow");
        } catch (MalformedURLException ex) {
            log.error("Cat URL is Malformed", ex);
            return;
        }
        InputStream is;
        try {
            URLConnection con = url.openConnection();
            is = con.getInputStream();
        } catch (IOException ex) {
            log.warn("Cat input stream Exception", ex);
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String message;
        try {
            message = br.readLine();
        } catch (IOException ex) {
            log.warn("IOException in Cat", ex);
            return;
        }
        message = message.substring(9, message.length() - 2);
        message = message.replace("\\/", "/");
        message = cont.getMessage().getMessage().getAuthor().mention() + " " + message;
        sendMessage(message, cont.getMessage());
    }

    private void disconnect(commContext cont) {
        List<String> args = parseArgs(cont.getMessage());
        if (args.size() < 2) {
            missingArgs(cont.getMessage(), "disconnect", args);
        }
        IUser user;
        try {
            user = cont.getMessage().getMessage().getMentions().get(0);
        } catch (IndexOutOfBoundsException ex) {
            notFound(cont.getMessage(), "disconnect", "User", "");
            return;
        }
        IVoiceChannel remove;
        try {
            remove = cont.getMessage().getMessage().getGuild().createVoiceChannel("Disconnect");
            try {
                user.moveToVoiceChannel(remove);
            } catch (DiscordException | RateLimitException ex) {
                error(cont.getMessage(), "disconnect", ex);
            } catch (MissingPermissionsException ex) {
                missingPermissions(cont.getMessage(), "disconnect", ex);
            }
            remove.delete();
        } catch (DiscordException | RateLimitException ex) {
            error(cont.getMessage(), "disconnect", ex);
        } catch (MissingPermissionsException ex) {
            missingPermissions(cont.getMessage(), "disconnect", ex);
        }
    }

    private void purge(commContext cont) {
        MessageList clear;
        List<String> args = parseArgs(cont.getMessage());
        boolean parsable = true;
        if (args.size() < 2) {
            missingArgs(cont.getMessage(), "purge", args);
            return;
        }
        if (tryInt(args.get(1))) {
            missingArgs(cont.getMessage(), "purge", args);
            return;
        }
        int number = Integer.parseInt(args.get(1)) + 1;
        if (number > 100) {
            customException(cont.getMessage(), "purge", "You cannont delete more than 100 messages at a time (" + number + ")");
            return;
        }
        clear = new MessageList(this.client, cont.getMessage().getMessage().getChannel(), number);
        try {
            clear.bulkDelete(clear);
        } catch (DiscordException | RateLimitException ex) {
            error(cont.getMessage(), "purge", ex);
        } catch (MissingPermissionsException ex) {
            missingPermissions(cont.getMessage(), "purge", ex);
        }

    }

    private void bring(commContext cont) {
        String message = "";
        boolean found = false;
        IUser[] Users = cont.getMessage().getMessage().getGuild().getUsers().toArray(new IUser[0]);
        IVoiceChannel back;
        IVoiceChannel current;
        try {
            back = cont.getMessage().getMessage().getAuthor().getConnectedVoiceChannels().get(0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return;
        }
        for (IUser user : Users) {
            try {
                current = user.getConnectedVoiceChannels().get(0);
            } catch (ArrayIndexOutOfBoundsException ex) {
                continue;
            }
            if (current != back) {
                found = true;
                if (!message.equals("")) {
                    message = message + "\n";
                }
                message = message + "Moving " + user.getName() + " to " + back.toString();
                try {
                    user.moveToVoiceChannel(back);
                } catch (DiscordException | RateLimitException ex) {
                    error(cont.getMessage(), "bring", ex);
                } catch (MissingPermissionsException ex) {
                    missingPermissions(cont.getMessage(), "bring", ex);
                    return;
                }
            }
        }
        if (!found) {
            sendMessage("No Users found in Outside of your Channel", cont.getMessage());
        } else {
            sendMessage(message, cont.getMessage());
        }
    }

    private void info(commContext cont) {
        String message = "```" + botName + " version " + version + "\n" + "Created with " + frameName + " version " + frameVersion + "\n" + "For help type: " + commChar + helpCommand + "\n" + "This bot was created by " + author + "```";
        sendMessage(message, cont.getMessage());
    }

    public void banned(MessageReceivedEvent e, String word) {
        sendMessage(e.getMessage().getAuthor().toString() + " violated Soviet Russia by saying \"" + word + "\" in the chat.", e);

    }

    private void unafk(commContext cont) {
        String message = "";
        boolean found = false;
        IVoiceChannel empty = null;
        IVoiceChannel afk = cont.getMessage().getMessage().getGuild().getAFKChannel();
        if (afk == null) {
            sendMessage("There is no AFK Channel.", cont.getMessage());
            return;
        }
        IUser[] Users = cont.getMessage().getMessage().getGuild().getUsers().toArray(new IUser[0]);
        IVoiceChannel back;
        IVoiceChannel current;
        try {
            back = cont.getMessage().getMessage().getAuthor().getConnectedVoiceChannels().get(0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return;
        }
        for (IUser user : Users) {
            try {
                current = user.getConnectedVoiceChannels().get(0);
            } catch (ArrayIndexOutOfBoundsException ex) {
                continue;
            }
            if (current == afk) {
                found = true;
                if (!message.equals("")) {
                    message = message + "\n";
                }
                message = message + "User Found in AFK: " + user.getName() + " - Moving to " + back.toString();
                try {
                    user.moveToVoiceChannel(back);
                } catch (DiscordException | RateLimitException ex) {
                    error(cont.getMessage(), "unafk", ex);
                } catch (MissingPermissionsException ex) {
                    missingPermissions(cont.getMessage(), "unafk", ex);
                    return;
                }
            }
        }
        if (!found) {
            sendMessage("No Users found in AFK Channel", cont.getMessage());
        } else {
            sendMessage(message, cont.getMessage());
        }
    }

    private void rekt(commContext cont) {
        int i = 0;
        AudioInputStream[] sources = Ohhs;
        if (cont.getArgs().size() > 1) {
            for (String command : OhhsIndex) {
                if (command.equalsIgnoreCase(cont.getArgs().get(1))) {
                    sources = new AudioInputStream[1];
                    sources[0] = Ohhs[i];
                }
                i++;
            }
        }

        try {
            getAudioPlayerForGuild(cont.getMessage().getMessage().getGuild()).queue(sources[rn.nextInt(sources.length)]);
        } catch (IOException ex) {
            error(cont.getMessage(), "rekt", ex);
        }
    }

    private void help(commContext cont) {
        if (cont.getArgs().size() >= 2) {
            int command = -1;
            int a = 0;
            for (String s : instSet) {
                if (cont.getArgs().get(1).startsWith(s)) {
                    command = a;
                }
                a++;
            }
            if (command == -1) {
                sendMessage("Command does not Exist", cont.getMessage());
                return;
            }
            String name = instSet[command];
            String description = helpText[command];
            //String syntax = arguments[command];
            //String options = options[command];
        } else {
            String message = "```";
            int a = 0;
            for (String s : instSet) {
                message = message + commChar + s + " - ";
                try {
                    message = message + helpText[a] + "\n";
                } catch (IndexOutOfBoundsException ex) {
                    message = message + ex.getMessage() + "\n";
                    error(cont.getMessage(), "help", ex);
                }
                a++;
            }
            message = message + "```";
            sendMessage(message, cont.getMessage());
        }
    }

    private void defaultMessage(commContext cont) {
        sendMessage(quotes[rn.nextInt(quotes.length)], cont.getMessage());
    }

    private void terminate(commContext cont) {
        if (!cont.getMessage().getMessage().getAuthor().getID().equals("141981833951838208")) {
            sendMessage("Communism marches on!", cont.getMessage());
            return;
        }
        reconnect.set(false);
        try {
            client.logout();
        } catch (RateLimitException | DiscordException ex) {
            log.warn("Logout failed", ex);
        }
        log.info("\n------------------------------------------------------------------------\n"
                + "Terminated\n"
                + "------------------------------------------------------------------------");
        System.exit(0);
    }

    private void sendMessage(String message, MessageReceivedEvent e) {
        try {
            new MessageBuilder(this.client).appendContent(message).withChannel(e.getMessage().getChannel()).build();
        } catch (RateLimitException | DiscordException ex) {
            error(e, "sendMessage(event)", ex);
        } catch (MissingPermissionsException ex) {
            missingPermissions(e, "sendMessage(event)", ex);
            try {
                new MessageBuilder(this.client).appendContent(message + "\nThis Bot needs the permission: " + ex.toString()).withChannel(client.getOrCreatePMChannel(e.getMessage().getAuthor())).build();
            } catch (DiscordException | RateLimitException ex2) {
                error(e, "sendMessage(event)", ex2);
            } catch (MissingPermissionsException ex2) {
                missingPermissions(e, "sendMessage(event)", ex2);
            }
        }
    }

    public void sendMessage(String message, IChannel channel) {
        try {
            new MessageBuilder(this.client).appendContent(message).withChannel(channel).build();
        } catch (RateLimitException | DiscordException ex) {
            error(null, "sendMessage(String)", ex);
        } catch (MissingPermissionsException ex) {
            missingPermissions((MessageReceivedEvent) null, "sendMessage(String)", ex);
        }
    }

    private void notFound(MessageReceivedEvent e, String methodName, String type, String name) {
        log.info(methodName + " failed to find " + type + ": \"" + name + "\" in Server: \"" + e.getMessage().getGuild().getName() + "\"");
    }

    private void customException(MessageReceivedEvent e, String methodName, String message) {
        log.info(methodName + " - Server: \"" + e.getMessage().getGuild().getName() + "\": " + message);
    }

    private void missingPermissions(MessageReceivedEvent e, String methodName, MissingPermissionsException ex) {
        log.info(methodName + ": " + ex.getErrorMessage() + " on Server: \"" + e.getMessage().getGuild().getName() + "\" in channel: " + e.getMessage().getChannel().getName());
    }

    public void missingPermissions(TrackQueueEvent e, String methodName, MissingPermissionsException ex) {
        log.info(methodName + ": " + ex.getErrorMessage() + " on Server: \"" + e.getPlayer().getGuild());
    }

    public void missingPermissions(TrackFinishEvent e, String methodName, MissingPermissionsException ex) {
        log.info(methodName + ": " + ex.getErrorMessage() + " on Server: \"" + e.getPlayer().getGuild());
    }

    private void missingArgs(MessageReceivedEvent e, String methodName, List<String> args) {
        log.info(methodName + " called with incomplete arguments: " + args.toString() + " in Server: \"" + e.getMessage().getGuild().getName() + "\"");
    }

    private void missingArgs(MessageReceivedEvent e, String methodName, List<String> args, Exception ex) {
        log.info(methodName + " called with incomplete arguments: " + args.toString() + " in Server: \"" + e.getMessage().getGuild().getName() + "\" returned Exception: " + ex.getMessage());
    }

    private void error(MessageReceivedEvent e, String methodName, Exception ex) {
        log.error(methodName + " in the Server: \"" + e.getMessage().getGuild().getName() + "\" returned error: " + ex.getMessage());
    }
}
