package Main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.d4j.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.*;
import sx.blah.discord.util.audio.AudioPlayer;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;

import static Main.Parsable.tryInt;
import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static sx.blah.discord.util.audio.AudioPlayer.getAudioPlayerForGuild;

class Instance {
    private static final Logger log = LoggerFactory.getLogger(Instance.class);
    private static final String version = "1.1.6";
    private static final String botName = "SovietBot";
    private static final String frameName = sx.blah.discord.Discord4J.NAME;
    private static final String frameVersion = sx.blah.discord.Discord4J.VERSION;
    private static final String helpCommand = "help";
    private static final String author = "robot_rover";
    private static final String invite = "https://discordapp.com/oauth2/authorize?&client_id=184445488093724672&scope=bot&permissions=19950624";
    private final String token;
    private final AudioInputStream[] sfx;
    private final String[] sfxIndex;
    private final Random rn;
    private final String[] quotes;
    private final Map<String, Consumer<CommContext>> commandsTest = new HashMap<>();
    private volatile IDiscordClient client;
    private CommandList commands;

    Instance(String token) {
        BufferedReader reader;
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            reader = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream("commands.json")));
            Gson gson = new GsonBuilder().create();
            commands = gson.fromJson(reader, CommandList.class);
        } catch (NullPointerException ex) {
            log.error("commands.json not found. exiting...", ex);
            System.exit(1);
        }
        commandsTest.put("quote", this::defaultMessage);
        commandsTest.put("stop", this::terminate);
        commandsTest.put("help", this::help);
        commandsTest.put("rekt", this::rekt);
        commandsTest.put("unafk", this::unafk);
        commandsTest.put("info", this::info);
        commandsTest.put("bring", this::bring);
        commandsTest.put("purge", this::purge);
        commandsTest.put("disconnect", this::disconnect);
        commandsTest.put("cat", this::cat);
        commandsTest.put("roll", this::roll);
        commandsTest.put("coin", this::coin);
        commandsTest.put("weather", this::weather);
        commandsTest.put("connect", this::connect);
        commandsTest.put("music", this::music);
        commandsTest.put("uptime", this::uptime);
        commandsTest.put("log", this::log);
        commandsTest.put("invite", this::invite);
        //commandsTest.put("commChar", this::setChar);
        this.quotes = new String[17];
        this.sfxIndex = new String[6];
        rn = new Random();
        this.token = token;
        ClassLoader classLoader = this.getClass().getClassLoader();
        this.sfx = new AudioInputStream[6];
        try {
            sfx[0] = getAudioInputStream(classLoader.getResource("ohs/womboCombo.mp3"));
            sfx[1] = getAudioInputStream(classLoader.getResource("ohs/wrongNumber.mp3"));
            sfx[2] = getAudioInputStream(classLoader.getResource("ohs/violinAirhorn.mp3"));
            sfx[3] = getAudioInputStream(classLoader.getResource("ohs/noOneHasEver.mp3"));
            sfx[4] = getAudioInputStream(classLoader.getResource("ohs/noscoped.mp3"));
            sfx[5] = getAudioInputStream(classLoader.getResource("ohs/nopeSong.mp3"));
        } catch (IOException | UnsupportedAudioFileException ex) {
            log.warn("Error initializing audio streams", ex);
        }

        sfxIndex[0] = "wombo";
        sfxIndex[1] = "wrong";
        sfxIndex[2] = "airhorn";
        sfxIndex[3] = "never";
        sfxIndex[4] = "scope";
        sfxIndex[5] = "nope";
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
        quotes[12] = "https://qph.ec.quoracdn.net/Main-qimg-99907edafce7fb6acb5dc766368bf9af-c?convert_to_webp=true";
        quotes[13] = "http://67.media.tumblr.com/tumblr_meknuzRXuD1rxustho1_500.jpg";
        quotes[14] = "https://cdn.meme.am/instances/10678438.jpg";
        quotes[15] = "http://files.sharenator.com/in_soviet_russia_holy_crap_not_another_internet_meme_demotivational_poster_1247942328-s640x458-173710.jpg";
        quotes[16] = "http://ci.memecdn.com/689/2331689.jpg";
    }

    void login() throws DiscordException {
        client = new ClientBuilder().withToken(token).build();
        client.getDispatcher().registerListener(this);
        client.login();
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) throws DiscordException, RateLimitException {
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
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getAuthor().isBot()) {
            return;
        }
        String message = e.getMessage().getContent();
        if (message.startsWith(commands.commChar)) {
            CommContext cont = new CommContext(e, commands.commChar);
            Consumer exec;
            try {
                exec = commandsTest.get(cont.getArgs().get(0));
                if (exec == null) {
                    throw new NullPointerException();
                }
            } catch (NullPointerException ex) {
                return;
            }
            CommandList.Command command;
            try {
                command = commands.getCommand(cont.getArgs().get(0));
            } catch (NoSuchElementException ex) {
                log.warn("The CommandList does not match the list of Executable Methods");
                log.debug("Full Stacktrace - ", ex);
                return;
            }
            if (command.delete && !cont.getMessage().getMessage().getChannel().isPrivate()) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ex) {
                            threadInterrupted(ex, "onMessage");
                        }
                        try {
                            e.getMessage().delete();
                        } catch (MissingPermissionsException ex) {
                            missingPermissions(e.getMessage().getChannel(), "onMessage", ex);
                        } catch (RateLimitException ex) {
                            try {
                                Thread.sleep(ex.getRetryDelay());
                            } catch (InterruptedException ex2) {
                                threadInterrupted(ex2, "onMessage");
                            }
                        } catch (DiscordException ex) {
                            error(e.getMessage().getGuild(), "onMessage", ex);
                        }
                    }
                };
                thread.start();
            }
            exec.accept(cont);
        }
    }

    private void invite(CommContext cont) {
        String message = "Invite Me to Your Server:\n " + invite;
        sendMessage(message, cont.getMessage().getMessage().getChannel());
    }

    private void log(CommContext cont) {
        String path;
        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).equals("full")) {
            path = "debug.log";
        } else {
            path = "events.log";
        }
        File file = new File(path);
        try {
            cont.getMessage().getMessage().getChannel().sendFile(file);
        } catch (IOException e) {
            log.warn("Log file not found");
        } catch (MissingPermissionsException ex) {
            missingPermissions(cont.getMessage().getMessage().getChannel(), "log", ex);
        } catch (RateLimitException ex) {
            rateLimit(ex, this::log, cont);
        } catch (DiscordException ex) {
            error(cont.getMessage().getMessage().getGuild(), "log", ex);
        }
    }

    private void uptime(CommContext cont) {
        LocalDateTime launchTime = Discord4J.getLaunchTime();
        LocalDateTime current = LocalDateTime.now();
        long hours = launchTime.until(current, ChronoUnit.HOURS);
        launchTime = launchTime.plusHours(hours);
        long minutes = launchTime.until(current, ChronoUnit.MINUTES);
        launchTime = launchTime.plusMinutes(minutes);
        long seconds = launchTime.until(current, ChronoUnit.SECONDS);
        String message = "`SovietBot has been running for " + Long.toString(hours) + " hours, " + Long.toString(minutes) + " minutes, and " + Long.toString(seconds) + " seconds.`";
        sendMessage(message, cont.getMessage().getMessage().getChannel());
    }

    private void leaveChannel(IGuild guild) {
        try {
            client.getConnectedVoiceChannels().stream().filter(v -> v.getGuild().equals(guild)).findAny().orElse(null).leave();
        } catch (NullPointerException ex) {
            log.debug("Did not leave channel: Not in Channel");
        }
    }

    private void connect(CommContext cont) {
        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).equals("/")) {
            leaveChannel(cont.getMessage().getMessage().getGuild());
        } else if (cont.getArgs().size() >= 2) {
            try {
                IVoiceChannel next = cont.getMessage().getMessage().getGuild().getVoiceChannelsByName(cont.getArgs().get(1)).get(0);
                if (!next.isConnected()) {
                    next.join();
                }
            } catch (MissingPermissionsException ex) {
                missingPermissions(cont.getMessage().getMessage().getChannel(), "connect", ex);
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
                        possible = client.getConnectedVoiceChannels().stream().filter(v -> v.getGuild().equals(cont.getMessage().getMessage().getGuild())).findAny().orElseThrow(NullPointerException::new);
                    } catch (NullPointerException ex) {
                        disconnect = false;
                    }
                    if (disconnect) {
                        possible.leave();
                    }
                    next.join();
                }
            } catch (MissingPermissionsException ex) {
                missingPermissions(cont.getMessage().getMessage().getChannel(), "connect", ex);
            } catch (NullPointerException | IndexOutOfBoundsException ex) {
                log.debug("Could not connect: Author not in voice channel");
            }
        }
    }

    private void music(CommContext cont) {
        log.info("starting music");
        AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getMessage().getGuild());
        MusicPlayer player = new MusicPlayer();
        player.setVolume(1);
        aPlayer.queue(player);
        if (cont.getArgs().size() < 2) {
            missingArgs(cont.getMessage(), "music", cont.getArgs());
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
        log.info("playlist into array");
        log.info("more than one source");
        for (AudioSource source : sources) {
            AudioInfo info = source.getInfo();
            List<AudioSource> queue = player.getAudioQueue();
            if (info.getError() == null) {
                queue.add(source);
                if (player.isStopped()) {
                    player.play();
                }
            } else {
                log.warn("Error in music source, skipping...");
                sources.remove(source);
            }
        }
    }

    private void weather(CommContext cont) {
    }

    private void coin(CommContext cont) {
        String message;
        if (rn.nextBoolean()) {
            message = "Heads";
        } else {
            message = "Tails";
        }
        sendMessage(message, cont.getMessage().getMessage().getChannel());
    }

    private void roll(CommContext cont) {
        int roll;
        boolean dnd;
        int d = 0;
        try {
            d = cont.getArgs().get(1).indexOf("d");
            dnd = cont.getArgs().get(1).contains("d") && tryInt(cont.getArgs().get(1).substring(0, d)) && tryInt(cont.getArgs().get(1).substring(d + 1));
        } catch (IndexOutOfBoundsException ex) {
            dnd = false;
        }
        if (cont.getArgs().size() >= 2 && tryInt(cont.getArgs().get(1))) {
            roll = Integer.parseInt(cont.getArgs().get(1));
            if (roll < 1) {
                sendMessage("Rolling 0 to 0: 0", cont.getMessage().getMessage().getChannel());
                return;
            }
            String message = "Rolling 1 to " + Integer.toString(roll) + ": " + (rn.nextInt(roll) + 1);
            sendMessage(message, cont.getMessage().getMessage().getChannel());
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
            sendMessage(message, cont.getMessage().getMessage().getChannel());
        } else {
            sendMessage("Rolling 1 to 100: " + (rn.nextInt(100) + 1), cont.getMessage().getMessage().getChannel());
        }
    }

    private void cat(CommContext cont) {
        URL url;
        try {
            url = new URL("http://random.cat/meow");
        } catch (MalformedURLException ex) {
            customException(cont.getMessage(), "cat", "Cat URL is Malformed", ex);
            return;
        }
        InputStream is;
        try {
            URLConnection con = url.openConnection();
            is = con.getInputStream();
        } catch (IOException ex) {
            error(cont.getMessage().getMessage().getGuild(), "cat", ex);
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String message;
        try {
            message = br.readLine();
        } catch (IOException ex) {
            error(cont.getMessage().getMessage().getGuild(), "cat", ex);
            return;
        }
        message = message.substring(9, message.length() - 2);
        message = message.replace("\\/", "/");
        message = cont.getMessage().getMessage().getAuthor().mention() + " " + message;
        sendMessage(message, cont.getMessage().getMessage().getChannel());
    }

    private void disconnect(CommContext cont) {
        if (cont.getArgs().size() < 2) {
            missingArgs(cont.getMessage(), "disconnect", cont.getArgs());
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
            } catch (DiscordException ex) {
                error(cont.getMessage().getMessage().getGuild(), "disconnect", ex);
            } catch (MissingPermissionsException ex) {
                missingPermissions(cont.getMessage().getMessage().getChannel(), "disconnect", ex);
            } catch (RateLimitException ex) {
                rateLimit(ex, this::disconnect, cont);
            }
            remove.delete();
        } catch (DiscordException ex) {
            error(cont.getMessage().getMessage().getGuild(), "disconnect", ex);
        } catch (MissingPermissionsException ex) {
            missingPermissions(cont.getMessage().getMessage().getChannel(), "disconnect", ex);
        } catch (RateLimitException ex) {
            rateLimit(ex, this::disconnect, cont);
        }
    }

    private void purge(CommContext cont) {
        MessageList clear;
        if (cont.getArgs().size() < 2) {
            missingArgs(cont.getMessage(), "purge", cont.getArgs());
            return;
        }
        if (!tryInt(cont.getArgs().get(1))) {
            wrongArgs(cont.getMessage(), "purge", cont.getArgs());
            return;
        }
        int number = Integer.parseInt(cont.getArgs().get(1)) + 1;
        if (number > 100) {
            customException(cont.getMessage(), "purge", "You cannont delete more than 100 messages at a time (" + number + ")", null);
            return;
        }
        clear = new MessageList(this.client, cont.getMessage().getMessage().getChannel(), number);
        try {
            clear.bulkDelete(clear);
        } catch (DiscordException ex) {
            error(cont.getMessage().getMessage().getGuild(), "purge", ex);
        } catch (MissingPermissionsException ex) {
            missingPermissions(cont.getMessage().getMessage().getChannel(), "purge", ex);
        } catch (RateLimitException ex) {
            rateLimit(ex, this::purge, cont);
        }

    }

    private void bring(CommContext cont) {
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
                } catch (DiscordException ex) {
                    error(cont.getMessage().getMessage().getGuild(), "bring", ex);
                } catch (MissingPermissionsException ex) {
                    missingPermissions(cont.getMessage().getMessage().getChannel(), "bring", ex);
                    return;
                } catch (RateLimitException ex) {
                    rateLimit(ex, this::bring, cont);
                }
            }
        }
        if (!found) {
            sendMessage("No Users found in Outside of your Channel", cont.getMessage().getMessage().getChannel());
        } else {
            sendMessage(message, cont.getMessage().getMessage().getChannel());
        }
    }

    private void info(CommContext cont) {
        String message = "```" + botName + " version " + version + "\n" + "Created with " + frameName + " version " + frameVersion + "\n" + "For help type: " + commands.commChar + helpCommand + "\n" + "This bot was created by " + author + "```";
        sendMessage(message, cont.getMessage().getMessage().getChannel());
    }

    private void unafk(CommContext cont) {
        String message = "";
        boolean found = false;
        IVoiceChannel afk = cont.getMessage().getMessage().getGuild().getAFKChannel();
        if (afk == null) {
            sendMessage("There is no AFK Channel.", cont.getMessage().getMessage().getChannel());
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
                    error(cont.getMessage().getMessage().getGuild(), "unafk", ex);
                } catch (MissingPermissionsException ex) {
                    missingPermissions(cont.getMessage().getMessage().getChannel(), "unafk", ex);
                    return;
                }
            }
        }
        if (!found) {
            sendMessage("No Users found in AFK Channel", cont.getMessage().getMessage().getChannel());
        } else {
            sendMessage(message, cont.getMessage().getMessage().getChannel());
        }
    }

    private void rekt(CommContext cont) {
        int i = 0;
        AudioInputStream[] sources = sfx;
        if (cont.getArgs().size() > 1) {
            for (String command : sfxIndex) {
                if (command.equalsIgnoreCase(cont.getArgs().get(1))) {
                    sources = new AudioInputStream[1];
                    sources[0] = sfx[i];
                }
                i++;
            }
        }

        try {
            getAudioPlayerForGuild(cont.getMessage().getMessage().getGuild()).queue(sources[rn.nextInt(sources.length)]);
        } catch (IOException ex) {
            error(cont.getMessage().getMessage().getGuild(), "rekt", ex);
        }
    }

    private void help(CommContext cont) {
        if (cont.getArgs().size() >= 2) {
            CommandList.Command comm = null;
            for (CommandList.Command s : commands.commands) {
                if (cont.getArgs().get(1).startsWith(s.commandName)) {
                    comm = s;
                }
            }
            if (comm == null) {
                sendMessage("Command does not Exist", cont.getMessage().getMessage().getChannel());
                return;
            }
            sendMessage("`" + commands.commChar + comm.toString() + "`", cont.getMessage().getMessage().getChannel());
        } else {
            sendMessage("```\n" + commands.toString() + "\n```", cont.getMessage().getMessage().getChannel());
        }
    }

    private void defaultMessage(CommContext cont) {
        sendMessage(quotes[rn.nextInt(quotes.length)], cont.getMessage().getMessage().getChannel());
    }

    private void terminate(CommContext cont) {
        if (!cont.getMessage().getMessage().getAuthor().getID().equals("141981833951838208")) {
            sendMessage("Communism marches on!", cont.getMessage().getMessage().getChannel());
            return;
        }
        if (cont.getMessage().getMessage().getChannel().isPrivate()) {
            try {
                cont.getMessage().getMessage().delete();
            } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
                log.debug("Error while deleting stop command", ex);
            }
        }
        try {
            client.logout();
        } catch (DiscordException ex) {
            log.error("Logout failed", ex);
            return;
        } catch (RateLimitException ex) {
            rateLimit(ex, this::terminate, cont);
        }
        log.info("\n------------------------------------------------------------------------\n"
                + "Terminated\n"
                + "------------------------------------------------------------------------");
        System.exit(0);
    }

    private void sendMessage(String message, IChannel channel) {
        try {
            channel.sendMessage(message);
        } catch (DiscordException ex) {
            error(channel.getGuild(), "sendMessage(event)", ex);
        } catch (RateLimitException ex2) {
            rateLimit(ex2, (cont) -> sendMessage(cont.getReturnMessage(), cont.getChannel()), new CommContext(channel, message));
        } catch (MissingPermissionsException ex) {
            missingPermissions(channel, "sendMessage(event)", ex);
        }
    }

    private void threadInterrupted(InterruptedException ex, String methodName) {
        log.debug("The Method " + methodName + "'s Sleep was interrupted - ", ex);
    }

    private void notFound(MessageReceivedEvent e, String methodName, String type, String name) {
        log.info(methodName + " failed to find " + type + ": \"" + name + "\" in Server: \"" + e.getMessage().getGuild().getName() + "\"");
    }

    private void customException(MessageReceivedEvent e, String methodName, String message, Exception ex) {
        log.info(methodName + " - Server: \"" + e.getMessage().getGuild().getName() + "\": " + message);
        if (ex != null) {
            log.debug("Full Stack Trace - ", ex);
        }
    }

    private void missingPermissions(IChannel channel, String methodName, MissingPermissionsException ex) {
        log.info(methodName + ": " + ex.getErrorMessage() + " on Server: \"" + channel.getGuild().getName() + "\" in channel: " + channel.getName());
    }

    private void missingArgs(MessageReceivedEvent e, String methodName, List<String> args) {
        log.info(methodName + " called without enough arguments: " + args.toString() + " in Server: \"" + e.getMessage().getGuild().getName() + "\"");
    }

    private void wrongArgs(MessageReceivedEvent e, String methodName, List<String> args) {
        log.info(methodName + " called with incomplete arguments: " + args.toString() + " in Server: \"" + e.getMessage().getGuild().getName() + "\"");
    }

    private void error(IGuild guild, String methodName, Exception ex) {
        log.error(methodName + " in the Server: \"" + guild.getName() + "\" returned error: " + ex.getMessage());
        log.debug("Full Stack Trace - ", ex);
    }

    private void rateLimit(RateLimitException ex, Consumer<CommContext> method, CommContext args) {
        log.debug("Rate Limited - ", ex);
        try {
            Thread.sleep(ex.getRetryDelay());
        } catch (InterruptedException e) {
            log.debug("RateLimit Sleep Interrupted, Cancelling retry.");
            return;
        }
        method.accept(args);
    }
}
