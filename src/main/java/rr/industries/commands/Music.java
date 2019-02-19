package rr.industries.commands;

import com.discord4j.fsm.UnhandledTransitionException;
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
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Image;
import discord4j.core.object.util.Snowflake;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.voice.AudioReceiver;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import rr.industries.SovietBot;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.exceptions.ServerError;
import rr.industries.pojos.youtube.YoutubeSearch;
import rr.industries.util.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

@CommandInfo(
        commandName = "music",
        helpText = "Plays YouTube and SoundCloud music in a voice channel."
)
//todo: Music Loading Progress Bar
public class Music implements Command {
    final static int DEFAULT_VOLUME = 200;

    @SubCommand(name = "connect", Syntax = {
            @Syntax(helpText = "Connects to the named voice channel.", args = {@Argument(description = "\"Channel Name\"", value = Validate.LONGTEXT)}),
            @Syntax(helpText = "Connects the bot to the voice channel you are connected too", args = {})
    })
    public Mono<Void> connect(CommContext cont) throws PMNotSupportedException {
        Member caller = cont.getMember();
        Snowflake guildId = caller.getGuildId();
        Mono<VoiceChannel> toConnect;

        if (cont.getArgs().size() > 2) {
            String channelName = cont.getConcatArgs(2);
            toConnect = cont.getMessage().getGuild()
                    .flatMapMany(Guild::getChannels)
                    .filter(v -> v.getType().equals(Channel.Type.GUILD_VOICE))
                    .filter(v -> v.getName().equals(channelName))
                    .next()
                    .cast(VoiceChannel.class);
        } else {
            toConnect = caller.getVoiceState()
                    .flatMap(VoiceState::getChannel);
        }

        GuildMusicManager manager;
        Session session = sessions.get(guildId);
        if(session != null) {
            manager = session.musicManager;
            session.connection.disconnect();
        } else {
            manager = new GuildMusicManager(playerManager);
        }

        return toConnect
                .flatMap(channel -> channel.join(joinSpec -> joinSpec.setProvider(manager.getAudioProvider())))
                .map(connection -> {sessions.put(guildId, new Session(connection, manager)); return connection;}).then();
    }

    @SubCommand(name = "search", Syntax = {@Syntax(helpText = "Searches youtube for a video to play", args = {@Argument(description = "Search Terms", value = Validate.LONGTEXT)})})
    public Mono<Void> search(CommContext cont) throws BotException {
        try {
            Snowflake guildId = cont.getGuildId();

            HttpResponse<String> response = Unirest.get("https://www.googleapis.com/youtube/v3/search")
                    .queryString("key", cont.getActions().getConfig().googleKey)
                    .queryString("part", "snippet")
                    .queryString("maxResults", "1")
                    .queryString("q", cont.getConcatArgs(1))
                    .asString();
            YoutubeSearch search = SovietBot.gson.fromJson(response.getBody(), YoutubeSearch.class);

            if(search.items.size() == 0) {
                return cont.getChannel().createMessage("Could not find anything for " + cont.getConcatArgs(1)).then();
            }

            String link = "https://www.youtube.com/watch?v=" + search.items.get(0).id.videoId;
            Session session = getSession(guildId);
            playerManager.loadItemOrdered(session.musicManager, link, new CustomResultHandler(cont, session.musicManager, link));
        } catch (UnirestException e) {
            throw new ServerError("Error querying youtube", e);
        }
        return Mono.empty();
    }

    private Session getSession(Snowflake guildId) throws IncorrectArgumentsException {
        Session session = sessions.get(guildId);
        if(session == null) {
            throw new IncorrectArgumentsException("Not connected to voice channel!");
        }
        return session;
    }

    @SubCommand(name = "list", Syntax = {@Syntax(helpText = "Shows you what tracks are queued up", args = {})})
    public Mono<Void> playlist(CommContext cont) throws BotException {
        Snowflake guildId = cont.getGuildId();
        Session session = getSession(guildId);
        Queue<AudioTrack> queue = session.musicManager.scheduler.getQueue();

        String authorName = "Current Music Queue";
        Mono<Optional<String>> authorIcon = cont.getMessage().getGuild().map(v -> v.getIconUrl(Image.Format.PNG));
        StringBuilder description = new StringBuilder();

        AudioTrack currentTrack = session.musicManager.player.getPlayingTrack();
        if(currentTrack != null)
            description.append("\uD83D\uDD0A - [").append(msToMinutesAndSeconds(currentTrack.getInfo().length)).append("] - **[").append(currentTrack.getInfo().title).append("](").append(currentTrack.getInfo().uri).append(")**\n");

        int previewed = 0;
        final int previewLength = 5;
        for(AudioTrack track : queue) {
            if(previewed < previewLength) {
                description.append(previewed + 1).append(". - [").append(msToMinutesAndSeconds(track.getInfo().length)).append("] - **[").append(track.getInfo().title).append("](").append(track.getInfo().uri).append(")**\n");
                previewed++;
            }
        }
        if(queue.size() > previewLength)
            description.append("and ").append(queue.size() - previewLength).append(" more...\n");

        return authorIcon.flatMap(icon -> cont.getChannel().createMessage(messageSpec -> messageSpec.setEmbed(embedSpec -> {
            embedSpec.setDescription(description.toString());
            embedSpec.setAuthor(authorName, null, icon.orElse(null));
        }))).then();

    }

