package rr.industries.commands;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.demo.d4j.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import rr.industries.SovietBot;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.ServerError;
import rr.industries.pojos.youtube.YoutubeSearch;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@CommandInfo(
        commandName = "music",
        helpText = "Plays YouTube and SoundCloud music in a voice channel."
)
//todo: Music Loading Progress Bar
public class Music implements Command {
    final static int DEFAULT_VOLUME = 200;

    @SubCommand(name = "search", Syntax = {@Syntax(helpText = "Searches youtube for a video to play", args = {@Argument(description = "Search Terms", value = Validate.LONGTEXT)})})
    public void search(CommContext cont) throws BotException {
        try {
            HttpResponse<String> response = Unirest.get("https://www.googleapis.com/youtube/v3/search")
                    .queryString("key", cont.getActions().getConfig().googleKey)
                    .queryString("part", "snippet")
                    .queryString("maxResults", "1")
                    .queryString("q", cont.getConcatArgs(1))
                    .asString();
            YoutubeSearch search = SovietBot.gson.fromJson(response.getBody(), YoutubeSearch.class);
            if(search.items.size() == 0) {
                cont.getActions().channels().sendMessage(cont.builder().withContent("Could not find anything for " + cont.getConcatArgs(1)));
                return;
            }
            String link = "https://www.youtube.com/watch?v=" + search.items.get(0).id.videoId;
            GuildMusicManager musicManager = getGuildAudioPlayer(cont.getMessage().getGuild());
            playerManager.loadItemOrdered(musicManager, link, new CustomResultHandler(cont, musicManager, link));
        } catch (UnirestException e) {
            throw new ServerError("Error querying youtube", e);
        }
    }

    @SubCommand(name = "list", Syntax = {@Syntax(helpText = "Shows you what tracks are queued up", args = {})})
    public void playlist(CommContext cont) throws BotException {
        GuildMusicManager musicManager = getGuildAudioPlayer(cont.getMessage().getGuild());
        Queue<AudioTrack> queue = musicManager.scheduler.getQueue();
        EmbedBuilder embed = new EmbedBuilder();
        embed.withAuthorIcon(cont.getMessage().getGuild().getIconURL());
        embed.withAuthorName("Current Music Queue");
        AudioTrack currentTrack = musicManager.player.getPlayingTrack();
        if(currentTrack != null)
            embed.appendDescription("\uD83D\uDD0A - [" + msToMinutesAndSeconds(currentTrack.getInfo().length) + "] - **[" + currentTrack.getInfo().title + "](" + currentTrack.getInfo().uri + ")**\n");

        int previewed = 0;
        final int previewLength = 5;
        long totalDuration = 0;
        boolean lengthUnknown = false;
        for(AudioTrack track : queue) {
            if(previewed < previewLength) {
                embed.appendDescription((previewed + 1) + ". - [" + msToMinutesAndSeconds(track.getInfo().length) + "] - **[" + track.getInfo().title + "](" + track.getInfo().uri + ")**\n");
                previewed++;
            }
        }
        if(queue.size() > previewLength)
            embed.appendDescription("and " + (queue.size() - previewLength) + " more...\n");
        cont.getActions().channels().sendMessage(cont.builder().withEmbed(embed.build()));

    }

    @SubCommand(name = "volume", Syntax = {@Syntax(helpText = "Sets the Volume for the bot (0-100)%", args = {@Argument(description = "Volume", value = Validate.NUMBER)})}, permLevel = Permissions.REGULAR)
    public void volume(CommContext cont) throws BotException {
        GuildMusicManager musicManager = getGuildAudioPlayer(cont.getMessage().getGuild());
        int volume = 0;
        try {
            volume = Integer.parseInt(cont.getArgs().get(2));
            volume = Math.min(100, Math.max(0, volume));
            musicManager.player.setVolume(volume * 10);
        } catch (NumberFormatException e) {
            throw new IncorrectArgumentsException(cont.getArgs().get(1) + " is not a number");
        }
        cont.getActions().channels().sendMessage(cont.builder().withContent("Setting volume to " + volume + "%"));
    }

    @SubCommand(name = "skip", Syntax = {
            @Syntax(helpText = "Skips the currently playing track", args = {})}, permLevel = Permissions.MOD)
    public void skip(CommContext cont) {
        GuildMusicManager musicManager = getGuildAudioPlayer(cont.getMessage().getGuild());
        musicManager.scheduler.nextTrack();
    }

