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
import rr.industries.pojos.youtube.YoutubeSearch;
import rr.industries.util.*;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageOutputStream;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@CommandInfo(
        commandName = "music",
        helpText = "Plays YouTube and SoundCloud music in a voice channel."
)
//todo: Music Loading Progress Bar
public class Music implements Command {
    static float DEFAULT_VOLUME = 0.4f;

    //todo: save volume

    @SubCommand(name = "list", Syntax = {@Syntax(helpText = "Shows you what tracks are queued up", args = {})})
    public void playlist(CommContext cont) throws BotException {
        IAudioManager manager = cont.getMessage().getGuild().getAudioManager();
        ArrayList<String> messageLines = new ArrayList<>();
        int i = 1;
        if (manager.getAudioProvider() instanceof MusicPlayer) {
            MusicPlayer player = (MusicPlayer) manager.getAudioProvider();
            AudioSource currentSource = player.getCurrentAudioSource();
            if (currentSource != null) {
                messageLines.add(":speaker: `[" + currentSource.getInfo().getDuration().getTimestamp() + "]` - " + currentSource.getInfo().getTitle());
                for (AudioSource source : player.getAudioQueue()) {
                    messageLines.add(String.format("%3s", i) + " -  `[" + source.getInfo().getDuration().getTimestamp() + "]` - " + source.getInfo().getTitle());
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
            message = "Queue is Empty";
        }
        Optional<IMessage> delete = cont.getActions().channels().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getChannel()));
        if (delete.isPresent())
            cont.getActions().channels().delayDelete(delete.get(), 45000);
    }

    @SubCommand(name = "volume", Syntax = {@Syntax(helpText = "Sets the Volume for the bot (0-100)", args = {Arguments.NUMBER})}, permLevel = Permissions.REGULAR)
    public void volume(CommContext cont) throws BotException {
        Float volume = Float.parseFloat(cont.getArgs().get(2));
        if (volume > 100 || volume < 0)
            throw new IncorrectArgumentsException("Volume must be between 100 and 0");
        IAudioProvider provider = cont.getMessage().getGuild().getAudioManager().getAudioProvider();
        if (provider instanceof MusicPlayer) {
            ((MusicPlayer) provider).setVolume(volume / 100F);
        }
        cont.getActions().channels().sendMessage(cont.builder().withContent("Setting volume to " + volume + "%"));
    }

    @SubCommand(name = "skip", Syntax = {
            @Syntax(helpText = "Skips the currently playing track", args = {})}, permLevel = Permissions.MOD)
    public void skip(CommContext cont) {
        IAudioManager manager = cont.getMessage().getGuild().getAudioManager();
        MusicPlayer player = ((MusicPlayer) manager.getAudioProvider());
        cont.getActions().channels().sendMessage(cont.builder().withContent("Skipping " + (player.getCurrentAudioSource().getInfo() != null ? player.getCurrentAudioSource().getInfo().getTitle() : "Current Source")));
        if (manager.getAudioProvider() instanceof MusicPlayer) {
            player.skipToNext();
        }
    }

    @SubCommand(name = "stop", Syntax = {@Syntax(helpText = "Stops the bot playing music and clears the queue", args = {})}, permLevel = Permissions.MOD)
    public void stop(CommContext cont) {
        MusicPlayer player = new MusicPlayer();
        IAudioManager manager = cont.getMessage().getGuild().getAudioManager();
        player.setVolume(DEFAULT_VOLUME);
        manager.setAudioProvider(player);
        manager.setAudioProvider(player);
        cont.getActions().channels().sendMessage(cont.builder().withContent("Music Player Successfully Stopped"));
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Queues the video the link is for", args = {Arguments.LINK}),
            @Syntax(helpText = "Queues the first video in a search the phrase", args = {Arguments.LONGTEXT})
    })
    public void execute(CommContext cont) throws BotException {
        String link;
        String id = null;
        try {
            link = new URL(cont.getArgs().get(1)).toString();
        } catch (MalformedURLException e) {
            id = searchYoutube(cont.getConcatArgs(1), cont.getActions().getConfig().googleKey);
            link = "https://www.youtube.com/watch?v=" + id;
        }
        cont.getActions().channels().delayDelete(cont.getMessage(), 2000);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new MessageOutputStream(cont.getMessage().getChannel())));
        if (id != null) {
            Entry<String, String> data = getYoutubeData(id, cont.getActions().getConfig().googleKey);
            writeChars(writer, data.first() + " - `" + data.second() + "`");
        }
        writeChars(writer, (id != null ? "\n" : "") + "Processing Queue |");
        IAudioManager manager = cont.getMessage().getGuild().getAudioManager();
        MusicPlayer player;
        if (manager.getAudioProvider() instanceof MusicPlayer) {
            player = (MusicPlayer) manager.getAudioProvider();
        } else {
            player = new MusicPlayer();
            player.setVolume(DEFAULT_VOLUME);
            manager.setAudioProvider(player);
        }
        try {
            Playlist playlist = Playlist.getPlaylist(link);
            List<AudioSource> sources = new LinkedList<>(playlist.getSources());
            for (Iterator<AudioSource> it = sources.iterator(); it.hasNext(); ) {
                AudioSource source = it.next();
                AudioInfo info = source.getInfo();
                List<AudioSource> queue = player.getAudioQueue();
                if (info.getError() == null) {
                    queue.add(source);
                    writeChars(writer, " :musical_note:");
                    if (player.isStopped())
                        player.play();
                } else {
                    writeChars(writer, " :warning:");
                    LOG.info(info.getError());
                    it.remove();
                }
            }
            writeChars(writer, "| Done!");
        } catch (NullPointerException ex) {
            writeChars(writer, " - `Invalid Link!` - |");
        }
        try {
            writer.close();
        } catch (IOException e) {
            LOG.error(IOException.class.getName(), e);
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
            if (link == null || link.items.size() == 0) {
                throw new IncorrectArgumentsException("No youtube video found from search terms: " + params);
            }
            return link.items.get(0).id.videoId;
        } catch (UnsupportedEncodingException ex) {
            throw new InternalError("Unsupported Encoding hardcoded in Youtube Search", ex);
        } catch (UnirestException ex) {
            throw BotException.returnException(ex);
        }
    }

    private Entry<String, String> getYoutubeData(String videoID, String apiKey) throws BotException {
        try {
            HttpResponse<java.lang.String> response = Unirest.get("https://www.googleapis.com/youtube/v3/videos").queryString("key", apiKey).queryString("part", "snippet")
                    .queryString("maxResults", 1).queryString("id", videoID).asString();
            YoutubeSearch video = gson.fromJson(response.getBody(), YoutubeSearch.class);
            if (video.items.size() == 0)
                throw new InternalError("Youtube API couldn't find Video" + videoID);
            return new Entry<>(video.items.get(0).snippet.title, video.items.get(0).snippet.channelTitle);
        } catch (UnirestException ex) {
            throw BotException.returnException(ex);
        }
    }
}