    @SubCommand(name = "volume", Syntax = {@Syntax(helpText = "Sets the Volume for the bot (0-100)%", args = {@Argument(description = "Volume", value = Validate.NUMBER)})}, permLevel = Permissions.REGULAR)
    public Mono<Void> volume(CommContext cont) throws BotException {
        Snowflake guildId = cont.getGuildId();
        Session session = getSession(guildId);
        int volume = 0;
        try {
            volume = Integer.parseInt(cont.getArgs().get(2));
            volume = Math.min(100, Math.max(0, volume));
            session.musicManager.player.setVolume(volume * 10);
        } catch (NumberFormatException e) {
            throw new IncorrectArgumentsException(cont.getArgs().get(1) + " is not a number");
        }
        return cont.getChannel().createMessage("Setting volume to " + volume + "%").then();
    }

    @SubCommand(name = "skip", Syntax = {
            @Syntax(helpText = "Skips the currently playing track", args = {})}, permLevel = Permissions.MOD)
    public Mono<Void> skip(CommContext cont) throws BotException {
        Snowflake guildId = cont.getGuildId();
        Session session = getSession(guildId);
        session.musicManager.scheduler.nextTrack();
        return Mono.empty();
    }

    @SubCommand(name = "disconnect", Syntax = {
            @Syntax(helpText = "Skips all queued tracks and disconnects", args = {})}, permLevel = Permissions.MOD)
    public Mono<Void> stop(CommContext cont) throws BotException {
        Snowflake guildId = cont.getGuildId();
        Session session = getSession(guildId);
        session.connection.disconnect();
        session.musicManager.player.destroy();
        sessions.remove(guildId);
        return cont.getChannel().createMessage("Music Stopped...").then();
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Queues the video the link is for", args = {@Argument(description = "Video Link", value = Validate.LINK)}),
    })
    public Mono<Void> execute(CommContext cont) throws BotException {
        Snowflake guildId = cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new);
        Session session = sessions.get(guildId);
        if(session == null) {
            return cont.getChannel().createMessage("Not connected to voice channel!").then();
        }
        playerManager.loadItemOrdered(session.musicManager, cont.getArgs().get(1), new CustomResultHandler(cont, session.musicManager, cont.getArgs().get(1)));
        return Mono.empty();
    }

    private String msToMinutesAndSeconds(long ms) {
        long seconds = ms / 1000L;
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }

    private final AudioPlayerManager playerManager;
    private final Map<Snowflake, GuildMusicManager> musicManagers = new HashMap<>();

    public Music() {
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    private final Map<Snowflake, Session> sessions = new HashMap<>();

    class CustomResultHandler implements AudioLoadResultHandler {
        CommContext cont;
        GuildMusicManager musicManager;
        String url;
        Mono<Void> result;

        public Mono<Void> getResult() {
            return result;
        }

        public CustomResultHandler(CommContext cont, GuildMusicManager musicManager, String url) {
            this.cont = cont;
            this.musicManager = musicManager;
            this.url = url;
        }
        @Override
        public void trackLoaded(AudioTrack track) {
            try {
                Member author = cont.getMember();
                musicManager.scheduler.queue(track);
                cont.getChannel().createMessage(messageSpec -> messageSpec.setEmbed(embedSpec -> {
                    embedSpec.setAuthor(author.getDisplayName() + " added to queue", null, author.getAvatarUrl());
                    embedSpec.setDescription("**[" + track.getInfo().title + "](" + track.getInfo().uri + ")**");
                    embedSpec.addField("Channel", track.getInfo().author, true);
                    if (track.getInfo().length < Long.MAX_VALUE) {
                        embedSpec.addField("Length", msToMinutesAndSeconds(track.getInfo().length), true);
                    }
                    embedSpec.setFooter("Position in Queue: " + (musicManager.scheduler.getQueueLength() + 1), null);
                })).subscribe();
            } catch (PMNotSupportedException e) {
                LOG.error("Music Track loaded but no member existed", e);
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            try {
                Member author = cont.getMember();
                cont.getChannel().createMessage(messageSpec -> messageSpec.setEmbed(embedSpec -> {
                    embedSpec.setAuthor((author == null ? "?" : author.getDisplayName()) + " added " + playlist.getTracks().size() + " songs to the queue",
                            null,
                            author.getAvatarUrl());
                    StringBuilder description = new StringBuilder();
                    description.append("**[").append(playlist.getName()).append("](").append(url).append(")**\n");
                    description.append("------\n");
                    embedSpec.setFooter("Position in Queue: " + (musicManager.scheduler.getQueueLength() + 1), null);
                    int previewed = 0;
                    final int previewLength = 5;
                    for (AudioTrack track : playlist.getTracks()) {
                        long trackLength = track.getInfo().length;
                        if (previewed < previewLength) {
                            description.append("[" + msToMinutesAndSeconds(trackLength) + "] - **[" + track.getInfo().title + "](" + track.getInfo().uri + ")**\n");
                            previewed++;
                        }
                    }
                    if (playlist.getTracks().size() > previewLength)
                        description.append("and " + (playlist.getTracks().size() - previewLength) + " more...\n");
                    embedSpec.setDescription(description.toString());
                })).subscribe();
                musicManager.scheduler.queue(playlist);
            } catch (PMNotSupportedException e) {
                LOG.error("Music Track loaded but no member existed", e);
            }
        }

        @Override
        public void noMatches() {
            cont.getChannel().createMessage("Nothing found by " + url).subscribe();
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            cont.getChannel().createMessage("Could not play: " + exception.getMessage()).subscribe();
        }
    }

    class Session {
        final VoiceConnection connection;
        final GuildMusicManager musicManager;

        public Session(VoiceConnection connection, GuildMusicManager musicManager) {
            this.connection = connection;
            this.musicManager = musicManager;
        }
    }
}