    @SubCommand(name = "stop", Syntax = {
            @Syntax(helpText = "Skips all queued tracks", args = {})}, permLevel = Permissions.MOD)
    public void stop(CommContext cont) {
        GuildMusicManager musicManager = newGuildAUdioPlayer(cont.getMessage().getGuild());
        cont.getActions().channels().sendMessage(cont.builder().withContent("Music Stopped..."));
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Queues the video the link is for", args = {@Argument(description = "Video Link", value = Validate.LINK)}),
    })
    public void execute(CommContext cont) throws BotException {
        GuildMusicManager musicManager = getGuildAudioPlayer(cont.getMessage().getGuild());
        playerManager.loadItemOrdered(musicManager, cont.getArgs().get(1), new CustomResultHandler(cont, musicManager, cont.getArgs().get(1)));
    }

    private String msToMinutesAndSeconds(long ms) {
        long seconds = ms / 1000L;
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    public Music() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild) {
        long guildId = guild.getLongID();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManager.player.setVolume(DEFAULT_VOLUME);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        return musicManager;
    }

    private synchronized GuildMusicManager newGuildAUdioPlayer(IGuild guild) {
        long guildId = guild.getLongID();
        GuildMusicManager musicManager = null;
        musicManagers.remove(guildId);
        musicManager = new GuildMusicManager(playerManager);
        musicManager.player.setVolume(DEFAULT_VOLUME);
        musicManagers.put(guildId, musicManager);

        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        return musicManager;
    }

    class CustomResultHandler implements AudioLoadResultHandler {
        CommContext cont;
        GuildMusicManager musicManager;
        String url;

        public CustomResultHandler(CommContext cont, GuildMusicManager musicManager, String url) {
            this.cont = cont;
            this.musicManager = musicManager;
            this.url = url;
        }
        @Override
        public void trackLoaded(AudioTrack track) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.withAuthorName(cont.getMessage().getAuthor().getDisplayName(cont.getMessage().getGuild()) + " added to queue");
            embed.withAuthorIcon(cont.getMessage().getAuthor().getAvatarURL());
            embed.withDescription("**[" + track.getInfo().title + "](" + track.getInfo().uri + ")**");
            embed.appendField("Channel", track.getInfo().author, true);
            if(track.getInfo().length < Long.MAX_VALUE) {
                embed.appendField("Length", msToMinutesAndSeconds(track.getInfo().length), true);
            }
            embed.withFooterText("Position in Queue: " + (musicManager.scheduler.getQueueLength() + 1));
            cont.getActions().channels().sendMessage(cont.builder().withEmbed(embed.build()));
            musicManager.scheduler.queue(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.withAuthorName(cont.getMessage().getAuthor().getDisplayName(cont.getMessage().getGuild()) + " added " + playlist.getTracks().size() + " songs to the queue");
            embed.withAuthorIcon(cont.getMessage().getAuthor().getAvatarURL());
            embed.withDescription("**[" + playlist.getName() + "](" + url + ")**\n");
            embed.appendDescription("------\n");
            embed.withFooterText("Position in Queue: " + (musicManager.scheduler.getQueueLength() + 1));
            int previewed = 0;
            final int previewLength = 5;
            long totalDuration = 0;
            boolean lengthUnknown = false;
            for(AudioTrack track : playlist.getTracks()) {
                long trackLength = track.getInfo().length;
                if(trackLength < Long.MAX_VALUE) {
                    totalDuration += trackLength;
                } else {
                    lengthUnknown = true;
                }

                if(previewed < previewLength) {
                    embed.appendDescription("[" + msToMinutesAndSeconds(trackLength) + "] - **[" + track.getInfo().title + "](" + track.getInfo().uri + ")**\n");
                    previewed++;
                }
            }
            if(playlist.getTracks().size() > previewLength)
                embed.appendDescription("and " + (playlist.getTracks().size() - previewLength) + " more...\n");
            cont.getActions().channels().sendMessage(cont.builder().withEmbed(embed.build()));

            musicManager.scheduler.queue(playlist);
        }

        @Override
        public void noMatches() {
            cont.getActions().channels().sendMessage(cont.builder().withContent("Nothing found by " + url));
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            cont.getActions().channels().sendMessage(cont.builder().withContent("Could not play: " + exception.getMessage()));
        }
    }
}

