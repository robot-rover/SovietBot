package rr.industries.commands;

import net.dv8tion.d4j.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import rr.industries.util.*;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MessageOutputStream;
import sx.blah.discord.util.audio.AudioPlayer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
    public void playlist(CommContext cont) {
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
        delete.ifPresent((v) -> cont.getActions().channels().delayDelete(v, 15000));
    }

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

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Queues the video the link is for", args = {Arguments.LINK})})
    public void execute(CommContext cont) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new MessageOutputStream(cont.getMessage().getChannel())));
        try {
            writer.write("Processing Queue [");
            writer.flush();
        } catch (IOException ex) {
            LOG.warn("Error with Message Output Stream", ex);
        }
        AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getGuild());
        MusicPlayer player = new MusicPlayer();
        player.setVolume(1);
        aPlayer.queue(player);
        String url = cont.getArgs().get(1);
        Playlist playlist;
        try {
            playlist = Playlist.getPlaylist(url);
        } catch (NullPointerException ex) {
            LOG.warn("The YT-DL playlist process resulted in a null or zero-length INFO!");
            try {
                writer.write("The YT-DL playlist process resulted in a null or zero-length INFO!");
                writer.flush();
            } catch (IOException ex2) {
                LOG.warn("Error with Message Output Stream", ex2);
            }

            aPlayer.skip();
            return;
        }
        ConcurrentLinkedQueue<AudioSource> sources = new ConcurrentLinkedQueue<>(playlist.getSources());
        int i = 0;
        for (AudioSource source : sources) {
            AudioInfo info = source.getInfo();
            List<AudioSource> queue = player.getAudioQueue();
            if (info.getError() == null) {
                try {
                    writer.write("][");
                    writer.flush();
                } catch (IOException ex2) {
                    LOG.warn("Error with Message Output Stream", ex2);
                }
                queue.add(source);
                if (player.isStopped()) {
                    player.play();
                }
            } else {
                sources.remove(source);
            }
        }
        try {
            writer.write("] Done!");
            writer.close();
        } catch (IOException ex2) {
            LOG.warn("Error with Message Output Stream", ex2);
        }
    }
}
