package rr.industries.commands;

import net.dv8tion.d4j.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import rr.industries.CommandList;
import rr.industries.util.*;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.audio.AudioPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static sx.blah.discord.util.audio.AudioPlayer.getAudioPlayerForGuild;

@CommandInfo(
        commandName = "music",
        helpText = "Plays YouTube and SoundCloud music in a voice channel."
)
//todo: Music Loading Progress Bar
public class Music implements Command {
    static {
        CommandList.defaultCommandList.add(Music.class);
    }
    @SubCommand(name = "list", Syntax = {@Syntax(helpText = "Shows you what tracks are queued up", args = {})})
    public void playlist(CommContext cont) {
        AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getMessage().getGuild());
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
        IMessage delete = cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
        //cont.getActions().delayDelete(delete, 15000);
        //todo: uncomment
    }

    @SubCommand(name = "skip", Syntax = {@Syntax(helpText = "Skips the currently playing track", args = {})}, permLevel = Permissions.REGULAR)
    public void skip(CommContext cont) {
        AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getMessage().getGuild());
        IAudioProvider provider = aPlayer.getCurrentTrack().getProvider();
        if (provider instanceof MusicPlayer) {
            LOG.info("Is isntance of, skipping only one track...");
            ((MusicPlayer) provider).skipToNext();
        } else {
            LOG.info("Skipping Provider");
            aPlayer.skip();
        }
    }

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Queues the video the link is for", args = {Arguments.LINK})})
    public void execute(CommContext cont) {
        if (cont.getArgs().size() < 2) {
            cont.getActions().missingArgs(cont.getMessage().getMessage().getChannel());
        } else {
            AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getMessage().getGuild());
            MusicPlayer player = new MusicPlayer();
            player.setVolume(1);
            aPlayer.queue(player);
            String url = cont.getArgs().get(1);
            Playlist playlist;
            try {
                playlist = Playlist.getPlaylist(url);
            } catch (NullPointerException ex) {
                LOG.warn("The YT-DL playlist process resulted in a null or zero-length INFO!");
                aPlayer.skip();
                return;
            }
            ConcurrentLinkedQueue<AudioSource> sources = new ConcurrentLinkedQueue<>(playlist.getSources());
            for (AudioSource source : sources) {
                AudioInfo info = source.getInfo();
                List<AudioSource> queue = player.getAudioQueue();
                if (info.getError() == null) {
                    queue.add(source);
                    if (player.isStopped()) {
                        player.play();
                    }
                } else {
                    sources.remove(source);
                }
            }
        }
    }
}
