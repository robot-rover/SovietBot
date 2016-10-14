package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.d4j.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.InternalError;
import rr.industries.util.*;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageOutputStream;
import sx.blah.discord.util.audio.AudioPlayer;
import youtube.YoutubeSearch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static sx.blah.discord.util.audio.AudioPlayer.getAudioPlayerForGuild;

@CommandInfo(
        commandName = "music",
        helpText = "Plays YouTube and SoundCloud music in a voice channel."
)
//todo: Music Loading Progress Bar
public class Music implements Command {
    @SubCommand(name = "list", Syntax = {@Syntax(helpText = "Shows you what tracks are queued up", args = {})})
    public void playlist(CommContext cont) throws BotException {
        AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getGuild());
        List<AudioPlayer.Track> playlist = aPlayer.getPlaylist();
        ArrayList<String> messageLines = new ArrayList<>();
        int i = 1;
        for (AudioPlayer.Track track : playlist) {
            if (track.getProvider() instanceof MusicPlayer) {
                MusicPlayer player = (MusicPlayer) track.getProvider();
                AudioSource currentSource = player.getCurrentAudioSource();
                messageLines.add("```Now Playing - [" + currentSource.getInfo().getDuration().getTimestamp() + "] - " + currentSource.getInfo().getTitle() + " - Now Playing");
                for (AudioSource source : player.getAudioQueue()) {
                    messageLines.add(String.format("%1$" + 12 + "s.", i) + " [" + source.getInfo().getDuration().getTimestamp() + "] - " + source.getInfo().getTitle());
                    i++;
                }
            }
        }
        int characters = 0;
        int line = 0;
        String message = "";
        for (String s : messageLines) {
            characters += s.length();
            if (characters >= 1970 || line > 11) {
                message = message + "            + " + (messageLines.size() - line) + " more...";
                break;
            } else {
                message = message + s + "\n";
            }
            line++;

        }
        if (message.equals("")) {
            message = "```Queue is Empty";
        }
        message = message + "```";
        Optional<IMessage> delete = cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getChannel()));
        if (delete.isPresent())
            cont.getActions().channels().delayDelete(delete.get(), 15000);
    }

    //todo: save volume

    @SubCommand(name = "volume", Syntax = {@Syntax(helpText = "Sets the Volume for the bot", args = {Arguments.NUMBER})})
    public void volume(CommContext cont) {
        IAudioProvider provider = getAudioPlayerForGuild(cont.getMessage().getGuild()).getCurrentTrack().getProvider();
        if (provider instanceof MusicPlayer) {
            ((MusicPlayer) provider).setVolume(Float.parseFloat(cont.getArgs().get(2)));
        } else {
            cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel())
                    .withContent("Unsupported with Rekt command..."));

        }
    }

    @SubCommand(name = "skip", Syntax = {
            @Syntax(helpText = "Skips the currently playing track", args = {})}, permLevel = Permissions.REGULAR)
    public void skip(CommContext cont) {
        AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getGuild());
        if (aPlayer.getCurrentTrack() != null) {
            IAudioProvider provider = aPlayer.getCurrentTrack().getProvider();
            if (provider instanceof MusicPlayer) {
                ((MusicPlayer) provider).pause();
                ((MusicPlayer) provider).skipToNext();
                if (provider.isReady())
                ((MusicPlayer) provider).play();
            } else {
                aPlayer.skip();
            }
        }
    }

    @SubCommand(name = "stop", Syntax = {@Syntax(helpText = "Stops the bot playing music and clears the queue", args = {})})
    public void stop(CommContext cont) {
        getAudioPlayerForGuild(cont.getMessage().getGuild()).clear();
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Queues the video the link is for", args = {Arguments.LINK}),
            @Syntax(helpText = "Queues the first video in a search the phrase", args = {Arguments.LONGTEXT})
    })
    public void execute(CommContext cont) throws BotException {
        String link;
        try {
            link = new URL(cont.getArgs().get(1)).toString();
        } catch (MalformedURLException e) {
            link = searchYoutube(cont.getConcatArgs(1), cont.getActions().getConfig().googleKey);
        }
        LOG.info(link);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new MessageOutputStream(cont.getMessage().getChannel())));
        writeChars(writer, "Processing Queue |");
        AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getGuild());
        MusicPlayer player = new MusicPlayer();
        player.setVolume(1);
        aPlayer.queue(player);
        try {
            Playlist playlist = Playlist.getPlaylist(link);
            ConcurrentLinkedQueue<AudioSource> sources = new ConcurrentLinkedQueue<>(playlist.getSources());
            for (AudioSource source : sources) {
                AudioInfo info = source.getInfo();
                List<AudioSource> queue = player.getAudioQueue();
                if (info.getError() == null) {
                    writeChars(writer, "[]");
                    queue.add(source);
                    if (player.isStopped()) {
                        player.play();
                    }
                } else {
                    sources.remove(source);
                }
            }
            writeChars(writer, "| Done!");
        } catch (NullPointerException ex) {
            LOG.warn("The YT-DL playlist process resulted in a null or zero-length INFO!");
            writeChars(writer, "The Link resulted in no content]");
            aPlayer.skip();
        }
    }

    private void writeChars(BufferedWriter writer, String chars) {
        try {
            writer.write(chars);
            writer.flush();
        } catch (IOException ex2) {
            LOG.warn("Error with Message Output Stream", ex2);
        }
    }

    private String searchYoutube(String params, String apiKey) throws BotException {
        try {
            HttpResponse<String> response = Unirest.get("https://www.googleapis.com/youtube/v3/search").queryString("key", apiKey).queryString("part", "id")
                    .queryString("maxResults", 1).queryString("type", "video").queryString("q", URLEncoder.encode(params, "UTF-8")).asString();
            YoutubeSearch link = gson.fromJson(response.getBody(), YoutubeSearch.class);
            if (link.items.size() != 0) {
                return "https://www.youtube.com/watch?v=" + link.items.get(0).id.videoId;
            } else {
                throw new IncorrectArgumentsException("No youtube video found from search terms: " + params);
            }
        } catch (UnsupportedEncodingException ex) {
            throw new InternalError("Unsupported Encoding hardcoded in Youtube Search", ex);
        } catch (UnirestException ex) {
            BotException.translateException(ex);
        }
        throw new InternalError("Unknown error in searchYoutube method");
    }
}
